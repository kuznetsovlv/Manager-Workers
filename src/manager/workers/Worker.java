package manager.workers;

import java.util.logging.Level;
import java.util.logging.Logger;


class Worker implements Runnable {
    
    private final Job job;
    private final Thread thread;
    private final Manager manager;
    private final int id;
    
    private boolean finished;

    public Worker(Job job, Manager manager, int id) {
        this.job = job;
        this.manager = manager;
        this.id = id;
        thread = new Thread(this, Integer.toString(id));
    }
    
    public void start() {
        finished = false;
        thread.start();
    }
    
    public void join() throws InterruptedException {
        thread.join();
    }
    
    public void stop() {
        finished = true;
    }
    
    @Override
    public void run() {
        while(!finished) {
            synchronized(manager) {
                while(!manager.canWork(this) && !finished) {
                    try {
                        manager.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            
                if (!finished) {
                    job.perfom();
                }
            
                manager.finished(this);
                manager.notifyAll();
            }
        }
    }
}
