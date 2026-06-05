package com.mpl.mpl.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mpl.mpl.entity.Player;
import com.mpl.mpl.entity.Team;
import com.mpl.mpl.repository.PlayerRepository;
import com.mpl.mpl.repository.TeamRepository;
import com.mpl.mpl.service.AuctionService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Adjust to match Vite/React URL
public class AuctionController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AuctionService auctionService;

    @GetMapping("/players")
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @GetMapping("/teams")
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @PostMapping("/update-player")
    public ResponseEntity<?> updatePlayer(@RequestBody Map<String, Object> payload) {
        try {
            Long id = Long.valueOf(payload.get("id").toString());
            String status = payload.get("status").toString();
            String buyerTeam = payload.get("buyerTeam").toString();
            Double soldPrice = Double.valueOf(payload.get("soldPrice").toString());

            Player updated = auctionService.updatePlayerStatus(id, status, buyerTeam, soldPrice);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}