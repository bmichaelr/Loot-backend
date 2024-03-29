//package com.loot.server.socket.logic.impl;
//
//import com.loot.server.domain.cards.Card;
//import com.loot.server.socket.logic.IPlayerHandler;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@AllArgsConstructor
//@Data
//public class PlayerHandler implements IPlayerHandler {
//
//    private List<PlayerDto> playersInRoom;
//    private List<PlayerDto> playersInRound;
//    private List<PlayerDto> readyPlayers;
//    private int playerTurnIndex;
//
//    Map<PlayerDto, List<Card>> playedCards;
//    Map<PlayerDto, Integer> numberOfWins;
//
//    private int numberOfPlayersForGame = 4;
//    private int numberOfWinsNeeded = 3;
//
//    public PlayerHandler() {
//        playersInRoom = new ArrayList<>();
//        playersInRound = new ArrayList<>();
//        readyPlayers = new ArrayList<>();
//    }
//
//    @Override
//    public void addPlayer(PlayerDto player) {
//        playersInRoom.add(player);
//    }
//
//    @Override
//    public Boolean hasNextPlayer() {
//        return !playersInRound.isEmpty();
//    }
//
//    @Override
//    public PlayerDto getNextPlayer() {
//        PlayerDto player = playersInRound.get(playerTurnIndex);
//        if(++playerTurnIndex >= playersInRound.size()) {
//            playerTurnIndex = 0;
//        }
//
//        return player;
//    }
//
//    @Override
//    public Boolean addWinToPlayer(PlayerDto player) {
//        int currentNumberOfWins = numberOfWins.get(player) + 1;
//        if(currentNumberOfWins == numberOfWinsNeeded) {
//            return true;
//        }
//
//        numberOfWins.put(player, currentNumberOfWins);
//        return false;
//    }
//
//    @Override
//    public void addPlayedCard(PlayerDto player, Card card) {
//        playedCards.get(player).add(card);
//    }
//
//    @Override
//    public Boolean readyUp(PlayerDto player) {
//        playersInRoom.forEach(playerDto -> {
//            if(playerDto.getId().equals(player.getId())){
//                playerDto.setReady(true);
//            }
//        });
//
//        if(!readyPlayers.contains(player)){
//            readyPlayers.add(player);
//        }
//        if(readyPlayers.size() == numberOfPlayersForGame) {
//            startNewRound();
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void removePlayerFromRound(PlayerDto player) {
//        playersInRound.remove(player);
//    }
//
//    @Override
//    public void startNewRound() {
//        // TODO : For the future, the starting player should be the one who just won, not the host
//        playerTurnIndex = 0;
//        playedCards = new HashMap<>();
//        numberOfWins = new HashMap<>();
//        for(PlayerDto readyPlayer : readyPlayers) {
//            playedCards.put(readyPlayer, new ArrayList<>());
//            numberOfWins.put(readyPlayer, 0);
//        }
//    }
//
//}
