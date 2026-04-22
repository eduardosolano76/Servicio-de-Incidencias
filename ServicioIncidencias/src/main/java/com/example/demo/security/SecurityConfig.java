package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ayuntamiento.security_lib.jwt.JwtAuthenticationEntryPoint;
import com.ayuntamiento.security_lib.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
    private final JwtAuthenticationFilter jwtFilter; 
    private final JwtAuthenticationEntryPoint entryPoint;
    
    public SecurityConfig(JwtAuthenticationFilter jwtFilter, JwtAuthenticationEntryPoint entryPoint) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // =======================================================
                // 🟢 ZONA PÚBLICA (No piden Token JWT)
                // Agrega aquí todas las rutas que cualquiera puede ver.
                // =======================================================
                .requestMatchers(
                    "/api/auth/login",       // Ejemplo: Ruta para iniciar sesión
                    "/api/auth/registro",    // Ejemplo: Ruta para registrarse
                    "/api/incidencias/**"        // Ejemplo: Cualquier cosa dentro de "publico"
                ).permitAll()
                
                // =======================================================
                // 🔴 ZONA PRIVADA (Exigen Token JWT válido obligatoriamente)
                // Cualquier ruta que NO esté en la lista de arriba, cae aquí.
                // =======================================================
                .anyRequest().authenticated() 
            );
            
        // El cadenero de tu librería entra en acción
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
