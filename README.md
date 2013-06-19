Android WebSocket Client
========================

Overview
--------
This project is an Android library which implements a WebSocket Client (RFC6455) for Android from API 8 to API 17.
The project includes an application and a server for testing.


Project Contents
----------------
- **client** directory is the Android Library project for the WebSocket client.
- **test_app** directory is an Android Application project which contains the test application.
- **test_node_server** directory contains a simple Node.js WebSocket server using the
  great Worlizer [websocket server module](https://github.com/Worlize/WebSocket-Node.git).
  This server is used for testing only.


Installation
------------
To use the WebSocket client in your project using Eclipse
you need to import the library into your workspace using
***File/Import/Android/Existing Android Code into Workspace***.
The default name of this project is **websocket_client**.

Then you should inform Eclipse that your project depends on the WebSocliet client library.
Right-click on your project in ***Package Explorer***, select ***Properties***,
select **Android** category and then adds a reference to **websocket_client**
in the **Library** list.


Architecture
------------
This WebSocket client uses two threads, a handler and a transmission queue.
When the client is instantiated an Android handler is created associated with
the caller thread. This handler is used to send events to the caller thread
through its listener.

The reception thread is responsible for connection with the
server, reading messages from the socket, decoding them and
generating most listener events.

The transmission thread is created only after the connection
with the server succeeds and it blocks reading from the transmission
queue waiting for messages to send.
When a message is inserted in the transmission queue,
the transmission thread will remove it
and try to send it through the connected socket.
After the message is sent (inserted into the operation
system network buffer) it generates an event for the listener,
identifying the message which was sent


Web Socket Client usage
-----------------------

- To create a new WebSocket client it is necessary to pass
  a configuration object and a listener to receive the
  events. Then the client should be started.
  After the client is started it will try to connect
  with server, using the URI specified in the configuration.
  If an error occurs, it will wait **config.mRetryInterval**
  milliseconds and then try again indefinitely until is
  stopped by **client.stop()**.

- When the connection with the server succeeds the Listener
  **onClienteConnected()** method will be called, otherwise,
  on any errors **onClientError()** is called and the client
  continues retrying.

- To send TEXT or BINARY messages to the server the method
  ***client.send(msg)*** should be used.
  This method is non-blocking and returns
  immediately after it inserts the message into the transmission queue.
  The method returns the ID of the message or null if the transmission
  queue is full. When the message is sent (inserted into the operating
  system network buffer) the listener method **onClientSent()** is
  called. The messages are inserted in the transmission queue even
  if the client is not started or connected yet.

- To send TEXT or BINARY fragmented messages to the server 
  the methods **sendFirst()**, **sendNext()** and **sendLast()** should
  be used. These methods are also non-blocking and returns immediately
  after inserting the message fragment in the transmission queue.

- When a message is received from the server, the listener **onClientRecv()**
  method is called. This method informs the type of the message received using
  the constants (```F_TEXT, F_TEXT_FIRST```, etc.) and its payload.


```
    // API usage example
    // Creates WebSocket Client
    Client.Config config = new Client.Config();
    config.mURI           = "wss://www.example.com/path";
    config.mQueueSize     = 10;
    config.mConnTimeout   = 5000;
    config.mRetryInterval = 5000;
    config.mMaxRxSize     = 128*1024;
    config.mRespondPing   = true;
    config.mServerCert    = true;
    config.mLogTag        = "WSCLIENT";
    Client client;
    try {
        client = Client(config, new Listener() {
            // Called when the client is started
            @Override
            public void onClientStart() {
            }

            // Called when the client is about to connect
            @Override
            public void onClientConnect() {
            }

            // Called when the client connected successfully
            @Override
            public void onClientConnected() {
            }

            // Called when any error occurs.
            @Override
            public void onClientError(int code, String msg) {
            }

            // Called when a TEXT message or fragment is received
            @Override
            public void onClientRecv(int type, String data) {
            }
   
            // Called when a BINARY message or fragment is received
            @Override
            public void onClientRecv(int type, byte[] data) {
            }
           
            // Called when a message or fragment was sent to the server
            @Override
            public void onClientSent(int fid) {
            }
        
            // Called when the client stops.
            @Override
            public void onClientStop() {
            }
        });
    } catch (Error e) {

    }

    // Starts the client
    client.start();

    // Sends TEXT message
    client.send("MESSAGE");

    // Sends BINARY message
    cliend.send(new byte[]{1,2,3,4,5});

    // Sends TEXT fragmented message
    client.sendFirst("THIS IS");
    client.sendNext("A");
    client.sendLast("MESSAGE");

    // Sends BINARY fragmented message
    client.sendFirst(new byte[]{1,2});
    client.sendNext(new byte[]{3,4});
    client.sendLast(new byte[]{5});


```


Installation of the test application
------------------------------------

The test application and server allow testing most of the client
functionality.  To install the test application using Eclipse:

1. Import the WebSocket client library project using
  ***File/Import/Android/Existing Android Code into Workspace***.
  The default name of this project is **websocket_client**.
2. Import the test application project using
  ***File/Import/Android/Existing Android Code into Workspace***.
  After it is imported you should inform Eclipse that this projects depends on the WebSocket
  client library. Right-click on the ***websocket_client_test*** project in ***Package Explorer***,
  select ***Properties***, select **Android** category and then adds a reference to **websocket_client**
  in the **Library** list.

If you use command line tools the test application can be built
using ***ant debug*** command inside the application directory

3. To run the test server it is necessary to have Node.js installed in your
   system. Then it is necessary to install the required modules.
   A **bash** script in the server directory installs the modules locally
   using Node.js **NPM**.




