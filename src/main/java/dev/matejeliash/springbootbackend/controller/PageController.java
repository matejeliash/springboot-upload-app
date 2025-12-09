package dev.matejeliash.springbootbackend.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
Provies all basics pages that user can access in browser,
every method returns name of thymeleaf template from resources/templates

 */
@Controller
public class PageController {


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


   @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Upload page
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload"; // resolves to upload.html in templates/
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

}
