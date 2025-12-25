package com.library.library_system.service;

import com.library.library_system.entity.Reader;
import com.library.library_system.entity.BookLoan;
import com.library.library_system.repository.ReaderRepository;
import com.library.library_system.repository.BookLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с читателями
 */
@Service
public class ReaderService {

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private BookLoanRepository bookLoanRepository;

    /**
     * Получить всех читателей из базы данных
     *
     * @return список всех читателей
     */
    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }

    /**
     * Сохранить читателя в базе данных (создание или обновление)
     *
     * @param reader читатель для сохранения
     * @throws IllegalStateException если нарушена уникальность номера билета или телефона
     */
    @Transactional
    public void saveReader(Reader reader) {
        if (reader.getReaderId() == null) {
            Reader existingReader = readerRepository.findByTicketNumber(reader.getTicketNumber());
            if (existingReader != null) {
                throw new IllegalStateException("Читатель с номером билета '" +
                        reader.getTicketNumber() + "' уже существует.");
            }

            existingReader = readerRepository.findByPhoneNumber(reader.getPhoneNumber());
            if (existingReader != null) {
                throw new IllegalStateException("Читатель с номером телефона '" +
                        reader.getPhoneNumber() + "' уже существует.");
            }
        }
        else {
            Reader existingTicket = readerRepository.findByTicketNumber(reader.getTicketNumber());
            if (existingTicket != null && !existingTicket.getReaderId().equals(reader.getReaderId())) {
                throw new IllegalStateException("Читатель с номером билета '" +
                        reader.getTicketNumber() + "' уже существует.");
            }

            Reader existingPhone = readerRepository.findByPhoneNumber(reader.getPhoneNumber());
            if (existingPhone != null && !existingPhone.getReaderId().equals(reader.getReaderId())) {
                throw new IllegalStateException("Читатель с номером телефона '" +
                        reader.getPhoneNumber() + "' уже существует.");
            }
        }

        readerRepository.save(reader);
    }

    /**
     * Найти читателя по идентификатору
     *
     * @param id идентификатор читателя
     * @return найденный читатель
     * @throws IllegalArgumentException если читатель не найден
     */
    public Reader getReaderById(Long id) {
        return readerRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Читатель с ID " + id + " не найден"));
    }

    /**
     * Удалить читателя по идентификатору
     *
     * @param id идентификатор читателя для удаления
     * @throws IllegalStateException если у читателя есть активные выдачи или он есть в истории выдач
     */
    @Transactional
    public void deleteReaderById(Long id) {
        Reader reader = getReaderById(id);

        List<BookLoan> activeLoans = bookLoanRepository.findByReaderReaderIdAndReturnDateIsNull(id);
        if (!activeLoans.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить читателя '" + reader.getFullName() +
                    "', так как у него есть активные выдачи книг. " +
                    "Сначала верните все книги или отмените выдачи.");
        }

        List<BookLoan> allLoans = bookLoanRepository.findByReaderReaderId(id);
        if (!allLoans.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить читателя '" + reader.getFullName() +
                    "', так как он есть в истории выдач. " +
                    "Для сохранения истории сначала удалите записи о выдаче.");
        }

        readerRepository.deleteById(id);
    }

    /**
     * Поиск читателей по ключевому слову в ФИО
     *
     * @param keyword ключевое слово для поиска
     * @return список найденных читателей или всех читателей если ключевое слово пустое
     */
    public List<Reader> searchReaders(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return readerRepository.findByFullNameContainingIgnoreCase(keyword);
        }
        return getAllReaders();
    }

    /**
     * Проверить возможность удаления читателя
     *
     * @param id идентификатор читателя
     * @return true если читателя можно удалить, false если у него есть активные выдачи или он в истории выдач
     */
    public boolean canDeleteReader(Long id) {
        List<BookLoan> activeLoans = bookLoanRepository.findByReaderReaderIdAndReturnDateIsNull(id);
        List<BookLoan> allLoans = bookLoanRepository.findByReaderReaderId(id);
        return activeLoans.isEmpty() && allLoans.isEmpty();
    }

    /**
     * Получить активные выдачи читателя
     *
     * @param readerId идентификатор читателя
     * @return список активных выдач для указанного читателя
     */
    public List<BookLoan> getActiveReaderLoans(Long readerId) {
        return bookLoanRepository.findByReaderReaderIdAndReturnDateIsNull(readerId);
    }

    /**
     * Получить все выдачи читателя
     *
     * @param readerId идентификатор читателя
     * @return список всех выдач для указанного читателя
     */
    public List<BookLoan> getAllReaderLoans(Long readerId) {
        return bookLoanRepository.findByReaderReaderId(readerId);
    }

    /**
     * Проверить существование читателя по номеру читательского билета
     *
     * @param ticketNumber номер читательского билета
     * @return true если читатель существует, false если нет
     */
    public boolean existsByTicketNumber(String ticketNumber) {
        return readerRepository.findByTicketNumber(ticketNumber) != null;
    }

    /**
     * Проверить существование читателя по номеру телефона
     *
     * @param phoneNumber номер телефона читателя
     * @return true если читатель существует, false если нет
     */
    public boolean existsByPhoneNumber(String phoneNumber) {
        return readerRepository.findByPhoneNumber(phoneNumber) != null;
    }
}