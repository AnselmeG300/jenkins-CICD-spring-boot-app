package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.ConnectionViewModel;
import com.paymybuddy.paymybuddy.model.viewmodel.TransactionViewModel;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import com.paymybuddy.paymybuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.Objects;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private UserService        userService;
    @Autowired
    private ConnectionService  connectionService;
    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public String showHomePage(Model model) {
        User connectedUser = userService.getAuthenticatedUser();

        ConnectionViewModel mostRecentConnection = connectionService.getConnections()
                                                                    .stream()
                                                                    .filter(connection ->
                                                                                    Objects.equals(connection.getInitializer().getId(), connectedUser.getId())
                                                                                    | Objects.equals(connection.getReceiver().getId(),
                                                                                                     connectedUser.getId()))
                                                                    .max(Comparator.comparing(ConnectionViewModel :: getStartingDate))
                                                                    .orElse(null);
        TransactionViewModel mostRecentTransaction = transactionService.getUserTransactions(connectedUser.getId())
                                                                       .stream()
                                                                       .max(Comparator.comparing(TransactionViewModel :: getDate))
                                                                       .orElse(null);
        model.addAttribute("user", connectedUser);
        model.addAttribute("page", "home");
        model.addAttribute("mostRecentConnection", mostRecentConnection);
        model.addAttribute("mostRecentTransaction", mostRecentTransaction);
        return "home";
    }

}
