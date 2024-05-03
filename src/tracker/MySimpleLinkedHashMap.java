package tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import task.Task;

class Node {
    public Task value;
    public Node next;
    public Node prev;

    public Node(Task task) {
        value = task;
    }
}

public class MySimpleLinkedHashMap {
    Node head;
    Node tail;
    private int size = 0;
    private HashMap<Integer, Node> indexToNode;
    private HashMap<Long, Integer> taskIdToIndex;

    MySimpleLinkedHashMap() {
        indexToNode = new HashMap<>();
        taskIdToIndex = new HashMap<>();
        head = null;
        tail = null;
    }

    void addLast(Task task) {
        Node node = new Node(task);

        if (taskIdToIndex.containsKey(task.getId())) {
            remove(taskIdToIndex.get(task.getId()));
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

        indexToNode.put(size, node);
        taskIdToIndex.put(task.getId(), size);
        size++;
    }

    Task get(int index) {
        return indexToNode.get(index).value;
    }

    Task removeById(Long taskId) {
        return remove(taskIdToIndex.get(taskId));
    }

    Task remove(int index) {
        Node node = indexToNode.get(index);
        remove(node);
        taskIdToIndex.remove(node.value.getId());
        for (int i = index; i < size - 1; i++) {
            Node tmp = indexToNode.get(i + 1);
            indexToNode.put(i, tmp);
            taskIdToIndex.put(tmp.value.getId(), i);
        }
        size--;
        indexToNode.remove(size);
        return node.value;
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
            Node prev = node.prev; // A
            Node next = node.next; // C
            prev.next = next;
            next.prev = prev;
        }
    }

    private void link(Node left, Node right) {
        left.next = right;
        right.prev = left;
    }

    public List<Task> getValues() {
        List<Task> tasks = new ArrayList<>(size);
        Node next = head;
        while (next != null) {
            tasks.add(next.value);
            next = next.next;
        }

        return tasks;
    }
}

