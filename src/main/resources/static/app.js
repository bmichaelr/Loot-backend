/*let initPlayerId;
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
}*/

// use this
class Player {
    constructor(name) {
        this.name = name;
        this.ready = false;
        this.id = crypto.randomUUID();
    }
}

var player;
let roomKey;

class Lobby {
    constructor(roomKey, players) {
        this.roomKey = roomKey;
        this.players = players;
    }
}

let lobby;
let initialLoad = true;
let ready = false;
let allReady = false;
let gameBeginning = false;

window.onload = function () {
    const urlParams = new URLSearchParams(window.location.search);
    const playerName = urlParams.get('playerName');

    console.log('Player name:', playerName);
    player = new Player(playerName);
    sock_connect(player.id);
};

// <div className="player-item">
//     <div className="player-name">Player 1</div>
//     <div className="ready-status">✔️</div>
// </div>

function handlePlayersInLobby(parsedData) {
    const players = parsedData.players;
    const allReady = parsedData.allReady;

    if (!lobby) {
        lobby = new Lobby(roomKey, players);
        players.forEach(player => {
            addPlayerToLobby(player);
        });
    } else {
        console.log('in the else for handling players in lobby')
        lobby.players.forEach(existingPlayer => {
           const inLobby = players.find(player => player.id === existingPlayer.id);
           if(!inLobby) {
               const playerToRemove = document.getElementById('player-'+existingPlayer.id);
               if(playerToRemove) {
                   playerToRemove.remove();
               }
           }
        });

        players.forEach(player => {
           if(document.getElementById('player-'+player.id)) {
               console.log(player)
               console.log('have the wrapper player div, player ready = ', player.ready)
               const playerWrapper = document.getElementById('player-' + player.id);
               const statusDivToUpdate = playerWrapper.querySelector('.ready-status');
               if(player.ready) {
                   statusDivToUpdate.textContent = "✔️";
               } else {
                   statusDivToUpdate.textContent = "❌";
               }
           } else {
               addPlayerToLobby(player);
           }
        });
    }

    lobby.players = players;
    if(allReady) {
        beginGameStartThings();
    } else if(gameBeginning) {
        cancelGameBeginningCountdown();
    }
}

function addPlayerToLobby(player) {
    const parentContainer = document.getElementById("list-of-players");
    const wrapper = document.createElement('div');
    wrapper.classList.add('player-item');
    wrapper.id = 'player-' + player.id;

    const nameDiv = document.createElement('div');
    nameDiv.textContent = player.name;
    nameDiv.classList.add('player-name');
    const statusDiv = document.createElement('div');
    if(player.ready) {
        statusDiv.textContent = "✔️";
    } else {
        statusDiv.textContent = "❌";
    }
    statusDiv.classList.add('ready-status');

    wrapper.appendChild(nameDiv);
    wrapper.appendChild(statusDiv);
    parentContainer.appendChild(wrapper);
}

function handleGamePlay(gameUpdate) {
    const binaryData = gameUpdate._binaryBody;
    const stringData = new TextDecoder().decode(binaryData);
    const parsedData = JSON.parse(stringData);
}

function cancelGameBeginningCountdown() {
    allReady = false;
    gameBeginning = false;
    document.getElementById("readyBtn").style.backgroundColor = "#007bff";
    const gameCounter = document.getElementById("gameCounter");
    gameCounter.style.display = "none";
    gameCounter.innerText = "Game starting in 3...";
}

async function beginGameStartThings() {
    allReady = true;
    gameBeginning = true;
    document.getElementById("readyBtn").style.backgroundColor = "gray";

    const gameCounter = document.getElementById("gameCounter");
    gameCounter.style.display = "contents";
    await sleep(1000);
    gameCounter.innerText = "Game starting in 2...";
    await sleep(1000);
    gameCounter.innerText = "Game starting in 1...";
    await sleep(1000);
    gameCounter.innerText = "Game starting now!";
    // Josh, put the new html page activation here and set the current one to none
}

function sleep(ms = 0) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

$(function () {
    $("#createGameBtn").click(() => {
        if (connected && player) {
            sock_createGame(player)
        }
    });
    $("#joinGameBtn").click(() => {
        let roomKey = prompt("Enter game room key:");
        if (roomKey && player) {
            sock_joinGame(player, roomKey);
        }
    });
    $("#readyBtn").click(() => {
        if(player && roomKey && !allReady) {
            ready = !ready;
            document.getElementById("readyBtn").innerText = (ready) ? "Unready" : "Ready up";
            sock_readyUp(player, roomKey, ready);
        }
    });
    $("#leaveGame").click(() => {
        sock_leaveGame(player, roomKey);
    });
});