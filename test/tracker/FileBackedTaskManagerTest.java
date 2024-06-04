package tracker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static utilities.TaskTestUtilities.addTime;
import static utilities.TaskTestUtilities.createRandomEpicTask;
import static utilities.TaskTestUtilities.createRandomSimpleTask;
import static utilities.TaskTestUtilities.withId;

class FileBackedTaskManagerTest {
    @Test
    void shouldSaveLoadFile() throws Exception {
        Path path = Path.of("testFile.csv");
        try {
            File testFile = Files.createFile(path).toFile();
            EpicTask randomEpicTask = createRandomEpicTask();
            try (TaskManager manager = Managers.getFileBasedTaskManager(testFile)) {
                manager.addTask(randomEpicTask);
            }

            try (TaskManager manager = Managers.getFileBasedTaskManager(testFile)) {
                assertEquals(randomEpicTask, manager.findTaskById(randomEpicTask.getId()));
            } catch (Exception e) {
                fail(e.getMessage(), e);
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void shouldSaveLoadHistory() throws Exception {
        Path path = Path.of("testFile.csv");
        try {
            File testFile = Files.createFile(path).toFile();

            LinkedList<Task> orderedTasks = new LinkedList<>();

            for (int i = 0; i < 10; i++) {
                orderedTasks.add(withId(createRandomSimpleTask(), i));
            }
            System.out.println(Arrays.toString(orderedTasks.toArray()));

            try (TaskManager manager = Managers.getFileBasedTaskManager(testFile)) {
                for (Task task : orderedTasks) {
                    manager.addTask(task);
                }
                System.out.println(manager.getHistoryAsString());
            }

            try (TaskManager manager = Managers.getFileBasedTaskManager(testFile)) {
                System.out.println(manager.getHistoryAsString());
                assertIterableEquals(orderedTasks, manager.getHistory());
            } catch (Exception e) {
                fail(e.getMessage(), e);
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void shouldSaveLoadHistoryWithTime() throws Exception {
        Path path = Path.of("testFile.csv");
        try {
            File testFile = Files.createFile(path).toFile();

            LinkedList<Task> orderedTasks = new LinkedList<>();

            for (int i = 0; i < 10; i++) {
                orderedTasks.add(withId(addTime(createRandomSimpleTask(),
                        LocalDateTime.now().plus(Duration.ofHours(i)),
                        Duration.ofMinutes(i + 1)), i));
            }
            System.out.println(Arrays.toString(orderedTasks.toArray()));

            try (TaskManager manager = Managers.getFileBasedTaskManager(testFile)) {
                for (Task task : orderedTasks) {
                    manager.addTask(task);
                }
                System.out.println(manager.getHistoryAsString());
            }

            try (TaskManager manager = Managers.getFileBasedTaskManager(testFile)) {
                assertIterableEquals(orderedTasks, manager.getHistory());
            } catch (Exception e) {
                fail(e.getMessage(), e);
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }
}