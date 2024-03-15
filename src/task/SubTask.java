package task;

public class SubTask extends Task {
    private Long parent;

    public SubTask(String name, String description) {
        super(name, description);
    }

    @Override
    public void updateStatus(Status status) {
        super.updateStatus(status);
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public void removeParent() {
        this.parent = null;
    }

    @Override
    public String toString() {
        String parentId = parent != null ? String.valueOf(parent) : "null";
        return "SubTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", parent id=" + parentId +
                '}';
    }
}
