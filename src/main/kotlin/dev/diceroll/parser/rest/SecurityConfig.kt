package dev.diceroll.parser.rest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                // Public API: allow anonymous access to the DiceController routes only
                authorize(HttpMethod.GET, "/", permitAll)
                authorize(HttpMethod.GET, "/eval", permitAll)
                authorize(HttpMethod.POST, "/eval", permitAll)
                authorize(HttpMethod.GET, "/roll", permitAll)
                authorize(HttpMethod.POST, "/roll", permitAll)
                // Error dispatches for 400s from the controller's exception handlers
                authorize("/error", permitAll)
                authorize(anyRequest, denyAll)
            }
            // Stateless public API with no cookies or sessions, so CSRF tokens would only block anonymous POSTs
            csrf { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            redirectToHttps {
                // Only redirect when running behind a proxy (e.g. Heroku), so plain HTTP works on localhost
                requestMatchers = arrayOf(RequestHeaderRequestMatcher("X-Forwarded-Proto"))
            }
        }
        return http.build()
    }
}
