package uk.co.squadlist.web.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PingController {

    private final static Logger log = LogManager.getLogger(PingController.class);

    @RequestMapping("/healthz")
    public ResponseEntity<String> ping() {
        log.info("Ping returning ok");
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}
