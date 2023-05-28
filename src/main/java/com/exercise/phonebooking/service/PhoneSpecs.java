package com.exercise.phonebooking.service;

import java.util.Optional;

public record PhoneSpecs(
    String model,
    String technology,
    String band2G,
    Optional<String> band3G,
    Optional<String> band4G
) {
}
