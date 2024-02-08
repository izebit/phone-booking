package ru.izebit.mobile.booking.dto;


import lombok.Builder;

import java.util.List;

@Builder
public record Phone(long id,
                    String title,
                    Reservation reservation,
                    List<Detail> details
) {
}
