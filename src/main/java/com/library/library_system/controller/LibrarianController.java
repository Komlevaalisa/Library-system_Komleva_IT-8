package com.library.library_system.controller;

import com.library.library_system.entity.Librarian;
import com.library.library_system.entity.BookLoan;
import com.library.library_system.service.LibrarianService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/librarians")
public class LibrarianController {

    @Autowired
    private LibrarianService librarianService;

    /**
     * Обрабатывает GET-запрос для отображения списка библиотекарей
     *
     * @param search необязательный параметр для поиска библиотекарей
     * @param model объект Model для передачи данных в представление
     * @return имя представления для отображения списка библиотекарей
     */
    @GetMapping
    public String listLibrarians(@RequestParam(required = false) String search, Model model) {
        List<Librarian> librarians;
        if (search != null && !search.trim().isEmpty()) {
            librarians = librarianService.searchLibrarians(search);
            model.addAttribute("search", search);
        } else {
            librarians = librarianService.getAllLibrarians();
        }

        for (Librarian librarian : librarians) {
            List<BookLoan> loans = librarianService.getLibrarianLoans(librarian.getLibrarianId());
            librarian.setLoansCount(loans.size());
        }

        model.addAttribute("librarians", librarians);
        return "librarians/list";
    }

    /**
     * Обрабатывает GET-запрос для отображения формы добавления нового библиотекаря
     *
     * @param model объект Model для передачи данных в представление
     * @return имя представления с формой добавления библиотекаря
     */
    @GetMapping("/new")
    public String showAddForm(Model model) {
        Librarian librarian = new Librarian();
        librarian.setLoansCount(0);
        model.addAttribute("librarian", librarian);
        return "librarians/form";
    }

    /**
     * Обрабатывает GET-запрос для отображения формы редактирования библиотекаря
     *
     * @param id идентификатор библиотекаря для редактирования
     * @param model объект Model для передачи данных в представление
     * @return имя представления с формой редактирования или redirect на список библиотекарей при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Librarian librarian = librarianService.getLibrarianById(id);

            List<BookLoan> loans = librarianService.getLibrarianLoans(id);
            librarian.setLoansCount(loans.size());

            model.addAttribute("librarian", librarian);
            return "librarians/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/librarians";
        }
    }

    /**
     * Обрабатывает POST-запрос для сохранения библиотекаря (создание или обновление)
     *
     * @param librarian объект Librarian с данными из формы
     * @param result объект BindingResult для валидации данных
     * @param model объект Model для передачи данных в представление
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return имя представления с формой при ошибках или redirect на список библиотекарей при успешном сохранении
     */
    @PostMapping("/save")
    public String saveLibrarian(@Valid @ModelAttribute Librarian librarian,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "librarians/form";
        }

        try {
            librarianService.saveLibrarian(librarian);
            redirectAttributes.addFlashAttribute("successMessage",
                    librarian.getLibrarianId() == null ?
                            "Библиотекарь успешно добавлен!" :
                            "Данные библиотекаря успешно обновлены!");
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "librarians/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    "Произошла ошибка при сохранении данных библиотекаря: " + e.getMessage());
            return "librarians/form";
        }

        return "redirect:/librarians";
    }

    /**
     * Обрабатывает GET-запрос для удаления библиотекаря
     *
     * @param id идентификатор библиотекаря для удаления
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return redirect на список библиотекарей
     */
    @GetMapping("/delete/{id}")
    public String deleteLibrarian(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            Librarian librarian = librarianService.getLibrarianById(id);
            librarianService.deleteLibrarianById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Библиотекарь '" + librarian.getFullName() + "' успешно удален!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при удалении библиотекаря: " + e.getMessage());
        }
        return "redirect:/librarians";
    }

    /**
     * Обрабатывает GET-запрос для просмотра детальной информации о библиотекаре
     *
     * @param id идентификатор библиотекаря для просмотра
     * @param model объект Model для передачи данных в представление
     * @return имя представления с детальной информацией или redirect на список библиотекарей при ошибке
     */
    @GetMapping("/view/{id}")
    public String viewLibrarian(@PathVariable Long id, Model model) {
        try {
            Librarian librarian = librarianService.getLibrarianById(id);

            List<BookLoan> librarianLoans = librarianService.getLibrarianLoans(id);
            librarian.setLoansCount(librarianLoans.size());

            model.addAttribute("librarian", librarian);
            model.addAttribute("canDelete", librarianService.canDeleteLibrarian(id));
            model.addAttribute("librarianLoans", librarianLoans);
            return "librarians/view";
        } catch (IllegalArgumentException e) {
            return "redirect:/librarians";
        }
    }

    /**
     * AJAX endpoint для проверки возможности удаления библиотекаря
     *
     * @param id идентификатор библиотекаря для проверки
     * @return строку с результатом проверки: "OK" если удаление возможно, "ERROR: [сообщение]" если невозможно
     */
    @GetMapping("/check-delete/{id}")
    @ResponseBody
    public String checkDelete(@PathVariable Long id) {
        try {
            Librarian librarian = librarianService.getLibrarianById(id);

            if (librarianService.canDeleteLibrarian(id)) {
                return "OK";
            } else {
                return "ERROR: Библиотекарь '" + librarian.getFullName() +
                        "' оформлял выдачи книг. Удалите или переоформите записи о выдаче.";
            }
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}