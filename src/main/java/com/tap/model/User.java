package com.tap.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", unique = true, nullable = false)
    private String googleId;

    @Column(nullable = false)
    private String email;

    private String name;
    private String picture;

    @Column(name = "free_checks")
    private int freeChecks = 5;

    @Column(name = "paid_checks")
    private int paidChecks = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() { return id; }
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
    public int getFreeChecks() { return freeChecks; }
    public void setFreeChecks(int freeChecks) { this.freeChecks = freeChecks; }
    public int getPaidChecks() { return paidChecks; }
    public void setPaidChecks(int paidChecks) { this.paidChecks = paidChecks; }
}