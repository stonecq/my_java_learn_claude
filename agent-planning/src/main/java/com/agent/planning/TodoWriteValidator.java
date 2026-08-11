package com.agent.planning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//输入校验器
public class TodoWriteValidator {
    private static final ObjectMapper mapper = new ObjectMapper();

    public ValidationResult<List<TodoItem>> validate(Object todosInput) {
        List<Map<String, Object>> rawList = parseToList(todosInput);
        if (rawList == null) {
            return ValidationResult.failure("todos must be a list");
        }
        List<TodoItem> items = new ArrayList<>();
        for (int i = 0; i < rawList.size(); i++) {
            Map<String, Object> map = rawList.get(i);
            try {
                items.add(TodoItem.fromMap(map));
            } catch (IllegalArgumentException e) {
                return ValidationResult.failure("todos[" + i + "] " + e.getMessage());
            }
        }
        return ValidationResult.success(items);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseToList(Object input) {
        if (input instanceof List) return (List<Map<String, Object>>) input;
        if (input instanceof String s) {
            try {
                return mapper.readValue(s, List.class);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        return null;
    }


}
