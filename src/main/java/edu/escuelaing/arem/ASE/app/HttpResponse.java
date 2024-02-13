package edu.escuelaing.arem.ASE.app;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final String httpVersion = "HTTP/1.1";
    private String status;
    private String body;
    private Map<String, String> headers;

    public HttpResponse () {
        headers = new HashMap<String, String>();
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (status == 200) {
            this.status = String.valueOf(status) + " OK";
        } else if (status == 201) {
            this.status = String.valueOf(status) + " Created";
        }

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

    public void setType(String type) {
        headers.put("Content-Type", type);
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "httpVersion='" + httpVersion + '\'' +
                ", status='" + status + '\'' +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                '}';
    }
}
