package com.example.cinema.model.entity;

import com.example.cinema.model.enums.MovieStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "branch_movies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "movie_id"})
})
public class BranchMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority_level")
    @com.fasterxml.jackson.annotation.JsonProperty("priority")
    private Integer priorityLevel = 1;

    @Column(name = "assigned_at")
    @com.fasterxml.jackson.annotation.JsonProperty("assignedAt")
    private java.time.LocalDateTime assignedAt = java.time.LocalDateTime.now();

    public BranchMovie() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public MovieStatus getStatus() { return status; }
    public void setStatus(MovieStatus status) { this.status = status; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public java.time.LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(java.time.LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
