package edu.escuelaing.arem.ASE.app;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    HttpServer server = HttpServer.getInstance();

    ExternalRestApiConnection app = new ExternalRestApiConnection();
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testExternalApiConnection() {
        try {
            HttpServer.location("/public");
            String result = ExternalRestApiConnection.movieDataService("/movie?t=Inception");
            assertNotNull(result);
            // Checks if the result is brought from the external API
            assertEquals("{\"Title\":\"Inception\",\"Year\":\"2010\",\"Rated\":\"PG-13\",\"Released\":\"16 Jul 2010\",\"Runtime\":\"148 min\",\"Genre\":\"Action, Adventure, Sci-Fi\",\"Director\":\"Christopher Nolan\",\"Writer\":\"Christopher Nolan\",\"Actors\":\"Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page\",\"Plot\":\"A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.\",\"Language\":\"English, Japanese, French\",\"Country\":\"United States, United Kingdom\",\"Awards\":\"Won 4 Oscars. 159 wins & 220 nominations total\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_SX300.jpg\",\"Ratings\":[{\"Source\":\"Internet Movie Database\",\"Value\":\"8.8/10\"},{\"Source\":\"Rotten Tomatoes\",\"Value\":\"87%\"},{\"Source\":\"Metacritic\",\"Value\":\"74/100\"}],\"Metascore\":\"74\",\"imdbRating\":\"8.8\",\"imdbVotes\":\"2,517,914\",\"imdbID\":\"tt1375666\",\"Type\":\"movie\",\"DVD\":\"20 Jun 2013\",\"BoxOffice\":\"$292,587,330\",\"Production\":\"N/A\",\"Website\":\"N/A\",\"Response\":\"True\"}", ExternalRestApiConnection.connectionWithExternalRestApi("Inception"));
            // Checks the content of the answer
            assertTrue(result.contains("\"Title\":\"Inception\",\"Year\":\"2010\""));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown: " + e.getMessage());
        }
        assertTrue( true );
    }

    /**
     * Rigourous Test :-)
     */
    public void testCache() {
        try {
            HttpServer.location("/public");
            String result = ExternalRestApiConnection.movieDataService("/movie?t=Inception");
            assertNotNull(result);

            String result2 = ExternalRestApiConnection.movieDataService("/movie?t=Inception");
            // Checks if the result was consulted at the cache
            assertEquals("{\"Title\":\"Inception\",\"Year\":\"2010\",\"Rated\":\"PG-13\",\"Released\":\"16 Jul 2010\",\"Runtime\":\"148 min\",\"Genre\":\"Action, Adventure, Sci-Fi\",\"Director\":\"Christopher Nolan\",\"Writer\":\"Christopher Nolan\",\"Actors\":\"Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page\",\"Plot\":\"A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.\",\"Language\":\"English, Japanese, French\",\"Country\":\"United States, United Kingdom\",\"Awards\":\"Won 4 Oscars. 159 wins & 220 nominations total\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_SX300.jpg\",\"Ratings\":[{\"Source\":\"Internet Movie Database\",\"Value\":\"8.8/10\"},{\"Source\":\"Rotten Tomatoes\",\"Value\":\"87%\"},{\"Source\":\"Metacritic\",\"Value\":\"74/100\"}],\"Metascore\":\"74\",\"imdbRating\":\"8.8\",\"imdbVotes\":\"2,517,914\",\"imdbID\":\"tt1375666\",\"Type\":\"movie\",\"DVD\":\"20 Jun 2013\",\"BoxOffice\":\"$292,587,330\",\"Production\":\"N/A\",\"Website\":\"N/A\",\"Response\":\"True\"}", ExternalRestApiConnection.getMoviesSearched().get("Inception"));
            // Checks if the results are the same
            assertEquals(result, result2);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown: " + e.getMessage());
        }
        assertTrue( true );
    }

    /**
     * Rigourous Test :-)
     */
    public void testLoadFilesFromDisk() {
        try {
            HttpServer.location("/public");
            String errorFile = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type:text/html; charset=utf-8\r\n" +
                    "\r\n" +
                    "<!DOCTYPE html>\r\n" +
                    "<html>\r\n" +
                    "<head>\r\n" +
                    "    <title>Error Not found</title>\r\n" +
                    "    <meta charset=\"UTF-8\">\r\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n" +
                    "</head>\r\n" +
                    "<body>\r\n" +
                    "<h1>Error, we could not find that resource</h1>\r\n" +
                    "</body>\r\n" +
                    "\r\n";

            String errorFileFromDisk = server.httpRequestTextFiles("/notFound.html");


            String result = ExternalRestApiConnection.movieDataService("/movie?t=Inception");
            assertNotNull(errorFileFromDisk);

            // Checks if the result was consulted at the cache
            assertEquals(errorFile, errorFileFromDisk);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown: " + e.getMessage());
        }
        assertTrue( true );
    }

}
