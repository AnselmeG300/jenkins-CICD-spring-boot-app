package com.paymybuddy.paymybuddy.model.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionViewModel {
    private Integer id;
    private UserViewModel initializer;
    private UserViewModel receiver;
    private LocalDateTime startingDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionViewModel connectionViewModel = (ConnectionViewModel) o;
        return (id.equals(connectionViewModel.id) &&
                initializer.equals(connectionViewModel.initializer) &&
                receiver.equals(connectionViewModel.receiver) &&
                startingDate.isEqual(connectionViewModel.startingDate));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, initializer, receiver, startingDate);
    }
}
