package com.library.library_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Сущность, представляющая выдачу книги читателю
 */
@Entity
@Table(name = "book_loans")
public class BookLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @NotNull(message = "Книга обязательна")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "reader_id", nullable = false)
    @NotNull(message = "Читатель обязателен")
    private Reader reader;

    @ManyToOne
    @JoinColumn(name = "librarian_id", nullable = false)
    @NotNull(message = "Библиотекарь обязателен")
    private Librarian librarian;

    @Column(name = "loan_date", nullable = false)
    @NotNull(message = "Дата выдачи обязательна")
    private LocalDate loanDate = LocalDate.now();

    @Column(name = "due_date", nullable = false)
    @NotNull(message = "Срок возврата обязателен")
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    public BookLoan() {}

    /**
     * Создает новую выдачу книги с указанными данными
     *
     * @param book выданная книга
     * @param reader читатель, получивший книгу
     * @param librarian библиотекарь, оформивший выдачу
     * @param loanDate дата выдачи книги
     * @param dueDate срок возврата книги
     */
    public BookLoan(Book book, Reader reader, Librarian librarian, LocalDate loanDate, LocalDate dueDate) {
        this.book = book;
        this.reader = reader;
        this.librarian = librarian;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
    }

    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Reader getReader() { return reader; }
    public void setReader(Reader reader) { this.reader = reader; }

    public Librarian getLibrarian() { return librarian; }
    public void setLibrarian(Librarian librarian) { this.librarian = librarian; }

    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    /**
     * Проверяет, возвращена ли книга
     *
     * @return true если книга возвращена, иначе false
     */
    public boolean isReturned() {
        return returnDate != null;
    }

    /**
     * Проверяет, просрочена ли выдача книги
     *
     * @return true если книга не возвращена и срок возврата истек, иначе false
     */
    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Вычисляет количество дней просрочки
     *
     * @return количество дней просрочки (0 если книга возвращена или не просрочена)
     */
    public long getDaysOverdue() {
        if (isReturned() || !isOverdue()) {
            return 0;
        }
        return LocalDate.now().toEpochDay() - dueDate.toEpochDay();
    }

    @Override
    public String toString() {
        return "BookLoan{" +
                "loanId=" + loanId +
                ", book=" + book.getTitle() +
                ", reader=" + reader.getFullName() +
                ", loanDate=" + loanDate +
                '}';
    }
}