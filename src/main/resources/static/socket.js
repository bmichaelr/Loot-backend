const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/game-websocket'
});

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

let lobbyUpdates;

let connected = false;
// Function that is called when the user is connected to the websocket
function sock_connect(playerId) {
    stompClient.activate();
    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/matchmaking/'+playerId, (lobbyData) => {
            handleMatchmaking(lobbyData);
        });
        stompClient.subscribe("/topic/error/" + playerId, (error) => {
           handleError(error);
        });

        connected = true;
    };
}

// Function that is called when we want to disconnect from the WebSocket
function disconnect() {
    stompClient.deactivate();
    console.log("Disconnected");
}

function sock_createGame(player, name) {
    console.log("Data to send: ", JSON.stringify({playerDto: player}))
    stompClient.publish({
        destination: "/app/createGame",
        body: JSON.stringify({ player: player, roomKey: name })
    });
}

function sock_joinGame(player, roomKey) {
    stompClient.publish({
        destination: "/app/joinGame",
        body: JSON.stringify({ player: player, roomKey: roomKey })
    });
}

function sock_leaveGame(player, roomKey) {
    if(lobbyUpdates) {
        lobbyUpdates.unsubscribe();
        lobbyUpdates = null
    }

    stompClient.publish({
        destination: "/app/leaveGame",
        body: JSON.stringify({ player: player, roomKey: roomKey })
    });

    var lobbyDiv = document.getElementById("lobby-list");
    lobbyDiv.style.display = "none";
    var menuDiv = document.getElementById("div-menu-choices");
    menuDiv.style.display = "contents";
    initialLoad = true;
}

function sock_readyUp(player, roomKey, ready) {
    player.ready = ready;
    stompClient.publish({
        destination: "/app/ready",
        body: JSON.stringify({ player: player, roomKey: roomKey })
    });
}

function sock_subscribeToGameChannels(roomKey, player) {
    stompClient.subscribe('/topic/game/dealtCard/' + player.id, (dealtCard) => {
        handleDealtCard(dealtCard);
    });

    stompClient.subscribe('/topic/game/update/' + roomKey, (gameUpdates) => {
       handleGameUpdates(gameUpdates);
    });

    stompClient.subscribe('/topic/game/turnStatus/' + roomKey, (turnStatus) => {
        handleTurnStatus(turnStatus);
    });

    stompClient.publish({
        destination: "/app/game/loadedIn",
        body: JSON.stringify({ player: player, roomKey: roomKey })
    });
}

function sock_initGameRoomSubscription(roomKey) {
    lobbyUpdates = stompClient.subscribe('/topic/lobby/' + roomKey, (lobbyUpdates) => {
        handleMatchmaking(lobbyUpdates);
    });
}

function handleDealtCard(card) {
    const binaryData = card._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);

    const prevCards = document.getElementById("personal-cards");
    prevCards.innerHTML += `<div class="hand-card" style="z-index: 1"><div class="card-number">${parsedData.power}</div></div>`;

    const btn = document.getElementById("playCard");
    btn.disabled = false;
    btn.style.backgroundColor = "green";
}

function handleGameUpdates(gameUpdate) {
    const binaryData = gameUpdate._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);

    let message = parsedData.message;
    displayGameMessage(message);

    let idOfStartingPlayer = parsedData.startingPlayer;
    if (idOfStartingPlayer === player.id) {
        document.getElementById("playCard").disabled = false;
        document.getElementById("playCard").style.backgroundColor = "green";
    }
}

function handleTurnStatus(turnStatus) {
    const binaryData = turnStatus._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);

    let msg = parsedData.message;
    let gameOver = parsedData.gameOver;
    let roundOver = parsedData.roundOver;
    let cards = parsedData.cards;

    if (gameOver) {
        displayGameMessage("The game is over!")
    } else if (roundOver) {
        displayGameMessage("The round is over!")
    } else {
        displayGameMessage(msg);
    }

    console.log("Cards map: ", cards)

    for (const gamePlayer in cards) {
        console.log("Looking for div with id: " + gamePlayer.id + " from player " + gamePlayer);
        const div = document.getElementById(gamePlayer.id);
        if (div) {
            gamePlayer.forEach(card => {
                div.innerHTML += `<div class="hand-card" style="z-index: 3"><div class="card-number">${card.power}</div></div>`;
            });
        }
    }
}

// This function will be responsible for parsing the creation and login responses
function handleMatchmaking(lobbyData) {

    const binaryData = lobbyData._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);

    if(initialLoad) {
        var menuDiv = document.getElementById("div-menu-choices");
        menuDiv.style.display = "none";
        var lobbyDiv = document.getElementById("lobby-list");
        lobbyDiv.style.display = "contents";
        document.getElementById("roomKey").textContent += parsedData.roomKey
        initialLoad = false
        roomKey = parsedData.roomKey;
        sock_initGameRoomSubscription(parsedData.roomKey);
    }

    if(parsedData.allReady) {
        stompClient.subscribe('/topic/gameplay/' + roomKey, (gameUpdate) => {
            handleGamePlay(gameUpdate);
        });
    }
    handlePlayersInLobby(parsedData);
}

function handleError(error) {
    const binaryData = error._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);

    alert(parsedData.details);
}

function playGuessingCard(cardNumber, guess, playerName) {
    const pdOn = getPlayer(playerName);

    stompClient.publish({
        destination: "/app/game/playCard",
        body: JSON.stringify({ roomKey: roomKey, player: player, card: {type: "guess", power: cardNumber, guessedOn: pdOn, guessedCard: guess} })
    });
}

function playTargetedCard(target, cardNumber) {
    const trgtPlayer = getPlayer(target);

    stompClient.publish({
        destination: "/app/game/playCard",
        body: JSON.stringify({ roomKey: roomKey, player: player, card: {type: "targeted", power: cardNumber, playedOn: trgtPlayer}})
    });
}

function playSingleCard(cardNumber) {
    stompClient.publish({
        destination: "/app/game/playCard",
        body: JSON.stringify({ roomKey: roomKey, player: player, card: {type: "personal", power: cardNumber}})
    });
}