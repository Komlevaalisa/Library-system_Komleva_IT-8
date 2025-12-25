package com.library.library_system.service;

import com.library.library_system.entity.*;
import com.library.library_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервис для работы с выдачами книг
 */
@Service
public class BookLoanService {

    @Autowired
    private BookLoanRepository bookLoanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private LibrarianRepository librarianRepository;

    /**
     * Получить все выдачи книг
     *
     * @return список всех выдач
     */
    public List<BookLoan> getAllLoans() {
        return bookLoanRepository.findAll();
    }

    /**
     * Получить активные выдачи (не возвращенные книги)
     *
     * @return список активных выдач
     */
    public List<BookLoan> getActiveLoans() {
        return bookLoanRepository.findByReturnDateIsNull();
    }

    /**
     * Получить просроченные выдачи
     *
     * @return список просроченных выдач
     */
    public List<BookLoan> getOverdueLoans() {
        return bookLoanRepository.findByReturnDateIsNullAndDueDateBefore(LocalDate.now());
    }

    /**
     * Сохранить выдачу книги (создание или обновление)
     *
     * @param loan выдача книги для сохранения
     * @throws IllegalArgumentException если найдены ошибки в данных
     * @throws IllegalStateException если книга уже выдана другому читателю
     */
    @Transactional
    public void saveLoan(BookLoan loan) {
        Book book = bookRepository.findById(loan.getBook().getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Книга не найдена"));

        Reader reader = readerRepository.findById(loan.getReader().getReaderId())
                .orElseThrow(() -> new IllegalArgumentException("Читатель не найден"));

        Librarian librarian = librarianRepository.findById(loan.getLibrarian().getLibrarianId())
                .orElseThrow(() -> new IllegalArgumentException("Библиотекарь не найден"));

        if (loan.getLoanDate() == null) {
            throw new IllegalArgumentException("Дата выдачи обязательна");
        }

        if (loan.getDueDate() == null) {
            throw new IllegalArgumentException("Срок возврата обязателен");
        }

        if (loan.getDueDate().isBefore(loan.getLoanDate())) {
            throw new IllegalArgumentException("Срок возврата не может быть раньше даты выдачи");
        }

        if (loan.getReturnDate() != null && loan.getReturnDate().isBefore(loan.getLoanDate())) {
            throw new IllegalArgumentException("Дата возврата не может быть раньше даты выдачи");
        }

        if (loan.getLoanId() == null) {
            List<BookLoan> activeLoans = bookLoanRepository.findByBookBookIdAndReturnDateIsNull(book.getBookId());
            if (!activeLoans.isEmpty()) {
                throw new IllegalStateException("Книга '" + book.getTitle() +
                        "' уже выдана другому читателю. Сначала верните книгу.");
            }
        }
        else {
            BookLoan existingLoan = getLoanById(loan.getLoanId());
            if (!existingLoan.getBook().getBookId().equals(book.getBookId()) &&
                    !existingLoan.isReturned()) {
                List<BookLoan> activeLoans = bookLoanRepository.findByBookBookIdAndReturnDateIsNull(book.getBookId());
                if (!activeLoans.isEmpty()) {
                    throw new IllegalStateException("Книга '" + book.getTitle() +
                            "' уже выдана другому читателю. Сначала верните книгу.");
                }
            }
        }

        bookLoanRepository.save(loan);
    }

    /**
     * Найти выдачу по идентификатору
     *
     * @param id идентификатор выдачи
     * @return найденная выдача
     * @throws IllegalArgumentException если выдача не найдена
     */
    public BookLoan getLoanById(Long id) {
        return bookLoanRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Выдача с ID " + id + " не найдена"));
    }

    /**
     * Удалить выдачу по идентификатору
     *
     * @param id идентификатор выдачи для удаления
     * @throws IllegalStateException если попытка удалить активную выдачу
     */
    @Transactional
    public void deleteLoanById(Long id) {
        BookLoan loan = getLoanById(id);

        if (!loan.isReturned()) {
            throw new IllegalStateException("Невозможно удалить активную выдачу. " +
                    "Сначала верните книгу или отмените выдачу через возврат.");
        }

        bookLoanRepository.deleteById(id);
    }

    /**
     * Отметить книгу как возвращенную
     *
     * @param loanId идентификатор выдачи
     * @throws IllegalStateException если книга уже возвращена
     */
    @Transactional
    public void returnBook(Long loanId) {
        BookLoan loan = getLoanById(loanId);
        if (loan.isReturned()) {
            throw new IllegalStateException("Книга уже возвращена");
        }
        loan.setReturnDate(LocalDate.now());
        saveLoan(loan);
    }

    /**
     * Получить все книги для выпадающего списка
     *
     * @return список всех книг
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Получить всех читателей для выпадающего списка
     *
     * @return список всех читателей
     */
    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }

    /**
     * Получить всех библиотекарей для выпадающего списка
     *
     * @return список всех библиотекарей
     */
    public List<Librarian> getAllLibrarians() {
        return librarianRepository.findAll();
    }

    /**
     * Проверить возможность удаления выдачи
     *
     * @param id идентификатор выдачи
     * @return true если выдача возвращена и может быть удалена, false если активна
     */
    public boolean canDeleteLoan(Long id) {
        BookLoan loan = getLoanById(id);
        return loan.isReturned();
    }
}