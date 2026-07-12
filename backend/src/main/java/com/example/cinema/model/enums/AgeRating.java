package com.example.cinema.model.enums;

public enum AgeRating {
    P("Mọi độ tuổi"),
    K("Dưới 13 tuổi với người giám hộ"),
    T13("Trên 13 tuổi"),
    T16("Trên 16 tuổi"),
    T18("Trên 18 tuổi");

    private final String description;
    AgeRating(String description) { this.description = description; }
    public String getDescription() { return description; }
}
