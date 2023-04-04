package com.OxGames.OxShell.Data;

public class ResImage {
    //private int id;
    private String id;
    private String name;

    public ResImage(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
}
