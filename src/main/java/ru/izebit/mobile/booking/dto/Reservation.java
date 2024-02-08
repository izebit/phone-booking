package ru.izebit.mobile.booking.dto;

import lombok.Builder;

@Builder
public record Reservation(
        String username,
        String date,
        boolean doesUserOwn
) {
}
