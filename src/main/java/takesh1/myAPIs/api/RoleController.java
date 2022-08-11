package takesh1.myAPIs.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import takesh1.myAPIs.entity.Role;
import takesh1.myAPIs.service.RoleService;

import java.util.List;

@RestController
@RequestMapping(path = "/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping(path = "/add")
    public void registerUser(@RequestBody Role role) {
        roleService.addRole(role);
    }

    @GetMapping
    public List<Role> getRoles() {
        return roleService.getRoles();
    }

}
