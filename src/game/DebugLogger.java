package game;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DebugLogger {
    private static DebugLogger instance;
    private BlockingQueue<String> logQueue;
    private Thread loggerThread;
    private boolean running = false;
    
    private DebugLogger() {
        logQueue = new LinkedBlockingQueue<>();
        startLogger();
    }
    
    public static synchronized DebugLogger getInstance() {
        if (instance == null) {
            instance = new DebugLogger();
        }
        return instance;
    }
    
    private void startLogger() {
        running = GameConfig.DEBUG_MODE;
        loggerThread = new Thread(() -> {
            while (running) {
                try {
                    String msg = logQueue.take();
                    System.out.println(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        loggerThread.setDaemon(true);
        loggerThread.start();
    }
    
    public void log(String message) {
        try {
            logQueue.offer(message);
        } catch (Exception e) {
            System.out.println(message);
        }
    }
    
    public void shutdown() {
        running = false;
        if (loggerThread != null) {
            loggerThread.interrupt();
        }
    }
}

