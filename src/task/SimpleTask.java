package task;

public class SimpleTask extends Task {
    public SimpleTask(String name, String description) {
        super(name, description);
    }

    public SimpleTask(Task clone) {
        super(clone);
    }

    @Override
    protected void setType() {
        this.type = TaskType.TASK;
    }
}
