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

function sock_createGame(player) {
    console.log("Data to send: ", JSON.stringify({playerDto: player}))
    stompClient.publish({
        destination: "/app/createGame",
        body: JSON.stringify({ player: player })
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

function sock_initGameRoomSubscription(roomKey) {
    lobbyUpdates = stompClient.subscribe('/topic/lobby/' + roomKey, (lobbyUpdates) => {
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