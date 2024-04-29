package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.exceptions.EmailAlreadyUsedException;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import com.paymybuddy.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    @Autowired
    private UserService        userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConnectionService  connectionService;

    @GetMapping
    public String showSignUpPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping
    public String signUp(User user, Model model, RedirectAttributes redirAttrs) {
        try {
            userService.createUser(user);
            redirAttrs.addFlashAttribute("created", "You can now take full advantage of PayMyBuddy!");
            return "redirect:/login";
        } catch (IllegalArgumentException | EmailAlreadyUsedException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }
}
