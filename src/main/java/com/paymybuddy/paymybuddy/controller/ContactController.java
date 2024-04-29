package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.viewmodel.ContactViewModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contact")
public class ContactController {

	@GetMapping
	public String showContactPage(Model model) {
		model.addAttribute("page", "contact");
		model.addAttribute("contactForm", new ContactViewModel());
		return "contact";
	}

	@PostMapping
	public String send(ContactViewModel contactForm, Model model) {
		return "redirect:/contact?sent";
	}
}
