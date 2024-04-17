package com.loot.server.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clientId")
    private UUID uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "uniqueName")
    private String uniqueName;

    @Column(name = "picture")
    private String profilePicture;

    @Column(name = "color")
    private String profileColor;
}
