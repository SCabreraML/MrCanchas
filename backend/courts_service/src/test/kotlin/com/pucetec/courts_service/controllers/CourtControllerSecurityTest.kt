package com.pucetec.courts_service.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.security.web.FilterChainProxy

@SpringBootTest
class CourtControllerSecurityTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var springSecurityFilterChain: FilterChainProxy

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    private lateinit var mockMvc: MockMvc

    private val courtJson = """
        { "name": "Court A", "sport": "Tennis", "location": "Zone 1", "available": true }
    """.trimIndent()

    private fun buildMockMvc(): MockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .addFilters<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurityFilterChain)
            .build()

    @Test
    fun `POST courts without token returns 401`() {
        mockMvc = buildMockMvc()
        mockMvc.perform(
            post("/courts/api/courts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courtJson)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST courts with USER role returns 403`() {
        mockMvc = buildMockMvc()
        mockMvc.perform(
            post("/courts/api/courts")
                .with(jwt().authorities(SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(courtJson)
        ).andExpect(status().isForbidden)
    }
}