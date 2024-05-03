package tracker;

import java.util.List;

import task.EStatus;
import task.EpicTask;
import task.SimpleTask;
import task.SubTask;
import task.Task;
import task.TaskType;

public interface TaskManager {
    // Create
    Long addTask(Task task);

    // Read
    Task findTaskById(Long id);

    EpicTask findEpicTask(Long id);

    SimpleTask findSimpleTask(Long id);

    SubTask findSubTask(Long id);

    String printAllTasks();

    List<Task> getAllTasks();

    List<? extends Task> getAllTasksByType(TaskType type);

    // Update
    void updateTask(Task task);

    void updateStatus(Long id, EStatus status);

    // Remove
    void removeTask(Long id);

    void removeRecursiveTask(Long id);

    void removeAll();

    void removeAllByType(TaskType type);

    List<Task> getHistory();

    String getHistoryAsString();

    List<SubTask> findTasksByParentId(EpicTask task);

    List<SubTask> findTasksByParentId(Long parentId);

    void addSubTasksToEpic(Long eid, Long... sids);
}
