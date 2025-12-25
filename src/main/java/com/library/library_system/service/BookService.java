package com.library.library_system.service;

import com.library.library_system.entity.Book;
import com.library.library_system.entity.BookLoan;
import com.library.library_system.repository.BookRepository;
import com.library.library_system.repository.BookLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с книгами
 */
@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookLoanRepository bookLoanRepository;

    /**
     * Получить все книги из базы данных
     *
     * @return список всех книг
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * Сохранить книгу в базе данных (создание или обновление)
     *
     * @param book книга для сохранения
     * @throws IllegalStateException если нарушена уникальность инвентарного номера
     */
    @Transactional
    public void saveBook(Book book) {
        if (book.getBookId() == null) {
            Book existingBook = bookRepository.findByBookNumber(book.getBookNumber());
            if (existingBook != null) {
                throw new IllegalStateException("Книга с инвентарным номером '" + book.getBookNumber() + "' уже существует.");
            }
        }
        else {
            Book existingBook = bookRepository.findByBookNumber(book.getBookNumber());
            if (existingBook != null && !existingBook.getBookId().equals(book.getBookId())) {
                throw new IllegalStateException("Книга с инвентарным номером '" + book.getBookNumber() + "' уже существует.");
            }
        }

        bookRepository.save(book);
    }

    /**
     * Найти книгу по идентификатору
     *
     * @param id идентификатор книги
     * @return найденная книга
     * @throws IllegalArgumentException если книга не найдена
     */
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Книга с ID " + id + " не найдена"));
    }

    /**
     * Удалить книгу по идентификатору
     *
     * @param id идентификатор книги для удаления
     * @throws IllegalStateException если книга имеет активные выдачи или находится в истории выдач
     */
    @Transactional
    public void deleteBookById(Long id) {
        Book book = getBookById(id);

        List<BookLoan> activeLoans = bookLoanRepository.findByBookBookIdAndReturnDateIsNull(id);
        if (!activeLoans.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить книгу '" + book.getTitle() +
                    "', так как она в настоящее время выдана читателю. " +
                    "Сначала верните книгу или отмените выдачу.");
        }

        List<BookLoan> allLoans = bookLoanRepository.findByBookBookId(id);
        if (!allLoans.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить книгу '" + book.getTitle() +
                    "', так как она есть в истории выдач. " +
                    "Для сохранения истории сначала удалите записи о выдаче.");
        }

        bookRepository.deleteById(id);
    }

    /**
     * Поиск книг по ключевому слову в названии
     *
     * @param keyword ключевое слово для поиска
     * @return список найденных книг или всех книг если ключевое слово пустое
     */
    public List<Book> searchBooks(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCase(keyword);
        }
        return getAllBooks();
    }

    /**
     * Проверить возможность удаления книги
     *
     * @param id идентификатор книги
     * @return true если книгу можно удалить, false если есть активные или исторические выдачи
     */
    public boolean canDeleteBook(Long id) {
        List<BookLoan> activeLoans = bookLoanRepository.findByBookBookIdAndReturnDateIsNull(id);
        List<BookLoan> allLoans = bookLoanRepository.findByBookBookId(id);
        return activeLoans.isEmpty() && allLoans.isEmpty();
    }

    /**
     * Получить активные выдачи книги
     *
     * @param bookId идентификатор книги
     * @return список активных выдач для указанной книги
     */
    public List<BookLoan> getActiveBookLoans(Long bookId) {
        return bookLoanRepository.findByBookBookIdAndReturnDateIsNull(bookId);
    }

    /**
     * Получить все выдачи книги
     *
     * @param bookId идентификатор книги
     * @return список всех выдач для указанной книги
     */
    public List<BookLoan> getAllBookLoans(Long bookId) {
        return bookLoanRepository.findByBookBookId(bookId);
    }

    /**
     * Проверить существование книги по инвентарному номеру
     *
     * @param bookNumber инвентарный номер книги
     * @return true если книга существует, false если нет
     */
    public boolean existsByBookNumber(String bookNumber) {
        return bookRepository.findByBookNumber(bookNumber) != null;
    }
}