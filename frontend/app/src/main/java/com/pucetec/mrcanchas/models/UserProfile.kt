package com.pucetec.mrcanchas.models

data class UserProfileRequest(
    val name: String,
    val email: String,
    val phone: String
)

data class UserProfileResponse(
    val id: Long,
    val cognitoId: String,
    val name: String,
    val email: String,
    val phone: String
)
