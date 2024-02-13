package edu.escuelaing.arem.ASE.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalRestApiConnection {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static String GET_URL = "http://www.omdbapi.com/?apikey=414f88f2&t=";
    private static ConcurrentHashMap<String, String> moviesSearched = new ConcurrentHashMap<>();

    /**
     * Connection with external REST API
     * @param movieName Name of the movie to be asked
     * @return movie's data
     * @throws IOException
     */
    public static String connectionWithExternalRestApi(String movieName) throws IOException {
        URL obj = new URL(GET_URL + movieName);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        //The following invocation perform the connection implicitly before getting the code
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        StringBuffer response = null;

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("GET request not worked");
        }
        System.out.println("GET DONE");
        return response.toString();
    }

    public static String movieDataService(String resource) throws IOException {

        String movieName = resource.split("=")[1];

        String movieData, outputLine;

        // Checks if the hashmap already has the movie
        if (moviesSearched.containsKey(movieName)) {
            movieData = moviesSearched.get(movieName);
        } else {
            movieData = ExternalRestApiConnection.connectionWithExternalRestApi(movieName);
            moviesSearched.put(movieName, movieData);
        }

        if (movieData.contains("Movie not found")) {
            outputLine = null;
        } else {
            outputLine = movieData;
        }
        return outputLine;
    }

    public static ConcurrentHashMap<String, String> getMoviesSearched() {
        return moviesSearched;
    }
}
