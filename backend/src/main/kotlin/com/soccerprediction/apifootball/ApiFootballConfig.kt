package com.soccerprediction.apifootball

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory

@Configuration
class ApiFootballConfig(
    @Value("\${apifootball.api-key:}") private val apiKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun apiFootballRestTemplate(): RestTemplate {
        log.info("Configuring API-Football RestTemplate (key present: ${apiKey.isNotBlank()})")
        val restTemplate = RestTemplate()
        restTemplate.uriTemplateHandler = DefaultUriBuilderFactory("https://v3.football.api-sports.io")
        restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            request.headers.set("x-apisports-key", apiKey)
            execution.execute(request, body)
        })
        return restTemplate
    }
}
