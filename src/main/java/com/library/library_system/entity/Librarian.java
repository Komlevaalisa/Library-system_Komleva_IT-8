package com.library.library_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Сущность, представляющая библиотекаря
 */
@Entity
@Table(name = "librarians")
public class Librarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long librarianId;

    @Column(name = "librarian_number", unique = true, nullable = false)
    @NotBlank(message = "Табельный номер обязателен")
    private String librarianNumber;

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    @Column(nullable = false)
    private String position = "Библиотекарь";

    @Transient
    private Integer loansCount = 0;

    public Librarian() {}

    /**
     * Создает нового библиотекаря с указанными данными
     *
     * @param librarianNumber табельный номер библиотекаря
     * @param fullName полное имя библиотекаря
     * @param position должность библиотекаря
     */
    public Librarian(String librarianNumber, String fullName, String position) {
        this.librarianNumber = librarianNumber;
        this.fullName = fullName;
        this.position = position;
    }

    public Long getLibrarianId() { return librarianId; }
    public void setLibrarianId(Long librarianId) { this.librarianId = librarianId; }

    public String getLibrarianNumber() { return librarianNumber; }
    public void setLibrarianNumber(String librarianNumber) { this.librarianNumber = librarianNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getLoansCount() { return loansCount; }
    public void setLoansCount(Integer loansCount) { this.loansCount = loansCount; }

    /**
     * Проверяет, оформлял ли библиотекарь какие-либо выдачи
     *
     * @return true если библиотекарь оформлял выдачи, иначе false
     */
    public boolean hasLoans() {
        return loansCount > 0;
    }

    @Override
    public String toString() {
        return "Librarian{" +
                "librarianId=" + librarianId +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}