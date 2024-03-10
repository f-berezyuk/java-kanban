package task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class EpicTask extends Task {
    private final HashSet<SubTask> subTasks = new HashSet<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public void add(SubTask task) {
        if (!Objects.equals(task.getParent(), this)) {
            task.setParent(this);
        }
        this.subTasks.add(task);
    }

    public void addAll(SubTask... tasks) {
        Arrays.stream(tasks).forEach(task -> task.setParent(this));
        this.subTasks.addAll(List.of(tasks));
    }

    public List<SubTask> getSubTasks() {
        return subTasks.stream().toList();
    }


    public void removeSubTask(SubTask task) {
        // TODO:
        //  Тут очень странная проблема. Почему-то не срабатывает удаление функции.
        //  Я проверял, что методы equals() и hashCode() возвращают true.
        //  Проверял со значениями таски из сета и таски из параметров.
        //  Я не понимать, что сделано не так, буду рад помощи.
        this.subTasks.remove(task);
        this.updateStatus();

        task.removeParent();
    }

    public void updateStatus() {
        if (this.subTasks.isEmpty() || this.subTasks.stream().allMatch(subTask -> subTask.getStatus() == Status.NEW)) {
            this.status = Status.NEW;
        } else if (this.subTasks.stream().allMatch(subTask -> subTask.getStatus() == Status.DONE)) {
            this.status = Status.DONE;
        } else {
            this.status = Status.IN_PROGRESS;
        }
    }

    @Override
    public String toString() {
        long[] subTasksIds = subTasks.stream().map(subTask -> subTask.id).mapToLong(Long::longValue).toArray();
        return "EpicTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subTask ids=" + Arrays.toString(subTasksIds) +
                "}";
    }

    @Override
    public void updateStatus(Status status) {
        this.updateStatus();
    }

    @Override
    public Long[] clean() {
        Long[] linkedTasksIds = this.subTasks.stream().map(Task::getId).toArray(Long[]::new);
        this.subTasks.forEach(SubTask::removeParent);
        this.subTasks.clear();
        return linkedTasksIds;
    }
}
