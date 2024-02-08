package ru.izebit.mobile.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.izebit.mobile.booking.model.Phone;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
}