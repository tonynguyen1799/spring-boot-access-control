package com.meta.accesscontrol.config;

import com.meta.accesscontrol.model.Role;
import com.meta.accesscontrol.model.User;
import com.meta.accesscontrol.repository.RoleRepository;
import com.meta.accesscontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-password}")
    private String defaultPassword;

    private static final int BATCH_SIZE = 500;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByUsername("testuser1")) {
            log.info("Test data already exists. Skipping data seeding.");
            return;
        }

        log.info("Seeding large volume of test users...");

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_USER not found. Cannot seed data."));

        String encodedPassword = passwordEncoder.encode(defaultPassword);

        List<User> userBatch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= 1000; i++) {
            User user = new User(
                    "testuser" + i,
                    "testuser" + i + "@meta.com",
                    encodedPassword
            );
            user.setRoles(Set.of(userRole));
            userBatch.add(user);

            if (userBatch.size() == BATCH_SIZE) {
                userRepository.saveAll(userBatch);
                userBatch.clear();
                log.info("Saved a batch of {} users", BATCH_SIZE);
            }
        }

        // Save any remaining users in the last batch
        if (!userBatch.isEmpty()) {
            userRepository.saveAll(userBatch);
            log.info("Saved the final batch of {} users", userBatch.size());
        }

        log.info("Finished seeding 1,000 test users.");
    }
}
