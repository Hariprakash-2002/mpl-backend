
package com.mpl.mpl.service;

import com.mpl.mpl.entity.Player;
import com.mpl.mpl.entity.Team;
import com.mpl.mpl.repository.PlayerRepository;
import com.mpl.mpl.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuctionService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Transactional
    public Player updatePlayerStatus(Long id, String status, String buyerTeam, Double soldPrice) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        player.setStatus(status);
        player.setBuyerTeam(buyerTeam);
        player.setSoldPrice(soldPrice);
        playerRepository.save(player);

        // Calculate and update spent values for all franchises
        recalculateTeamBudgets();

        return player;
    }

    private void recalculateTeamBudgets() {
        List<Team> teams = teamRepository.findAll();
        List<Player> allPlayers = playerRepository.findAll();

        for (Team team : teams) {
            double totalSpent = allPlayers.stream()
                    .filter(p -> "Sold".equalsIgnoreCase(p.getStatus()) && team.getName().equalsIgnoreCase(p.getBuyerTeam()))
                    .mapToDouble(Player::getSoldPrice)
                    .sum();
            team.setSpent(totalSpent);
            teamRepository.save(team);
        }
    }
}