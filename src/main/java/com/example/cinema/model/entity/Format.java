package com.example.cinema.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "formats")
public class Format {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // VD: "2D", "3D", "IMAX", "4DX"

    private String description;

    public Format() {}
    public Format(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public Format(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
