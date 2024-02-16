package edu.escuelaing.arem.ASE.app;

import edu.escuelaing.arem.ASE.app.springsimulation.annotations.*;
import edu.escuelaing.arem.ASE.app.sparksimulation.HttpRequest;
import edu.escuelaing.arem.ASE.app.sparksimulation.HttpResponse;
import edu.escuelaing.arem.ASE.app.sparksimulation.WebService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {


    private static String location = null;
    private static HttpServer _instance = new HttpServer();
    private static boolean locationSetted = false;
    private static String route = "";
    private static Map<String, WebService> services = new HashMap<String, WebService>();
    private static Map<String, Method> springGetServices = new HashMap<String, Method>();
    private static Map<String, Method> springPostServices = new HashMap<String, Method>();
    private static final String folderPath = "target/classes/edu/escuelaing/arem/ASE/app/springsimulation/controllers";

    private HttpServer() {}

    public static HttpServer getInstance(){
        return _instance;
    }

    public void runServer(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        if(args.length != 0) {
            Class<?> c = Class.forName(args[0]);
            if (c.isAnnotationPresent(Component.class)) {
                loadMethods(c);
            }
        } else {
            List<String> classNames = getClassNames(folderPath);
            List<Class<?>> loadedClasses = loadClasses(classNames);
            for (Class<?> loadClass : loadedClasses) {
                if (loadClass.isAnnotationPresent(Component.class)) {
                    loadMethods(loadClass);
                }
            }

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
                    System.out.println("Received: " + inputLine);
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

            if(locationSetted) {
                path = path.replace(location, "");
            }

            if (services.containsKey(path)) {
                HttpRequest request = new HttpRequest(method, fileUri, requestBody);
                HttpResponse response = new HttpResponse();
                outputLine = services.get(path).handle(request, response);
                if(!outputLine.contains("HTTP/1.1")){
                    if (response.getHeaders().get("Content-Type").equals("application/json")) {
                        outputLine = buildJsonHeader() + outputLine;
                    }
                }
            } else if (path.contains("/jpeg") || path.contains("/png")) {
                // When client asks for an image
                OutputStream outputForImage = clientSocket.getOutputStream();
                httpRequestImage(path, outputForImage);
                outputLine = null;
            } else if (method.equals("GET") && springGetServices.containsKey(path)) {
                Method controllerMethod = springGetServices.get(path);
                String contentTypeSpring = controllerMethod.getAnnotation(GetMapping.class).contentType();
                String query = fileUri.getQuery();
                Parameter[] parameters = controllerMethod.getParameters();
                if (contentTypeSpring.equals("application/json")) {
                    outputLine = buildJsonHeader();
                } else {
                    outputLine = buildHTMLHeader();
                }
                if (controllerMethod.getParameters().length == 1) {
                    if (parameters[0].isAnnotationPresent(PathVariable.class)) {
                        outputLine = outputLine + controllerMethod.invoke(null, query.split("=")[1]);
                    }

                } else {
                    outputLine = outputLine + controllerMethod.invoke(null);
                }
                System.out.println(outputLine);

            } else if (method.equals("POST") && springPostServices.containsKey(path)) {
                Method controllerMethod = springPostServices.get(path);
                Parameter[] parameters = controllerMethod.getParameters();
                if (parameters[0].isAnnotationPresent(RequestBody.class)) {
                    controllerMethod.invoke(null, requestBody);
                    outputLine = httpResponseCreated();
                }

            } else {
                try{
                    outputLine = httpRequestTextFiles(path);
                } catch(Exception e){
                    e.printStackTrace();
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
                springGetServices.put(springRoute, method);
            }
            if (method.isAnnotationPresent(PostMapping.class)) {
                String springRoute = method.getAnnotation(PostMapping.class).value();
                springPostServices.put(springRoute, method);
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
        Path file = null;
        if(locationSetted) {
            file = Paths.get("target/classes" + location + requestedFile);
        } else {
            file = Paths.get("target/classes" + requestedFile);
        }

        byte[] buffer = new byte[1024]; // Tamaño del buffer
        String header = null;
        try (InputStream inputStream = Files.newInputStream(file)) {
            if (requestedFile.contains("/jpeg")) {
                header = buildJpegHeader(file);
            } else if (requestedFile.contains("/png")) {
                header = buildPngHeader(file);
            }

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
        Path file = null;
        if(locationSetted) {
            file = Paths.get("target/classes" + location + requestedFile);
        } else {
            file = Paths.get("target/classes" + requestedFile);
        }
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

    public static String buildJpegHeader (Path file) throws IOException {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:image/jpeg\r\n" +
                "Content-Length: " + Files.size(file) + "\r\n" +
                "\r\n";

        return header;
    }

    public static String buildPngHeader (Path file) throws IOException {
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type:image/png\r\n" +
                "Content-Length: " + Files.size(file) + "\r\n" +
                "\r\n";

        return header;
    }

    public static String httpResponseCreated() {
        String response = "HTTP/1.1 201 Created\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n";
        return response;
    }

    public static void location(String fileLocation) {
        locationSetted = true;
        location = fileLocation;
    }

    public static void post(String r, WebService s) {
        services.put(r, s);
    }


    private List<String> getClassNames(String folderPath) {
        List<String> classNames = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    // Quitar la extensión ".class" y reemplazar "/" por "."
                    String className = file.getName().substring(0, file.getName().length() - 6).replace(File.separatorChar, '.');
                    classNames.add("edu.escuelaing.arem.ASE.app.springsimulation.controllers." + className);
                }
            }
        }
        return classNames;
    }

    private static List<Class<?>> loadClasses(List<String> classNames) throws IOException, ClassNotFoundException {
        List<Class<?>> loadedClasses = new ArrayList<>();
        ClassLoader classLoader = new URLClassLoader(new URL[]{new File("").toURI().toURL()});
        for (String className : classNames) {
            loadedClasses.add(Class.forName(className, true, classLoader));
        }
        return loadedClasses;
    }

}