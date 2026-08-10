package com.pucetec.courts_service.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "match_results",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["reservation_id"])
    ]
)
class MatchResult(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    val reservation: Reservation,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MatchStatus = MatchStatus.SCHEDULED,

    @Column(name = "team_a", nullable = false, length = 100)
    var teamA: String,

    @Column(name = "score_a", nullable = false)
    var scoreA: Int,

    @Column(name = "team_b", nullable = false, length = 100)
    var teamB: String,

    @Column(name = "score_b", nullable = false)
    var scoreB: Int,

    @Column(length = 100)
    var winner: String? = null,

    @Column(name = "played_at", nullable = false)
    val playedAt: LocalDateTime = LocalDateTime.now()
) {
    enum class MatchStatus {
        SCHEDULED,
        IN_PROGRESS,
        FINISHED,
        CANCELLED
    }
}