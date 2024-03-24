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

function getPlayer(name)  {
    return lobby.players.find(player => player.name === name);
}

let lobby;
let initialLoad = true;
let ready = false;
let allReady = false;
let gameBeginning = false;
var gameClearedToStart = false;

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
        gameClearedToStart = true;
        beginGameStartThings();
    } else if(gameBeginning) {
        gameClearedToStart = false;
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

async function displayGameMessage(msg) {
    const messageDiv = document.getElementById("gameMessages");
    messageDiv.innerText = msg

    await sleep(4000)
    messageDiv.innerText = "";
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
    if(gameClearedToStart) {
        setupRound();
        const playButton = document.getElementById("playCard");
        playButton.disabled = true;
        playButton.style.backgroundColor = "gray";
        const lobbyDiv = document.getElementById("lobby-list")
        lobbyDiv.style.display = "none"
        const gameDiv = document.getElementById("game")
        gameDiv.style.display = "contents"
    }
    // Josh, put the new html page activation here and set the current one to none
}

function setupRound() {
    document.querySelector("tbody").innerHTML = lobby.players.map(player => createPlayerRow(player, null)).join("");
    sock_subscribeToGameChannels(roomKey, player);
}

function createPlayerRow(player, cards) {
    let handContent;
    if(cards) {
        const handCards = cards.map((power, index) => {
                `<div class="hand-card" style="z-index: ${index}"><div class="card-number">${power}</div></div>`
            }
        );
        handContent = handCards.length ? handCards.join("") : "None";
    } else {
        handContent = "None";
    }

    return `
        <tr>
          <td>${player.name}</td>
          <td>${player.status ? player.status : "Waiting"}</td>
          <td><div class="hand" id="${player.id}">${handContent}</div></td>
        </tr>
      `;
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
    $("#playCard").click(() => {
        let cardNumber = parseInt(prompt("Enter the card to play:"));
        switch (cardNumber) {
            case 1:
                let guessedCard = prompt("Enter card number to guess:")
                let playerName = prompt("Enter in player name to guess on:")
                playGuessingCard(cardNumber, guessedCard, playerName);
                break;
            case 2:
            case 3:
            case 5:
            case 6:
                let player = prompt("Enter player name to play on:")
                playTargetedCard(player, cardNumber);
                break;
            case 4:
            case 7:
            case 8:
                playSingleCard(cardNumber);
                break;
            default:
                console.log("Invalid card number");
        }
        const btn = document.getElementById("playCard");
        btn.disabled = true;
        btn.style.backgroundColor = "gray"
    })
});