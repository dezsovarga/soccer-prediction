package com.soccerprediction.fixture

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
class AdminFixtureController(
    private val adminFixtureService: AdminFixtureService
) {
    @GetMapping("/leagues/{leagueId}/fixtures")
    fun getFixtures(@PathVariable leagueId: UUID): List<FixtureDto> {
        return adminFixtureService.getFixtures(leagueId).map { it.toDto() }
    }

    @PostMapping("/leagues/{leagueId}/fixtures")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFixture(
        @PathVariable leagueId: UUID,
        @RequestBody request: CreateFixtureRequest
    ): FixtureDto {
        return adminFixtureService.createFixture(leagueId, request).toDto()
    }

    @PutMapping("/fixtures/{fixtureId}")
    fun updateFixture(
        @PathVariable fixtureId: UUID,
        @RequestBody request: UpdateFixtureRequest
    ): FixtureDto {
        return adminFixtureService.updateFixture(fixtureId, request).toDto()
    }

    @DeleteMapping("/fixtures/{fixtureId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFixture(@PathVariable fixtureId: UUID) {
        adminFixtureService.deleteFixture(fixtureId)
    }

    @PutMapping("/fixtures/{fixtureId}/result")
    fun enterResult(
        @PathVariable fixtureId: UUID,
        @RequestBody request: EnterResultRequest
    ): FixtureDto {
        return adminFixtureService.enterResult(fixtureId, request).toDto()
    }
}
