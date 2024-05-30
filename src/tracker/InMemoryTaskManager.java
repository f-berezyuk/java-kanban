package tracker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

    private final TreeSet<Task> tasksOrderByStartTime;
    private final HistoryManager historyManager;
    private Integer cashHash;

    public InMemoryTaskManager() {
        idGeneratorCount = 0L;
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
        simpleTasks = new HashMap<>();
        historyManager = Managers.getTasksHistoryManager();
        tasksOrderByStartTime = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        cashHash = tasksOrderByStartTime.hashCode();
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

        if (!isValid(task)) {
            throw new IllegalArgumentException("Tasks intersection.");
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
        if (task.getStartTime() != null) {
            addTaskToTreeSet(task);
        }

        return task.getId();
    }

    private boolean isValid(Task task) {
        return
                !(tasksOrderByStartTime.stream()
                        .anyMatch(t -> t.getStartTime().isBefore(task.getStartTime())
                                && t.getEndTime().isAfter(task.getStartTime()))
                        || tasksOrderByStartTime.stream()
                        .anyMatch(t -> task.getStartTime().isBefore(t.getStartTime())
                                && task.getEndTime().isAfter(t.getStartTime())));
    }


    @Override
    public void addSubTasksToEpic(Long eid, Long... sids) {
        if (epicTasks.containsKey(eid)) {
            EpicTask task = epicTasks.get(eid);
            String sb = Arrays.stream(sids)
                    .filter(sid -> !subTasks.containsKey(sid))
                    .map(sid -> "Did not find sub task with id: [" + sid + "]\n")
                    .collect(Collectors.joining());
            if (sb.length() > 0) {
                throw new IllegalArgumentException(sb);
            }
            task.addSubTasks(sids);
            Arrays.stream(sids).forEach(sid -> subTasks.get(sid).setParent(eid));
            checkDuration(task.getId());
            checkEpicStatus(task.getId());
        } else {
            throw new IllegalArgumentException("Did not find epic task with id: [" + eid + "]");
        }
    }

    @Override
    public Task fromDto(TaskDTO dto) throws NumberFormatException {
        Task task = new SimpleTask(dto.name, dto.description);
        task.setId(dto.id);
        task.setStatus(dto.status);
        if (dto.startTime != null) {
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
    public List<Task> getPrioritizedTasks() {
        checkTreeSetCache();

        return tasksOrderByStartTime.stream().toList();
    }

    private void addTaskToTreeSet(Task task) {
        if (cashHash != tasksOrderByStartTime.hashCode()) {
            System.out.println("tasks were changed. Update tree.");
            checkTreeSetCache();
        } else {
            tasksOrderByStartTime.add(task);
            updateCache();
        }
    }

    private void checkTreeSetCache() {
        List<Task> allTasksWithStartTime = getAllTasks().stream().filter(t -> t.getStartTime() != null).toList();
        Integer treeHash = tasksOrderByStartTime
                .stream()
                .map(Task::hashCode)
                .reduce(Integer::sum)
                .orElse(Integer.MIN_VALUE);

        if (!treeHash.equals(cashHash)) {
            invalidateTreeSetCache(allTasksWithStartTime);
        } else {
            Integer allTaskHash = allTasksWithStartTime
                    .stream()
                    .map(Task::hashCode)
                    .reduce(Integer::sum)
                    .orElse(Integer.MIN_VALUE);

            if (!treeHash.equals(allTaskHash)) {
                updateTreeSet(allTasksWithStartTime);
            }
        }
    }

    private void updateTreeSet(List<Task> allTasksWithStartTime) {
        System.out.println("Update tree set.");
        allTasksWithStartTime
                .stream()
                .filter(t -> !tasksOrderByStartTime.contains(t))
                .forEach(tasksOrderByStartTime::add);
        updateCache();
    }

    private void invalidateTreeSetCache(List<Task> allTasksWithStartTime) {
        System.out.println("Cache is invalid. Remake.");
        tasksOrderByStartTime.clear();
        tasksOrderByStartTime.addAll(allTasksWithStartTime);
        updateCache();
    }

    private void updateCache() {
        cashHash = tasksOrderByStartTime
                .stream()
                .map(Task::hashCode)
                .reduce(Integer::sum)
                .orElse(Integer.MIN_VALUE);
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

        simpleTasks.values().forEach(task -> {
            sb.append(task.toString());
            sb.append('\n');
        });

        epicTasks.values().forEach(task -> {
            sb.append(task.toString());
            sb.append('\n');
        });

        subTasks.values().forEach(task -> {
            sb.append(task.toString());
            sb.append('\n');
        });

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
        if (!isValid(task)) {
            throw new IllegalArgumentException("Tasks intersection.");
        }
        switch (task.getType()) {
            case SUB -> {
                result = subTasks.replace(task.getId(), (SubTask) task);
                checkEpicStatus(((SubTask) task).getParent());
                checkDuration(((SubTask) task).getParent());
            }
            case TASK -> result = simpleTasks.replace(task.getId(), (SimpleTask) task);
            case EPIC -> result = epicTasks.replace(task.getId(), (EpicTask) task);
        }
        if (result == null) {
            throw new IllegalArgumentException("Task with id: [" + task.getId() + "] does not exist.");
        } else {
            tasksOrderByStartTime.removeIf(t -> Objects.equals(t.getId(), task.getId()));
            tasksOrderByStartTime.add(task);
            updateCache();
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

                            subs
                                    .stream()
                                    .filter(subTask -> subTask.getStartTime() != null)
                                    .min(Comparator.comparing(Task::getStartTime))
                                    .ifPresent(s -> epic.setStartTime(s.getStartTime()));
                            subs
                                    .stream()
                                    .map(Task::getDuration)
                                    .filter(Objects::nonNull)
                                    .reduce(Duration::plus)
                                    .ifPresent(epic::setDuration);
                            subs
                                    .stream()
                                    .filter(subTask -> subTask.getEndTime() != null)
                                    .max(Comparator.comparing(Task::getEndTime))
                                    .ifPresent(s -> epic.setEndTime(s.getEndTime()));

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

        tasksOrderByStartTime.removeIf(t -> Objects.equals(t.getId(), id));
        updateCache();
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
        tasksOrderByStartTime.removeIf(t -> Objects.equals(t.getId(), id));
        updateCache();
    }

    @Override
    public void removeAll() {
        simpleTasks.clear();
        epicTasks.clear();
        subTasks.clear();
        historyManager.clear();
        tasksOrderByStartTime.clear();
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
        checkTreeSetCache();
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
