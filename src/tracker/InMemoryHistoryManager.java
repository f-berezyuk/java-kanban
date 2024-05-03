package tracker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import task.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Long, Task> history;
    private final MySimpleLinkedHashMap simpleHistory;

    public InMemoryHistoryManager() {
        history = new LinkedHashMap<>();
        simpleHistory = new MySimpleLinkedHashMap();
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasks1 = history.values().stream().toList();
        List<Task> tasks2 = simpleHistory.getValues();
        if (tasks2.size() != tasks1.size()) {
            System.out.println("mismatch between two task collections.");
        }
        return tasks2;
    }

    @Override
    public void add(Task task) {
        addLast(task);
    }

    private void addLast(Task task) {
        // Ensure that object will be a last item.
        history.remove(task.getId());
        history.put(task.getId(), task);

        simpleHistory.addLast(task);
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
        history.remove(id);
        simpleHistory.removeById(id);
    }
}

