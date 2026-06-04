package com.mpl.mpl.controller;


import com.mpl.mpl.entity.Player;

import com.mpl.mpl.repository.PlayerRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("player")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
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
