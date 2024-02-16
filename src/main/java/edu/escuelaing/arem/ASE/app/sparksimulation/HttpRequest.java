package edu.escuelaing.arem.ASE.app.sparksimulation;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private URI uri;
    private String body;
    private Map<String, String> headers;

    public HttpRequest (String method, URI uri, String body) {
        this.method = method;
        this.uri = uri;
        this.body = body;
        headers = new HashMap<String, String>();

    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URI getUri() {
        return uri;
    }

    public String getQuery() {
        return uri.getQuery();
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", uri=" + uri +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }
}
