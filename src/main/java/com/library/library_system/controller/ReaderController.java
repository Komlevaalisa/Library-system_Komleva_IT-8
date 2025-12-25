package com.library.library_system.controller;

import com.library.library_system.entity.Reader;
import com.library.library_system.entity.BookLoan;
import com.library.library_system.service.ReaderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/readers")
public class ReaderController {

    @Autowired
    private ReaderService readerService;

    /**
     * Обрабатывает GET-запрос для отображения списка читателей
     *
     * @param search необязательный параметр для поиска читателей
     * @param model объект Model для передачи данных в представление
     * @return имя представления для отображения списка читателей
     */
    @GetMapping
    public String listReaders(@RequestParam(required = false) String search, Model model) {
        List<Reader> readers;
        if (search != null && !search.trim().isEmpty()) {
            readers = readerService.searchReaders(search);
            model.addAttribute("search", search);
        } else {
            readers = readerService.getAllReaders();
        }

        for (Reader reader : readers) {
            List<BookLoan> activeLoans = readerService.getActiveReaderLoans(reader.getReaderId());
            List<BookLoan> allLoans = readerService.getAllReaderLoans(reader.getReaderId());

            reader.setActiveLoansCount(activeLoans.size());
            reader.setTotalLoansCount(allLoans.size());
        }

        model.addAttribute("readers", readers);
        return "readers/list";
    }

    /**
     * Обрабатывает GET-запрос для отображения формы добавления нового читателя
     *
     * @param model объект Model для передачи данных в представление
     * @return имя представления с формой добавления читателя
     */
    @GetMapping("/new")
    public String showAddForm(Model model) {
        Reader reader = new Reader();
        reader.setActiveLoansCount(0);
        reader.setTotalLoansCount(0);
        model.addAttribute("reader", reader);
        return "readers/form";
    }

    /**
     * Обрабатывает GET-запрос для отображения формы редактирования читателя
     *
     * @param id идентификатор читателя для редактирования
     * @param model объект Model для передачи данных в представление
     * @return имя представления с формой редактирования или redirect на список читателей при ошибке
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Reader reader = readerService.getReaderById(id);

            List<BookLoan> activeLoans = readerService.getActiveReaderLoans(id);
            List<BookLoan> allLoans = readerService.getAllReaderLoans(id);

            reader.setActiveLoansCount(activeLoans.size());
            reader.setTotalLoansCount(allLoans.size());

            model.addAttribute("reader", reader);
            return "readers/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/readers";
        }
    }

    /**
     * Обрабатывает POST-запрос для сохранения читателя (создание или обновление)
     *
     * @param reader объект Reader с данными из формы
     * @param result объект BindingResult для валидации данных
     * @param model объект Model для передачи данных в представление
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return имя представления с формой при ошибках или redirect на список читателей при успешном сохранении
     */
    @PostMapping("/save")
    public String saveReader(@Valid @ModelAttribute Reader reader,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "readers/form";
        }

        try {
            readerService.saveReader(reader);
            redirectAttributes.addFlashAttribute("successMessage",
                    reader.getReaderId() == null ?
                            "Читатель успешно добавлен!" :
                            "Данные читателя успешно обновлены!");
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "readers/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    "Произошла ошибка при сохранении данных читателя: " + e.getMessage());
            return "readers/form";
        }

        return "redirect:/readers";
    }

    /**
     * Обрабатывает GET-запрос для удаления читателя
     *
     * @param id идентификатор читателя для удаления
     * @param redirectAttributes атрибуты для передачи сообщений после redirect
     * @return redirect на список читателей
     */
    @GetMapping("/delete/{id}")
    public String deleteReader(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            Reader reader = readerService.getReaderById(id);
            readerService.deleteReaderById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Читатель '" + reader.getFullName() + "' успешно удален!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при удалении читателя: " + e.getMessage());
        }
        return "redirect:/readers";
    }

    /**
     * Обрабатывает GET-запрос для просмотра детальной информации о читателе
     *
     * @param id идентификатор читателя для просмотра
     * @param model объект Model для передачи данных в представление
     * @return имя представления с детальной информацией или redirect на список читателей при ошибке
     */
    @GetMapping("/view/{id}")
    public String viewReader(@PathVariable Long id, Model model) {
        try {
            Reader reader = readerService.getReaderById(id);

            List<BookLoan> activeLoans = readerService.getActiveReaderLoans(id);
            List<BookLoan> allLoans = readerService.getAllReaderLoans(id);

            reader.setActiveLoansCount(activeLoans.size());
            reader.setTotalLoansCount(allLoans.size());

            model.addAttribute("reader", reader);
            model.addAttribute("canDelete", readerService.canDeleteReader(id));
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("allLoans", allLoans);
            return "readers/view";
        } catch (IllegalArgumentException e) {
            return "redirect:/readers";
        }
    }

    /**
     * AJAX endpoint для проверки возможности удаления читателя
     *
     * @param id идентификатор читателя для проверки
     * @return строку с результатом проверки: "OK" если удаление возможно, "ERROR: [сообщение]" если невозможно
     */
    @GetMapping("/check-delete/{id}")
    @ResponseBody
    public String checkDelete(@PathVariable Long id) {
        try {
            Reader reader = readerService.getReaderById(id);

            if (readerService.canDeleteReader(id)) {
                return "OK";
            } else {
                List<BookLoan> activeLoans = readerService.getActiveReaderLoans(id);
                if (!activeLoans.isEmpty()) {
                    return "ERROR: У читателя '" + reader.getFullName() + "' есть активные выдачи книг. Сначала верните все книги.";
                } else {
                    List<BookLoan> allLoans = readerService.getAllReaderLoans(id);
                    if (!allLoans.isEmpty()) {
                        return "ERROR: Читатель '" + reader.getFullName() + "' есть в истории выдач. Удалите сначала записи о выдаче.";
                    } else {
                        return "ERROR: Читателя '" + reader.getFullName() + "' нельзя удалить по неизвестной причине.";
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
    }
}