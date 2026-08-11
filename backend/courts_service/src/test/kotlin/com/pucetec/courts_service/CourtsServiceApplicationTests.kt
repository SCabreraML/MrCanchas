package com.pucetec.courts_service

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Disabled("Desactivado para omitir la carga completa del contexto de Spring en pruebas unitarias")
class CourtsServiceApplicationTests {

	@Test
	fun contextLoads() {
	}

}