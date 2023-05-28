package com.exercise.phonebooking.service;

import com.exercise.phonebooking.entity.Booking;
import com.exercise.phonebooking.entity.Phone;
import com.exercise.phonebooking.entity.UserAction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record BookablePhone(
    Integer phoneId,
    String model,
    Optional<PhoneSpecs> phoneSpecs,
    boolean isAvailable,
    Optional<String> lastBookedBy,
    Optional<Instant> lastBookedAt,
    List<Booking> bookings
) {
    public BookablePhone getCopy(Optional<PhoneSpecs> argPhoneSpecs) {
        return new BookablePhone(
            this.phoneId,
            this.model,
            argPhoneSpecs,
            this.isAvailable,
            this.lastBookedBy,
            this.lastBookedAt,
            this.bookings
        );
    }

    public static BookablePhone fromEntity(Phone phone, boolean isAvailable) {
        Optional<Booking> maybeMostRecentBooking = getMostRecentBooking(phone);
        return new BookablePhone(
                phone.getId(),
                phone.getModel(),
            Optional.empty(),
                isAvailable,
                maybeMostRecentBooking.map(recentBookedPhone-> recentBookedPhone.getPhoneUser()),
                maybeMostRecentBooking.map(recentBookedPhone-> Instant.ofEpochMilli(recentBookedPhone.getTimestamp())),
                phone.getBookings()
        );
    }

    private static Optional<Booking> getMostRecentBooking(Phone phone) {
        return phone.getBookings().stream().filter(booking -> booking.getAction() == UserAction.BORROWED)
            .max( (Booking b1, Booking b2)-> b1.getId() - b2.getId());
    }
}
