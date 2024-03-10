package task;

import java.util.Objects;

public class Task {
    private static Long idGeneratorCount = 0L;
    protected final Long id;
    protected String name;
    protected String description;
    protected Status status;

    public Task() {
        idGeneratorCount++;
        this.id = idGeneratorCount;
        this.status = Status.NEW;
    }

    public Task(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public Task(Task clone) {
        id = clone.id;
        name = clone.name;
        description = clone.description;
        status = clone.status;
    }

    public Long getId() {
        return id;
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

    public Status getStatus() {
        return status;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || ((obj != null && this.getClass().equals(obj.getClass()))
                && Objects.equals(id, ((Task) obj).id)
                && Objects.equals(name, ((Task) obj).name)
                && Objects.equals(description, ((Task) obj).description)
                && status == ((Task) obj).status);
    }

    public Long[] clean() {
        return new Long[0];
    }
}
