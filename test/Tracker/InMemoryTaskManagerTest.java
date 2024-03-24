package Tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.SimpleTask;
import task.SubTask;
import task.Task;
import task.TaskType;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class InMemoryTaskManagerTest {
    private final Random random = new Random();
    TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Tracker.Managers.getDefault();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void taskManagerShouldWorkAfterInit() {
        taskManager.printAllTasks();
        taskManager.getHistory();
    }

    @Test
    void addNewTask() {
        Task task = new SimpleTask("Test addNewTask", "Test addNewTask description");
        final Long taskId = taskManager.addTask(task);

        final Task savedTask = taskManager.findTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldUpdateTask() {
        SimpleTask task = createRandomSimpleTask();
        SimpleTask updateTask = createRandomSimpleTask();

        Long id = taskManager.addTask(task);
        updateTask.setId(id);
        taskManager.updateTask(updateTask);

        assertEquals(updateTask, taskManager.findSimpleTask(id));
    }

    @Test
    void shouldThrowIfAddTaskWithExistedId() {
        SimpleTask task = createRandomSimpleTask();
        task.setId(0L);

        Long id = taskManager.addTask(createRandomSimpleTask());

        assertEquals(task.getId(), id);
        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task));
    }

    @Test
    void shouldReturnNullWhenNotExistedId() {
        assertNull(taskManager.findTaskById(-1L));
        assertNull(taskManager.findSubTask(-1L));
        assertNull(taskManager.findSimpleTask(-1L));
        assertNull(taskManager.findEpicTask(-1L));
        assertNull(taskManager.findTasksByParentId(-1L));
    }

    @Test
    void findEpicTaskShouldReturnExpected() {
        EpicTask task = createRandomEpicTask();
        Long id = taskManager.addTask(task);

        assertEquals(task, taskManager.findEpicTask(id));
    }

    @Test
    void subTaskShouldHasParent() {
        Task epic = createRandomEpicTask();
        Task sub = createRandomSubTask();

        Long eid = taskManager.addTask(epic);
        Long sid = taskManager.addTask(sub);

        taskManager.addSubTasksToEpic(eid, sid);

        assertEquals(sub, taskManager.findTasksByParentId(eid).get(0));
        assertEquals(sid, taskManager.findEpicTask(eid).getSubTasksIds().get(0));
    }

    @Test
    void getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(createRandomEpicTask());
            tasks.add(createRandomSubTask());
            tasks.add(createRandomSimpleTask());
        }

        tasks.forEach(task -> taskManager.addTask(task));

        assertEquals(tasks.size(), taskManager.getAllTasks().size());
        assertListEqualsNoOrder(tasks, taskManager.getAllTasks());
    }

    @Test
    void getAllTasksByType() {
        List<SimpleTask> simpleTasks = new ArrayList<>();
        List<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            simpleTasks.add(createRandomSimpleTask());
            subTasks.add(createRandomSubTask());
        }

        simpleTasks.forEach(taskManager::addTask);
        subTasks.forEach(taskManager::addTask);

        List<SimpleTask> actual = (List<SimpleTask>) taskManager.getAllTasksByType(TaskType.TASK);

        assertEquals(actual.size(), simpleTasks.size());
        assertArrayEquals(actual.toArray(), simpleTasks.toArray());

    }

    @Test
    public void shouldThrowWhenEpicAddEpicAsSub() {
        EpicTask epicTask1 = createRandomEpicTask();
        EpicTask epicTask2 = createRandomEpicTask();

        Long eid1 = taskManager.addTask(epicTask1);
        Long eid2 = taskManager.addTask(epicTask2);

        assertThrows(IllegalArgumentException.class, () -> taskManager.addSubTasksToEpic(eid1, eid2));
    }

    @Test
    public void shouldThrowWhenSubAddSubAsParent() {
        SubTask subTask = createRandomSubTask();
        Long sid = taskManager.addTask(subTask);

        assertThrows(IllegalArgumentException.class, () -> taskManager.addSubTasksToEpic(sid, sid));
    }

    @Test
    public void shouldBeUniqueId() {
        HashSet<Long> expected = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            expected.add((long) i);
            taskManager.addTask(createRandomSimpleTask());
        }

        HashSet<Long> actual = (HashSet<Long>) taskManager.getAllTasks()
                .stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        assertEquals(expected.size(), actual.size());
        assertListEqualsNoOrder(Arrays.asList(expected.toArray()), Arrays.asList(actual.toArray()));
    }

    @Test
    public void shouldBeOkWhenBaseUsage() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SubTask randomSubTask = createRandomSubTask();
            EpicTask randomEpicTask = createRandomEpicTask();
            Long sid = taskManager.addTask(randomSubTask);
            Long eid = taskManager.addTask(randomEpicTask);
            ids.add(sid);
            ids.add(eid);
            ids.add(taskManager.addTask(createRandomSimpleTask()));
            taskManager.addSubTasksToEpic(eid, sid);
        }

        System.out.println(taskManager.printAllTasks());

        List<Long> subIds = taskManager.getAllTasksByType(TaskType.SUB).stream().map(Task::getId).toList();
        List<Long> epicIds = taskManager.getAllTasksByType(TaskType.EPIC).stream().map(Task::getId).toList();
        List<Long> simpleIds = taskManager.getAllTasksByType(TaskType.TASK).stream().map(Task::getId).toList();

        assertEquals(ids.size(), (subIds.size() + epicIds.size() + simpleIds.size()));
        assertListEqualsNoOrder(ids, taskManager.getAllTasks().stream().map(Task::getId).toList());
    }

    @Test
    public void shouldReturnMthLastFindOperations() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ids.add(taskManager.addTask(createRandomSimpleTask()));
        }

        for (Long id : ids) {
            taskManager.findTaskById(id);
        }
        Long[] actual = taskManager.getTaskHistory().stream().map(Task::getId).toArray(Long[]::new);

        System.out.println("taskManager.getHistory() = " + taskManager.getHistory());
        assertArrayEquals(ids.subList(ids.size() - 10, ids.size()).toArray(), actual);
    }

    @Test
    void shouldRemoveTask() {
        SimpleTask task = createRandomSimpleTask();
        Long id = taskManager.addTask(task);

        taskManager.removeTask(id);

        assertNull(taskManager.findTaskById(id));
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    void shouldRemoveEpicAndSubTaskWhenRecursiveRemove() {
        List<SubTask> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(createRandomSubTask());
        }

        EpicTask epicTask = createRandomEpicTask();

        Long eid = taskManager.addTask(epicTask);
        tasks.forEach(taskManager::addTask);

        taskManager.addSubTasksToEpic(eid, tasks.stream().map(Task::getId).toArray(Long[]::new));

        assertEquals(11, taskManager.getAllTasks().size());
        assertEquals(10, taskManager.findTasksByParentId(epicTask).size());

        taskManager.removeRecursiveTask(eid);
        assertNull(taskManager.findEpicTask(eid));
        assertNull(taskManager.findTasksByParentId(eid));
        assertEquals(0, taskManager.getAllTasksByType(TaskType.SUB).size());
    }

    @Test
    void shouldRemoveOnlySubTasksFromEpic() {
        SubTask subTask1 = createRandomSubTask();
        SubTask subTask2 = createRandomSubTask();
        SubTask subTask3 = createRandomSubTask();
        EpicTask epicTask = createRandomEpicTask();
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);
        taskManager.addTask(subTask3);
        taskManager.addTask(epicTask);
        taskManager.addSubTasksToEpic(epicTask.getId(), subTask1.getId(), subTask2.getId());

        taskManager.removeRecursiveTask(epicTask.getId());

        assertEquals(subTask3, taskManager.findSubTask(subTask3.getId()));
    }

    private EpicTask createRandomEpicTask() {
        return new EpicTask("Name " + random.nextInt(), "Description " + random.nextDouble());
    }

    private SubTask createRandomSubTask() {
        return new SubTask("Name " + random.nextInt(), "Description " + random.nextDouble());
    }

    private SimpleTask createRandomSimpleTask() {
        return new SimpleTask("Name " + random.nextInt(), "Description " + random.nextDouble());
    }

    private <T> void assertListEqualsNoOrder(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size());

        HashSet<T> hashSet = new HashSet<>(expected);
        actual.forEach(hashSet::remove);
        if (!hashSet.isEmpty()) {
            for (T t : hashSet) {
                System.out.println("t = " + t);
            }
            System.out.println("Expected contains " + hashSet.size() + " values that not matched.");
            for (T t : hashSet) {
                System.out.println("Value = " + t);
            }
            fail();
        }
    }
}
