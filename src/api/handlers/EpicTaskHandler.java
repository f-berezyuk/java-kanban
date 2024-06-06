package api.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import api.HandlerUtilities;
import api.adapters.EpicTaskListTypeToken;
import api.adapters.SubTaskListTypeToken;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import task.EpicTask;
import task.TaskType;
import tracker.TaskManager;

import static api.HandlerUtilities.readRequestBodyAsString;
import static api.HandlerUtilities.write400Response;
import static api.HandlerUtilities.writeEndpoint404Response;
import static api.HandlerUtilities.writeResponse;
import static api.HandlerUtilities.writeTask404Response;

/**
 * @noinspection DuplicatedCode
 */
public class EpicTaskHandler extends TaskHandler {
    public EpicTaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            TaskHandlerEndpoint endpoint = getEndpoint(exchange);
            if (endpoint == TaskHandlerEndpoint.UNKNOWN) {
                writeEndpoint404Response(exchange);
            } else {
                switch (endpoint) {

                    case GET_TASK -> handleGetTask(exchange);
                    case POST_TASK -> handlePostTask(exchange);
                    case GET_TASKS -> handleGetTasks(exchange);
                    case DELETE_TASK -> super.handleDeleteTask(exchange);
                    case GET_SUBTASKS -> handleGetSubTasks(exchange);
                }
            }
        } catch (IOException e) {
            HandlerUtilities.write500(exchange, e);
        }
    }

    private void handleGetSubTasks(HttpExchange exchange) throws IOException {
        Optional<Long> optId = getId(exchange);
        if (optId.isEmpty()) {
            write400Response(exchange);
            return;
        }

        Optional<EpicTask> optTask = Optional.ofNullable(manager.findEpicTask(optId.get()));
        if (optTask.isEmpty()) {
            writeTask404Response(exchange, optId.get());
            return;
        }

        writeResponse(exchange, gson.toJson(manager.findTasksByParentId(optId.get()),
                new SubTaskListTypeToken().getType()), 200);
    }

    /**
     * @noinspection unchecked
     */
    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<EpicTask> tasks = (List<EpicTask>) manager.getAllTasksByType(TaskType.EPIC);
        writeResponse(exchange, gson.toJson(tasks, new EpicTaskListTypeToken().getType()), 200);
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String input = readRequestBodyAsString(exchange);
        try {
            EpicTask task = gson.fromJson(input, EpicTask.class);
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

        Optional<EpicTask> optTask = Optional.ofNullable(manager.findEpicTask(optId.get()));
        if (optTask.isEmpty()) {
            writeTask404Response(exchange, optId.get());
            return;
        }

        writeResponse(exchange, gson.toJson(optTask.get()), 200);
    }

    @Override
    TaskHandlerEndpoint getEndpoint(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 4 && requestMethod.equals("GET") && Objects.equals(pathParts[3], "subtasks")) {
            return TaskHandlerEndpoint.GET_SUBTASKS;
        } else {
            return super.getEndpoint(exchange);
        }
    }
}
