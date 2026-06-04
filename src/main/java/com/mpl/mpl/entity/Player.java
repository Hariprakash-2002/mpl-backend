package com.mpl.mpl.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "players")
@Data
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name")
    private String playerName;

    private String role;
    private Integer jersey;
    private String nationality;
    private Integer age;

    @Column(name = "buyer_team")
    private String buyerTeam;

    private String status;

    @Column(name = "sold_price")
    private Double soldPrice;

    @Column(name = "base_price")
    private Double basePrice;

    // --- BRIDGING MAPPINGS FOR FRONTEND COMPATIBILITY ---

    @JsonProperty("auctionStatus")
    public String getAuctionStatus() {
        return this.status;
    }

    @JsonProperty("auctionPrice")
    public Double getAuctionPrice() {
        return this.soldPrice;
    }
}