package com.exercise.phonebooking.controller;

import com.exercise.phonebooking.entity.Booking;
import com.exercise.phonebooking.service.BookablePhone;
import com.exercise.phonebooking.service.PhoneSpecs;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PhoneDetails(
        int id,
        String model,
        String technology,
        String bands2g,
        String bands3g,
        String bands4g,
        boolean isAvailable,
        String lastBookedBy,
        String lastBookedAt,
        List<Booking> bookings
) {

    public static PhoneDetails fromModel(BookablePhone phone, boolean includeBooking) {
        Optional<List<Booking>> maybeBookings = includeBooking ? Optional.of(phone.bookings()) : Optional.empty();
        return new PhoneDetails(
            phone.phoneId(),
            phone.model(),
            phone.phoneSpecs().map(PhoneSpecs::technology).orElse(null),
            phone.phoneSpecs().map(PhoneSpecs::band2G).orElse(null),
            phone.phoneSpecs().flatMap(PhoneSpecs::band3G).orElse(null),
            phone.phoneSpecs().flatMap(PhoneSpecs::band4G).orElse(null),
            phone.isAvailable(),
            phone.lastBookedBy().orElse("Not yet booked"),
            phone.lastBookedAt().map(Instant::toString).orElse("Not yet booked"),
            includeBooking ? phone.bookings() : null
        );
    }

    public static PhoneDetails fromModel(BookablePhone phone) {
        return fromModel(phone, false);
    }

}
