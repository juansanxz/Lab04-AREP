package edu.escuelaing.arem.ASE.app;

import java.io.IOException;

public interface WebService {
    public String handle(HttpRequest req, HttpResponse res) throws IOException;
}
