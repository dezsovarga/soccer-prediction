package com.soccerprediction.prediction

import com.soccerprediction.fixture.Fixture
import com.soccerprediction.user.User
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "predictions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "fixture_id"])]
)
class Prediction(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    val fixture: Fixture,

    @Column(name = "home_score", nullable = false)
    var homeScore: Int,

    @Column(name = "away_score", nullable = false)
    var awayScore: Int,

    @Column(name = "points_earned")
    var pointsEarned: Int? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
