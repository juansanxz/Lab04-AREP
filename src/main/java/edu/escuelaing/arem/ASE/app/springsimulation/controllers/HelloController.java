package edu.escuelaing.arem.ASE.app.springsimulation.controllers;

import edu.escuelaing.arem.ASE.app.springsimulation.annotations.Component;
import edu.escuelaing.arem.ASE.app.springsimulation.annotations.GetMapping;

@Component
public class HelloController {


    @GetMapping("/hello")
    public static String index() {
        return "Greetings from Spring Boot!";
    }
}
