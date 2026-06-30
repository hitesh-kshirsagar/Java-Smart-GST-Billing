package com.gst.billing.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an application user (admin or staff).
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login username */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** BCrypt-encrypted password */
    @Column(nullable = false)
    private String password;

    /** Role: ADMIN or USER */
    @Column(nullable = false, length = 20)
    private String role;
}
