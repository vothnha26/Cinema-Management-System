package com.example.cinema.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "room_types")
public class RoomType {
    @Id
    private String id;

    private String name;

    @jakarta.persistence.ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinTable(
        name = "room_type_formats",
        joinColumns = @jakarta.persistence.JoinColumn(name = "room_type_id"),
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "format_id")
    )
    private java.util.Set<Format> supportedFormats = new java.util.HashSet<>();

    public RoomType() {}

    public RoomType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public java.util.Set<Format> getSupportedFormats() { return supportedFormats; }
    public void setSupportedFormats(java.util.Set<Format> supportedFormats) { this.supportedFormats = supportedFormats; }
}
