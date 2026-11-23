package dev.diceroll.parser.rest

import jakarta.servlet.Filter
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    @Qualifier("springSecurityFilterChain")
    lateinit var securityFilterChain: Filter

    @Test
    fun `no redirect without X-Forwarded-Proto header`() {
        val response = filter(MockHttpServletRequest("GET", "/"))
        assertNull(response.redirectedUrl)
    }

    @Test
    fun `redirects to https when X-Forwarded-Proto header is present`() {
        val request = MockHttpServletRequest("GET", "/").apply {
            addHeader("X-Forwarded-Proto", "http")
        }
        val response = filter(request)
        assertEquals(302, response.status)
        assertEquals("https://localhost/", response.redirectedUrl)
    }

    @Test
    fun `anonymous POST is allowed`() {
        val request = MockHttpServletRequest("POST", "/eval").apply {
            contentType = "application/x-www-form-urlencoded"
            addParameter("dice", "2d6")
        }
        val chain = MockFilterChain()
        val response = MockHttpServletResponse()
        securityFilterChain.doFilter(request, response, chain)
        assertEquals(200, response.status)
        assertNotNull(chain.request, "request should reach the controller")
    }

    @ParameterizedTest
    @CsvSource("GET,/", "GET,/eval", "POST,/eval", "GET,/roll", "POST,/roll", "GET,/error")
    fun `anonymous access is allowed to API routes`(method: String, path: String) {
        val response = filter(MockHttpServletRequest(method, path))
        assertEquals(200, response.status)
    }

    @ParameterizedTest
    @CsvSource("GET,/other", "POST,/", "PUT,/eval", "DELETE,/roll", "GET,/eval/extra")
    fun `anonymous access is denied to other routes`(method: String, path: String) {
        val response = filter(MockHttpServletRequest(method, path))
        assertEquals(403, response.status)
    }

    private fun filter(request: MockHttpServletRequest): MockHttpServletResponse {
        val response = MockHttpServletResponse()
        securityFilterChain.doFilter(request, response, MockFilterChain())
        return response
    }
}
