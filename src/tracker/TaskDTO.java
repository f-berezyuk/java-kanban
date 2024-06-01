package tracker;

import java.time.Duration;
import java.time.LocalDateTime;

import task.EStatus;
import task.TaskType;

public class TaskDTO {
    public Long id;
    public String name;
    public String description;
    public EStatus status;
    public TaskType type;
    public Duration duration;
    public LocalDateTime startTime;

    public LocalDateTime endTime;
    public Long parent;

}
