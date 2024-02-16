package edu.escuelaing.arem.ASE.app.sparksimulation;

import edu.escuelaing.arem.ASE.app.sparksimulation.HttpRequest;
import edu.escuelaing.arem.ASE.app.sparksimulation.HttpResponse;

import java.io.IOException;

public interface WebService {
    public String handle(HttpRequest req, HttpResponse res) throws IOException;
}
