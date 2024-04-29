package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.constants.Pagination;
import com.paymybuddy.paymybuddy.exceptions.AlreadyABuddyException;
import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.TransferViewModel;
import com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel;
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

import java.util.List;

@Controller
@RequestMapping("/transfer")
public class TransferController {

    @Autowired
    private UserService        userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConnectionService  connectionService;

    @GetMapping
    public String showTransferPage(Model model,
                                   @RequestParam(value = "page", required = false) Integer page,
                                   @RequestParam(value = "size", required = false) Integer size) {
        // Connected user
        User connectedUser = userService.getAuthenticatedUser();

        // Transaction pagination
        int currentPage = page == null ? Pagination.DEFAULT_PAGE : page;
        int pageSize    = size == null ? Pagination.DEFAULT_SIZE : size;

        Page<?> pagedList = transactionService.getPaginatedUserTransactions(
                PageRequest.of(currentPage - 1, pageSize), connectedUser.getId());

        model.addAttribute("pagedList", pagedList);
        model.addAttribute("totalTransactionItems", pagedList.getTotalElements());

        List<UserViewModel> userConnections = connectionService.getUserConnections(connectedUser);

        model.addAttribute("user", connectedUser);
        model.addAttribute("connections", userConnections);
        model.addAttribute("page", "transfer");
        model.addAttribute("transferForm", new TransferViewModel());

        return "transfer";
    }

    @GetMapping("/add-connection")
    public String showAddConnectionPage(Model model) {
        model.addAttribute("page", "add-connection");
        return "add-connection";
    }

    @PostMapping("/add-connection")
    public String addConnection(String email, Model model, RedirectAttributes redirAttrs) {
        try {
            connectionService.createConnectionBetweenTwoUsers(userService.getAuthenticatedUser(),
                                                              email);
            redirAttrs.addFlashAttribute("success", "Congratulations, you have a new Buddy!");
            return "redirect:/transfer";
        } catch (IllegalArgumentException | BuddyNotFoundException | AlreadyABuddyException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/transfer";
        }
    }

    @GetMapping("/pay")
    public String showPayPage(TransferViewModel transferForm, Model model) {
        model.addAttribute("page", "pay");
        return "pay";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam String action, TransferViewModel transferForm, Model model,
                      RedirectAttributes redirAttrs) {
        try {
            model.addAttribute("page", "pay");
            switch (action) {
                case "pay" -> {
                    if (userService.getUserByEmail(transferForm.getPayeeEmail()).isEmpty()) {
                        throw new BuddyNotFoundException(
                                "Buddy with email (" + transferForm.getPayeeEmail() + ") does not exist.");
                    }
                    transactionService.createTransaction(userService.getAuthenticatedUser(),
                                                         userService.getUserByEmail(transferForm.getPayeeEmail()).get(),
                                                         transferForm.getDescription(),
                                                         transferForm.getAmount());
                    redirAttrs.addFlashAttribute("success",
                                                 "You successfully transferred " + transferForm.getAmount() + "€ to " + transferForm.getPayeeEmail());
                }
                case "redirect" -> {
                    model.addAttribute("transferForm", transferForm);
                    model.addAttribute("amountWithFee",
                                       transactionService.calculateAmountWithFee(transferForm.getAmount()).get("amountWithFee")
                                                         .toString());
                    return "pay";
                }
            }
        } catch (Exception e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transfer";
    }
}
