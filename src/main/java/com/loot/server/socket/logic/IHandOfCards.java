package com.loot.server.socket.logic;

public interface IHandOfCards {

  /**
   * called when a player plays a card, determines if the holding card needs to be updated or left alone
   * e.g. did they play the card they just drew or play the one they already had
   * @param power of the played card
   */
  void playedCard(Integer power);

  /**
   * get the card that the person is currently holding
   * @return int of card in hand
   */
  Integer getCardInHand();

  /**
   * method called to draw a card for the player. if they have no card in their hand (e.g. they just discarded) then
   * set the holding card to the drawn card, else we set the drawnCard to the new card
   * @param power of the drawn card
   */
  void drawCard(Integer power);

  /**
   * method called to discard a persons hand (whether for net troll, or loot, or otherwise)
   * @return the power of the card discarded
   */
  Integer discardHand();

  /**
   * Check if the current player has both a card in hand and a drawn card
   * @return true if they have two, false if not
   */
  Boolean hasTwoCards();
}
