package com.umutdogan.taskflow;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskService {

    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public Task addTask(String title, String priority) {
        int id = nextId.getAndIncrement();
        Task task = new Task(id, title, priority, false);
        tasks.put(id, task);
        return task;
    }

    public List<Task> listTasks(String status) {
        return tasks.values().stream()
                .filter(task -> matchesStatus(task, status))
                .sorted((a, b) -> Integer.compare(a.id(), b.id()))
                .toList();
    }

    public Optional<Task> completeTask(int id) {
        return Optional.ofNullable(tasks.computeIfPresent(id,
                (key, task) -> new Task(task.id(), task.title(), task.priority(), true)));
    }

    public List<Task> allTasks() {
        return listTasks(null);
    }

    private boolean matchesStatus(Task task, String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("all")) {
            return true;
        }
        boolean wantsDone = status.equalsIgnoreCase("done") || status.equalsIgnoreCase("complete");
        boolean wantsOpen = status.equalsIgnoreCase("open") || status.equalsIgnoreCase("pending");
        if (wantsDone) {
            return task.done();
        }
        if (wantsOpen) {
            return !task.done();
        }
        return true;
    }
}
