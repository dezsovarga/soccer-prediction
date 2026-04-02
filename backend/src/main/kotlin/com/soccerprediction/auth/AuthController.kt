package com.soccerprediction.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @GetMapping("/login")
    fun login(): Map<String, String> {
        return mapOf("redirectUrl" to "/api/auth/oauth2/authorize/google")
    }
}
