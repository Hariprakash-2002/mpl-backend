package com.mpl.mpl.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Player player;

    @ManyToOne
    private Team team;

    private Long soldAmount;

    private LocalDateTime soldTime;
}
