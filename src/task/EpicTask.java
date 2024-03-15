package task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class EpicTask extends Task {
    private final HashSet<Long> subTasksIds = new HashSet<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public List<Long> getSubTasksIds() {
        return subTasksIds.stream().toList();
    }

    public void addSubTask(Long id) {
        subTasksIds.add(id);
    }

    public void removeSubTask(Long id) {
        this.subTasksIds.remove(id);
    }

    @Override
    public void updateStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        Long[] subTasksIds = getSubTasksIds().toArray(Long[]::new);
        return "EpicTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subTask ids=" + Arrays.toString(subTasksIds) +
                "}";
    }

    public void removeSubTasks() {
        subTasksIds.clear();
    }
}
