package com.agent.planning.tool;

import com.agent.planning.*;
import com.agent.core.tool.Tool;
import com.agent.core.tool.ToolComponent;
import com.agent.core.tool.ToolDescriptor;

import java.util.List;
import java.util.Map;

@ToolComponent(group = "planning", subagentExcluded = true)
public class TodoWriteTool implements Tool {
    private final TodoWriteValidator validator = new TodoWriteValidator();
    private final TodoList todoList = new TodoList(new TodoRenderer());


    @Override
    public String execute(Map<String, Object> params) {
        if (params == null || params.get("todos") == null){
            return "Error: 缺少 'todos' 参数";
        }
        ValidationResult<List<TodoItem>> result = validator.validate(params.get("todos"));
        if (!result.isValid()){
            return ((ValidationResult.Failure<?>) result).error();
        }
        List<TodoItem> items = ((ValidationResult.Success<List<TodoItem>>) result).value();
        todoList.replaceAll(items);
        return "Updated " + items.size() + " tasks";
    }

    @Override
    public ToolDescriptor getDescriptor(){
        Map<String, Object> todoItemProperties  = Map.of(
                "content", Map.of("type", "string"),
                "status", Map.of(
                        "type", "string",
                        "enum", List.of("pending", "in_progress", "completed")
                )
        );
        Map<String, Object> itemSchema = Map.of(
                "type", "object",
                "properties", todoItemProperties,
                "required", List.of("content", "status")
        );

        Map<String, Object> todoArray = Map.of(
                "type", "array",
                "items", itemSchema
        );
        Map<String, Object> properties = Map.of(
                "todos", todoArray
        );
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("todos")
        );
        return new ToolDescriptor(
                getName(),
                "Create and manage a task list for your current coding session.",
                schema
        );
    }

    @Override
    public String getName() {
        return "todo_write";
    }
}
