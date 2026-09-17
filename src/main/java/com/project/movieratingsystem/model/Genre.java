package com.project.movieratingsystem.model;

public enum Genre {
    ACTION("Akcja"),
    COMEDY("Komedia"),
    DRAMA("Dramat"),
    THRILLER("Thriller"),
    SCI_FI("Science Fiction"),
    CRIME("Kryminał"),
    HORROR("Horror");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
