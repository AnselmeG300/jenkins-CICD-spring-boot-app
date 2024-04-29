package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.constants.Pagination;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import com.paymybuddy.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService        userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConnectionService  connectionService;

    @GetMapping
    public String showProfilePage(Model model,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "size", required = false) Integer size) {
        // Connected user
        User connectedUser = userService.getAuthenticatedUser();

        // Connection pagination
        int currentPage = page == null ? Pagination.DEFAULT_PAGE : page;
        int pageSize    = size == null ? Pagination.DEFAULT_SIZE : size;
        Page<?> pagedList =
                connectionService.getPaginatedUserConnections(PageRequest.of(currentPage - 1, pageSize), connectedUser);

        model.addAttribute("pagedList", pagedList);
        model.addAttribute("totalConnectionItems", pagedList.getTotalElements());


        model.addAttribute("user", connectedUser);
        model.addAttribute("balance", connectedUser.getBalance());
        model.addAttribute("page", "profile");

        return "profile";
    }

    @GetMapping("/update-balance")
    public String showUpdateBalancePage(Model model) {
        model.addAttribute("page", "update-balance");
        model.addAttribute("user", userService.getAuthenticatedUser());
        return "update-balance";
    }

    @PostMapping("/update-balance")
    public String updateBalance(@RequestParam String action, double amount, Model model,
                                RedirectAttributes redirAttrs) {
        try {
            String strAmount = String.valueOf(amount);
            switch (action) {
                case "deposit" -> userService.deposit(userService.getAuthenticatedUser(), strAmount);
                case "withdrawal" -> userService.withdraw(userService.getAuthenticatedUser(), strAmount);
            }
            redirAttrs.addFlashAttribute("success",
                                         "Your " + action + " of " + strAmount + "€ was successful!");
        } catch (Exception e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/update-balance";
        }
        return "redirect:/profile";
    }
}
