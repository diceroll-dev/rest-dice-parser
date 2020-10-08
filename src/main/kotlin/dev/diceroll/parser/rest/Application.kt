package dev.diceroll.parser.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.RequestMatcher

@SpringBootApplication
class SimpleRestDiceParserApplication

fun main(args: Array<String>) {
    runApplication<SimpleRestDiceParserApplication>(*args)
}

@Configuration
class WebSecurityConfig : WebSecurityConfigurerAdapter(true) {
    override fun configure(http: HttpSecurity) {
        http.requiresChannel()
                .requestMatchers(RequestMatcher { r -> r.getHeader("X-Forwarded-Proto") != null })
                .requiresSecure()
    }
}