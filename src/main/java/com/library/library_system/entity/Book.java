package com.library.library_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Сущность, представляющая книгу в библиотеке
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(name = "book_number", unique = true, nullable = false)
    @NotBlank(message = "Инвентарный номер обязателен")
    private String bookNumber;

    @Column(nullable = false)
    @NotBlank(message = "Название книги обязательно")
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Автор обязателен")
    private String author;

    @Column(name = "publication_year")
    @NotNull(message = "Год издания обязателен")
    @Min(value = 1500, message = "Год издания должен быть не менее 1500")
    private Integer publicationYear;

    @Transient
    private Integer activeLoansCount = 0;

    @Transient
    private Integer totalLoansCount = 0;

    public Book() {}

    /**
     * Создает новую книгу с указанными данными
     *
     * @param bookNumber инвентарный номер книги
     * @param title название книги
     * @param author автор книги
     * @param publicationYear год издания книги
     */
    public Book(String bookNumber, String title, String author, Integer publicationYear) {
        this.bookNumber = bookNumber;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookNumber() { return bookNumber; }
    public void setBookNumber(String bookNumber) { this.bookNumber = bookNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public Integer getActiveLoansCount() { return activeLoansCount; }
    public void setActiveLoansCount(Integer activeLoansCount) { this.activeLoansCount = activeLoansCount; }

    public Integer getTotalLoansCount() { return totalLoansCount; }
    public void setTotalLoansCount(Integer totalLoansCount) { this.totalLoansCount = totalLoansCount; }

    /**
     * Проверяет, доступна ли книга для выдачи
     *
     * @return true если книга доступна для выдачи, иначе false
     */
    public boolean isAvailable() {
        return activeLoansCount == 0;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}