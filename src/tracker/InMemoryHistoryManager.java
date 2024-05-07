package tracker;

import java.util.List;

import task.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private final MySimpleLinkedHashMap simpleLinkedHashMap;

    public InMemoryHistoryManager() {
        simpleLinkedHashMap = new MySimpleLinkedHashMap();
    }

    @Override
    public List<Task> getHistory() {
        return simpleLinkedHashMap.getValues();
    }

    @Override
    public void add(Task task) {
        addLast(task);
    }

    private void addLast(Task task) {
        simpleLinkedHashMap.addLast(task);
    }

    @Override
    public String getHistoryAsString() {
        StringBuilder sb = new StringBuilder();
        List<Task> history = getHistory();
        for (int i = 0; i < history.size(); i++) {
            sb.append(i).append(") ").append(history.get(i)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void remove(Long id) {
        simpleLinkedHashMap.removeById(id);
    }
}

