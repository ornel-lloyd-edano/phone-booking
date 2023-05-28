package com.exercise.phonebooking.controller;

import java.time.LocalDateTime;

public record ReturnPhone(int phoneId, LocalDateTime returnedAt) {
}
