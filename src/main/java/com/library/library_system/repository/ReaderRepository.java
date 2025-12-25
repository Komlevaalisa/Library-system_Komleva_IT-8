package com.library.library_system.repository;

import com.library.library_system.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Reader (читатели)
 */
@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    List<Reader> findByFullNameContainingIgnoreCase(String fullName);
    Reader findByTicketNumber(String ticketNumber);
    Reader findByPhoneNumber(String phoneNumber);
    List<Reader> findByRegistrationDate(LocalDate registrationDate);
}