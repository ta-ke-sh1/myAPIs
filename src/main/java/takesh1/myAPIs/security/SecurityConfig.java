package takesh1.myAPIs.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import takesh1.myAPIs.filter.AuthenticateFilter;
import takesh1.myAPIs.filter.AuthorizeFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthenticateFilter authenticateFilter = new AuthenticateFilter(authenticationManagerBean());
        // override default login url
        authenticateFilter.setFilterProcessesUrl("/login");

        http.csrf().disable(); // prevent cross site restriction policy attacks

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Authorization filter
        // permitALl = allow access to everybody
        http.authorizeRequests().antMatchers("/login/**", "/user/token/refresh", "/user/register", "/user/**").permitAll();

        // hasAnyAuthority -  restrict url access to a type of user
        http.authorizeRequests().antMatchers(GET, "/user/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");
        http.authorizeRequests().antMatchers(POST, "/user/add/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN");

        http.authorizeRequests().antMatchers("/role/add", "/role/delete", "/role/update").hasAnyAuthority("ROLE_SUPER_ADMIN");

        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(authenticateFilter);
        http.addFilterBefore(new AuthorizeFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}


