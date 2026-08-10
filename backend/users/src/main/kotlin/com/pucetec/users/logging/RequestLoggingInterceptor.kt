package com.pucetec.users.logging

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RequestLoggingInterceptor : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        val sub = (auth as? JwtAuthenticationToken)?.token?.subject ?: "anonimo"
        MDC.put("sub", sub)
        log.info("event=http.request | msg=${request.method} ${request.requestURI}")
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        log.info("event=http.response | msg=${response.status} ${request.method} ${request.requestURI}")
        MDC.remove("sub")
    }
}