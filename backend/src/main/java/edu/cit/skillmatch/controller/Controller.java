package edu.cit.skillmatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @GetMapping("")
    public String print() {
        return "Hello Marc Andre C. Dotarot";
    }
}
