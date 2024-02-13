package edu.escuelaing.arem.ASE.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    private static String location = null;
    private static HttpServer _instance = new HttpServer();
    private static String route = "";
    private static Map<String, WebService> services = new HashMap<String, WebService>();
    private static Map<String, Method> springServices = new HashMap<String, Method>();




    private HttpServer() {}

    public static HttpServer getInstance(){
        return _instance;
    }

    public void runServer(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Class<?> c = Class.forName(args[0]);
        if (c.isAnnotationPresent(Component.class)) {
            loadMethods(c);
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;
            outputLine = null;

            boolean firstLine = true;
            boolean readyToReadBody = false;
            String uriStr = "";
            String method = "";
            String requestBody = "";

            while ((inputLine = in.readLine()) != null) {
                if(firstLine){
                    method = inputLine.split(" ")[0];
                    uriStr = inputLine.split(" ")[1];
                    firstLine = false;
                }
                if (method.equals("POST")) {
                    // Lee el encabezado Content-Length para determinar la longitud del cuerpo de la solicitud
                    int contentLength = -1;
                    while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {

                        if (inputLine.startsWith("Content-Length:")) {
                            contentLength = Integer.parseInt(inputLine.split(" ")[1]);
                        }
                    }

                    if (contentLength > 0) {
                        char[] body = new char[contentLength];
                        in.read(body, 0, contentLength);
                        requestBody = new String(body);
                    }
                }


                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            URI fileUri = new URI(uriStr);
            outputLine = httpErrorNotFound();
            String path = fileUri.getPath();

            if (path.startsWith(location)){
                String webUri = path.replace(location, "");
                if (services.containsKey(webUri)) {
                    HttpRequest request = new HttpRequest(method, fileUri, requestBody);
                    HttpResponse response = new HttpResponse();
                    outputLine = services.get(webUri).handle(request, response);
                    if(!outputLine.contains("HTTP/1.1")){
                        if (response.getHeaders().get("Content-Type").equals("application/json")) {
                            outputLine = buildJsonHeader() + outputLine;
                        }
                    }
                } else if (webUri.startsWith("/jpeg")){
                    // When client asks for an image
                    OutputStream outputForImage = clientSocket.getOutputStream();
                    httpRequestImage(webUri, outputForImage);
                    outputLine = null;
                } else if (springServices.containsKey(webUri)) {
                    System.out.println("AQUIIIIIIIIIIIIIIIIIII");
                    if (method.equals("POST")) {
                        HttpRequest request = new HttpRequest(method, fileUri, requestBody);
                        String query = request.getQuery();
                        // extraer query!!!!
                    }
                    Method controllerMethod = springServices.get(webUri);
                    outputLine = buildHTMLHeader() + controllerMethod.invoke(null).toString();
                    System.out.println(outputLine);
                } else {
                    try{
                        outputLine = httpRequestTextFiles(webUri);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private void loadMethods(Class c) throws InvocationTargetException, IllegalAccessException {
        for (Method method : c.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                String springRoute = method.getAnnotation(GetMapping.class).value();
                springServices.put(springRoute, method);

            }
        }
    }

    /**
     * When a file asked is not found
     * @return outputLine to send
     * @throws IOException
     */
    private static String httpErrorNotFound() throws IOException {
        String outputLine = "HTTP/1.1 404 Not Found\r\n"
                + "Content-Type:text/html\r\n"
                + "\r\n";
        Charset charset = Charset.forName("UTF-8");
        Path file = Paths.get("target/classes/public/notFound.html");
        BufferedReader reader = Files.newBufferedReader(file, charset);
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            outputLine = outputLine + line + "\r\n";;
        }
        return outputLine;

    }

    /**
     * Looks for an image file and returns it
     * @param requestedFile the image requested
     * @param outputStream  the stream where the image is going to be sent
     * @throws IOException
     */
    public static void httpRequestImage(String requestedFile, OutputStream outputStream) throws IOException {
        Path file = Paths.get("target/classes" + location + requestedFile);
        byte[] buffer = new byte[1024]; // Tamaño del buffer
        try (InputStream inputStream = Files.newInputStream(file)) {
            String header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type:image/jpeg\r\n" +
                    "Content-Length: " + Files.size(file) + "\r\n" +
                    "\r\n";
            outputStream.write(header.getBytes()); // Envía los encabezados
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); // Escribir el buffer al OutputStream
            }
        }
    }

    /**
     * Looks for the text file requested
     * @param requestedFile the text file requested
     * @return outputLine the String that shows what the client requested
     * @throws IOException
     */
    public static String httpRequestTextFiles(String requestedFile) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        Path file = Paths.get("target/classes/" + location + requestedFile);
        BufferedReader reader = Files.newBufferedReader(file, charset);
        String line = null;
        String outputLine = null;
        String extension = requestedFile.split("\\.")[1];

        if (extension.equals("html")) {
           outputLine = buildHTMLHeader();
        } else if (extension.equals("js")) {
            outputLine = buildJsHeader();
        } else if (extension.equals(("css"))){
            outputLine = buildCssHeader();
        }


        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            outputLine = outputLine + line + "\r\n";;
        }

        return outputLine;

    }

    public static void get(String r, WebService s) {
        services.put(r, s);
    }


    public static String buildHTMLHeader () {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:text/html; charset=utf-8\r\n"
                + "\r\n";
        return header;
    }

    public static String buildJsHeader () {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:application/javascript; charset=utf-8\r\n"
                + "\r\n";
        return header;
    }

    public static String buildJsonHeader () {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:application/json; charset=utf-8\r\n"
                + "\r\n";
        return header;
    }

    public static String buildCssHeader () {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:text/css; charset=utf-8\r\n"
                + "\r\n";
        return header;
    }

    public static String httpResponseCreated() {
        String response = "HTTP/1.1 201 Created\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n";
        return response;
    }

    public static void location(String fileLocation) {
        location = fileLocation;
    }

    public static void post(String r, WebService s) {
        services.put(r, s);
    }

}