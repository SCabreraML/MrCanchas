package com.pucetec.courts_service.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reservations")
class Reservation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    val timeSlot: TimeSlot,

    @Column(name = "owner_user", nullable = false, length = 80)
    val ownerUser: String,
    
    @Column(name = "start_date_time", nullable = false)
    val startDateTime: LocalDateTime,

    @Column(name = "end_date_time", nullable = false)
    val endDateTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: Status = Status.CONFIRMED,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    enum class Status {
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}