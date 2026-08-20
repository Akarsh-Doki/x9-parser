package com.fcrm.fraud.x9parser.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String ADMIN_AUTHORITY = "FCRMADMIN";

    @Value("${x9.security.ad-domain}")
    private String adDomain;

    @Value("${x9.security.ad-url}")
    private String adUrl;

    @Bean
    public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider =
            new ActiveDirectoryLdapAuthenticationProvider(adDomain, adUrl, "dc=fcrm,dc=local");
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, ActiveDirectoryLdapAuthenticationProvider provider) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(provider);
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/css/**").permitAll()
            .requestMatchers("/no-permission").authenticated()
            .requestMatchers("/", "/parse").hasAuthority("FCRMADMIN")
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .successHandler(this::redirectAfterLogin)
            .permitAll())
        .logout(logout -> logout
            .logoutSuccessUrl("/login?loggedOut")
            .permitAll())
        .exceptionHandling(handling -> handling
            .accessDeniedHandler(this::sendToNoPermissionPage));

        return http.build();
    }
    private void redirectAfterLogin(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        if (isAdmin(authentication)) {
            log.info("{} signed in as an admin", authentication.getName());
            response.sendRedirect(request.getContextPath() + "/");
        }

        else {
            log.info("{} signed in without the admin role", authentication.getName());
            response.sendRedirect(request.getContextPath() + "/no-permission");
        }
    }

    // Runs when a signed in user without the Admin role tries to open the parse page directly
    private void sendToNoPermissionPage(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        response.sendRedirect(request.getContextPath() + "/no-permission");
    }

    private boolean isAdmin(Authentication authentication){
        for (GrantedAuthority authority : authentication.getAuthorities()){
            if (authority.getAuthority().equals(ADMIN_AUTHORITY)){
                return true;
            }
        }
        return false;
    }

}

