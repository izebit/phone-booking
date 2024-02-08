package ru.izebit.mobile.booking;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.izebit.mobile.booking.service.UserService;

@SpringBootApplication
@AllArgsConstructor
public class ApplicationLauncher {
    private final UserService userService;

    @PostConstruct
    public void createDefaultUser() {
        userService.createUser("TestUser", "test");
    }

    public static void main(String[] args) {
        SpringApplication.run(ApplicationLauncher.class, args);
    }
}
