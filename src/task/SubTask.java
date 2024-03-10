package task;

public class SubTask extends Task {
    private EpicTask parent;

    public SubTask(EpicTask parent, String name, String description) {
        super(name, description);
        setParent(parent);
    }

    public SubTask(String name, String description) {
        super(name, description);
    }

    @Override
    public void updateStatus(Status status) {
        super.updateStatus(status);
        parent.updateStatus();
    }

    public EpicTask getParent() {
        return parent;
    }

    public void setParent(EpicTask parent) {
        if (parent != null) {
            this.parent = parent;
            parent.add(this);
        } else {
            throw new IllegalArgumentException("Attempt to set null parent.");
        }
    }

    public void removeParent() {
        this.parent = null;
    }

    @Override
    public String toString() {
        String parentId = parent != null ? String.valueOf(parent.id) : "null";
        return "SubTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", parent id=" + parentId +
                '}';
    }

    @Override
    public Long[] clean() {
        if (parent != null) {
            this.parent.removeSubTask(this);
        }
        return super.clean();
    }
}
