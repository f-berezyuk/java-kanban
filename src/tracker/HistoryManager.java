package tracker;

import java.util.List;

import task.Task;

public interface HistoryManager {
    String getHistoryAsString();

    void add(Task task);

    List<Task> getHistory();

    void remove(Long id);

    void clear();
}
