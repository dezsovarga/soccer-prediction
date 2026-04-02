package com.soccerprediction.user

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal oauth2User: OAuth2User): ResponseEntity<UserDto> {
        val email = oauth2User.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().build()

        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(user.toDto())
    }
}
