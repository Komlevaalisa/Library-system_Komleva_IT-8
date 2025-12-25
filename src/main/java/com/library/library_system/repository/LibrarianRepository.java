package com.library.library_system.repository;

import com.library.library_system.entity.Librarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Librarian (библиотекари)
 */
@Repository
public interface LibrarianRepository extends JpaRepository<Librarian, Long> {

    List<Librarian> findByFullNameContainingIgnoreCase(String fullName);
    Librarian findByLibrarianNumber(String librarianNumber);
    List<Librarian> findByPositionContainingIgnoreCase(String position);
}