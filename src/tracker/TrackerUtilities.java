package tracker;

import task.SubTask;
import task.Task;
import task.TaskType;

public class TrackerUtilities {
    public static String csvDelimiter = ",";

    public static String[] fromCsvToStringArrayFormat(String value) throws NumberFormatException {
        return value.split(csvDelimiter);
    }

    public static String fromTaskToCsv(Task task) {
        return task.getType() == TaskType.SUB ? fromSubTaskToCsv((SubTask) task) : getBaseCsvLine(task);
    }

    public static String fromSubTaskToCsv(SubTask task) {
        return getBaseCsvLine(task) + csvDelimiter + task.getParent().toString();
    }

    private static String getBaseCsvLine(Task task) {
        return String.join(csvDelimiter,
                task.getId().toString(),
                task.getType().toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription());
    }
}

