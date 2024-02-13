package edu.escuelaing.arem.ASE.app.service;

import edu.escuelaing.arem.ASE.app.repository.User;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class UsersServiceImpl implements UsersService{
    Map<String, User> users = new HashMap<String, User>();
    @Override
    public User save(User user) {
        users.put(user.getName(), user);
        return users.get(user.getName());
    }

    @Override
    public List<User> all() {
        return new ArrayList<>(users.values());
    }
}
