package tracker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import task.EStatus;
import task.SubTask;
import task.Task;
import task.TaskType;

public class TrackerUtilities {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static String csvDelimiter = ",";

    public static TaskDTO fromCsvToTaskDTO(String value) throws NumberFormatException {
        String[] values = mySplit(value, csvDelimiter);
        if (values.length != 9) {
            throw new IllegalArgumentException("Unexpected value to parse. Value: " +
                    "[" + String.join(", ", values) + "].");
        }
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.id = Long.valueOf(values[0]);
        taskDTO.type = TaskType.valueOf(values[1]);
        taskDTO.name = values[2];
        taskDTO.status = EStatus.valueOf(values[3]);
        taskDTO.description = values[4];
        taskDTO.startTime = values[5].isBlank() ? null : LocalDateTime.parse(values[5], DATE_TIME_FORMAT);
        taskDTO.endTime = values[6].isBlank() ? null : LocalDateTime.parse(values[6], DATE_TIME_FORMAT);
        taskDTO.duration = values[7].isBlank() ? null : Duration.ofMinutes(Long.parseLong(values[7]));
        taskDTO.parent = values[8].isBlank() ? null : Long.valueOf(values[8]);

        return taskDTO;
    }

    public static String[] mySplit(String value, String csvDelimiter) {
        int from = 0;
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            int split = value.indexOf(csvDelimiter, from);
            if (split < 0) {
                values.add(value.substring(from));
                break;
            }
            String substring = value.substring(from, split);
            values.add(substring);
            from = split+1;
        }
        return values.toArray(new String[0]);
    }

    public static String fromTaskDtoToCsv(Task task) {
        TaskDTO dto = toDto(task);
        String id = dto.id.toString();
        String type = dto.type.toString();
        String status = dto.status.toString();
        String startTimeString = dto.startTime != null ? dto.startTime.format(DATE_TIME_FORMAT) : "";
        String endTimeString = dto.endTime != null ? dto.endTime.format(DATE_TIME_FORMAT) : "";
        String durationInMinutes = dto.duration != null ? Long.toString(dto.duration.toMinutes()) : "";
        String parentId = dto.parent != null ? dto.parent.toString() : "";
        return String.join(csvDelimiter,
                id,
                type,
                dto.name,
                status,
                dto.description,
                startTimeString,
                endTimeString,
                durationInMinutes,
                parentId);
    }

    public static TaskDTO toDto(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.id = task.getId();
        dto.type = task.getType();
        dto.name = task.getName();
        dto.status = task.getStatus();
        dto.description = task.getDescription();
        dto.startTime = task.getStartTime();
        dto.endTime = task.getEndTime();
        dto.duration = task.getDuration();
        dto.parent = task.getType() == TaskType.SUB ? ((SubTask) task).getParent() : null;
        return dto;
    }
}

