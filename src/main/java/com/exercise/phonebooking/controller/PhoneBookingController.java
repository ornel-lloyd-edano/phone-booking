package com.exercise.phonebooking.controller;

import com.exercise.phonebooking.service.PhoneBookingService;
import com.exercise.phonebooking.service.PhoneNotAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/phone-booking")
public class PhoneBookingController {

    @Autowired
    private PhoneBookingService phoneBookingService;

    @GetMapping
    public List<PhoneDetails> getPhones(@RequestParam(value = "includeSpec", defaultValue = "false") boolean includeSpec) {
        try {
            return phoneBookingService.getPhones(includeSpec).stream().map(PhoneDetails::fromModel).toList();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "See details in logs", ex);
        }
    }

    @GetMapping(path = "/{id}")
    public PhoneDetails getPhone(@PathVariable("id") int id) {
        try {
            return phoneBookingService.getPhoneWithSpecs(id).get()
                .map(phone -> PhoneDetails.fromModel(phone, true)).get();
        } catch (NoSuchElementException ex) {
            String message = String.format("Phone with id [%d] does not exist", id);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, message, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "See details in logs", ex);
        }
    }

    @PostMapping
    public PhoneDetails bookPhone(@RequestBody BookPhone bookPhone) {
        try {
            Instant bookedAt = bookPhone.bookedAt().toInstant(ZoneOffset.UTC);
            return phoneBookingService.bookPhone(bookPhone.phoneId(), bookPhone.bookedBy(), bookedAt)
                .map(phone -> PhoneDetails.fromModel(phone, true)).get();
        } catch (PhoneNotAvailableException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (NoSuchElementException ex) {
            String message = String.format("Fail to book phone with id [%d]. It does not exist", bookPhone.phoneId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "See details in logs", ex);
        }

    }

    @PostMapping("/return")
    public PhoneDetails returnPhone(@RequestBody ReturnPhone returnPhone) {
        try {
            return phoneBookingService.returnPhone(returnPhone.phoneId(), returnPhone.returnedAt().toInstant(ZoneOffset.UTC))
                .map(phone -> PhoneDetails.fromModel(phone, true)).get();
        } catch (NoSuchElementException ex) {
            String message = String.format("Fail to return phone with id [%d]. It does not exist", returnPhone.phoneId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "See details in logs", ex);
        }
    }
}
