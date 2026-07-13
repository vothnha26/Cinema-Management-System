package com.example.cinema.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "room_types")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String code;

    private String name;

    @jakarta.persistence.ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinTable(
        name = "room_type_formats",
        joinColumns = @jakarta.persistence.JoinColumn(name = "room_type_id"),
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "format_id")
    )
    private java.util.Set<Format> supportedFormats = new java.util.HashSet<>();

    public RoomType() {}

    public RoomType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public java.util.Set<Format> getSupportedFormats() { return supportedFormats; }
    public void setSupportedFormats(java.util.Set<Format> supportedFormats) { this.supportedFormats = supportedFormats; }
}
