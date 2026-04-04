package com.soccerprediction.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.slf4j.LoggerFactory
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val customOAuth2UserService: CustomOAuth2UserService,
    @Value("\${app.frontend-url}") private val frontendUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/api/auth/**").permitAll()
                it.requestMatchers("/api/admin/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }
            .oauth2Login {
                it.authorizationEndpoint { ae ->
                    ae.baseUri("/api/auth/oauth2/authorize")
                }
                it.redirectionEndpoint { re ->
                    re.baseUri("/api/auth/callback/*")
                }
                it.userInfoEndpoint { ui ->
                    ui.userService(customOAuth2UserService)
                }
                it.successHandler(oAuth2LoginSuccessHandler)
                it.failureHandler(AuthenticationFailureHandler { _, response, exception ->
                    log.error("OAuth2 login failed: ${exception.message}", exception)
                    response.sendRedirect("$frontendUrl/login?error=auth_failed")
                })
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .logout {
                it.logoutUrl("/api/auth/logout")
                it.logoutSuccessUrl(frontendUrl)
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf(frontendUrl)
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
