package com.mpl.mpl.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player player;


    @ManyToOne
    private Team team;

    private Long amount;

    private LocalDateTime bidtime;
}
