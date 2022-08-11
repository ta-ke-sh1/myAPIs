package takesh1.myAPIs.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
public class AuthorizeFilter extends OncePerRequestFilter {
    // Filter base class that aims to guarantee a single execution per request dispatch, on any servlet container.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // getServletPath: get the route of the request
        // Servlet Filters are an implementation of the Chain of responsibility design pattern.
        // doFilter(req, res) = continue to the next element in the chain
        if (request.getServletPath().equals("/login") || request.getServletPath().equals("/user/token/refresh")) {
            filterChain.doFilter(request, response);
        } else {
            String authHeader = request.getHeader(AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    // Take the token from the header then subtract the "Token " part
                    String token = authHeader.substring("Bearer ".length());

                    // Create algorithm, must be the same with the encrypt algorithm
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());

                    // Verify token
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);

                    // Get username from the decoded JWT
                    String username = decodedJWT.getSubject();

                    // Extract all the roles.
                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);

                    // Convert roles into authority granting
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    stream(roles).forEach(role -> {
                        authorities.add(new SimpleGrantedAuthority(role));
                    });

                    // Create auth token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // Set auth token
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Continue the chain
                    filterChain.doFilter(request, response);
                } catch (Exception exception) {
                    log.error("Error: {}", exception.getMessage());
                    response.setHeader("error", exception.getMessage());
                    response.setStatus(FORBIDDEN.value());
                    Map<String, String> error = new HashMap<>();
                    error.put("err_msg", exception.getMessage());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
}
