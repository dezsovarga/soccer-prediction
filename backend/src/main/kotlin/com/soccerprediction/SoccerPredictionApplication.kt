package com.soccerprediction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SoccerPredictionApplication

fun main(args: Array<String>) {
    runApplication<SoccerPredictionApplication>(*args)
}
