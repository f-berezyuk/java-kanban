package tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import static tracker.TaskTestUtilities.assertListEqualsNoOrder;
import static tracker.TaskTestUtilities.createRandomEpicTask;
import static tracker.TaskTestUtilities.createRandomSimpleTask;
import static tracker.TaskTestUtilities.createRandomSubTask;
import static tracker.TaskTestUtilities.random;

class InMemoryTaskManagerTest {
    TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = tracker.Managers.getDefault();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void shouldWorkAfterInit() {
        taskManager.printAllTasks();
        taskManager.getHistoryAsString();
    }

    @Test
    void shouldAddNewTask() {
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
    void whenFindEpicTaskShouldReturnExpected() {
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
    void shouldGetAllTasks() {
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

    /**
     * @noinspection unchecked
     */
    @Test
    void shouldGetAllTasksByType() {
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
    public void shouldReturnAllHistoryOperations() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ids.add(taskManager.addTask(createRandomSimpleTask()));
        }

        for (Long id : ids) {
            taskManager.findTaskById(id);
        }
        Long[] actual = taskManager.getHistory().stream().map(Task::getId).toArray(Long[]::new);

        System.out.println("taskManager.getHistory() = " + taskManager.getHistoryAsString());
        assertArrayEquals(ids.toArray(), actual);
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

    @Test
    void shouldNotThrowWhenUpdateTaskId() {
        SimpleTask task = createRandomSimpleTask();
        taskManager.addTask(task);
        Long oldId = task.getId();

        taskManager.findSimpleTask(oldId).setId(random.nextLong());

        assertNotNull(taskManager.findSimpleTask(oldId));
    }

    @Test
    void shouldAddTaskIntoHistoryWhenAdd() {
        Task task = createRandomEpicTask();

        taskManager.addTask(task);

        assertEquals(taskManager.getHistory().get(0), task);
    }


    @Test
    void shouldRemoveHistoryNotesWhenRemoveTask() {
        Task task = createRandomEpicTask();
        taskManager.addTask(task);

        taskManager.removeTask(task.getId());

        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    void extraTask() {
        Task simpleTask1 = createRandomSimpleTask();
        Task simpleTask2 = createRandomSimpleTask();
        Task epicTask1 = createRandomEpicTask();
        Task epicTask2 = createRandomEpicTask();
        Task subTask1 = createRandomSubTask();
        Task subTask2 = createRandomSubTask();
        Task subTask3 = createRandomSubTask();

        taskManager.addTask(simpleTask1);
        taskManager.addTask(simpleTask2);
        taskManager.addTask(epicTask1);
        taskManager.addTask(epicTask2);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);
        taskManager.addTask(subTask3);

        taskManager.addSubTasksToEpic(epicTask1.getId(), subTask1.getId(), subTask2.getId(), subTask3.getId());

        System.out.println("All tasks:");
        System.out.println(taskManager.printAllTasks());

        System.out.println("History version 1:");
        System.out.println(taskManager.getHistoryAsString());

        System.out.println("Do operation with first simple task, second epic task, second subtask.");
        taskManager.findTaskById(simpleTask1.getId());
        taskManager.findTaskById(epicTask2.getId());
        taskManager.findTaskById(subTask2.getId());
        System.out.println("History version 2:");
        System.out.println(taskManager.getHistoryAsString());

        System.out.println("Remove second simple task.");
        taskManager.removeTask(simpleTask2.getId());
        System.out.println("History version 3:");
        System.out.println(taskManager.getHistoryAsString());

        System.out.println("Remove recursively first epic task.");
        taskManager.removeRecursiveTask(epicTask1.getId());
        System.out.println("History version 4:");
        System.out.println(taskManager.getHistoryAsString());
    }

}
