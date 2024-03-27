package task;

import java.util.Objects;

public abstract class Task {
    protected Long id;
    protected String name;
    protected String description;
    protected EStatus status;
    protected TaskType type;

    public Task() {
        this.status = EStatus.NEW;
        setType();
    }

    public Task(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public Task(Task clone) {
        this();
        id = clone.id;
        name = clone.name;
        description = clone.description;
        status = clone.status;
        type = clone.type;
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
        // [TYPE-123/NEW] Name: Description.
        return "[" + type + "-" + id + "/" + status + "] " + name + ": " + description;
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
}
