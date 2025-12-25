package com.library.library_system.service;

import com.library.library_system.entity.Librarian;
import com.library.library_system.entity.BookLoan;
import com.library.library_system.repository.LibrarianRepository;
import com.library.library_system.repository.BookLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с библиотекарями
 */
@Service
public class LibrarianService {

    @Autowired
    private LibrarianRepository librarianRepository;

    @Autowired
    private BookLoanRepository bookLoanRepository;

    /**
     * Получить всех библиотекарей из базы данных
     *
     * @return список всех библиотекарей
     */
    public List<Librarian> getAllLibrarians() {
        return librarianRepository.findAll();
    }

    /**
     * Сохранить библиотекаря в базе данных (создание или обновление)
     *
     * @param librarian библиотекарь для сохранения
     * @throws IllegalStateException если нарушена уникальность табельного номера
     */
    @Transactional
    public void saveLibrarian(Librarian librarian) {
        if (librarian.getLibrarianId() == null) {
            Librarian existingLibrarian = librarianRepository.findByLibrarianNumber(librarian.getLibrarianNumber());
            if (existingLibrarian != null) {
                throw new IllegalStateException("Библиотекарь с табельным номером '" +
                        librarian.getLibrarianNumber() + "' уже существует.");
            }
        }
        else {
            Librarian existingLibrarian = librarianRepository.findByLibrarianNumber(librarian.getLibrarianNumber());
            if (existingLibrarian != null && !existingLibrarian.getLibrarianId().equals(librarian.getLibrarianId())) {
                throw new IllegalStateException("Библиотекарь с табельным номером '" +
                        librarian.getLibrarianNumber() + "' уже существует.");
            }
        }

        librarianRepository.save(librarian);
    }

    /**
     * Найти библиотекаря по идентификатору
     *
     * @param id идентификатор библиотекаря
     * @return найденный библиотекарь
     * @throws IllegalArgumentException если библиотекарь не найден
     */
    public Librarian getLibrarianById(Long id) {
        return librarianRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Библиотекарь с ID " + id + " не найден"));
    }

    /**
     * Удалить библиотекаря по идентификатору
     *
     * @param id идентификатор библиотекаря для удаления
     * @throws IllegalStateException если библиотекарь оформлял выдачи книг
     */
    @Transactional
    public void deleteLibrarianById(Long id) {
        Librarian librarian = getLibrarianById(id);

        List<BookLoan> librarianLoans = bookLoanRepository.findByLibrarianLibrarianId(id);
        if (!librarianLoans.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить библиотекаря '" + librarian.getFullName() +
                    "', так как он оформлял выдачи книг. " +
                    "Для сохранения истории сначала удалите или переоформите записи о выдаче.");
        }

        librarianRepository.deleteById(id);
    }

    /**
     * Поиск библиотекарей по ключевому слову в ФИО
     *
     * @param keyword ключевое слово для поиска
     * @return список найденных библиотекарей или всех библиотекарей если ключевое слово пустое
     */
    public List<Librarian> searchLibrarians(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return librarianRepository.findByFullNameContainingIgnoreCase(keyword);
        }
        return getAllLibrarians();
    }

    /**
     * Проверить возможность удаления библиотекаря
     *
     * @param id идентификатор библиотекаря
     * @return true если библиотекаря можно удалить, false если он оформлял выдачи
     */
    public boolean canDeleteLibrarian(Long id) {
        List<BookLoan> librarianLoans = bookLoanRepository.findByLibrarianLibrarianId(id);
        return librarianLoans.isEmpty();
    }

    /**
     * Получить все выдачи, оформленные библиотекарем
     *
     * @param librarianId идентификатор библиотекаря
     * @return список выдач, оформленных указанным библиотекарем
     */
    public List<BookLoan> getLibrarianLoans(Long librarianId) {
        return bookLoanRepository.findByLibrarianLibrarianId(librarianId);
    }

    /**
     * Проверить существование библиотекаря по табельному номеру
     *
     * @param librarianNumber табельный номер библиотекаря
     * @return true если библиотекарь существует, false если нет
     */
    public boolean existsByLibrarianNumber(String librarianNumber) {
        return librarianRepository.findByLibrarianNumber(librarianNumber) != null;
    }
}