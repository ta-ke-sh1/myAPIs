package takesh1.myAPIs.filter;

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import takesh1.myAPIs.service.SecurityService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class AuthenticateFilter extends UsernamePasswordAuthenticationFilter {
    // UsernamePasswordAuthenticationFilter class processes an authentication form submission.
    private final AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // default parameters of login form. See more here:
        // https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        log.info("Username is : {}", username);
        log.info("Password is : {}", password);

        // Get auth token
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        // The identity of the principal being authenticated.
        // In the case of an authentication request with username and password,
        // this would be the username.
        User user = (User) authentication.getPrincipal();

        // Selecting an encrypt algorithm
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        SecurityService securityService = new SecurityService();
        // Get the refresh and access tokens
        Map<String, String> tokens = securityService.getTokens(request, algorithm, user);

        // Send back with json data type
        response.setContentType(APPLICATION_JSON_VALUE);

        // Object mapper serializes Java objects (in this case, the tokens) into JSON string
        // A stream is a sequence of objects that supports various methods
        // which can be pipelined to produce the desired result.
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }
}
