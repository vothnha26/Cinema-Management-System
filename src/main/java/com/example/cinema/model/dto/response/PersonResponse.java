package com.example.cinema.model.dto.response;

public class PersonResponse {
    private String name;
    private String avatarUrl;

    public PersonResponse() {}
    public PersonResponse(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
