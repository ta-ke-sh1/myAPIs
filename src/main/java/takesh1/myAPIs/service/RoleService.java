package takesh1.myAPIs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import takesh1.myAPIs.entity.Role;
import takesh1.myAPIs.repository.RoleRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public void addRole(Role role){
        log.info("Added role: {}", role.getName());
        roleRepository.save(role);
    }

    public List<Role> getRoles(){
        return roleRepository.findAll();
    }
}
