package edu.escuelaing.arem.ASE.app.repository;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private String lastName;
    private String age;

    public User (String name, String lastName, String age) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
    }

    public User (String data) {
        String[] attributes = data.split("&");
        Map<String, String> values = new HashMap<String, String>();
        for (String attribute : attributes) {
            String[] keyValue = attribute.split("=");
             values.put(keyValue[0], keyValue[1]);
        }
        this.name = values.get("name");
        this.lastName = values.get("lastName");
        this.age = values.get("age");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"lastName\": \"" + lastName + "\", \"age\": " + age + "}";
    }
}

