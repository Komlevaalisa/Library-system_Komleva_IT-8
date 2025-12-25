package com.library.library_system.repository;

import com.library.library_system.entity.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с сущностью BookLoan (выдачи книг)
 */
@Repository
public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {

    List<BookLoan> findByReturnDateIsNull();
    List<BookLoan> findByBookBookIdAndReturnDateIsNull(Long bookId);
    List<BookLoan> findByBookBookId(Long bookId);
    List<BookLoan> findByReaderReaderIdAndReturnDateIsNull(Long readerId);
    List<BookLoan> findByReaderReaderId(Long readerId);
    List<BookLoan> findByReturnDateIsNullAndDueDateBefore(LocalDate date);
    List<BookLoan> findByLibrarianLibrarianId(Long librarianId);
    List<BookLoan> findByLoanDateBetween(LocalDate startDate, LocalDate endDate);
}