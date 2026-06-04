package com.mpl.mpl.controller;


import com.mpl.mpl.entity.Team;
import com.mpl.mpl.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
@RequestMapping("/team")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository){
        this.teamRepository = teamRepository;
    }

    @GetMapping("/all")
    public List<Team> getAllTeams(){
        return teamRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Team> getTeamById(@PathVariable Long id){
        return teamRepository.findById(id);
    }

    @PostMapping("/add")
    public ResponseEntity<Team> addTeam(@RequestBody Team team){
        teamRepository.save(team);
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long id){
        Team delTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        teamRepository.delete(delTeam);
        return ResponseEntity.ok("Team deleted successfully");
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<String> updateTeam(@RequestBody Team team, @PathVariable int id){
//        Team oldTeam = teamRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Team not found"));
//
//        oldTeam.setCaptainName(team.getCaptainName());
//        oldTeam.setTeamName(team.getTeamName());
//        oldTeam.setPool(team.getPool());
//
//        teamRepository.save(oldTeam);
//        return ResponseEntity.ok("Team updated successfully");
//
//    }
}
