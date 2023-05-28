package com.exercise.phonebooking.repository;

import com.exercise.phonebooking.entity.Booking;
import org.springframework.data.repository.CrudRepository;

public interface BookingRepository extends CrudRepository<Booking, Integer> {
}