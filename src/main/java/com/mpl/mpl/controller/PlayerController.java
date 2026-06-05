package com.mpl.mpl.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mpl.mpl.entity.Player;
import com.mpl.mpl.repository.PlayerRepository;

@RestController
@RequestMapping("player")
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerRepository playerRepository;

    // Thread-safe collection holding all active client connections
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private static final String ADMIN_TOKEN_ENV_NAME = "MPL_ADMIN_TOKEN";
    private static final String DEFAULT_TOKEN = "yourSecretPassword123";

    public PlayerController(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    // --- SSE Stream Endpoint ---
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates() {
        SseEmitter emitter = new SseEmitter(0L); // 0 = no timeout, keeps connection open indefinitely
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        // Send initial connected ping to browser
        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));
        } catch (Exception e) {
            this.emitters.remove(emitter);
        }
        return emitter;
    }

    // Broadcasts an event to all live subscriber streams
    private void broadcastUpdate(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                this.emitters.remove(emitter);
            }
        }
    }

    // Helper validating Token inside API Authorization Header
    private boolean isUnauthorized(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return true;
        }
        String headerToken = authHeader.replace("Bearer ", "").trim();
        String secureToken = System.getenv(ADMIN_TOKEN_ENV_NAME);
        if (secureToken == null || secureToken.isEmpty()) {
            secureToken = DEFAULT_TOKEN;
        }
        return !secureToken.equals(headerToken);
    }

    // --- Standard Endpoints ---

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
    public ResponseEntity<?> updatePlayer(
            @RequestBody Player payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (isUnauthorized(authHeader)) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid Admin Token");
        }

        Player existing = playerRepository.findById(payload.getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        existing.setStatus(payload.getStatus());
        existing.setBuyerTeam(payload.getBuyerTeam());
        existing.setSoldPrice(payload.getSoldPrice());

        playerRepository.save(existing);

        // Broadcast push update to public & admin clients
        broadcastUpdate("auction_update", "player_updated");

        return ResponseEntity.ok(existing);
    }

    @PostMapping("/set-upcoming/{id}")
    public ResponseEntity<?> setUpcomingPlayer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (isUnauthorized(authHeader)) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid Admin Token");
        }

        List<Player> allPlayers = playerRepository.findAll();

        // 1. Reset any existing "Upcoming" players back to "Unprocessed"
        for (Player p : allPlayers) {
            if ("Upcoming".equalsIgnoreCase(p.getStatus())) {
                p.setStatus("Unprocessed");
                playerRepository.save(p);
            }
        }

        // 2. Set the targeted player as "Upcoming"
        Player target = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        target.setStatus("Upcoming");
        target.setBuyerTeam("None");
        target.setSoldPrice(0.0);
        playerRepository.save(target);

        // Broadcast push update
        broadcastUpdate("auction_update", "upcoming_set");

        return ResponseEntity.ok(target);
    }

    // --- CSV Batch Uploader Endpoint ---
    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadPlayersCSV(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (isUnauthorized(authHeader)) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid Admin Token");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid CSV file");
        }

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            // Skip CSV Header row
            fileReader.readLine();
            String line;
            int count = 0;

            while ((line = fileReader.readLine()) != null) {
                // Parse standard comma separated values
                String[] data = line.split(",");
                if (data.length >= 6) {
                    Player player = new Player();
                    player.setPlayerName(data[0].trim());
                    player.setRole(data[1].trim());
                    player.setJersey(Integer.parseInt(data[2].trim()));
                    player.setNationality(data[3].trim());
                    player.setAge(Integer.parseInt(data[4].trim()));
                    player.setBasePrice(Double.parseDouble(data[5].trim()));
                    player.setStatus("Unprocessed");
                    player.setBuyerTeam("None");
                    player.setSoldPrice(0.0);

                    playerRepository.save(player);
                    count++;
                }
            }

            // Broadcast final status to redraw tables
            broadcastUpdate("auction_update", "players_uploaded");
            return ResponseEntity.ok("Successfully imported " + count + " players into database!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error importing CSV database: " + e.getMessage());
        }
    }
}