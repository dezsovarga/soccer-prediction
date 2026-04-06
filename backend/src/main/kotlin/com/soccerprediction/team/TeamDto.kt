package com.soccerprediction.team

import java.util.UUID

data class TeamDto(
    val id: UUID,
    val name: String,
    val countryCode: String?,
    val logoUrl: String?,
    val groupName: String?
)

data class CreateTeamRequest(
    val name: String,
    val countryCode: String? = null,
    val groupName: String? = null
)

data class UpdateTeamRequest(
    val name: String? = null,
    val countryCode: String? = null,
    val groupName: String? = null
)

fun Team.toDto() = TeamDto(
    id = id,
    name = name,
    countryCode = countryCode,
    logoUrl = logoUrl ?: countryCode?.let { "https://flagcdn.com/w80/${it.lowercase()}.png" },
    groupName = groupName
)
