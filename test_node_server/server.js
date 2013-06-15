#!/usr/bin/env node
"use scrict"

// Require standard modules
var http = require('http');

// Require external modules
var opt  = require('optimist');
var websocket_server = require('websocket').server;


var DEF_SERVER_PORT = 10000;


function main() {

    // Process command line options
    var argv = opt.usage("Test Server" + '\n' + 'usage: $0 options')
        .options('p', {
            describe: 'Port number to listen to',
            default:  DEF_SERVER_PORT
        })
        .argv;

    // Creates HTTP server
    var hServer = http.createServer(function(request, response) {
        console.log((new Date()) + ' Received request for ' + request.url);
        response.writeHead(404);
        response.end();
    });
    // Starts HTTP server
    hServer.listen(argv.p, function() {
        console.log((new Date()) + ' Server is listening on port ' + argv.p);
    });

    // Creates WebSocket server associated with HTTP server
    wsServer = new websocket_server({
        httpServer:                 hServer,
        autoAcceptConnections:      false,
        keepalive:                  false,
        keepaliveinterval:          2000,
        maxReceivedFrameSize:       1024*1024,
        maxReceivedMessageSize:     1024*1024,
        fragmentOutgoingMessages:   false,
        assembleFragments:          true,
    });


    // Process connection request
    wsServer.on('request', function(request) {
        if (!originIsAllowed(request.origin)) {
          request.reject();
          console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
          return;
        }

        var connection = request.accept(null, request.origin);
        console.log((new Date()) + ' Connection accepted.');

        // Process MESSAGES
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

        // Proces FRAMES
        connection.on("frame", function(frame) {
            console.log('Received FRAME:', frame.opcode, frame.length);
        });

        // Process ERRORS
        connection.on('error', function(error) {
            console.log((new Date()) + ' Error: ' + error);
        });

        // Process CLOSE
        connection.on('close', function(reasonCode, description) {
            console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected. Error: ' + description );
        });
    });
}


function originIsAllowed(origin) {
    return true;
}


main();

