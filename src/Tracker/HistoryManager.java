package Tracker;

import java.util.List;

public interface HistoryManager<T> {
    List<T> getHistoryObjects();

    void addObjectToHistory(T object);

    String getHistory();
}
