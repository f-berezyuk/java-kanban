package tracker;

import task.Task;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getTasksHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
