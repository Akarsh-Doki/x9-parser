package com.fcrm.fraud.x9parser.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "x9")
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final SecurityUsersConfig securityUsers;

    public SecurityConfig(SecurityUsersConfig securityUsers) {
        this.securityUsers=securityUsers;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder){
        List<UserDetails> users = new ArrayList<>();

        for (SecurityUsersConfig.UserEntry entry : securityUsers.getUsers()) {
            UserDetails user = User.withUsername(entry.getUsername())
                                .password(passwordEncoder.encode(entry.getPassword()))
                                .roles(entry.getRole())
                                .build();
            users.add(user);
            log.info("Loaded user {} with role {}", entry.getUsername(), entry.getRole());
        }

        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**").permitAll()
                .requestMatchers("/no-permission").authenticated()
                .requestMatchers("/", "/parse").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form.permitAll())
            .logout(logout -> logout
                    .logoutSuccessUrl("/login?loggedOut")
                    .permitAll());

        return http.build();
    }

}

