package api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import api.handlers.EpicTaskHandler;
import api.handlers.HistoryTaskHandler;
import api.handlers.PrioritizedTaskHandler;
import api.handlers.SubTaskHandler;
import api.handlers.TaskHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tracker.TaskManager;

public class HttpTaskServer implements AutoCloseable {

    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            httpServer.createContext("/ping", (HttpExchange exchange) -> {
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(200, 0);
                    os.write("pong".getBytes(StandardCharsets.UTF_8));
                }
                exchange.close();
            });
            httpServer.createContext("/tasks", new TaskHandler(manager));
            httpServer.createContext("/subtasks", new SubTaskHandler(manager));
            httpServer.createContext("/epics", new EpicTaskHandler(manager));
            httpServer.createContext("/history", new HistoryTaskHandler(manager));
            httpServer.createContext("/prioritized", new PrioritizedTaskHandler(manager));
            httpServer.start();
            System.out.println(httpServer.getAddress().getHostString());
            System.out.println(httpServer.getAddress().getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        System.out.println("STOP");
        httpServer.stop(0);
    }
}

