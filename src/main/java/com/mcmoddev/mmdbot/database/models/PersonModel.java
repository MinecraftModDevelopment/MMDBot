package com.mcmoddev.mmdbot.database.models;

public class PersonModel {
    private int id;
    private String name;

    public PersonModel(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
