package ru.izebit.mobile.booking.dto;

public record Message(
        Type type,
        String text
) {
    public enum Type {
        ERROR,
        WARNING,
        INFO,
    }
}

