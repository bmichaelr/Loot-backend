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

let connected = false;
// Function that is called when the user is connected to the websocket
function sock_connect(playerName) {
    stompClient.activate();
    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/matchmaking/' + playerName, (lobbyData) => {
            handleMatchmaking(lobbyData);
        });
        stompClient.subscribe("/topic/error/" + playerName, (error) => {
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

function sock_createGame(player) {
    stompClient.publish({
        destination: "/app/createGame",
        body: JSON.stringify({ playerDto: player })
    });
}

function sock_joinGame(player, roomKey) {
    stompClient.publish({
        destination: "/app/joinGame",
        body: JSON.stringify({ playerDto: player, roomKey: roomKey })
    });
}

function sock_readyUp(player, roomKey, ready) {
    player.ready = ready;
    stompClient.publish({
        destination: "/app/ready",
        body: JSON.stringify({ playerDto: player, roomKey: roomKey })
    });
}

function sock_initGameRoomSubscription(roomKey) {
    stompClient.subscribe('/topic/lobby/' + roomKey, (lobbyUpdates) => {
        handleMatchmaking(lobbyUpdates);
    });
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

    handlePlayersInLobby(parsedData.players, parsedData.roomKey);
}

function handleError(error) {
    const binaryData = error._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);

    alert(parsedData.details);
}