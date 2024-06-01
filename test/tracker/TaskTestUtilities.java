package tracker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import task.EpicTask;
import task.SimpleTask;
import task.SubTask;
import task.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TaskTestUtilities {
    public static final Random random = new Random();

    public static EpicTask createRandomEpicTask() {
        return new EpicTask("Name " + random.nextInt(), "Description " + random.nextDouble());
    }

    public static Task addTime(Task task) {
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(random.nextInt()));
        return task;
    }

    public static SubTask createRandomSubTask() {
        return new SubTask("Name " + random.nextInt(), "Description " + random.nextDouble());
    }

    public static SimpleTask createRandomSimpleTask() {
        return new SimpleTask("Name " + random.nextInt(), "Description " + random.nextDouble());
    }

    public static Task withId(Task task, long id) {
        task.setId(id);
        return task;
    }

    public static <T> void assertListEqualsNoOrder(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size());

        HashSet<T> hashSet = new HashSet<>(expected);
        actual.forEach(hashSet::remove);
        if (!hashSet.isEmpty()) {
            for (T t : hashSet) {
                System.out.println("t = " + t);
            }
            System.out.println("Expected contains " + hashSet.size() + " values that not matched.");
            for (T t : hashSet) {
                System.out.println("Value = " + t);
            }
            fail();
        }
    }
}
