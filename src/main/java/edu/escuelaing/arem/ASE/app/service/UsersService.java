package edu.escuelaing.arem.ASE.app.service;

import edu.escuelaing.arem.ASE.app.repository.User;
import java.util.List;

public interface UsersService {
    User save(User user);

    List<User> all();
    User findById(String id);

}
