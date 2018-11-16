package uk.co.squadlist.web.controllers

import org.apache.log4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class MehController {

    private val log = Logger.getLogger(LoginController::class.java)

    @GetMapping(value = "/meh")
    fun login(): ModelAndView {
        val mv = ModelAndView("meh")
        mv.addObject("meh", Meh("21323", "2323", things = listOf("dsjkd", "djsjfk2323")))
        return mv
    }



}
