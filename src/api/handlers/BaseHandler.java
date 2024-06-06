package api.handlers;

import java.util.Optional;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.TaskManager;

import static api.HandlerUtilities.createGson;

public abstract class BaseHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHandler(TaskManager manager) {
        this.manager = manager;
        gson = createGson();
    }

    Optional<Long> getId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Long.parseLong(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

}
