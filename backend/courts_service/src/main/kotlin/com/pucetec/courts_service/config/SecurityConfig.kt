package com.pucetec.courts_service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    // Lectura pública de canchas y horarios.
                    .requestMatchers(HttpMethod.GET, "/api/courts", "/api/courts/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/time-slots", "/api/time-slots/**").permitAll()
                    // Escritura de canchas y horarios: solo ADMIN.
                    .requestMatchers(HttpMethod.POST, "/api/courts", "/api/time-slots").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/courts/**", "/api/time-slots/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/courts/**", "/api/time-slots/**").hasRole("ADMIN")
                    // Todo lo demás (reservas, resultados) exige token válido.
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        authoritiesConverter.setAuthorityPrefix("ROLE_")
        authoritiesConverter.setAuthoritiesClaimName("cognito:groups")

        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        return converter
    }
}