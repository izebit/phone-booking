package ru.izebit.mobile.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.izebit.mobile.booking.model.Phone;
import ru.izebit.mobile.booking.model.Reservation;
import ru.izebit.mobile.booking.repository.PhoneRepository;
import ru.izebit.mobile.booking.repository.ReservationRepository;
import ru.izebit.mobile.booking.service.exception.PhoneIsAlreadyBookedException;
import ru.izebit.mobile.booking.service.exception.PhoneIsBookedBySomebodyElseException;
import ru.izebit.mobile.booking.service.exception.PhoneIsNotBookedException;
import ru.izebit.mobile.booking.service.exception.PhoneNotFoundException;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhoneService {
    private final PhoneRepository phoneRepository;
    private final ReservationRepository reservationRepository;

    public Collection<Entry<Phone, Reservation>> getAllPhonesWithReservations() {
        var phones = phoneRepository.findAll();
        return phones
                .stream()
                .map(e -> {
                    var reservation = reservationRepository.findByPhoneIdAndIsActiveTrue(e.getId());
                    return (Entry<Phone, Reservation>) new SimpleEntry<>(e, reservation);
                })
                .toList();
    }

    @Transactional
    public void bookPhoneBy(Long phoneId, String username) throws PhoneIsAlreadyBookedException, PhoneNotFoundException {
        var phone = phoneRepository.findById(phoneId)
                .orElseThrow(PhoneNotFoundException::new);

        var reservation = reservationRepository.findByPhoneIdAndIsActiveTrue(phoneId);
        if (reservation != null)
            throw new PhoneIsAlreadyBookedException();

        reservationRepository.save(Reservation.builder()
                .phone(phone)
                .bookedDate(LocalDateTime.now())
                .isActive(true)
                .username(username)
                .build());
    }

    @Transactional
    public void releasePhoneBy(Long phoneId, String username) throws PhoneIsBookedBySomebodyElseException, PhoneIsNotBookedException, PhoneNotFoundException {
        phoneRepository.findById(phoneId).orElseThrow(PhoneNotFoundException::new);
        var reservation = reservationRepository.findByPhoneIdAndIsActiveTrue(phoneId);
        if (reservation == null)
            throw new PhoneIsNotBookedException();

        if (!username.equals(reservation.getUsername()))
            throw new PhoneIsBookedBySomebodyElseException();

        reservation.setActive(false);
        reservationRepository.save(reservation);
    }

    public Optional<Phone> getPhoneById(Long phoneId) {
        return phoneRepository.findById(phoneId);
    }
}
