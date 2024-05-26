package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public abstract class Task {
    protected Long id;
    protected String name;
    protected String description;
    protected EStatus status;
    protected TaskType type;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task() {
        this.status = EStatus.NEW;
        setType();
    }

    public Task(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * @noinspection CopyConstructorMissesField
     */
    public Task(Task clone) {
        this();
        id = clone.id;
        name = clone.name;
        description = clone.description;
        status = clone.status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime time) {
        this.startTime = time;
    }

    protected abstract void setType();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EStatus getStatus() {
        return status;
    }

    public void setStatus(EStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 37 * result + name.hashCode();
        result = 37 * result + description.hashCode();
        result = 37 * result + status.hashCode();
        result = 37 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // 1,TASK,Task1,NEW,Description task1,
        // return String.join(delimiter, id.toString(), type.toString(), name, status.toString(), description);
        String s = "[" + type + "-" + id + "/" + status + "] " + name + ": " + description;
        if(startTime != null && duration != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
            s = s + String.format(". Start: [%s]. Duration: [%s]. End: [%s].",
                            startTime.format(formatter),
                            duration,
                            getEndTime().format(formatter));
        }
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || ((obj != null && this.getClass().equals(obj.getClass()))
                && Objects.equals(id, ((Task) obj).id)
                && Objects.equals(name, ((Task) obj).name)
                && Objects.equals(description, ((Task) obj).description)
                && status == ((Task) obj).status);
    }

    public TaskType getType() {
        return this.type;
    }
    
    public LocalDateTime getEndTime() {
        if (startTime != null) {
            return startTime.plus(Optional.ofNullable(duration).orElse(Duration.ZERO));
        }
        return null;
    }

    public Duration getDuration() {
        return this.duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
