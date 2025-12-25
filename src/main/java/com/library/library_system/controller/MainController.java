package com.library.library_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    /**
     * Обрабатывает GET-запрос для отображения главной страницы приложения
     *
     * @return имя представления главной страницы (index.html)
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}