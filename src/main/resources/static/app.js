let initPlayerId;
let initRoomKey;

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

async function getRoomKey(playerId) {
    try {
        const response = await fetch('http://localhost:8080/game/create', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error('Failed to fetch room key');
        }

        const responseData = await response.json();
        const roomKey = responseData.roomKey;

        return roomKey;
    } catch (error) {
        console.error('Error fetching room key:', error);
        return null;
    }
}


function connect(roomKey, playerId) {
    stompClient.activate()
    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        // Subscribe to game-related topics
        stompClient.subscribe('/topic/gameStatus/' + roomKey, (gameStatus) => {
            showGameStatus(gameStatus);
        });

        stompClient.publish({
            destination: "/app/createGame",
            body: JSON.stringify({ playerDto: { name: playerId }, roomKey: roomKey })
        });

        initPlayerId = playerId;
        initRoomKey = roomKey;
    };
}

function disconnect() {
    stompClient.deactivate();
    console.log("Disconnected");
}

async function createGame(playerId) {
    try {
        const roomKey = await getRoomKey(playerId); // Wait for the room key to be retrieved
        if (roomKey) {
            console.log("Room key created, attempting connection now...")
            connect(roomKey, playerId); // Connect to WebSocket after getting room key
        } else {
            console.error('Failed to create game: Room key not found');
        }
    } catch (error) {
        console.error('Failed to create game:', error);
    }
}

function joinGame(playerId, roomKey) {
    stompClient.activate()
    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        // Subscribe to game-related topics
        stompClient.subscribe('/topic/gameStatus/' + roomKey, (gameStatus) => {
            showGameStatus(gameStatus);
        });

        stompClient.publish({
            destination: "/app/joinGame",
            body: JSON.stringify({ playerDto: { name: playerId }, roomKey: roomKey })
        });
    };

    initPlayerId = playerId;
    initRoomKey = roomKey;
}

function readyUp(){
    console.log("Ready button has been pressed")
    stompClient.publish({
        destination : "/app/ready",
        body: JSON.stringify({ playerDto: { name: initPlayerId }, roomKey: initRoomKey })
    });
}

function showGameStatus(message) {
    console.log("message from server: ", message)
    $("#gameStatus").append("<p>" + JSON.stringify(message.body) + "</p>");
}

$(function () {
    $( "#disconnect" ).click(() => disconnect());
    $( "#createGame" ).click(() => {
        let playerId = prompt("Enter your username: ");
        if(playerId) {
            createGame(playerId)
        }
    });
    $( "#ready" ).click(() => readyUp());
    $( "#joinGame" ).click(() => {
        let playerId = prompt("Enter your username: ");
        let sessionId = prompt("Enter game session ID:");
        if (sessionId && playerId) {
            joinGame(playerId, sessionId);
        }
    });
});
