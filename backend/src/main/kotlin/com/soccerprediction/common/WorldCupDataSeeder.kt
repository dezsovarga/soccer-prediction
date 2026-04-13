package com.soccerprediction.common

import com.soccerprediction.league.LeagueRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Profile("!test")
class WorldCupDataSeeder(
    private val leagueRepository: LeagueRepository,
    private val dataSource: DataSource
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (leagueRepository.findByJoinCode("WC2026") != null) {
            log.info("FIFA World Cup 2026 data already exists, skipping seed")
            return
        }

        log.info("Seeding FIFA World Cup 2026 data...")
        dataSource.connection.use { connection ->
            ScriptUtils.executeSqlScript(
                connection,
                ClassPathResource("db/seed-worldcup-2026.sql")
            )
        }
        log.info("FIFA World Cup 2026 data seeded successfully: 48 teams, 72 fixtures, 48 standings")
    }
}
