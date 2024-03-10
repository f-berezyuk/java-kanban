import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import task.EpicTask;
import task.Status;
import task.SubTask;
import task.Task;

public class TaskManager {
    private final HashMap<Long, Task> tasks;

    public TaskManager() {
        this.tasks = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> List<T> getAllTasks(Class<T> type) {
        return tasks.values().stream()
                .filter(task -> task.getClass().equals(type))
                .map(task -> (T) task)
                .collect(Collectors.toList());
    }

    public <T extends Task> void removeAllTasks(Class<T> type) {
        List<Long> keysToRemove = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getClass().equals(type)) {
                keysToRemove.add(task.getId());
            }
        }
        keysToRemove.forEach(this::removeTask);
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> T findTaskById(Class<T> type, Long id) {
        Task task = this.tasks.get(id);
        if (task == null) {
            return null;
        }
        if (task.getClass().equals(type)) {
            return (T) task;
        }
        throw new ClassCastException("Unable to cast task with type ["
                + task.getClass().getName() + "] to type [" + type.getName() + "].");
    }

    public <T extends Task> void addTask(T task) {
        this.tasks.put(task.getId(), task);
    }

    public <T extends Task> void updateTask(T task) {
        this.tasks.replace(task.getId(), task);
    }

    public void removeTask(Long id) {
        removeTask(id, false);
    }

    public void removeTask(Long id, boolean recursive) {
        Task task = this.tasks.get(id);
        if (task != null) {
            Long[] linkedTasks = task.clean();
            if (recursive) {
                Arrays.stream(linkedTasks).forEach(linkedId -> removeTask(linkedId, true));
            }
            this.tasks.remove(id);
        }
    }

    public List<SubTask> getAllSubTasksFrom(EpicTask task) {
        return getAllSubTasksFrom(task.getId());
    }

    public List<SubTask> getAllSubTasksFrom(Long id) {
        EpicTask task = (EpicTask) this.tasks.get(id);
        return task.getSubTasks();
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    public void removeAllTasks() {
        this.tasks.clear();
    }

    public void updateStatus(Long id, Status status) {
        this.tasks.get(id).updateStatus(status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Task task : getAllTasks()) {
            sb.append(task);
            sb.append("\n");
        }

        return sb.toString();
    }

    public Task findTaskById(Long id) {
        return tasks.get(id);
    }
}
