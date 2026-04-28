package com.fitnote.backend.bootstrap;

import com.fitnote.backend.admin.SysAdmin;
import com.fitnote.backend.admin.SysAdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner seedAdminAccount(SysAdminRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            SysAdmin admin = new SysAdmin();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setNickname("FitNote Admin");
            admin.setRoleCode("ADMIN");
            admin.setStatus(1);
            repository.save(admin);
        };
    }
}
