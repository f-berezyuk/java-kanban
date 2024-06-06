package api.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import api.HandlerUtilities;
import api.adapters.SimpleTaskListTypeToken;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import task.SimpleTask;
import task.Task;
import task.TaskType;
import tracker.TaskManager;

import static api.HandlerUtilities.readRequestBodyAsString;
import static api.HandlerUtilities.write400Response;
import static api.HandlerUtilities.writeEndpoint404Response;
import static api.HandlerUtilities.writeResponse;
import static api.HandlerUtilities.writeTask404Response;

public class TaskHandler extends BaseHandler implements HttpHandler {
    public TaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            TaskHandlerEndpoint endpoint = getEndpoint(exchange);
            switch (endpoint) {
                case GET_TASK -> handleGetTask(exchange);
                case POST_TASK -> handlePostTask(exchange);
                case GET_TASKS -> handleGetTasks(exchange);
                case DELETE_TASK -> handleDeleteTask(exchange);
                case GET_SUBTASKS, UNKNOWN -> writeEndpoint404Response(exchange);
            }
        } catch (IOException e) {
            HandlerUtilities.write500(exchange, e);
        }
    }

    void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Long> optId = getId(exchange);
        if (optId.isEmpty()) {
            write400Response(exchange);
            return;
        }

        if (manager.findTaskById(optId.get()) == null) {
            writeTask404Response(exchange, optId.get());
            return;
        }
        manager.removeTask(optId.get());
        writeResponse(exchange, "Таск успешно удалён.", 200);
    }

    /**
     * @noinspection unchecked
     */
    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<SimpleTask> tasks = (List<SimpleTask>) manager.getAllTasksByType(TaskType.TASK);
        writeResponse(exchange, gson.toJson(tasks, new SimpleTaskListTypeToken().getType()), 200);
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String input = readRequestBodyAsString(exchange);
        try {
            SimpleTask task = gson.fromJson(input, SimpleTask.class);
            createOrUpdateTask(exchange, task);
        } catch (JsonSyntaxException e) {
            writeResponse(exchange, "Неправильный формат данных", 400);
        }

    }

    private void handleGetTask(HttpExchange exchange) throws IOException {
        Optional<Long> optId = getId(exchange);
        if (optId.isEmpty()) {
            write400Response(exchange);
            return;
        }

        Optional<SimpleTask> optTask = Optional.ofNullable(manager.findSimpleTask(optId.get()));
        if (optTask.isEmpty()) {
            writeTask404Response(exchange, optId.get());
            return;
        }

        writeResponse(exchange, gson.toJson(optTask.get()), 200);
    }


    TaskHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            if (requestMethod.equals("POST")) {
                return TaskHandlerEndpoint.POST_TASK;
            }
            if (requestMethod.equals("GET")) {
                return TaskHandlerEndpoint.GET_TASKS;
            }
        }
        if (pathParts.length == 3) {
            if (requestMethod.equals("GET")) {
                return TaskHandlerEndpoint.GET_TASK;
            }
            if (requestMethod.equals("DELETE")) {
                return TaskHandlerEndpoint.DELETE_TASK;
            }
        }
        return TaskHandlerEndpoint.UNKNOWN;
    }

    public void createOrUpdateTask(HttpExchange exchange, Task task) throws IOException {
        try {
            if (task.getId() != null) {
                manager.updateTask(task);
                writeResponse(exchange, "Успешно обновлён таск с id: " + task.getId(), 201);
            } else {
                manager.addTask(task);
                writeResponse(exchange, "Успешно добавлен таск", 201);
            }
        } catch (IllegalArgumentException e) {
            writeResponse(exchange, e.getMessage(), 406);
        }
    }
}

