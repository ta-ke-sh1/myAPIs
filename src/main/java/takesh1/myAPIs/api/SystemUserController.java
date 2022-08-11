package takesh1.myAPIs.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import takesh1.myAPIs.entity.SystemUser;
import takesh1.myAPIs.service.RoleService;
import takesh1.myAPIs.service.SecurityService;
import takesh1.myAPIs.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public void registerUser(@RequestBody SystemUser user) {
        log.info(user.toString());
        systemUserService.addSystemUser(user);
    }

    @GetMapping(path = "/delete/{userId}")
    public void deleteUser(@PathVariable("userId") UUID userId) {
        systemUserService.deleteUser(userId);
    }

    @PutMapping(path = "/update/{userId}")
    public void updateInfo(@PathVariable("userId") UUID userId,
                           @RequestParam(required = false) String oldPassword,
                           @RequestParam(required = false) String newPassword,
                           @RequestParam(required = false) String firstName,
                           @RequestParam(required = false) String lastName,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String address) {
        systemUserService.updateSystemUser(userId, oldPassword, newPassword, firstName, lastName, phone, address);
    }

    @PostMapping(path = "/authorize")
    public void addRoleToUser(@RequestParam String username, @RequestParam String roleName){
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

