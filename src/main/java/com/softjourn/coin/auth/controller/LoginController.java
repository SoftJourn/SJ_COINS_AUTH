package com.softjourn.coin.auth.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @RequestMapping(value="/login", method = RequestMethod.GET)
    public String login(Model model, @RequestParam(required = false) String error) {
        if(error != null)
            model.addAttribute("error", 1);
        return "login";
    }

}
