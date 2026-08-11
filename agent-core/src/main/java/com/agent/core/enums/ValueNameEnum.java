package com.agent.core.enums;

import lombok.Getter;

public interface ValueNameEnum<V, N> {
    V getValue();
    N getName();
}
