package com.agent.permission;

public sealed interface PermissionResult permits PermissionResult.Allowed,
        PermissionResult.Denied,
        PermissionResult.Ask,
        PermissionResult.Passthrough {

    record Allowed() implements PermissionResult{

    }

    record Denied(String reason) implements PermissionResult{

    }

    record Ask(String reason) implements PermissionResult{

    }

    record Passthrough() implements PermissionResult{

    }

    default boolean isAllowed(){
        return this instanceof Allowed;
    }

    default boolean isDenied() {
        return this instanceof Denied;
    }

}
