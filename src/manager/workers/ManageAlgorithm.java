package manager.workers;


public interface ManageAlgorithm<T> {
    public void init(Manager<T> manager);
    
    public boolean check(Runnable thread, Manager<T> manager);
    
    public void log(Runnable thread, Manager<T> manager);
}
