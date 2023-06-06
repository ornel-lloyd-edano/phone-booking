package com.exercise.phonebooking.service;

import com.exercise.phonebooking.entity.Booking;
import com.exercise.phonebooking.entity.Phone;
import com.exercise.phonebooking.entity.UserAction;
import com.exercise.phonebooking.repository.BookingRepository;
import com.exercise.phonebooking.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Service
public class PhoneBookingService {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GSMArenaPhoneSpecsProvider altPhoneSpecsProvider;

    private Optional<Booking> getMostRecentBooking(Phone phone) {
        return phone.getBookings().stream().max( (Booking b1, Booking b2)-> b1.getId() - b2.getId());
    }

    private Map<Integer, Object> phoneIdLocks = Collections.synchronizedMap(new HashMap<>(){{
        for (int id: IntStream.rangeClosed(1, 10).toArray()) {
            put(id, new Object());
        }
    }});

    private boolean getAvailability(Integer phoneId, boolean lockControlFromOutside) {
        if (lockControlFromOutside) {
            return phoneRepository.findById(phoneId).flatMap(phone -> getMostRecentBooking(phone))
                .map(recentBookedPhone-> recentBookedPhone.getAction() == UserAction.RETURNED)
                .orElse(true);
        } else {
            return Optional.ofNullable(phoneIdLocks.get(phoneId)).map(phoneLock -> {
                synchronized (phoneLock) {
                    return phoneRepository.findById(phoneId).flatMap(phone -> getMostRecentBooking(phone))
                        .map(recentBookedPhone-> recentBookedPhone.getAction() == UserAction.RETURNED)
                        .orElse(true);
                }
            }).orElse(false);
        }
    }

    public CompletableFuture<Optional<BookablePhone>> getPhone(int id) {
        return CompletableFuture.supplyAsync( () -> phoneRepository.findById(id)
            .map(phone -> BookablePhone.fromEntity(phone, getAvailability(phone.getId(), false)) ));
    }

    public CompletableFuture<Optional<BookablePhone>> getPhoneWithSpecs(int id) {
        return getPhone(id).thenApplyAsync( maybePhone -> {
            return maybePhone.map(phone -> {
                Optional<PhoneSpecs> phoneSpec = altPhoneSpecsProvider.getPhoneSpecs(phone.model());
                return phone.getCopy(phoneSpec);
            });
        });
    }

    public List<BookablePhone> getPhones(boolean includeSpec) throws InterruptedException, ExecutionException {
        List<CompletableFuture<BookablePhone>> phonesWithSpec = StreamSupport.stream(phoneRepository.findAll().spliterator(), true)
            .map(phone -> {
                BookablePhone bookablePhone = BookablePhone.fromEntity(phone, getAvailability(phone.getId(), false));
                if (includeSpec) {
                    return CompletableFuture.supplyAsync(() -> {
                        Optional<PhoneSpecs> phoneSpec = altPhoneSpecsProvider.getPhoneSpecs(phone.getModel());
                        return bookablePhone.getCopy(phoneSpec);
                    });
                } else {
                    return CompletableFuture.completedFuture(bookablePhone);
                }
            }).toList();

        return CompletableFuture.allOf(phonesWithSpec.toArray(new CompletableFuture[phonesWithSpec.size()]))
            .thenApply(arg -> {
                return phonesWithSpec.stream().map(future -> future.join())
                    .collect(Collectors.<BookablePhone>toList());
            }).get();
    }

    public Optional<BookablePhone> bookPhone(int phoneId, String bookedByUser, Instant bookedAt)
            throws PhoneNotAvailableException, InterruptedException, ExecutionException {
        phoneRepository.findById(phoneId).stream().forEach(phone -> {
            Optional.ofNullable(phoneIdLocks.get(phoneId)).stream().forEach(phoneLock -> {
                synchronized (phoneLock) {
                    if (getAvailability(phoneId, true)) {
                        Booking booking = new Booking();
                        booking.setPhone(phone);
                        booking.setPhoneUser(bookedByUser);
                        booking.setAction(UserAction.BORROWED);
                        booking.setTimestamp(bookedAt.toEpochMilli());
                        bookingRepository.save(booking);
                        List<Booking> prevBookings = phone.getBookings();
                        prevBookings.add(booking);
                        phone.setBookings(prevBookings);
                    } else {
                        getMostRecentBooking(phone).stream().forEach(booking -> {
                            throw new PhoneNotAvailableException(
                                phone.getModel(),
                                booking.getPhoneUser(),
                                Instant.ofEpochMilli(booking.getTimestamp())
                            );
                        });
                    }
                }
            });
        });
        return getPhone(phoneId).get();
    }

    public Optional<BookablePhone> returnPhone(int phoneId, Instant returnedAt) throws InterruptedException, ExecutionException {
        phoneRepository.findById(phoneId).stream().forEach(phone -> {
            if (!getAvailability(phoneId, false)) {
                Booking booking = new Booking();
                Optional<Booking> lastBooking = getMostRecentBooking(phone);
                String lastBorrower = lastBooking.map(lb -> lb.getPhoneUser()).orElse("Unknown User");
                booking.setPhoneUser(lastBorrower);
                booking.setTimestamp(returnedAt.toEpochMilli());
                booking.setAction(UserAction.RETURNED);
                booking.setPhone(phone);
                bookingRepository.save(booking);

                List<Booking> prevBookings = phone.getBookings();
                prevBookings.add(booking);
                phone.setBookings(prevBookings);
                phoneRepository.save(phone);
            }
        });
        return getPhone(phoneId).get();
    }
}
