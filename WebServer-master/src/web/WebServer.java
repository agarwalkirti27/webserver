package web;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * WebServer class to start the webserver
 */
public class WebServer implements Runnable {

    private ServerSocket server;
    private final String webRoot;
    private ExecutorService executorService;

    private final int port;
    private final int noOfThreads;

    /**
     * @param port
     * @param webRoot
     * @param maxThreads
     * Parametrized constructor
     */
    public WebServer(int port, String webRoot, int maxThreads) {
        this.port = port;
        this.noOfThreads = maxThreads;
        this.webRoot = webRoot;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        int port = 8080;
        String webRoot = "root/index.html";
        int maxThreads = 10;
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help"))
            System.out.println("Usage: java -cp WebServer.jar web.Server <port> <web root> <total no of threads>\n");
        else {
            port = Integer.parseInt(args[0]);
            webRoot = args[1];
            maxThreads = Integer.parseInt(args[2]);
        }
        new Thread(new WebServer(port, webRoot, maxThreads)).start();
    }


    /**
     * run method from where the Executors class starts the thread pooling
     */
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            executorService = Executors.newFixedThreadPool(noOfThreads);
        } catch (IOException e) {
            System.err.println("Cannot listen on port " + port);
            System.exit(1);
        }

        System.out.println("Running server on the port " + port +
                " with root folder \"" + webRoot + "\" and " + noOfThreads + " threads limit.");

        while (!Thread.interrupted()) {
            try {
                executorService.execute(new Thread(new Connection(server.accept(), this)));
            } catch (IOException e) {
                System.err.println("Cannot accept client.");
            }
        }
        close();
    }

    /**
     * customised close method to shut down executor service
     */
    public void close() {
        try {
            server.close();
        } catch (IOException e) {
            System.err.println("Error while closing server socket.");
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
                executorService.shutdownNow();
        } catch (InterruptedException e) {
        }
    }

    /**
     * @return webRoot
     */
    public String getWebRoot() {
        return webRoot;
    }

}
