package API;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class HttpTaskServer {

    private final HttpServer httpServer;

    public HttpTaskServer() {
        try {
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(8080), 0);
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

