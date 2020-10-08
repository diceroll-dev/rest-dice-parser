package dev.diceroll.parser.rest

import dev.diceroll.parser.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.net.URI
import javax.servlet.http.HttpServletRequest

@RestController
class DiceController(@Value("#{environment['home-redirect-uri']}") val redirectUri: String) {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Could not parse dice expression") // 400
    @ExceptionHandler(ParseException::class)
    fun parseException() { }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Could not process request") // 400
    @ExceptionHandler(value=[HttpMessageNotReadableException::class, IllegalArgumentException::class])
    fun badRequest() { }

    @GetMapping(path = ["/"], produces = [MediaType.TEXT_HTML_VALUE])
    fun home(): ResponseEntity<Any> {
        val headers = HttpHeaders()
        headers.location = URI.create(redirectUri)
        return ResponseEntity<Any>(headers, HttpStatus.MOVED_PERMANENTLY)
    }

    @GetMapping("/")
    fun help(request: HttpServletRequest, @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) userAgent: String?): String {
        val requestUrl: String = "/*$".toRegex().replace(request.requestURL.toString(), "")
        val helpText = """
            Welcome to the Dice Parser!!
            
            Roll dice by making a request:
            """.trimIndent()

        if (userAgent != null) {
            val agent: Agent? = fromUserAgent(userAgent)

            if (agent != null) {
                if (Agents.CURL.id == agent.id) {
                    return helpText + curlHelp(requestUrl)
                } else if (Agents.HTTPIE.id == agent.id) {
                    return helpText + httpieHelp(requestUrl)
                }
            }
        }

        return helpText + genericHelp(requestUrl)
    }

    @GetMapping("/eval")
    fun eval(@RequestParam("dice") dice: String): Int {
        return roll(dice)
    }

    // http --form http://localhost:8080/eval file@dice.txt
    @PostMapping(path = ["/eval"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun evalUploadFile(@RequestParam("file") file: MultipartFile): Int {
        val dice = file.inputStream.bufferedReader().use(BufferedReader::readText)
        return roll(dice)
    }

    // http --form POST http://localhost:8080/eval dice=2d6
    @PostMapping(path = ["/eval"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun evalPostForm(@RequestParam("dice", required = false) dice: String): Int {
        return roll(dice)
    }

    // echo "2d20+1" | http POST http://localhost:8080/eval
    @PostMapping(path = ["/eval"])
    fun evalPostBody(@RequestBody dice: String): Int {
        return roll(dice)
    }


    @GetMapping(path = ["/roll"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun describe(@RequestParam("dice") dice: String): ResultTree {
        return detailedRoll(dice)
    }

    @GetMapping(path = ["/roll"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun describeText(@RequestParam("dice") dice: String): String {
        return debug(detailedRoll(dice))
    }

    // http --form http://localhost:8080/roll "Accept: text/plain" file@dice.txt
    @PostMapping(path = ["/roll"], produces = [MediaType.TEXT_PLAIN_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
        val dice = file.inputStream.bufferedReader().use(BufferedReader::readText)
        return debug(detailedRoll(dice))
    }

    // http --form POST http://localhost:8080/roll dice=2d6 "Accept: text/plain"
    @PostMapping(path = ["/roll"], produces = [MediaType.TEXT_PLAIN_VALUE], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postFormDescribeText(@RequestParam("dice", required = false) dice: String): String {
        return debug(detailedRoll(dice))
    }

    // echo "2d20+1" | http POST http://localhost:8080/roll "Accept: text/plain"
    @PostMapping(path = ["/roll"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun postDescribeText(@RequestBody dice: String): String {
        return debug(detailedRoll(dice))
    }

    // http --form http://localhost:8080/roll file@dice.txt
    @PostMapping(path = ["/roll"], produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFileJson(@RequestParam("file") file: MultipartFile): ResultTree {
        val dice = file.inputStream.bufferedReader().use(BufferedReader::readText)
        return detailedRoll(dice)
    }

    // http --form POST http://localhost:8080/roll dice=2d6
    @PostMapping(path = ["/roll"], produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postFormDescribeJson(@RequestParam("dice", required = false) dice: String): ResultTree {
        return detailedRoll(dice)
    }

    // echo "2d20+1" | http POST http://localhost:8080/roll
    @PostMapping(path = ["/roll"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun postDescribeJson(@RequestBody dice: String): ResultTree {
        return detailedRoll(dice)
    }


    private fun httpieHelp(url: String): String? {
        return """
    http ${url}/eval\?dice\=2d6

View the details of what was rolled by using `/roll`:
    http ${url}/roll\?dice\=2d6 "Accept: text/plain"

Or get the result in JSON (the default):
    http ${url}/roll\?dice\=2d6 "Accept: application/json"
    
POST works too:
    http --form POST ${url}/eval dice='2d6+2'
"""
    }

    private fun curlHelp(url: String): String? {
        return """
    curl ${url}/eval\?dice\=2d6

View the details of what was rolled by using `/roll`:
    curl ${url}/roll\?dice\=2d6 -H "Accept: text/plain"

Or get the result in JSON (the default):
    curl ${url}/roll\?dice\=2d6 -H "Accept: application/json"

POST works too:
    curl ${url}/eval --data-urlencode 'dice=2d6+2'
"""
    }

    private fun genericHelp(url: String): String? {
        return """
    GET ${url}/eval\?dice\=2d6

View the details of what was rolled by using `/roll`:
    GET ${url}/roll\?dice\=2d6 
    Accept: text/plain

Or get the result in JSON (the default):
    GET ${url}/roll\?dice\=2d6 
    Accept: application/json

POST works too:
    POST ${url}/eval dice=2d6
"""
    }
}