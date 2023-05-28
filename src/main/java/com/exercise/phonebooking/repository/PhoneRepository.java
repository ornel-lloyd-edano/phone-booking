package com.exercise.phonebooking.repository;

import com.exercise.phonebooking.entity.Phone;
import org.springframework.data.repository.CrudRepository;

public interface PhoneRepository extends CrudRepository<Phone, Integer> {
}
