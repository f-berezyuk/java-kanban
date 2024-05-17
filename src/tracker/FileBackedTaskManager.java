package tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import task.Task;

public class FileBackedTaskManager extends InMemoryTaskManager implements AutoCloseable {
    private final File file;

    private FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        System.out.println("Start load from file: " + file.getPath());
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(fr)) {
            String line = br.readLine();
            while (line != null) {
                Task task = manager.fromString(line);
                manager.addTask(task);
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new ManagerLoadException(e);
        }

        return manager;
    }

    public void save() {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, false)) {
            for (Task task : getAllTasks()) {
                fw.write(task.toString() + '\n');
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }
    }

    @Override
    public void close() {
        System.out.println("Save changes.");
        save();
    }
}

