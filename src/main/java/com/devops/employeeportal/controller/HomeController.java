package com.devops.employeeportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/")
    public String home(Model model){

        model.addAttribute(
        "message",
        "Employee Portal Running through DevOps Pipeline"
        );

        return "index";
    }
}