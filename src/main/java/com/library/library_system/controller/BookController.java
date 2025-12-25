package com.library.library_system.controller;

import com.library.library_system.entity.Book;
import com.library.library_system.entity.BookLoan;
import com.library.library_system.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Контроллер для управления операциями с книгами.
 * Обрабатывает HTTP-запросы, связанные с созданием, чтением, обновлением и удалением книг.
 *
 * <p>Основные функции:</p>
 * <ul>
 *   <li>Отображение списка книг с возможностью поиска</li>
 *   <li>Добавление новых книг</li>
 *   <li>Редактирование существующих книг</li>
 *   <li>Удаление книг</li>
 *   <li>Просмотр детальной информации о книге</li>
 *   <li>Проверка возможности удаления книги</li>
 * </ul>
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Отображает список всех книг с возможностью поиска.
     *
     * @param search Поисковый запрос для фильтрации книг (необязательный)
     * @param model Модель для передачи данных в представление
     * @return Имя шаблона для отображения списка книг
     */
    @GetMapping
    public String listBooks(@RequestParam(required = false) String search,
                            Model model) {
        List<Book> books;
        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooks(search);
            model.addAttribute("search", search);
        } else {
            books = bookService.getAllBooks();
        }

        for (Book book : books) {
            List<BookLoan> activeLoans = bookService.getActiveBookLoans(book.getBookId());
            List<BookLoan> allLoans = bookService.getAllBookLoans(book.getBookId());

            book.setActiveLoansCount(activeLoans.size());
            book.setTotalLoansCount(allLoans.size());
        }

        model.addAttribute("books", books);
        return "books/list";
    }

    /**
     * Отображает форму для добавления новой книги.
     *
     * @param model Модель для передачи данных в представление
     * @return Имя шаблона формы добавления книги
     */
    @GetMapping("/new")
    public String showAddForm(Model model) {
        Book book = new Book();
        book.setActiveLoansCount(0);
        book.setTotalLoansCount(0);
        model.addAttribute("book", book);
        return "books/form";
    }

    /**
     * Отображает форму для редактирования существующей книги.
     *
     * @param id Идентификатор книги для редактирования
     * @param model Модель для передачи данных в представление
     * @return Имя шаблона формы редактирования книги или перенаправление на список книг при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Book book = bookService.getBookById(id);
            List<BookLoan> activeLoans = bookService.getActiveBookLoans(id);
            List<BookLoan> allLoans = bookService.getAllBookLoans(id);

            book.setActiveLoansCount(activeLoans.size());
            book.setTotalLoansCount(allLoans.size());

            model.addAttribute("book", book);
            return "books/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/books";
        }
    }

    /**
     * Сохраняет книгу (создает новую или обновляет существующую).
     *
     * @param book Объект книги с данными из формы
     * @param result Результат валидации объекта книги
     * @param model Модель для передачи данных в представление
     * @param redirectAttributes Атрибуты для перенаправления
     * @return Перенаправление на список книг при успешном сохранении или возврат к форме при ошибке
     */
    @PostMapping("/save")
    public String saveBook(@Valid @ModelAttribute Book book,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "books/form";
        }

        try {
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("successMessage",
                    book.getBookId() == null ?
                            "Книга успешно добавлена!" :
                            "Книга успешно обновлена!");
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "books/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    "Произошла ошибка при сохранении книги: " + e.getMessage());
            return "books/form";
        }

        return "redirect:/books";
    }

    /**
     * Удаляет книгу по идентификатору.
     *
     * @param id Идентификатор книги для удаления
     * @param redirectAttributes Атрибуты для перенаправления с сообщениями
     * @return Перенаправление на список книг
     */
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(id);
            bookService.deleteBookById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Книга '" + book.getTitle() + "' успешно удалена!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при удалении книги: " + e.getMessage());
        }
        return "redirect:/books";
    }

    /**
     * Отображает детальную информацию о книге.
     *
     * @param id Идентификатор книги для просмотра
     * @param model Модель для передачи данных в представление
     * @return Имя шаблона для отображения детальной информации о книге
     */
    @GetMapping("/view/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        try {
            Book book = bookService.getBookById(id);
            List<BookLoan> activeLoans = bookService.getActiveBookLoans(id);
            List<BookLoan> allLoans = bookService.getAllBookLoans(id);

            book.setActiveLoansCount(activeLoans.size());
            book.setTotalLoansCount(allLoans.size());

            model.addAttribute("book", book);
            model.addAttribute("canDelete", bookService.canDeleteBook(id));
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("allLoans", allLoans);
            return "books/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/books";
        }
    }

    /**
     * Проверяет возможность удаления книги (AJAX endpoint).
     *
     * @param id Идентификатор книги для проверки
     * @return Строка с результатом проверки: "OK" при возможности удаления или сообщение об ошибке
     */
    @GetMapping("/check-delete/{id}")
    @ResponseBody
    public String checkDelete(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);

            if (bookService.canDeleteBook(id)) {
                return "OK";
            } else {
                List<BookLoan> activeLoans = bookService.getActiveBookLoans(id);
                if (!activeLoans.isEmpty()) {
                    return "ERROR: Книга '" + book.getTitle() + "' выдана читателю. Сначала верните книгу.";
                } else {
                    List<BookLoan> allLoans = bookService.getAllBookLoans(id);
                    if (!allLoans.isEmpty()) {
                        return "ERROR: Книга '" + book.getTitle() + "' есть в истории выдач. Удалите сначала записи о выдаче.";
                    } else {
                        return "ERROR: Книгу '" + book.getTitle() + "' нельзя удалить по неизвестной причине.";
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}