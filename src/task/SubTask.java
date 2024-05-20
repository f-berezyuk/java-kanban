package task;

public class SubTask extends Task {
    private Long parent;

    public SubTask(String name, String description) {
        super(name, description);
    }

    public SubTask(Task clone) {
        super(clone);
        if (clone instanceof SubTask) {
            this.parent = ((SubTask) clone).parent;
        }
    }

    @Override
    protected void setType() {
        this.type = TaskType.SUB;
    }

    @Override
    public void setStatus(EStatus status) {
        super.setStatus(status);
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
        //return super.toString() + Task.delimiter + parentId;
        return super.toString() + " Parent: " + parentId;
    }
}
