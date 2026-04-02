package com.soccerprediction.auth

import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import com.soccerprediction.user.UserRole
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2LoginSuccessHandler(
    private val userRepository: UserRepository,
    @Value("\${app.admin-email}") private val adminEmail: String,
    @Value("\${app.frontend-url}") private val frontendUrl: String
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauth2User = authentication.principal as OAuth2User
        val email = oauth2User.getAttribute<String>("email") ?: return
        val name = oauth2User.getAttribute<String>("name") ?: email
        val picture = oauth2User.getAttribute<String>("picture")

        val existingUser = userRepository.findByEmail(email)

        if (existingUser != null) {
            existingUser.displayName = name
            existingUser.pictureUrl = picture
            userRepository.save(existingUser)
        } else {
            val role = if (email == adminEmail) UserRole.ADMIN else UserRole.USER
            val newUser = User(
                email = email,
                displayName = name,
                pictureUrl = picture,
                role = role
            )
            userRepository.save(newUser)
        }

        response.sendRedirect(frontendUrl)
    }
}
