package tracker;

import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBasedTaskManager(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }

    public static HistoryManager getTasksHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
