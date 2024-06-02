package API;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    void writeResponse(HttpExchange exchange,
                       String responseString,
                       int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }
        exchange.close();
    }


    Optional<Long> getId(HttpExchange exchange) {
        return getId(exchange, 2);
    }

    Optional<Long> getId(HttpExchange exchange, int indexInPath) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Long.parseLong(pathParts[indexInPath]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
