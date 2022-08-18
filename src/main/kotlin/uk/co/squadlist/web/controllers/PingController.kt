package uk.co.squadlist.web.controllers

import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PingController {

    private val log = LogManager.getLogger(PingController::class.java)

    @GetMapping("/healthz")
    fun ping(): ResponseEntity<String> {
        log.debug("Ping returning ok")
        return ResponseEntity("ok", HttpStatus.OK)
    }

}