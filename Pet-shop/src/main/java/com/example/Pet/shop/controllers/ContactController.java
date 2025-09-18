package com.example.Pet.shop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {
    @Autowired
    private JavaMailSender javaMailSender;

    @PostMapping("/contact")
    public String sendMail(@RequestParam String name, @RequestParam String email, @RequestParam String message, Model model) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("yor_email@gmail.com");
            mailMessage.setTo("kovalenkoanton11@gmail.com");
            mailMessage.setSubject("Повідомлення з контактної форми");
            mailMessage.setText("Ім'я: " + name + "\nEmail: " + email + "\n\nПовідомлення:\n " + message);

            javaMailSender.send(mailMessage);
            model.addAttribute("success", "Лист успішно надіслано");
        } catch (Exception e) {
            model.addAttribute("error", "Помилка надсилання");
        }
        return "contact";
    }
}
