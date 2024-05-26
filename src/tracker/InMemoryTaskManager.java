package tracker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import task.EStatus;
import task.EpicTask;
import task.SimpleTask;
import task.SubTask;
import task.Task;
import task.TaskType;

public class InMemoryTaskManager implements TaskManager {
    private static Long idGeneratorCount = 0L;
    private final HashMap<Long, SimpleTask> simpleTasks;
    private final HashMap<Long, EpicTask> epicTasks;
    private final HashMap<Long, SubTask> subTasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        idGeneratorCount = 0L;
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
        simpleTasks = new HashMap<>();
        historyManager = Managers.getTasksHistoryManager();
    }

    @Override
    public Long addTask(Task task) {
        if (task.getId() == null) {
            task.setId(idGeneratorCount++);
        } else if (findTaskById(task.getId()) != null) {
            throw new IllegalArgumentException("Attempt to rewrite existed task.");
        } else if (idGeneratorCount <= task.getId()) {
            idGeneratorCount = task.getId() + 1;
        }

        switch (task.getType()) {
            case SUB -> {
                subTasks.put(task.getId(), (SubTask) task);
                Optional.ofNullable(((SubTask) task).getParent())
                        .ifPresent(parent -> {
                            checkEpicStatus(parent);
                            checkDuration(parent);
                        });

            }
            case TASK -> simpleTasks.put(task.getId(), (SimpleTask) task);
            case EPIC -> epicTasks.put(task.getId(), (EpicTask) task);
        }

        historyManager.add(task);

        return task.getId();
    }


    @Override
    public void addSubTasksToEpic(Long eid, Long... sids) {
        if (epicTasks.containsKey(eid)) {
            EpicTask task = epicTasks.get(eid);
            StringBuilder sb = new StringBuilder();
            for (Long sid : sids) {
                if (!subTasks.containsKey(sid)) {
                    sb.append("Did not find sub task with id: [").append(sid).append("]\n");
                }
            }
            if (sb.length() > 0) {
                throw new IllegalArgumentException(sb.toString());
            }
            task.addSubTasks(sids);
            Arrays.stream(sids).forEach(sid -> subTasks.get(sid).setParent(eid));
        } else {
            throw new IllegalArgumentException("Did not find epic task with id: [" + eid + "]");
        }
    }

    @Override
    public Task fromDto(TaskDTO dto) throws NumberFormatException {
        Task task = new SimpleTask(dto.name, dto.description);
        task.setId(dto.id);
        task.setStatus(dto.status);
        if(dto.startTime != null) {
            task.setStartTime(dto.startTime);
            task.setDuration(dto.duration);
        }
        switch (dto.type) {
            case TASK -> {
                return task;
            }
            case EPIC -> {
                return new EpicTask(task);
            }
            case SUB -> {
                SubTask subTask = new SubTask(task);
                Long parentId = dto.parent;
                if (epicTasks.containsKey(parentId)) {
                    subTask.setParent(parentId);
                    epicTasks.get(parentId).addSubTask(subTask.getId());
                    checkDuration(parentId);
                }
                return subTask;
            }
            default -> throw new IllegalArgumentException("Unknown task type: [" + dto.type + "]");
        }
    }

    @Override
    public Task findTaskById(Long id) {
        Task result = null;
        Task copy = null;
        if (simpleTasks.containsKey(id)) {
            result = simpleTasks.get(id);
            copy = new SimpleTask(result);
        }
        if (epicTasks.containsKey(id)) {
            result = epicTasks.get(id);
            copy = new EpicTask(result);
        }
        if (subTasks.containsKey(id)) {
            result = subTasks.get(id);
            copy = new SubTask(result);
        }
        if (result != null) {
            historyManager.add(copy);
        }
        return result;
    }

    @Override
    public EpicTask findEpicTask(Long id) {
        return (EpicTask) findTaskById(id);
    }

    @Override
    public SimpleTask findSimpleTask(Long id) {
        return (SimpleTask) findTaskById(id);
    }

    @Override
    public SubTask findSubTask(Long id) {
        return (SubTask) findTaskById(id);
    }

    @Override
    public String printAllTasks() {
        StringBuilder sb = new StringBuilder();

        for (SimpleTask task : simpleTasks.values()) {
            sb.append(task.toString());
            sb.append('\n');
        }

        for (EpicTask task : epicTasks.values()) {
            sb.append(task.toString());
            sb.append('\n');
        }

        for (SubTask task : subTasks.values()) {
            sb.append(task.toString());
            sb.append('\n');
        }

        return sb.toString();
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        result.addAll(simpleTasks.values());
        result.addAll(epicTasks.values());
        result.addAll(subTasks.values());

        return result;
    }

    @Override
    public List<? extends Task> getAllTasksByType(TaskType type) {
        switch (type) {
            case SUB -> {
                return subTasks.values().stream().toList();
            }
            case TASK -> {
                return simpleTasks.values().stream().toList();
            }
            case EPIC -> {
                return epicTasks.values().stream().toList();
            }
            default -> throw new IllegalArgumentException("Task type is undefined.");
        }
    }

    @Override
    public void updateTask(Task task) {
        Task result = null;
        switch (task.getType()) {
            case SUB -> {
                result = subTasks.replace(task.getId(), (SubTask) task);
                checkEpicStatus(((SubTask) task).getParent());
            }
            case TASK -> result = simpleTasks.replace(task.getId(), (SimpleTask) task);
            case EPIC -> result = epicTasks.replace(task.getId(), (EpicTask) task);
        }
        if (result == null) {
            throw new IllegalArgumentException("Task with id: [" + task.getId() + "] does not exist.");
        }
    }

    @Override
    public void updateStatus(Long id, EStatus status) {
        Task task = findTaskById(id);
        if (task != null) {
            switch (task.getType()) {
                case TASK -> task.setStatus(status);
                case SUB -> {
                    task.setStatus(status);
                    checkEpicStatus(((SubTask) task).getParent());
                }
                default -> {
                }
            }
        }
    }

    private void checkDuration(Long parent) {
        Optional.ofNullable(epicTasks.get(parent))
                .ifPresent(epic ->
                        {
                            List<SubTask> subs = epic.getSubTasksIds()
                                    .stream()
                                    .map(subTasks::get)
                                    .filter(Objects::nonNull)
                                    .toList();

                            epic.setStartTime(subs
                                    .stream()
                                    .filter(subTask -> subTask.getStartTime() != null)
                                    .min(Comparator.comparing(Task::getStartTime))
                                    .orElseThrow(RuntimeException::new)
                                    .getStartTime());
                            epic.setEndTime(subs
                                    .stream()
                                    .filter(subTask -> subTask.getEndTime() != null)
                                    .max(Comparator.comparing(Task::getEndTime))
                                    .orElseThrow(RuntimeException::new)
                                    .getEndTime());
                            epic.setDuration(subs
                                    .stream()
                                    .map(Task::getDuration)
                                    .filter(Objects::nonNull)
                                    .reduce(Duration::plus)
                                    .orElseThrow(RuntimeException::new));

                        }
                );
    }

    private void checkEpicStatus(Long id) {
        EpicTask epicTask = (EpicTask) findTaskById(id);
        if (epicTask == null) {
            return;
        }
        List<Long> subTasksIds = epicTask.getSubTasksIds();
        if (subTasksIds.stream().allMatch(sId -> subTasks.get(sId).getStatus() == EStatus.DONE)) {
            epicTask.setStatus(EStatus.DONE);
        } else if (subTasksIds.stream().allMatch(sId -> subTasks.get(sId).getStatus() == EStatus.NEW)) {
            epicTask.setStatus(EStatus.NEW);
        } else {
            epicTask.setStatus(EStatus.IN_PROGRESS);
        }
    }

    @Override
    public void removeTask(Long id) {
        Task task = findTaskById(id);
        switch (task.getType()) {
            case TASK -> simpleTasks.remove(id);
            case EPIC -> {
                ((EpicTask) task).getSubTasksIds().stream().filter(subTasks::containsKey).forEach(sid -> subTasks.get(sid).removeParent());
                epicTasks.remove(id);
            }
            case SUB -> {
                Long parent = ((SubTask) task).getParent();
                if (epicTasks.containsKey(parent)) {
                    epicTasks.get(parent).removeSubTask(id);
                }
                subTasks.remove(id);
            }
            default -> {
            }
        }

        historyManager.remove(id);
    }

    @Override
    public void removeRecursiveTask(Long id) {
        Task task = findTaskById(id);
        switch (task.getType()) {
            case TASK -> simpleTasks.remove(id);
            case EPIC -> {
                ((EpicTask) task).getSubTasksIds().forEach(subTasks::remove);
                ((EpicTask) task).getSubTasksIds().forEach(historyManager::remove);
                epicTasks.remove(id);
            }
            case SUB -> {
                Long parent = ((SubTask) task).getParent();
                removeTask(id);
                removeRecursiveTask(parent);
            }
            default -> {
            }
        }

        historyManager.remove(id);
    }

    @Override
    public void removeAll() {
        simpleTasks.clear();
        epicTasks.clear();
        subTasks.clear();
        historyManager.clear();
    }

    @Override
    public void removeAllByType(TaskType type) {
        switch (type) {
            case TASK -> simpleTasks.clear();
            case EPIC -> {
                List<Long> epicIds = epicTasks.keySet().stream().toList();
                epicIds.forEach(this::removeTask);
            }
            case SUB -> {
                List<Long> subIds = subTasks.keySet().stream().toList();
                subIds.forEach(this::removeTask);
            }
            default -> {
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public String getHistoryAsString() {
        return historyManager.getHistoryAsString();
    }

    @Override
    public List<SubTask> findTasksByParentId(EpicTask task) {
        return findTasksByParentId(task.getId());
    }

    @Override
    public List<SubTask> findTasksByParentId(Long parentId) {
        EpicTask epicTask = epicTasks.get(parentId);
        if (epicTask != null) {
            return epicTask.getSubTasksIds().stream().map(this::findSubTask).toList();
        }

        return null;
    }

    @Override
    public void close() {

    }
}
