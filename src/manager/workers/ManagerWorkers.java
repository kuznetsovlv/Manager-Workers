package manager.workers;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagerWorkers {
    
    private static final int DURATION = 1000;
    private static final Job MANAGE_JOB = () -> System.out.printf("Thread %s working.\n\n", Thread.currentThread().getName());
    private static final Job WORKER_JOB = () -> System.out.printf("Thread %s working.\n", Thread.currentThread().getName());
    
    private static void wait(Manager manager, int ms) throws InterruptedException {
        Thread.sleep(ms);
        manager.stop();
        manager.join();
    }
    
    private static void allByOne (int duration) throws InterruptedException {
        ManageAlgorithm<Boolean> algorithm = new ManageAlgorithm<Boolean>() {
            @Override
            public boolean check(Runnable thread, Manager<Boolean> manager) {
                return manager.getThreads().get(thread);
            }

            @Override
            public void log(Runnable thread, Manager<Boolean> manager) {
                HashMap<Runnable, Boolean> threads = manager.getThreads();

                if (thread == manager) {
                    for(Runnable t: threads.keySet()) {
                        threads.put(t, t != manager);
                    }
                } else {
                    threads.put(thread, Boolean.FALSE);

                    for(Runnable t: threads.keySet()) {
                        if (threads.get(t)) {
                            return;
                        }
                    }

                    threads.put(manager, Boolean.TRUE);
                }
            }

            @Override
            public void init(Manager<Boolean> manager) {
                HashMap<Runnable, Boolean> threads = manager.getThreads();

                for(Runnable thread: threads.keySet()) {
                    threads.put(thread, thread != manager);
                }
            }
        };
        
        Manager<Boolean> manager = new Manager<>(
                algorithm,
                MANAGE_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB
        );
        
        manager.start();
        
        wait(manager, duration);
    }
    
    private static void oneForAll(int duration) throws InterruptedException {
        ManageAlgorithm<Integer> algorithm = new ManageAlgorithm<Integer>() {
            @Override
            public void init(Manager<Integer> manager) {
                HashMap<Runnable, Integer> threads = manager.getThreads();
                
                for(Runnable thread: threads.keySet()) {
                    threads.put(thread, 0);
                }
            }

            @Override
            public boolean check(Runnable thread, Manager<Integer> manager) {
                HashMap<Runnable, Integer> threads = manager.getThreads();
                
                int workerNum = threads.size() - 1;
                
                int value = threads.get(thread);
                
                if (value > 0 && value < workerNum) {
                    return true;
                }

                if (value >= workerNum) {
                    return false;
                }
                
                for(Runnable t: threads.keySet()) {
                    if (thread == t) {
                        continue;
                    }
                    
                    value = threads.get(t);
                    
                    if (value >= workerNum) {
                        return thread == manager;
                    }

                    if(value > 0) {
                        return false;
                    }                   
                }
                
                return thread != manager;
            }

            @Override
            public void log(Runnable thread, Manager<Integer> manager) {
                HashMap<Runnable, Integer> threads = manager.getThreads();
                
                if(thread != manager) {
                    threads.put(thread, threads.get(thread).intValue() + 1);
                } else {
                    for(Runnable t: threads.keySet()) {
                        threads.put(t, 0);
                    }
                }
            }
        };
        
        Manager<Integer> manager = new Manager<>(
                algorithm,
                MANAGE_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB,
                WORKER_JOB
        );
        
        manager.start();
        
        wait(manager, duration);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Need argument algorithm number.");
        } else if(args.length > 1) {
            System.out.println("Too many arguments.");
        } else {
            Integer value = Integer.parseInt(args[0]);
            
            if (value == null) {
                System.out.printf("Unknown argument: \"%s\".", args[0]);
                return;
            }
            
            try {
                switch(value) {
                    case 1:
                        allByOne(DURATION);
                        break;
                    case 2:
                        oneForAll(DURATION);
                        break;
                    default:
                        System.out.printf("Unknown argument: \"%s\".", args[0]);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ManagerWorkers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
