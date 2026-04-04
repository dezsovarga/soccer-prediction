package com.soccerprediction.auth

import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication

class OAuth2LoginSuccessHandlerTest {

    private val request = mockk<HttpServletRequest>()
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val authentication = mockk<Authentication>()

    private val handler = OAuth2LoginSuccessHandler(frontendUrl = "http://localhost:5173")

    @Test
    fun `redirects to frontend url after success`() {
        handler.onAuthenticationSuccess(request, response, authentication)

        verify { response.sendRedirect("http://localhost:5173") }
    }
}
