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
import java.time.LocalDate;
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
        return systemUserRepository.findByUsername(username).orElseThrow(
                () -> new IllegalStateException("User does not exist!")
        );
    }

    public Role getRole(String roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new IllegalStateException("Role does not exist!")
        );
    }

    public SystemUser getUser(UUID id) {
        return systemUserRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("User does not exist!")
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUser systemUser = getUser(username);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // Add authority for each role of the user
        systemUser.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new User(systemUser.getUsername(), systemUser.getPassword(), authorities);
    }

    public void registerUser(SystemUser c) {
        Optional<SystemUser> systemUser = systemUserRepository.findByUsername(c.getUsername());
        if (systemUser.isPresent()) {
            log.error("User already exists!");
            throw new UsernameNotFoundException("User already exists!");
        }
        c.setPassword(passwordEncoder.encode(c.getPassword()));
        Role role = getRole("ROLE_USER");
        c.getRoles().add(role);
        systemUserRepository.save(c);
    }

    public void addUser(String username, String password, String firstName, String lastName, String phone, String address, String email, LocalDate dob, String role) {
        Optional<SystemUser> systemUser = systemUserRepository.findByUsername(username);
        if (systemUser.isPresent()) {
            log.error("User already exists!");
            throw new UsernameNotFoundException("User already exists!");
        }

        SystemUser c = new SystemUser(username, password, firstName, lastName, phone, address, email, dob);

        Optional<Role> roleCheck = roleRepository.findByName(role);
        if (roleCheck.isEmpty()) {
            throw new UsernameNotFoundException("Role does not exists!");
        }

        systemUserRepository.save(c);
        addRoleToUser(username, role);

    }

    public void deleteUser(UUID id) {
        boolean exist = systemUserRepository.existsById(id);
        if (!exist) {
            throw new IllegalStateException("User does not exist!");
        }
        systemUserRepository.deleteById(id);
    }

    public void updateSystemUser(String id, String firstName, String lastName, String phone, String address, String email, LocalDate dob, String role) {
        log.info("{}, {}, {}, {}, {}, {}, {}, {}", id, firstName, lastName, phone, address, email, dob, role);
        SystemUser systemUser = getUser(UUID.fromString(id));

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
            systemUser.setPhone(phone);
        }

        if (email != null && email.length()  > 0 && !Objects.equals(systemUser.getEmail(), email)) {
            systemUser.setEmail(email);
        }

        if (!Objects.equals(systemUser.getDob(), dob)) {
            systemUser.setDob(dob);
        }

        Collection<Role> userRoles = systemUser.getRoles();
        String[] roleNames = role.split(";");
        for(String roleName: roleNames){
            Role r = getRole(roleName);
            if(!userRoles.contains(r)){
                addRoleToUser(systemUser.getUsername(), roleName);
            }
        }

    }

    public void addRoleToUser(String username, String roleName) {
        SystemUser systemUser = getUser(username);
        Role role = getRole(roleName);
        systemUser.getRoles().add(role);
        log.info("Added role: {} to user: {}", roleName, username);
    }

    public void overrideRoleFromUser( SystemUser systemUser, Collection<Role> roles) {
        for (Role role : roles) {
            getRole(role.getName());
        }
        systemUser.setRoles(roles);
        log.info("Override user {} roles", systemUser.getUsername());
    }
}
