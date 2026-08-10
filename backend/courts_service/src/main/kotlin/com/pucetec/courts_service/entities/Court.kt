package com.pucetec.courts_service.entities

import jakarta.persistence.*

@Entity
@Table(name = "courts")
class Court(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 40)
    var sport: String,

    @Column(nullable = false, length = 100)
    var location: String,

    @Column(nullable = false)
    var available: Boolean = true
)
