package network;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkLogger {
    private static NetworkLogger instance;
    private BlockingQueue<String> logQueue;
    private Thread loggerThread;
    private boolean running = false;
    
    private NetworkLogger() {
        logQueue = new LinkedBlockingQueue<>();
        startLogger();
    }
    
    public static synchronized NetworkLogger getInstance() {
        if (instance == null) {
            instance = new NetworkLogger();
        }
        return instance;
    }
    
    private void startLogger() {
        running = true;
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
        loggerThread.setName("NetworkLogger-Thread");
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

