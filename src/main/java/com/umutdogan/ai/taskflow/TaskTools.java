package com.umutdogan.ai.taskflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskTools {

    private final TaskService taskService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskTools(TaskService taskService) {
        this.taskService = taskService;
    }

    @McpTool(name = "add_task", description = "Add a new task with a title and an optional priority (LOW, MEDIUM, HIGH; default MEDIUM)")
    public String addTask(
            @McpToolParam(description = "The task title", required = true) String title,
            @McpToolParam(description = "Priority: LOW, MEDIUM, or HIGH", required = false) String priority) {
        String effectivePriority = (priority == null || priority.isBlank()) ? "MEDIUM" : priority.toUpperCase();
        Task task = taskService.addTask(title, effectivePriority);
        return "Created task #%d: \"%s\" (priority: %s)".formatted(task.id(), task.title(), task.priority());
    }

    @McpTool(name = "list_tasks", description = "List tasks, optionally filtered by status: open, done, or all (default: all)")
    public String listTasks(
            @McpToolParam(description = "Filter: open, done, or all", required = false) String status) {
        List<Task> tasks = taskService.listTasks(status);
        if (tasks.isEmpty()) {
            return "No tasks found.";
        }
        return tasks.stream()
                .map(t -> "#%d [%s] %s (%s)".formatted(t.id(), t.done() ? "x" : " ", t.title(), t.priority()))
                .collect(Collectors.joining("\n"));
    }

    @McpTool(name = "complete_task", description = "Mark a task as complete by its id")
    public String completeTask(
            @McpToolParam(description = "The task id to complete", required = true) int id) {
        return taskService.completeTask(id)
                .map(t -> "Task #%d marked as complete.".formatted(t.id()))
                .orElse("No task found with id %d.".formatted(id));
    }

    @McpResource(uri = "tasks://all", name = "All Tasks",
            description = "A JSON snapshot of every task, open and completed",
            mimeType = "application/json")
    public String allTasksResource() throws Exception {
        return objectMapper.writeValueAsString(taskService.allTasks());
    }
}
