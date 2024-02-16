package edu.escuelaing.arem.ASE.app.sparksimulation;

import edu.escuelaing.arem.ASE.app.ExternalRestApiConnection;
import edu.escuelaing.arem.ASE.app.HttpServer;
import edu.escuelaing.arem.ASE.app.repository.User;
import edu.escuelaing.arem.ASE.app.service.UsersService;
import edu.escuelaing.arem.ASE.app.service.UsersServiceImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;


public class MyWebServices {
    public static void main(String[] args) throws IOException, URISyntaxException {
        //Para Spark
        //HttpServer.location("/public");

        AtomicReference<String> movieData = new AtomicReference<>();
        UsersService usersService = new UsersServiceImpl();

        HttpServer.get("/movie", (req, res) -> {
            String query = req.getQuery();
            movieData.set(ExternalRestApiConnection.movieDataService(query));
            res.setType("text/html");
            return HttpServer.httpRequestTextFiles("/movieInfo.html");
        });

        HttpServer.get("/movieData", (req, res) -> {
            res.setStatus(200);
            res.setType("application/json");
            return movieData.get();
        });

        HttpServer.get("/allUsers", (req, res) -> {
            res.setStatus(200);
            res.setType("application/json");
            return usersService.all().toString();
        });

        HttpServer.post("/createUsers", (req, res) -> {
            res.setStatus(201);
            res.setType("text/html");
            String userString = req.getBody();
            usersService.save(new User(userString));
            return HttpServer.httpResponseCreated();
        });
        as

        //HttpServer.setFromCommandLine(true);


        try {
            HttpServer.getInstance().runServer(args);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
