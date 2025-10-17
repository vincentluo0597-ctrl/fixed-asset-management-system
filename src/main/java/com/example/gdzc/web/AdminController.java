package com.example.gdzc.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "资产管理后台");
        return "admin/index";
    }

    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("title", "供应商管理");
        return "admin/suppliers/index";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("title", "设备分类管理");
        return "admin/categories/index";
    }

    @GetMapping("/equipment")
    public String equipment(Model model) {
        model.addAttribute("title", "设备管理");
        return "admin/equipment/index";
    }

    @GetMapping("/locations")
    public String locations(Model model) {
        model.addAttribute("title", "位置管理");
        return "admin/locations/index";
    }

    @GetMapping("/maintenances")
    public String maintenances(Model model) {
        model.addAttribute("title", "维修管理");
        return "admin/maintenances/index";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("title", "盘库（设备一览）");
        return "admin/inventory/index";
    }

    @GetMapping("/transfers")
    public String transfers(Model model) {
        model.addAttribute("title", "设备调用管理");
        // 页面模板位于 templates/transfers/index.html
        return "transfers/index";
    }
}