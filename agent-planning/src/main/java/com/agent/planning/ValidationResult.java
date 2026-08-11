package com.agent.planning;

public sealed interface ValidationResult<T> {
        record Success<T>(T value) implements ValidationResult<T> {}
        record Failure<T>(String error) implements ValidationResult<T> {}

        static <T> ValidationResult<T> success(T value) { return new Success<>(value); }
        static <T> ValidationResult<T> failure(String error) { return new Failure<>(error); }

        default boolean isValid() { return this instanceof Success<T>; }
    }