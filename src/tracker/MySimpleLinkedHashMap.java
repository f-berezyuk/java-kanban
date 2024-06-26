package tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import task.Task;

public class MySimpleLinkedHashMap {
    private final HashMap<Long, Node> taskIdToNode;
    private Node head;
    private Node tail;

    MySimpleLinkedHashMap() {
        taskIdToNode = new HashMap<>();
        head = null;
        tail = null;
    }

    void addLast(Task task) {
        Node node = new Node(task);

        if (taskIdToNode.containsKey(task.getId())) {
            removeById(task.getId());
        }

        if (head == null) {
            head = node;
            head.prev = null;
            head.next = null;
        } else if (tail == null) {
            tail = node;
            tail.next = null;
            link(head, tail);
        } else {
            link(tail, node);
            tail = node;
        }

        taskIdToNode.put(task.getId(), node);
    }

    void removeById(Long taskId) {
        Node node = taskIdToNode.get(taskId);
        if (node != null) {
            remove(node);
            taskIdToNode.remove(taskId);
        }
    }

    private void remove(Node node) {
        // A <-> B <-> C
        if (node == head) {
            if (head.next != null) {
                if (head.next == tail) {
                    head = tail;
                    tail = null;
                } else {
                    head = head.next;
                    head.prev = null;
                }
            } else {
                head = null;
            }
        } else if (node == tail) {
            if (tail.prev == head) {
                tail = null;
                head.next = null;
            } else {
                tail = tail.prev;
                tail.next = null;
            }
        } else {
            Node prev = node.prev;
            Node next = node.next;
            prev.next = next;
            next.prev = prev;
        }
    }

    private void link(Node left, Node right) {
        left.next = right;
        right.prev = left;
    }

    public List<Task> getValues() {
        List<Task> tasks = new ArrayList<>(taskIdToNode.size());
        Node next = head;
        while (next != null) {
            tasks.add(next.value);
            next = next.next;
        }

        return tasks;
    }

    public void clear() {
        taskIdToNode.clear();
        head = null;
        tail = null;
    }

    private static class Node {
        public Task value;
        public Node next;
        public Node prev;

        public Node(Task task) {
            value = task;
        }
    }
}

