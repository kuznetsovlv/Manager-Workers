package manager.workers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager<T> implements Runnable{
    
    private final Thread thread;
    private final ManageAlgorithm algorithm;
    private final Job job;
    private final HashMap<Runnable, T> map;
    
    private boolean finished;

    public Manager(ManageAlgorithm algorithm, Job job, Job ...workerJobs) {
        this.algorithm = algorithm;
        this.job = job;
        thread = new Thread(this, "Manager");
        
        map = new HashMap<>();
        map.put(this, null);
        
        for (int i = 0; i < workerJobs.length; i++) {
            map.put(new Worker(workerJobs[i], this, i), null);
        }
        
        algorithm.init(this);
    }
    
    public HashMap<Runnable, T> getThreads() {
        return map;
    }
    
    public boolean canWork(Runnable thread) {
        return algorithm.check(thread, this);
    }
    
    public void finished(Runnable thread) {
        algorithm.log(thread, this);
    }
    
    public void join() throws InterruptedException {
        for(Runnable worker : map.keySet()) {
            if(worker != this) {
                ((Worker) worker).join();
            }
        }
        
        thread.join();
    }
    
    public void start() {
        finished = false;
        thread.start();
        
        for(Runnable worker : map.keySet()) {
            if(worker != this) {
                ((Worker) worker).start();
            }
        }
    }
    
    public void stop() {
        finished = true;
    }
    
    private void stopWorkers() {
        for(Runnable worker : map.keySet()) {
            if(worker != this) {
                ((Worker) worker).stop();
            }
        }
    }

    @Override
    public void run() {
        while(!finished) {
            synchronized(this) {
                while(!canWork(this)) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                job.perfom();
            
                finished(this);
                notifyAll();
            }
        }
        
        synchronized(this) {
            stopWorkers();
        }
    }
    
}
