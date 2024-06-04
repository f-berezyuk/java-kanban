package api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import api.adapters.EpicTaskListTypeToken;
import api.adapters.SimpleTaskListTypeToken;
import api.adapters.SubTaskListTypeToken;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SimpleTask;
import task.SubTask;
import task.Task;
import task.TaskType;
import tracker.Managers;
import tracker.TaskManager;
import utilities.TaskTestUtilities;

import static api.HandlerUtilities.createGson;
import static utilities.TaskTestUtilities.addTime;
import static utilities.TaskTestUtilities.createRandomEpicTask;
import static utilities.TaskTestUtilities.createRandomSimpleTask;
import static utilities.TaskTestUtilities.createRandomSubTask;
import static utilities.TaskTestUtilities.withId;

/**
 * @noinspection resource
 */
class HttpTaskServerTest {

    Gson gson = createGson();

    private static HttpResponse<String> getStringHttpResponse(String path) throws IOException, InterruptedException {
        HttpResponse<String> response;
        HttpClient client = HttpClient.newHttpClient();
        response = client.send(HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build(), HttpResponse.BodyHandlers.ofString());

        return response;
    }

    private static HttpResponse<String> postRequest(TaskManager manager, String taskJson, String path) throws Exception {
        try (HttpTaskServer ignored = new HttpTaskServer(manager)) {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080" + path))
                            .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(15))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());

            return response;
        }
    }

    @Test
    public void shouldStartAndStop() {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());
        server.close();
    }

    @Test
    public void shouldPongResponse() throws Exception {
        try (HttpTaskServer ignored = new HttpTaskServer(Managers.getDefault())) {
            String path = "/ping";
            HttpResponse<String> response;
            response = getStringHttpResponse(path);
            System.out.println(response.body());
            Assertions.assertEquals(response.body(), "pong");
            System.out.println(response.statusCode());
        }
    }

    @Test
    public void shouldSerializeDeserializeTaskWithTime() {
        Task task = withId(addTime(createRandomSimpleTask()), 121);
        String taskJson = gson.toJson(task);
        System.out.println(task);
        SimpleTask fromJson = gson.fromJson(taskJson, SimpleTask.class);
        System.out.println(fromJson);
        Assertions.assertEquals(task, fromJson);

    }

    @Test
    public void shouldSerializeDeserializeSimpleTask() {
        SimpleTask randomSimpleTask = createRandomSimpleTask();
        String task = gson.toJson(randomSimpleTask);
        System.out.println(task);
        SimpleTask fromJson = gson.fromJson(task, SimpleTask.class);
        System.out.println(fromJson);
        Assertions.assertEquals(randomSimpleTask, fromJson);
    }

    @Test
    public void shouldSerializeDeserializeEpicTask() {
        EpicTask randomEpicTask = createRandomEpicTask();
        randomEpicTask.addSubTasks(214L, 124L, 421L, 412L, 4214L);
        String task = gson.toJson(randomEpicTask);
        System.out.println(task);
        EpicTask fromJson = gson.fromJson(task, EpicTask.class);
        System.out.println(fromJson);
        Assertions.assertEquals(randomEpicTask, fromJson);
    }

    @Test
    public void shouldSerializeDeserializeSubTask() {
        SubTask randomSubTask = createRandomSubTask();
        randomSubTask.setParent(13L);
        String task = gson.toJson(randomSubTask);
        System.out.println(task);
        SubTask fromJson = gson.fromJson(task, SubTask.class);
        System.out.println(fromJson);
        Assertions.assertEquals(randomSubTask, fromJson);
    }

    @Test
    public void shouldAddSimpleTask() throws Exception {
        TaskManager manager = Managers.getDefault();
        String taskJson = gson.toJson(createRandomSimpleTask(), SimpleTask.class);
        String path = "/tasks";
        postRequest(manager, taskJson, path);

        Assertions.assertTrue(manager.getAllTasks().stream().findAny().isPresent());
    }

    @Test
    public void shouldAddEpicTask() throws Exception {
        TaskManager manager = Managers.getDefault();
        String taskJson = gson.toJson(createRandomEpicTask(), EpicTask.class);
        String path = "/epics";
        postRequest(manager, taskJson, path);

        Assertions.assertTrue(manager.getAllTasks().stream().findAny().isPresent());
    }

    @Test
    public void shouldAddSubTask() throws Exception {
        TaskManager manager = Managers.getDefault();
        String taskJson = gson.toJson(createRandomSubTask(), SubTask.class);
        String path = "/subtasks";
        postRequest(manager, taskJson, path);

        Assertions.assertTrue(manager.getAllTasks().stream().findAny().isPresent());
    }

    /**
     * @noinspection unchecked
     */
    @Test
    public void shouldReturnAllTasks() throws Exception {
        TaskManager manager = Managers.getDefault();
        manager.addTask(createRandomSubTask());
        manager.addTask(createRandomSubTask());
        manager.addTask(createRandomSubTask());
        manager.addTask(createRandomEpicTask());
        manager.addTask(createRandomEpicTask());
        manager.addTask(createRandomEpicTask());
        manager.addTask(createRandomSimpleTask());
        manager.addTask(createRandomSimpleTask());
        manager.addTask(createRandomSimpleTask());

        try (HttpTaskServer ignored = new HttpTaskServer(manager)) {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();

            String[] paths = {"/tasks", "/subtasks", "/epics"};
            TaskType[] types = {TaskType.TASK, TaskType.SUB, TaskType.EPIC};


            for (int i = 0; i < 3; i++) {
                response = client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080" + paths[i]))
                                .GET()
                                .header("Accept", "application/json")
                                .header("Content-Type", "application/json")
                                .timeout(Duration.ofSeconds(15))
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
                System.out.println(response.body());
                System.out.println(response.statusCode());

                switch (types[i]) {
                    case TASK -> TaskTestUtilities.assertListEqualsNoOrder(
                            (List<SimpleTask>) manager.getAllTasksByType(types[i]),
                            gson.fromJson(response.body(), new SimpleTaskListTypeToken().getType()));
                    case EPIC -> TaskTestUtilities.assertListEqualsNoOrder(
                            (List<EpicTask>) manager.getAllTasksByType(types[i]),
                            gson.fromJson(response.body(), new EpicTaskListTypeToken().getType()));
                    case SUB -> TaskTestUtilities.assertListEqualsNoOrder(
                            (List<SubTask>) manager.getAllTasksByType(types[i]),
                            gson.fromJson(response.body(), new SubTaskListTypeToken().getType()));
                }
            }
        }
    }

    @Test
    public void shouldDeleteTask() throws Exception {
        TaskManager manager = Managers.getDefault();
        Long id = manager.addTask(createRandomSimpleTask());
        try (HttpTaskServer ignored = new HttpTaskServer(manager)) {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/tasks" + "/" + id))
                            .DELETE()
                            .timeout(Duration.ofSeconds(15))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());
        }
        Assertions.assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    public void shouldReturnTask() throws Exception {
        TaskManager manager = Managers.getDefault();
        SimpleTask task = createRandomSimpleTask();
        Long id = manager.addTask(task);
        try (HttpTaskServer ignored = new HttpTaskServer(manager)) {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/tasks" + "/" + id))
                            .GET()
                            .timeout(Duration.ofSeconds(15))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());

            Assertions.assertEquals(task, gson.fromJson(response.body(), SimpleTask.class));
        }
    }

    @Test
    public void shouldReturnPrioritized() throws IOException, InterruptedException {
        TaskManager manager = Managers.getDefault();

        for (int i = 0; i < 10; i++) {
            manager.addTask(addTime(createRandomSimpleTask(),
                    LocalDateTime.now().plus(Duration.ofHours(i)),
                    Duration.ofMinutes(i + 1)));
        }
        try (HttpTaskServer ignored = new HttpTaskServer(manager)) {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/prioritized"))
                            .GET()
                            .timeout(Duration.ofSeconds(15))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());
        }
    }

    @Test
    public void shouldReturnHistory() throws IOException, InterruptedException {
        TaskManager manager = Managers.getDefault();
        SimpleTask[] tasks = {
                (SimpleTask) addTime(createRandomSimpleTask()),
                (SimpleTask) addTime(createRandomSimpleTask()),
                (SimpleTask) addTime(createRandomSimpleTask()),
                (SimpleTask) addTime(createRandomSimpleTask()),
                (SimpleTask) addTime(createRandomSimpleTask()),
                (SimpleTask) addTime(createRandomSimpleTask())
        };

        for (SimpleTask task : tasks) {
            try {
                manager.addTask(task);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        try (HttpTaskServer ignored = new HttpTaskServer(manager)) {
            HttpResponse<String> response;
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/history"))
                            .GET()
                            .timeout(Duration.ofSeconds(15))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            System.out.println(response.statusCode());
        }
    }

    @Test
    public void shouldReturn406WhenIntersect() throws Exception {
        TaskManager manager = Managers.getDefault();
        LocalDateTime now = LocalDateTime.now();
        SimpleTask task1 = (SimpleTask) addTime(createRandomSimpleTask(), now, Duration.ofMinutes(100));
        SimpleTask task2 = (SimpleTask) addTime(createRandomSimpleTask(), now.plus(Duration.ofMinutes(10)),
                Duration.ofMinutes(100));
        String taskJson1 = gson.toJson(task1, SimpleTask.class);
        String taskJson2 = gson.toJson(task2, SimpleTask.class);
        String path = "/tasks";
        HttpResponse<String> response1 = postRequest(manager, taskJson1, path);
        Assertions.assertEquals(201, response1.statusCode());
        HttpResponse<String> response2 = postRequest(manager, taskJson2, path);
        Assertions.assertEquals(406, response2.statusCode());
    }
}