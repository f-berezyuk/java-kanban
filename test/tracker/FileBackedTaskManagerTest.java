package tracker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;
import task.EpicTask;
import task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static tracker.TaskTestUtilities.createRandomEpicTask;
import static tracker.TaskTestUtilities.createRandomSimpleTask;
import static tracker.TaskTestUtilities.withId;

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
                fail(e.getMessage());
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
                fail(e.getMessage());
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }
}