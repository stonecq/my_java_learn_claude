package com.agent.core.tool;

import com.agent.core.model.content_block.ToolResultBlock;
import com.agent.core.model.content_block.ToolUseBlock;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class ToolRegistry {
    private final Map<String, Tool> tools = new HashMap<>();

    private final Map<String, ToolDescriptor> toolDescriptors = new HashMap<>();

    /**
     * 自动扫描包下的工具类；可传多个包以跨模块发现 @ToolComponent 工具
     * @param basePackages
     */
    public void autoRegisterByAnnotation(String... basePackages){
        if (basePackages == null || basePackages.length == 0){
            throw new IllegalArgumentException("basePackages 不能为空");
        }
        for (String basePackage : basePackages) {
            Reflections reflections = new Reflections(basePackage);

            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(ToolComponent.class);

            for (Class<?> clazz : annotatedClasses){
                if (!Tool.class.isAssignableFrom(clazz)){
                    System.err.println("警告: " + clazz.getName() + " 标注了 @ToolComponent 但未实现 Tool 接口，已跳过");
                    continue;
                }
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())){
                    continue;
                }
                ToolComponent toolComponent = clazz.getAnnotation(ToolComponent.class);
                if (!toolComponent.enabled()){
                    continue;
                }
                try {
                    Tool tool = (Tool) clazz.getDeclaredConstructor().newInstance();
                    this.register(tool);
                }catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                        NoSuchMethodException e) {
                    System.err.println("实例化工具失败: " + clazz.getName() + " - " + e.getMessage());
                }
            }
        }
    };

    public void register(Tool tool){
        if (tool == null){
            throw new IllegalArgumentException("tool 不能为null");
        }
        if (tool.getName() == null || tool.getName().isBlank()){
            throw new IllegalArgumentException("tool 的 name 不能为空");
        }
        tools.put(tool.getName(), tool);
        toolDescriptors.put(tool.getName(), tool.getDescriptor());
    }

    public ToolResultBlock dispatch(ToolUseBlock block){
        String toolName = block.name();
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return new ToolResultBlock(block.id(), "Error: 未注册的工具: " + toolName, true);
        }
        String result = tool.execute(block.input());
        return new ToolResultBlock(block.id(), result, null);
    }

    public List<ToolDescriptor> getAllDescriptors() {
        return new ArrayList<>(toolDescriptors.values());
    }

    public ToolDescriptor getDescriptor(String name){
        return toolDescriptors.get(name);
    }
}
