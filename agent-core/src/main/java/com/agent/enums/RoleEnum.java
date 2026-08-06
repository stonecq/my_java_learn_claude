package com.agent.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum implements ValueNameEnum<String, String> {
   USER( "user", "用户"),
   ASSISTANT("assistant", "智能体");

   private final String value;

   private final String name;
}
