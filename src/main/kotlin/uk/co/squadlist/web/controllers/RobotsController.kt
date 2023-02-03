package uk.co.squadlist.web.controllers

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import javax.servlet.http.HttpServletRequest

@Controller
class RobotsController {

    private val content = "User-agent: Yandex\n" +
            "Disallow: /"

    @RequestMapping(value = ["/robots.txt"], method = [RequestMethod.GET])
    fun getRobots(request: HttpServletRequest): ResponseEntity<String> {
        val httpHeaders = HttpHeaders()
        httpHeaders.setContentType(TEXT_PLAIN)
        return ResponseEntity<String>(content, httpHeaders, HttpStatus.OK)
    }

}
