package takesh1.myAPIs.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import takesh1.myAPIs.entity.Role;
import takesh1.myAPIs.entity.SystemUser;
import takesh1.myAPIs.service.SecurityService;
import takesh1.myAPIs.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@RequestMapping(path = "/user")
public class SystemUserController {
    private final SystemUserService systemUserService;
    private final SecurityService securityService;

    @Autowired
    public SystemUserController(SystemUserService systemUserService, SecurityService securityService) {
        this.systemUserService = systemUserService;
        this.securityService = securityService;
    }

    @GetMapping
    public List<SystemUser> getUsers() {
        return systemUserService.getUsers();
    }

    @GetMapping(path = "/{userId}")
    public SystemUser getUser(@PathVariable("userId") UUID userId) {
        return systemUserService.getUser(userId);
    }

    @PostMapping(path = "/add")
    public void addUser(@RequestBody SystemUser user) {
        log.info(user.toString());
        systemUserService.addUser(user);
    }

    @PostMapping(path = "/register")
    public void registerUser(@RequestBody SystemUser user) {
        log.info(user.toString());
        systemUserService.registerUser(user);
    }

    @PostMapping(path = "/verify/mail")
    public ResponseEntity<Map<String, String>> sendMail(@RequestBody Map<String, String> userId) {
        log.info("Verifying, {}", userId.get("userId"));
        UUID id = UUID.fromString(userId.get("userId"));
        SystemUser user = systemUserService.getUser(id);
        return securityService.verifyByMail(user);
    }

    @GetMapping(path = "/delete/{userId}")
    public void deleteUser(@PathVariable("userId") UUID userId) {
        systemUserService.deleteUser(userId);
    }

    @PostMapping(path = "/update/")
    public void updateInfo(@RequestBody(required = false) UpdateUserForm form) {
        systemUserService.updateSystemUser(form.getUserId(), form.getFirstName(), form.getLastName(), form.getPhone(), form.getAddress(), form.getEmail(), form.getRoles());
    }

    @PostMapping(path = "/authorize")
    public void addRoleToUser(@RequestParam String username, @RequestParam String roleName) {
        log.info("User: {}, Role: {}", username, roleName);
        systemUserService.addRoleToUser(username, roleName);
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authHeader.substring("Bearer ".length());
                // Create algorithm
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());

                // Verify the refresh_token
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);

                // Get the username
                String username = decodedJWT.getSubject();
                SystemUser user = systemUserService.getUser(username);

                Map<String, String> tokens = securityService.refreshTokens(request, response, algorithm, user, refresh_token);

                // Set return content
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(403);
                // Send error message in header
                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }

        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}

@Data
class UpdateUserForm {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String roleName;
    private String email;
    private Collection<Role> roles;
}

