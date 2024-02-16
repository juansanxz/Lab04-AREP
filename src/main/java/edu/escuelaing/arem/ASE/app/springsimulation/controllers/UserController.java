package edu.escuelaing.arem.ASE.app.springsimulation.controllers;

import edu.escuelaing.arem.ASE.app.repository.User;
import edu.escuelaing.arem.ASE.app.service.UsersService;
import edu.escuelaing.arem.ASE.app.service.UsersServiceImpl;
import edu.escuelaing.arem.ASE.app.springsimulation.annotations.*;

@Component
public class UserController {
    static UsersService usersService = new UsersServiceImpl();

    @GetMapping(value = "/users", contentType = "application/json")
    public static String getAll() {
        return usersService.all().toString();
    }

    @PostMapping("/users")
    public static void newUser(@RequestBody String user) {
        User newUser = new User(user);
        usersService.save(newUser);
    }

    @GetMapping(value = "/users/user", contentType = "application/json")
    public static String getUser(@PathVariable("id") String id) {
        return usersService.findById(id).toString();
    }



}
