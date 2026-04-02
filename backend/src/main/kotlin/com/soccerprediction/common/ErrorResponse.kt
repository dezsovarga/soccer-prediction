package com.soccerprediction.common

data class ErrorResponse(
    val error: String,
    val code: String,
    val details: Any? = null
)
