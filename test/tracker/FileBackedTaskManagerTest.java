package tracker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import task.EpicTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static tracker.TaskTestUtilities.createRandomEpicTask;

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
            } catch (Exception ignored) {
                fail();
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }
}