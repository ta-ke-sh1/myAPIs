package takesh1.myAPIs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import takesh1.myAPIs.entity.Role;
import takesh1.myAPIs.entity.SystemUser;
import takesh1.myAPIs.repository.SystemUserRepository;
import takesh1.myAPIs.repository.RoleRepository;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SystemUserService implements UserDetailsService {
    private final SystemUserRepository systemUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<SystemUser> getUsers() {
        return systemUserRepository.findAll();
    }

    public SystemUser getUser(String username) {
        return systemUserRepository.findByUsername(username);
    }

    public SystemUser getUser(UUID id) {
        return systemUserRepository.findById(id).orElseThrow(
                () -> new IllegalStateException(
                        "Customer does not exist!"
                )
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUser systemUser = getUser(username);
        if (systemUser == null) {
            log.error("User does not exists!");
            throw new UsernameNotFoundException("User does not exists!");
        } else {
            log.error("User found! username: {}", username);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // Add authority for each role of the user
        systemUser.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));

        return new User(systemUser.getUsername(), systemUser.getPassword(), authorities);
    }
    public void addSystemUser(SystemUser c) {
        SystemUser systemUser = getUser(c.getUsername());
        if (systemUser != null) {
            log.error("User already exists!");
            throw new UsernameNotFoundException("User does not exists!");
        }
        c.setPassword(passwordEncoder.encode(c.getPassword()));
        systemUserRepository.save(c);
    }

    public void deleteUser(UUID id) {
        boolean exist = systemUserRepository.existsById(id);
        if (!exist) {
            throw new IllegalStateException("User does not exist!");
        }
        systemUserRepository.deleteById(id);
    }

    public void updateSystemUser(UUID id, String oldPassword, String newPassword, String firstName, String lastName, String phone, String address) {
        SystemUser systemUser = systemUserRepository.findById(id).orElseThrow(
                () -> new IllegalStateException(
                        "Customer with id: " + id + "does not exists"
                )
        );

        String encoded = passwordEncoder.encode(oldPassword);
        if (!Objects.equals(encoded, systemUser.getPassword())) {
            throw new IllegalStateException(
                    "Old password does not match!"
            );
        }

        if (newPassword != null && newPassword.length() > 0 && !Objects.equals(oldPassword, newPassword)) {
            systemUser.setPassword(passwordEncoder.encode(newPassword));
        }

        if (firstName != null && firstName.length() > 0 && !Objects.equals(systemUser.getFirstName(), firstName)) {
            systemUser.setFirstName(firstName);
        }

        if (lastName != null && lastName.length() > 0 && !Objects.equals(systemUser.getLastName(), lastName)) {
            systemUser.setLastName(lastName);
        }

        if (address != null && address.length() > 0 && !Objects.equals(systemUser.getAddress(), address)) {
            systemUser.setAddress(address);
        }

        if (phone != null && phone.length() < 15 && phone.length() > 9 && !Objects.equals(systemUser.getPhone(), phone)) {
            Optional<SystemUser> systemUserOptional = systemUserRepository.findBySystemUserId(id);
            if (systemUserOptional.isPresent()) {
                throw new IllegalStateException("Phone number taken");
            }
            systemUser.setPhone(phone);
        }
    }

    public void addRoleToUser(String username, String roleName) {
        SystemUser systemUser = systemUserRepository.findByUsername(username);
        Role role = roleRepository.findByName(roleName);

        Optional<SystemUser> optUser = Optional.ofNullable(systemUser);
        Optional<Role> optRole = Optional.ofNullable(role);

        if(optUser.isEmpty() || optRole.isEmpty()){
            throw new IllegalStateException("Invalid input!");
        }

        systemUser.getRoles().add(role);
        log.info("Added role: {} to user: {}", roleName, username);
    }

}
