package com.library.library_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * Сущность, представляющая читателя библиотеки
 */
@Entity
@Table(name = "readers")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long readerId;

    @Column(name = "ticket_number", unique = true, nullable = false)
    @NotBlank(message = "Номер читательского билета обязателен")
    private String ticketNumber;

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    @Column(name = "phone_number", unique = true, nullable = false)
    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = "^\\+?[0-9\\s\\-\\(\\)]+$", message = "Некорректный номер телефона")
    private String phoneNumber;

    @Column(name = "registration_date")
    private LocalDate registrationDate = LocalDate.now();

    @Transient
    private Integer activeLoansCount = 0;

    @Transient
    private Integer totalLoansCount = 0;

    public Reader() {}

    /**
     * Создает нового читателя с указанными данными
     *
     * @param ticketNumber номер читательского билета
     * @param fullName полное имя читателя
     * @param phoneNumber номер телефона читателя
     */
    public Reader(String ticketNumber, String fullName, String phoneNumber) {
        this.ticketNumber = ticketNumber;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public Integer getActiveLoansCount() { return activeLoansCount; }
    public void setActiveLoansCount(Integer activeLoansCount) { this.activeLoansCount = activeLoansCount; }

    public Integer getTotalLoansCount() { return totalLoansCount; }
    public void setTotalLoansCount(Integer totalLoansCount) { this.totalLoansCount = totalLoansCount; }

    /**
     * Проверяет, есть ли у читателя активные выдачи книг
     *
     * @return true если у читателя есть активные выдачи, иначе false
     */
    public boolean hasActiveLoans() {
        return activeLoansCount > 0;
    }

    @Override
    public String toString() {
        return "Reader{" +
                "readerId=" + readerId +
                ", fullName='" + fullName + '\'' +
                ", ticketNumber='" + ticketNumber + '\'' +
                '}';
    }
}