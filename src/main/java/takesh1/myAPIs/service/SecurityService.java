package takesh1.myAPIs.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import takesh1.myAPIs.entity.Role;
import takesh1.myAPIs.entity.SystemUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SecurityService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}") private String sender;

    public Map<String, String> getTokens(HttpServletRequest request, Algorithm algorithm, User user){
        // Access token
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

        // Refresh token -> extends access token time
        String refresh_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);

        return tokens;
    }

    public Map<String, String> refreshTokens(HttpServletRequest request, HttpServletResponse response, Algorithm algorithm, SystemUser user, String refresh_token){
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10*60*1000))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .sign(algorithm);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        return tokens;
    }

    public ResponseEntity<Map<String, String>> verifyByMail(SystemUser systemUser){
        Random r = new Random();
        Map<String, String> code = new HashMap<>();
        code.put("Verify", String.valueOf(r.nextInt(8999)+1000));
        log.info("Code is: {}", code);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("trunght.yrc@gmail.com");
        mailMessage.setTo(systemUser.getEmail());
        mailMessage.setText("Your verification code is " + code.get("Verify"));
        mailMessage.setSubject("Verification code");

        try{
            javaMailSender.send(mailMessage);
        } catch (MailException e){
            log.info("Error: {}", e.getMessage());
        }

        return new ResponseEntity<>(code, HttpStatus.OK);
    }
}
