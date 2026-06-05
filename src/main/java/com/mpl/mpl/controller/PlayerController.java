package com.mpl.mpl.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mpl.mpl.entity.Player;
import com.mpl.mpl.repository.PlayerRepository;

@RestController
@RequestMapping("player")
@CrossOrigin(origins = {"*"})
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    @GetMapping("/all")
    public List<Player> getAllTeams(){
        return playerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Player> getTeamById(@PathVariable Long id){
        return playerRepository.findById(id);
    }

    @PostMapping("/add")
    public ResponseEntity<Player> addTeam(@RequestBody Player player){
        playerRepository.save(player);
        return ResponseEntity.ok(player);
    }
    @PostMapping("/update-player")
    public ResponseEntity<?> updatePlayer(@RequestBody Player payload) {
        // 1. Fetch from DB first to preserve name, age, role, etc.
        Player existing = playerRepository.findById(payload.getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // 2. Update ONLY the auction changes
        existing.setStatus(payload.getStatus());
        existing.setBuyerTeam(payload.getBuyerTeam());
        existing.setSoldPrice(payload.getSoldPrice());

        // 3. Save the updated player back to DB
        playerRepository.save(existing);

        return ResponseEntity.ok(existing);
    }
}
