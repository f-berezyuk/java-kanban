package task;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class EpicTask extends Task {
    private HashSet<Long> subTasksIds = new HashSet<>();

    private LocalDateTime endTime;

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public EpicTask(Task clone) {
        super(clone);
        if (clone instanceof EpicTask) {
            this.subTasksIds = ((EpicTask) clone).subTasksIds;
        }
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    protected void setType() {
        this.type = TaskType.EPIC;
    }

    public void addSubTask(Long id) {
        subTasksIds.add(id);
    }

    public void addSubTasks(Long... ids) {
        subTasksIds.addAll(Arrays.stream(ids).toList());
    }

    public List<Long> getSubTasksIds() {
        return subTasksIds.stream().toList();
    }

    @Override
    public void setStatus(EStatus status) {
        this.status = status;
    }

    public void removeSubTask(Long id) {
        this.subTasksIds.remove(id);
    }

    @Override
    public String toString() {
        Long[] subTasksIds = getSubTasksIds().toArray(Long[]::new);
        return super.toString() + " SubTasks: " + Arrays.toString(subTasksIds);
    }
}
