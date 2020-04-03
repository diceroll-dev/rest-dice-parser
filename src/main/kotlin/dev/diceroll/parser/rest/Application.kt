package dev.diceroll.parser.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleRestDiceParserApplication

fun main(args: Array<String>) {
    runApplication<SimpleRestDiceParserApplication>(*args)
}
