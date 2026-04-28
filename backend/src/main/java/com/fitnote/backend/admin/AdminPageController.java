package com.fitnote.backend.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping({"/admin", "/admin/"})
    public String adminPage() {
        return "redirect:/admin/index.html";
    }

    @GetMapping({"/admin/big-screen", "/admin/big-screen/"})
    public String bigScreen() {
        return "redirect:/admin/big-screen.html";
    }
}
