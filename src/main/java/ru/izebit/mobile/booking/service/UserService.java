package ru.izebit.mobile.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.izebit.mobile.booking.service.exception.EmptyPasswordException;
import ru.izebit.mobile.booking.service.exception.EmptyUsernameException;
import ru.izebit.mobile.booking.service.exception.UsernameIsAlreadyUsedException;

@Service
@AllArgsConstructor
public class UserService {
    private static final String DEFAULT_ROLE = "QA";
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsManager userDetailsService;

    public void createUser(final String username, final String password)
            throws UsernameIsAlreadyUsedException, EmptyPasswordException, EmptyUsernameException {

        if (!StringUtils.hasText(username))
            throw new EmptyUsernameException();
        if (!StringUtils.hasText(password))
            throw new EmptyPasswordException();
        if (userDetailsService.userExists(username))
            throw new UsernameIsAlreadyUsedException();

        userDetailsService.createUser(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(DEFAULT_ROLE)
                .build()
        );
    }
}
