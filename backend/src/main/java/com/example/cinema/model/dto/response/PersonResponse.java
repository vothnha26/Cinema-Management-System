package com.example.cinema.model.dto.response;

public class PersonResponse {
    private Long id;
    private String name;
    private String avatarUrl;

    public PersonResponse() {}
    public PersonResponse(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
    public PersonResponse(Long id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
