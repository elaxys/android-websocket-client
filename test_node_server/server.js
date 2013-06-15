#!/usr/bin/env node
"use scrict"

var WebSocketServer = require('websocket').server;
var http = require('http');

var SERVER_PORT = 10000;

function main() {

    // Creates HTTP server
    var server = http.createServer(function(request, response) {
        console.log((new Date()) + ' Received request for ' + request.url);
        response.writeHead(404);
        response.end();
    });
    // Starts HTTP server
    server.listen(SERVER_PORT, function() {
        console.log((new Date()) + ' Server is listening on port ' + SERVER_PORT);
    });

    // Creates WebSocket server associated with HTTP server
    wsServer = new WebSocketServer({
        httpServer: server,
        autoAcceptConnections: false,
        keepalive: false,
        keepaliveinterval: 2000,
        maxReceivedFrameSize: 1024*1024,
        maxReceivedMessageSize: 1024*1024,
        fragmentOutgoingMessages: false,
        assembleFragments: true,
    });


    wsServer.on('request', function(request) {
        if (!originIsAllowed(request.origin)) {
          request.reject();
          console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
          return;
        }

        var connection = request.accept(null, request.origin);
        console.log((new Date()) + ' Connection accepted.');
        connection.on('message', function(message) {
            if (message.type === 'utf8') {
                console.log('Received TEXT Message of ' + message.utf8Data.length + ' bytes');
                connection.sendUTF(message.utf8Data);
            }
            else if (message.type === 'binary') {
                console.log('Received BINARY Message of ' + message.binaryData.length + ' bytes');
                connection.sendBytes(message.binaryData);
            }
        });
        connection.on("frame", function(frame) {
            console.log('Received FRAME:', frame.opcode, frame.length);
        });
        connection.on('error', function(error) {
            console.log((new Date()) + ' Error: ' + error);
        });
        connection.on('close', function(reasonCode, description) {
            console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected. Error: ' + description );
        });
    });
}


function originIsAllowed(origin) {
    // put logic here to detect whether the specified origin is allowed.
    return true;
}


main();

