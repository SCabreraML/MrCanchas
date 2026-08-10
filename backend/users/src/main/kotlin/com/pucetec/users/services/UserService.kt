package com.pucetec.users.services

import com.pucetec.users.dto.UserRequest
import com.pucetec.users.dto.UserResponse
import com.pucetec.users.entities.User
import com.pucetec.users.exceptions.BlankNameException
import com.pucetec.users.exceptions.DuplicateCognitoIdException
import com.pucetec.users.exceptions.UserNotFoundException
import com.pucetec.users.mappers.toEntity
import com.pucetec.users.mappers.toResponse
import com.pucetec.users.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// Holds the business logic for user profiles.
@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    // Registers a user profile linked to its cognitoId.
    // The cognitoId comes from the token ("sub" claim), not from the body.
    fun createUser(cognitoId: String, request: UserRequest): UserResponse {
        if (request.name.isBlank()) {
            throw BlankNameException("Name cannot be blank")
        }

        // The cognitoId -> profile relation is 1:1: no two profiles
        // for the same Cognito user.
        if (userRepository.existsByCognitoId(cognitoId)) {
            throw DuplicateCognitoIdException("Profile already exists for user $cognitoId")
        }

        val userEntity = request.toEntity(cognitoId)
        val savedUser = userRepository.save(userEntity)
        logger.info("event=user.created | msg=User profile created | userId=${savedUser.id}")
        return savedUser.toResponse()
    }

    fun getAllUsers(): List<UserResponse> {
        logger.info("event=user.list | msg=Listing all users")
        return userRepository.findAll().map { it.toResponse() }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            UserNotFoundException("User $id not found")
        }
        logger.info("event=user.read | msg=User fetched by id | userId=$id")
        return user.toResponse()
    }

    // Core of the microservice: given a cognitoId, returns the linked profile.
    fun getUserByCognitoId(cognitoId: String): UserResponse {
        val user = userRepository.findByCognitoId(cognitoId).orElseThrow {
            UserNotFoundException("No profile found for user $cognitoId")
        }
        logger.info("event=user.read | msg=User fetched by cognitoId")
        return user.toResponse()
    }

    fun updateUser(cognitoId: String, request: UserRequest): UserResponse {
        val user = userRepository.findByCognitoId(cognitoId).orElseThrow {
            UserNotFoundException("No profile found for user $cognitoId")
        }
        if (request.name.isBlank()) {
            throw BlankNameException("Name cannot be blank")
        }
        val updated = User(
            id = user.id,
            cognitoId = user.cognitoId,
            name = request.name,
            email = request.email,
            phone = request.phone
        )
        val saved = userRepository.save(updated)
        logger.info("event=user.updated | msg=User profile updated | userId=${saved.id}")
        return saved.toResponse()
    }

    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException("User $id not found")
        }
        userRepository.deleteById(id)
        logger.info("event=user.deleted | msg=User deleted | userId=$id")
    }
}