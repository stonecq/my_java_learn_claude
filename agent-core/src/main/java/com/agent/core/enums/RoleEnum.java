package com.agent.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum implements ValueNameEnum<String, String> {
   USER( "user", "用户"),
   ASSISTANT("assistant", "智能体");

   private final String value;

   private final String name;
}
