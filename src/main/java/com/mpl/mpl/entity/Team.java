package com.mpl.mpl.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "teams")
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    private Double spent;
}