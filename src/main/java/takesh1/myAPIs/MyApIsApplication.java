package takesh1.myAPIs;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import takesh1.myAPIs.entity.Role;
import takesh1.myAPIs.entity.SystemUser;
import takesh1.myAPIs.repository.SystemUserRepository;
import takesh1.myAPIs.repository.RoleRepository;
import takesh1.myAPIs.service.SystemUserService;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
@AllArgsConstructor
public class MyApIsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApIsApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner commandLineRunner(SystemUserRepository systemUserRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, SystemUserService systemUserService) {
        return args -> {
            String password = passwordEncoder.encode("12345");
            SystemUser c1 = new SystemUser("trung", password, "Trung", "Ha The", "0818161998", "Linh Dam, Hoang Mai, Ha Noi", LocalDate.of(1998, 6, 1));
            SystemUser c2 = new SystemUser("phuong", password, "Phuong", "Hoang Ha", "0984673837", "Van Cao, Hai Phong", LocalDate.of(1999, 10, 25));
            SystemUser c3 = new SystemUser("duc", password, "Duc", "Trinh Minh", "0942373456", "Tran Van Binh, Ha Noi", LocalDate.of(2001, 8, 17));
            SystemUser c4 = new SystemUser("phong", password, "Phong", "Pham Thanh", "0912731234", "Giai Phong, Hai Ba Trung, Ha Noi", LocalDate.of(2002, 12, 4));

            systemUserRepository.saveAll(
                    List.of(c1, c2, c3, c4)
            );

            Role r1 = new Role("ROLE_USER");
            Role r2 = new Role("ROLE_ADMIN");
            Role r3 = new Role("ROLE_SUPER_ADMIN");
            Role r4 = new Role("ROLE_MANAGER");

            roleRepository.saveAll(
                    List.of(r1, r2, r3, r4)
            );

            systemUserService.addRoleToUser("trung", "ROLE_ADMIN");
            systemUserService.addRoleToUser("trung", "ROLE_USER");
            systemUserService.addRoleToUser("phuong", "ROLE_USER");
            systemUserService.addRoleToUser("duc", "ROLE_MANAGER");
            systemUserService.addRoleToUser("phong", "ROLE_ADMIN");
        };
    }
}
