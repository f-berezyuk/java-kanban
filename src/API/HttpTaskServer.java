package API;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class HttpTaskServer {

    private final HttpServer httpServer;

    public HttpTaskServer() {
        try {
            httpServer = HttpServer.create();
            httpServer.createContext("/tasks", new TaskHandler());
            httpServer.createContext("/subtasks", new SubTaskHandler());
            httpServer.createContext("/epics", new EpicTaskHandler());
            httpServer.createContext("/history", new HistoryTaskHandler());
            httpServer.createContext("/prioritized", new PrioritizedTaskHandler());
            httpServer.bind(new InetSocketAddress(8080), 0);
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

