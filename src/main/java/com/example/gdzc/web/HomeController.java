package com.example.gdzc.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        // 默认跳转到后台首页
        return "redirect:/admin";
    }
}