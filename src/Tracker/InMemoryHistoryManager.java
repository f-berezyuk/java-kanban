package Tracker;

import java.util.ArrayList;
import java.util.List;

import task.Task;

public class InMemoryHistoryManager<T extends Task> implements HistoryManager<T> {

    private final int memory = 10;
    private List<T> collection;

    public InMemoryHistoryManager() {
        collection = new ArrayList<>();
    }

    @Override
    public List<T> getHistoryObjects() {
        int size = collection.size();
        if (size > 0) {
            int fromIndex = Math.max(size - memory, 0);
            return collection.subList(fromIndex, size);
        }
        return new ArrayList<>();
    }

    @Override
    public void addObjectToHistory(T object) {
        collection.add(object);
        checkMemory();
    }

    @Override
    public String getHistory() {
        StringBuilder sb = new StringBuilder();
        List<T> history = getHistoryObjects();
        for (int i = 0; i < history.size(); i++) {
            sb.append(i).append(") ").append(history.get(i)).append("\n");
        }
        return sb.toString();
    }

    private void checkMemory() {
        if (collection.size() > memory * 2) {
            collection = getHistoryObjects();
        }
    }
}
