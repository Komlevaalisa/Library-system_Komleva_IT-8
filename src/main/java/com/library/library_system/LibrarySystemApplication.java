package com.library.library_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Library System.
 * Используется для запуска Spring Boot приложения.
 */
@SpringBootApplication
public class LibrarySystemApplication {

    /**
     * Точка входа в приложение.
     * Запускает Spring Boot приложение с конфигурацией по умолчанию.
     *
     * @param args аргументы командной строки, передаваемые при запуске приложения
     */
    public static void main(String[] args) {
        SpringApplication.run(LibrarySystemApplication.class, args);
    }

}