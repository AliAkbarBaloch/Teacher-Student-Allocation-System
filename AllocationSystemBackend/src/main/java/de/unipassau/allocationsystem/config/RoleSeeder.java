package de.unipassau.allocationsystem.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seed("ADMIN", "System administrator");
        seed("USER", "Regular user");
        seed("MODERATOR", "Moderator user");
    }

    private void seed(String title, String description) {
        roleRepository.findByTitle(title).orElseGet(() -> {
            Role role = new Role();
            role.setTitle(title);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }
}
