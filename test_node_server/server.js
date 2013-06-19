#!/usr/bin/env node
"use scrict"

// Require standard modules
var fs    = require('fs');
var http  = require('http');
var https = require('https');
var util  = require('util');

// Require external modules
var opt  = require('optimist');
var websocket_server = require('websocket').server;


var DEF_SERVER_PORT = 10000;


function main() {


    // Process command line options
    var args = opt.usage("Test Server" + '\n' + 'usage: $0 options')
        .options('h', {
            describe: 'Show help text',
            alias:    'help'
        })
        .options('p', {
            describe: 'Port number to listen to',
            alias:    'port',
            default:   DEF_SERVER_PORT
        })
        .options('s', {
            describe: 'Use SSL',
            alias:    'ssl',
        });
    var argv = args.argv;
    if (argv.h) {
        args.showHelp();
        process.exit(0);
    }

    var msg_ssl = ''; 
    // Creates HTTPS server
    if (argv.ssl) {
        var options = {
            key:  fs.readFileSync('cert/privkey.pem'),
            cert: fs.readFileSync('cert/cacert.pem')
        };
        var hServer = https.createServer(options, function(request, response) {
            response.writeHead(404);
            response.end();
        });
        msg_ssl = "(USING SSL)"
    }
    // Creates HTTP server
    else {
        var hServer = http.createServer(function(request, response) {
            log('Received request for: ' + request.url);
            response.writeHead(404);
            response.end();
        });
    }   
    hServer.on('error', function(err) {
        log('%s', err);
        process.exit(0)
    });

    // Starts server
    hServer.listen(argv.p, function() {
        log('Server is listening on port: %d %s', argv.p, msg_ssl);
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
          log('Connection from origin: %s rejected', request.origin);
          return;
        }

        var connection = request.accept(null, request.origin);
        log('Connection accepted.');

        // Process MESSAGES
        connection.on('message', function(message) {
            if (message.type === 'utf8') {
                log('Received TEXT Message of %d bytes',  message.utf8Data.length);
                connection.sendUTF(message.utf8Data);
            }
            else if (message.type === 'binary') {
                log('Received BINARY Message of  %d bytes', message.binaryData.length);
                connection.sendBytes(message.binaryData);
            }
        });

        // Proces FRAMES
        connection.on("frame", function(frame) {
            log('Received FRAME with OPCODE: %d LENGTH: %d:', frame.opcode, frame.length);
        });

        // Process ERRORS
        connection.on('error', function(error) {
            log('Error: %s', error);
        });

        // Process CLOSE
        connection.on('close', function(reasonCode, description) {
            log('Peer: %s disconnected. Error: %s ', connection.remoteAddress,  description);
        });
    });
}


function log(fmt) {
    var args;
    var pos;

    args = [];
    for (pos = 0; pos < arguments.length; pos++) {
        args.push(arguments[pos]);
    }
    msg = util.format.apply(null, args);
    console.log((new Date()) + " " + msg);
}



function originIsAllowed(origin) {
    return true;
}


main();

