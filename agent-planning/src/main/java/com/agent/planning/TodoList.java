package com.agent.planning;

import java.util.ArrayList;
import java.util.List;

public class TodoList {
    private final List<TodoItem> items = new ArrayList<>();
    private final TodoRenderer renderer;

    public TodoList(TodoRenderer renderer){
        this.renderer = renderer;
    }
    public synchronized String replaceAll(List<TodoItem> todoItems){
        items.clear();
        items.addAll(todoItems);
        return renderer.render(items);
    }

    public synchronized List<TodoItem> snapshot(){
        return List.copyOf(items);
    }

    public synchronized int size(){
        return items.size();
    }

    public synchronized long countByStatus(TodoStatus status){
        return items.stream().filter(t -> t.status().equals(status)).count();
    }
}
