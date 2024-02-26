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

function connect() {
    stompClient.activate();
    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
    };
}

function disconnect() {
    stompClient.deactivate();
    console.log("Disconnected");
}
