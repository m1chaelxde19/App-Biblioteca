package com.WebBiblioteca.Config;


import com.WebBiblioteca.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UserService userService;
    private final EncodeConfig encodeConfig;
    private final JwtAuthFilter jwtAuthFilter;
    public SecurityConfig(UserService userService,EncodeConfig encodeConfig, JwtAuthFilter jwtAuthFilter) {
        this.userService = userService;
        this.encodeConfig = encodeConfig;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(encodeConfig.passwordEncoder());
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/ADMIN/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("ADMIN","LIBRARIAN")
                        .requestMatchers("/api/user/").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers("/api/reserve/**").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                        .requestMatchers("/api/loan/**").hasAnyRole("ADMIN", "LIBRARIAN", "USER")
                        .requestMatchers("/api/LIBRARIAN/books/actives", "/api/LIBRARIAN/books/inactives","/api/LIBRARIAN/books/book-info/**").hasAnyRole("LIBRARIAN", "ADMIN", "USER")
                        .requestMatchers("/api/LIBRARIAN/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers("/api/loan/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults());
        return httpSecurity.build();
    }
}
