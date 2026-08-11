package com.agent.planning;

import com.agent.core.enums.ValueNameEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum TodoStatus implements ValueNameEnum<String, String> {
    PENDING("pending", " "),
    IN_PROGRESS("in_progress", "▸"),
    COMPLETED("completed", "✓");
    ;
    private final String name;
    private final String value;

    public static TodoStatus fromName(String name){
      for (TodoStatus status : TodoStatus.values()){
          if (status.getName().equals(name)){
              return status;
          }
      }
      throw new IllegalArgumentException("Invalid status: " + name);
    };
}
