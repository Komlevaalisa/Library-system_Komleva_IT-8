package com.library.library_system.controller;

import com.library.library_system.entity.*;
import com.library.library_system.service.BookLoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/loans")
public class BookLoanController {

    @Autowired
    private BookLoanService bookLoanService;

    /**
     * Обрабатывает GET-запрос для отображения списка выданных книг
     *
     * @param filter необязательный параметр фильтрации ("active" - активные, "overdue" - просроченные)
     * @param model объект Model для передачи данных в представление
     * @return имя представления для отображения списка выдач
     */
    @GetMapping
    public String listLoans(@RequestParam(required = false) String filter, Model model) {
        List<BookLoan> loans;
        if ("active".equals(filter)) {
            loans = bookLoanService.getActiveLoans();
            model.addAttribute("filter", "active");
        } else if ("overdue".equals(filter)) {
            loans = bookLoanService.getOverdueLoans();
            model.addAttribute("filter", "overdue");
        } else {
            loans = bookLoanService.getAllLoans();
        }
        model.addAttribute("loans", loans);
        model.addAttribute("today", LocalDate.now());
        return "loans/list";
    }

    /**
     * Обрабатывает GET-запрос для отображения формы добавления новой выдачи
     *
     * @param model объект Model для передачи данных в представление
     * @return имя представления с формой добавления выдачи
     */
    @GetMapping("/new")
    public String showAddForm(Model model) {
        BookLoan loan = new BookLoan();
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setLoanDate(LocalDate.now());

        model.addAttribute("loan", loan);
        model.addAttribute("books", bookLoanService.getAllBooks());
        model.addAttribute("readers", bookLoanService.getAllReaders());
        model.addAttribute("librarians", bookLoanService.getAllLibrarians());
        return "loans/form";
    }

    /**
     * Обрабатывает GET-запрос для отображения формы редактирования существующей выдачи
     *
     * @param id идентификатор выдачи для редактирования
     * @param model объект Model для передачи данных в представление
     * @return имя представления с формой редактирования или redirect на список выдач при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            BookLoan loan = bookLoanService.getLoanById(id);
            model.addAttribute("loan", loan);
            model.addAttribute("books", bookLoanService.getAllBooks());
            model.addAttribute("readers", bookLoanService.getAllReaders());
            model.addAttribute("librarians", bookLoanService.getAllLibrarians());
            return "loans/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/loans";
        }
    }

    /**
     * Обрабатывает POST-запрос для сохранения выдачи (создание или обновление)
     *
     * @param loan объект BookLoan с данными из формы
     * @param result объект BindingResult для валидации данных
     * @param model объект Model для передачи данных в представление
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return имя представления с формой при ошибках или redirect на список выдач при успешном сохранении
     */
    @PostMapping("/save")
    public String saveLoan(@Valid @ModelAttribute BookLoan loan,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("books", bookLoanService.getAllBooks());
            model.addAttribute("readers", bookLoanService.getAllReaders());
            model.addAttribute("librarians", bookLoanService.getAllLibrarians());
            return "loans/form";
        }

        try {
            bookLoanService.saveLoan(loan);
            redirectAttributes.addFlashAttribute("successMessage",
                    loan.getLoanId() == null ?
                            "Выдача книги успешно оформлена!" :
                            "Данные выдачи успешно обновлены!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("books", bookLoanService.getAllBooks());
            model.addAttribute("readers", bookLoanService.getAllReaders());
            model.addAttribute("librarians", bookLoanService.getAllLibrarians());
            return "loans/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    "Произошла ошибка при сохранении выдачи: " + e.getMessage());
            model.addAttribute("books", bookLoanService.getAllBooks());
            model.addAttribute("readers", bookLoanService.getAllReaders());
            model.addAttribute("librarians", bookLoanService.getAllLibrarians());
            return "loans/form";
        }

        return "redirect:/loans";
    }

    /**
     * Обрабатывает GET-запрос для удаления выдачи по идентификатору
     *
     * @param id идентификатор выдачи для удаления
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return redirect на список выдач
     */
    @GetMapping("/delete/{id}")
    public String deleteLoan(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            BookLoan loan = bookLoanService.getLoanById(id);
            bookLoanService.deleteLoanById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Запись о выдаче книги '" + loan.getBook().getTitle() +
                            "' успешно удалена!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при удалении выдачи: " + e.getMessage());
        }
        return "redirect:/loans";
    }

    /**
     * Обрабатывает GET-запрос для просмотра детальной информации о выдаче
     *
     * @param id идентификатор выдачи для просмотра
     * @param model объект Model для передачи данных в представление
     * @return имя представления с детальной информацией или redirect на список выдач при ошибке
     */
    @GetMapping("/view/{id}")
    public String viewLoan(@PathVariable Long id, Model model) {
        try {
            BookLoan loan = bookLoanService.getLoanById(id);
            model.addAttribute("loan", loan);
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("canDelete", bookLoanService.canDeleteLoan(id));
            return "loans/view";
        } catch (IllegalArgumentException e) {
            return "redirect:/loans";
        }
    }

    /**
     * Обрабатывает GET-запрос для отметки книги как возвращенной
     *
     * @param id идентификатор выдачи для возврата книги
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return redirect на список выдач
     */
    @GetMapping("/return/{id}")
    public String returnBook(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            BookLoan loan = bookLoanService.getLoanById(id);
            bookLoanService.returnBook(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Книга '" + loan.getBook().getTitle() + "' успешно возвращена!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при возврате книги: " + e.getMessage());
        }
        return "redirect:/loans";
    }

    /**
     * AJAX endpoint для проверки возможности удаления выдачи
     *
     * @param id идентификатор выдачи для проверки
     * @return строку с результатом проверки: "OK" если удаление возможно, "ERROR: [сообщение]" если невозможно
     */
    @GetMapping("/check-delete/{id}")
    @ResponseBody
    public String checkDelete(@PathVariable Long id) {
        try {
            BookLoan loan = bookLoanService.getLoanById(id);

            if (bookLoanService.canDeleteLoan(id)) {
                return "OK";
            } else {
                return "ERROR: Невозможно удалить активную выдачу книги '" +
                        loan.getBook().getTitle() + "'. Сначала верните книгу.";
            }
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}