package edu.escuelaing.arem.ASE.app;

@Component
public class HelloControler {


    @GetMapping("/hello")
    public static String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/helloName")
    public static String helloName(String name) {
        return "Greetings from Spring Boot" + name + "!";
    }
}
