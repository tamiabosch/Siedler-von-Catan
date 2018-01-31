package network;

import com.google.gson.JsonObject;

/**
 * Created by Tami on 11.12.2016.
 */
public interface Protocol {

    JsonObject startConnectionToClient(String versionString, String protocolString);

    JsonObject startConnectionToServer(String versionString);

    JsonObject assignIDToClient(int id);

    JsonObject answerToClientAction(boolean valid, String errorMessage);

    JsonObject sendChatMessageToServer(String messageFromUser);

    JsonObject sendChatMessageToClients(int user, String messageFromUser);

    JsonObject sendPlayerInfoToServer(String name, String color);

    JsonObject playerIsReadyToServer();

    JsonObject accessDeniedToClient();

    JsonObject sendBoardToClients(JsonObject boardFromModel);

    JsonObject statusUpdateToClient(JsonObject playerObject);

    JsonObject diceNumberToClient(int player, int diceNumber);

    JsonObject profitToClient(int player, JsonObject resource);

    JsonObject sendNewBuildingToClients(JsonObject buildingFromClient);

    JsonObject throwDice();

    JsonObject build(String type, String location);

    JsonObject finishMove();

    JsonObject sendPlayerWinsToClient(String message, int id);

    JsonObject sendExpenseToClient(int id, JsonObject resources);

    JsonObject robberMovedToClient(int id, String location, int idRobbedPlayer);
    JsonObject robberMovedToClient(int id, JsonObject location, int idRobbedPlayer);

    JsonObject submitResourcesToServer(JsonObject resources);

    JsonObject moveRobberToServer(String location, int idRobbedPlayer);

    JsonObject offerHarborTradeToServer(JsonObject offer, JsonObject request);

    JsonObject offerDomesticTradeToServer(JsonObject offer, JsonObject request);

    JsonObject requestDomesticTradeToClient(int id, int tradeId, JsonObject offer, JsonObject request);

    JsonObject acceptOfferToServer(int tradeId);

    JsonObject acceptOfferToClient(int id, int tradeId);

    JsonObject finishTradeToServer(int tradeId, int otherPlayer);

    JsonObject finishTradeToClient(int player, int otherPlayer);

    JsonObject cancelTradeToServer(int tradeId);

    JsonObject cancelTradeToClient(int id,int tradeId);

    JsonObject diceNumberToClient(int player, int[] diceNumber);

    JsonObject playerBoughtDevCard(int id, String developmentCard);

    JsonObject longestRoadToClient(int id);

    JsonObject biggestKnightForceToClient(int id);

    JsonObject lostLongestRoadToClient();

    JsonObject playKnightCardToServer(String location, int destinationId);

    JsonObject playKnightCardToClient(int playerId, String location, int destinationId);

    JsonObject playBuildStreetCardToServer(String street1, String street2);

    JsonObject playBuildStreetCardToClient(int playerId, String street1, String street2);

    JsonObject playYearOfPlentyCardToServer(JsonObject resources);

    JsonObject playYearOfPlentyCardToClient(int playerId, JsonObject resources);

    JsonObject buyDevelopmentCard();

}
