package uk.co.squadlist.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PingController {

    @RequestMapping("/healthz")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<>(
                "ok",
                HttpStatus.OK);
    }

}
