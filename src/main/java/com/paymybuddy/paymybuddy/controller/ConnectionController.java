package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.viewmodel.ConnectionViewModel;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/connection")
public class ConnectionController {
    @Autowired
    ConnectionService connectionService;

    /**
     * Lists all connections.
     *
     * @return List of all connections.
     */
    @GetMapping
    public List<ConnectionViewModel> getConnections() {
        return connectionService.getConnections();
    }

    /**
     * Gets a connection by its ID.
     *
     * @param id
     *         connection to find
     *
     * @return Optional connection
     */
    @GetMapping("/{id}")
    public Optional<ConnectionViewModel> getConnectionById(@PathVariable Integer id) {
        return connectionService.getConnectionById(id);
    }
}
