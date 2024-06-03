package api.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import api.HandlerUtilities;
import api.adapters.SubTaskListTypeToken;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import task.SubTask;
import task.TaskType;
import tracker.TaskManager;

import static api.HandlerUtilities.readRequestBodyAsString;
import static api.HandlerUtilities.write400Response;
import static api.HandlerUtilities.writeEndpoint404Response;
import static api.HandlerUtilities.writeResponse;
import static api.HandlerUtilities.writeTask404Response;

public class SubTaskHandler extends TaskHandler {
    public SubTaskHandler(TaskManager manager) {
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
                case DELETE_TASK -> super.handleDeleteTask(exchange);
                case GET_SUBTASKS,
                        UNKNOWN -> writeEndpoint404Response(exchange);
            }
        } catch (IOException e) {
            HandlerUtilities.write500(exchange, e);
        }
    }

    /**
     * @noinspection unchecked
     */
    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<SubTask> tasks = (List<SubTask>) manager.getAllTasksByType(TaskType.SUB);
        writeResponse(exchange, gson.toJson(tasks, new SubTaskListTypeToken().getType()), 200);
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String input = readRequestBodyAsString(exchange);
        try {
            SubTask task = gson.fromJson(input, SubTask.class);
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

        Optional<SubTask> optTask = Optional.ofNullable(manager.findSubTask(optId.get()));
        if (optTask.isEmpty()) {
            writeTask404Response(exchange, optId.get());
            return;
        }

        writeResponse(exchange, gson.toJson(optTask.get()), 200);
    }


}
