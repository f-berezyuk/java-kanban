import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import task.EpicTask;
import task.Status;
import task.SubTask;
import task.Task;

public class TaskManager {
    private static Long idGeneratorCount = 0L;
    private final HashMap<Long, Task> tasks;
    private final HashMap<Long, EpicTask> epicTasks;
    private final HashMap<Long, SubTask> subTasks;

    public TaskManager() {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>(tasks.values());
        allTasks.addAll(subTasks.values());
        allTasks.addAll(epicTasks.values());
        return allTasks;
    }

    public void addTask(Task task) {
        task.setId(idGeneratorCount++);
        tasks.put(task.getId(), task);
    }

    public void addTask(SubTask task) {
        task.setId(idGeneratorCount++);
        subTasks.put(task.getId(), task);
    }

    public void addTask(EpicTask task) {
        task.setId(idGeneratorCount++);
        epicTasks.put(task.getId(), task);
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.replace(task.getId(), task);
        } else if (epicTasks.containsKey(task.getId())) {
            epicTasks.replace(task.getId(), (EpicTask) task);
        } else if (subTasks.containsKey(task.getId())) {
            subTasks.replace(task.getId(), (SubTask) task);
        } else {
            throw new IllegalArgumentException("Task with id: [" + task.getId() + "] does not exist.");
        }
    }

    public void removeTask(Long id) {
        removeTask(id, false);
    }

    public void removeTask(Long id, boolean recursive) {
        if (tasks.remove(id) != null) {
            return;
        }
        if (subTasks.containsKey(id)) {
            Long parent = subTasks.get(id).getParent();
            if (recursive) {
                removeTask(parent);
            } else if (epicTasks.containsKey(parent)) {
                epicTasks.get(parent).removeSubTask(id);
            }
            subTasks.remove(id);
        } else if (epicTasks.containsKey(id)) {
            for (Long subTaskId : epicTasks.get(id).getSubTasksIds()) {
                if (recursive) {
                    subTasks.remove(subTaskId);
                } else {
                    subTasks.get(subTaskId).removeParent();
                }
            }
            epicTasks.remove(id).removeSubTasks();
        }
    }

    public void addToEpic(Long epicId, Long subId) {
        epicTasks.get(epicId).addSubTask(subId);
        subTasks.get(subId).setParent(epicId);
    }

    public void addAllToEpic(Long epicId, Long... ids) {
        List.of(ids).forEach(id -> addToEpic(epicId, id));
    }

    public List<SubTask> getAllSubTasksFrom(EpicTask task) {
        return getAllSubTasksFrom(task.getId());
    }

    public List<SubTask> getAllSubTasksFrom(Long id) {
        if (epicTasks.containsKey(id)) {
            return epicTasks.get(id).getSubTasksIds().stream().map(subTasks::get).toList();
        }
        return null;
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllEpicTasks(boolean recursive) {
        int length = epicTasks.size();
        Long[] ids = epicTasks.keySet().toArray(new Long[0]);
        for (int i = 0; i < length; i++) {
            long id = ids[i];
            removeTask(id, recursive);
        }
    }

    public void removeAllSubTasks(boolean recursive) {
        int length = subTasks.size();
        Long[] ids = subTasks.keySet().toArray(new Long[0]);
        for (int i = 0; i < length; i++) {
            long id = ids[i];
            removeTask(id, recursive);
        }
    }

    public void removeAll() {
        tasks.clear();
        epicTasks.clear();
        subTasks.clear();
    }

    public void updateStatus(Long id, Status status) {
        if (tasks.containsKey(id)) {
            tasks.get(id).updateStatus(status);
        } else if (subTasks.containsKey(id)) {
            subTasks.get(id).updateStatus(status);
            updateEpicTaskStatus(subTasks.get(id).getParent());
        }
    }

    public void updateEpicTaskStatus(Long id) {
        EpicTask epicTask = epicTasks.get(id);
        epicTask.updateStatus(
                (epicTask.getSubTasksIds().stream().allMatch(subId ->
                        subTasks.get(subId).getStatus() == Status.NEW)) ? Status.NEW
                        : epicTask.getSubTasksIds().stream().allMatch(subId ->
                        subTasks.get(subId).getStatus() == Status.DONE) ? Status.DONE
                        : Status.IN_PROGRESS
        );
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
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epicTasks.containsKey(id)) {
            return epicTasks.get(id);
        } else {
            return subTasks.getOrDefault(id, null);
        }
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public List<EpicTask> getAllEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    public void removeAllEpicTasks() {
        removeAllEpicTasks(false);
    }

    public void removeAllSubTasks() {
        removeAllSubTasks(false);
    }
}
