package com.smartcampus.config;

import com.smartcampus.entity.Role;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdmin implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapAdmin.class);

    private final AuthService authService;
    private final UserRepository userRepository;
    private final String email;
    private final String name;
    private final String password;

    public BootstrapAdmin(
            AuthService authService,
            UserRepository userRepository,
            @Value("${bootstrap.admin.email:}") String email,
            @Value("${bootstrap.admin.name:Campus Administrator}") String name,
            @Value("${bootstrap.admin.password:}") String password) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            logger.info("No bootstrap admin configured; set BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD to provision one.");
            return;
        }
        if (userRepository.existsByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))) {
            return;
        }
        authService.registerStaff(name, email, password, Role.ADMIN, null);
        logger.info("Created the configured bootstrap administrator account.");
    }
}
