import java.util.Arrays;
import java.util.List;

import Tracker.Managers;
import Tracker.TaskManager;
import task.EStatus;
import task.EpicTask;
import task.SimpleTask;
import task.SubTask;
import task.Task;
import task.TaskType;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        Task task1 = new SimpleTask("Task 1", "Task for tests");
        Task task2 = new SimpleTask("Task 2", "Task for tests");
        EpicTask epicTask1 = new EpicTask("EpicTask 1", "EpicTask for tests");
        SubTask subTask1 = new SubTask("SubTask 1 for EpicTask 1", "SubTask for tests");
        SubTask subTask2 = new SubTask("SubTask 2 for EpicTask 1", "SubTask for tests");
        EpicTask epicTask2 = new EpicTask("EpicTask 2", "EpicTask for tests");
        SubTask subTask3 = new SubTask("SubTask 3 for EpicTask 2", "SubTask for tests");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(epicTask1);
        manager.addTask(subTask1);
        manager.addTask(subTask2);
        manager.addSubTasksToEpic(epicTask1.getId(), subTask1.getId(), subTask2.getId());
        manager.addTask(epicTask2);
        manager.addTask(subTask3);
        manager.addSubTasksToEpic(epicTask2.getId(), subTask3.getId());

        System.out.println("Initial state.");
        System.out.println(manager.printAllTasks());

        System.out.println("All Task.class tasks:");
        System.out.println(Arrays.toString(new List[]{manager.getAllTasks()}));
        System.out.println();

        System.out.println("All SubTask.class tasks:");
        System.out.println(Arrays.toString(new List[]{manager.getAllTasksByType(TaskType.SUB)}));
        System.out.println();

        System.out.println("All EpicTask.class tasks:");
        System.out.println(Arrays.toString(new List[]{manager.getAllTasksByType(TaskType.EPIC)}));
        System.out.println();

        System.out.println("Find Simple task task by id:");
        System.out.println(manager.findSimpleTask(task1.getId()));
        System.out.println();

        System.out.println("Find Any.class task by id:");
        System.out.println(manager.findTaskById(task1.getId()));
        System.out.println();

        System.out.println("Find Any.class task by non-existed id:");
        System.out.println(manager.findTaskById(-1L));
        System.out.println();

        System.out.println("Update task.");
        System.out.println("Before:");
        System.out.println(manager.findTaskById(task2.getId()));
        Task updatedTask = new SimpleTask(task2);
        updatedTask.setName("Updated task.");
        updatedTask.setDescription("Updated description.");
        manager.updateTask(updatedTask);
        System.out.println("After:");
        System.out.println(manager.findTaskById(task2.getId()));
        System.out.println(manager.findTaskById(task2.getId()).getName());
        System.out.println(manager.findTaskById(task2.getId()).getDescription());
        System.out.println();

        System.out.println("Get all SubTasks for EpicTask1");
        System.out.println("by object itself:");
        System.out.println(Arrays.toString(new List[]{manager.findTasksByParentId(epicTask1)}));
        System.out.println("by id:");
        System.out.println(Arrays.toString(new List[]{manager.findTasksByParentId(epicTask1.getId())}));
        System.out.println();

        System.out.println("Update SubTask 1 status. EpicTask 1 should update status too.");
        manager.updateStatus(subTask1.getId(), EStatus.IN_PROGRESS);
        System.out.println("subTask1 status = " + subTask1.getStatus());
        System.out.println("epicTask1 status = " + epicTask1.getStatus());
        System.out.println();

        System.out.println("Update SubTask 1 and 2 status to DONE. EpicTask 1 should update status to DONE.");
        manager.updateStatus(subTask1.getId(), EStatus.DONE);
        manager.updateStatus(subTask2.getId(), EStatus.DONE);
        System.out.println("epicTask1 = " + manager.findTaskById(epicTask1.getId()));
        System.out.println();

        System.out.println("""
                Remove only EpicTask 2 (not recursive).
                SubTasks should be in TaskManager.
                SubTask list should be empty in EpicTask 2.
                Parent should be null in SubTask 3.""");
        System.out.println("Before:\n" + manager);
        manager.removeTask(epicTask2.getId());
        System.out.println("After:");
        System.out.println("epicTask2 = " + epicTask2);
        System.out.println("subTask3 = " + subTask3);
        System.out.println();
        System.out.println(manager.printAllTasks());

        System.out.println("Remove SubTask 1. EpicTask 1 have to contain SubTask 2 only.");
        manager.removeTask(subTask1.getId());
        System.out.println(manager.printAllTasks());

        System.out.println("Remove recursively EpicTask 1. SubTask 2 should be removed too.");
        manager.removeRecursiveTask(epicTask1.getId());
        System.out.println(manager.printAllTasks());

        System.out.println("Remove Task 1.");
        manager.removeTask(task1.getId());
        System.out.println(manager.printAllTasks());

        System.out.println("Add Task 1 again. Add EpicTask 2. Add SubTask 1 with parent EpicTask 2.");
        manager.addTask(task1);
        manager.addTask(epicTask2);
        manager.addTask(subTask1);
        manager.findEpicTask(epicTask2.getId()).addSubTask(subTask1.getId());
        System.out.println(manager.printAllTasks());

        System.out.println("Remove all tasks (type Task).");
        manager.removeAllByType(TaskType.TASK);
        System.out.println(manager.printAllTasks());

        System.out.println("Remove all sub tasks.");
        manager.removeAllByType(TaskType.SUB);
        System.out.println(manager.printAllTasks());

        System.out.println("Remove all epic tasks.");
        manager.removeAllByType(TaskType.EPIC);
        System.out.println(manager.printAllTasks());

        System.out.println("Remove all tasks.");
        manager.removeAll();
        System.out.println(manager.printAllTasks());

        System.out.println("History = " + manager.getHistory());
    }

}
