package com.soccerprediction.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.resource.NoResourceFoundException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleNotFound returns 404 with error response`() {
        val ex = NoResourceFoundException(org.springframework.http.HttpMethod.GET, "test")

        val response = handler.handleNotFound(ex)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Resource not found", response.body?.error)
        assertEquals("NOT_FOUND", response.body?.code)
    }

    @Test
    fun `handleBadRequest returns 400 with error response`() {
        val ex = IllegalArgumentException("Invalid input")

        val response = handler.handleBadRequest(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid input", response.body?.error)
        assertEquals("BAD_REQUEST", response.body?.code)
    }

    @Test
    fun `handleGeneral returns 500 with error response`() {
        val ex = RuntimeException("Something went wrong")

        val response = handler.handleGeneral(ex)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Internal server error", response.body?.error)
        assertEquals("INTERNAL_ERROR", response.body?.code)
    }
}
