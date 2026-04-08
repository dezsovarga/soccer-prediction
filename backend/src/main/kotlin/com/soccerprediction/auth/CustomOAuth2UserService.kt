package com.soccerprediction.auth

import com.soccerprediction.user.User
import com.soccerprediction.user.UserRepository
import com.soccerprediction.user.UserRole
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    @Value("\${app.admin-email}") private val adminEmail: String
) : DefaultOAuth2UserService() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        log.info("Loading OAuth2 user...")
        val oauth2User = super.loadUser(userRequest)
        val email = oauth2User.getAttribute<String>("email")
        log.info("OAuth2 user email: $email, admin email: $adminEmail")
        if (email == null) return oauth2User
        val name = oauth2User.getAttribute<String>("name") ?: email
        val picture = oauth2User.getAttribute<String>("picture")

        val user = userRepository.findByEmail(email)
        val role: UserRole
        if (user != null) {
            if (!user.isActive) {
                log.warn("Login blocked for deactivated user: $email")
                throw OAuth2AuthenticationException(
                    OAuth2Error("account_deactivated", "Your account has been deactivated.", null)
                )
            }
            user.displayName = name
            user.pictureUrl = picture
            userRepository.saveAndFlush(user)
            role = user.role
            log.info("Updated existing user: $email, role: $role")
        } else {
            role = if (email == adminEmail) UserRole.ADMIN else UserRole.USER
            userRepository.saveAndFlush(
                User(email = email, displayName = name, pictureUrl = picture, role = role)
            )
            log.info("Created new user: $email, role: $role")
        }

        val authorities = oauth2User.authorities.toMutableList()
        if (role == UserRole.ADMIN) {
            authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
        }

        return DefaultOAuth2User(authorities, oauth2User.attributes, "sub")
    }
}
