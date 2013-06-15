Android WebSocket Client
========================

Overview
--------
This project is an Android library which implements a WebSocket Client (RFC6455) for Android from API 8 to API 17.
The project includes an application and a server for testing.


Project Contents
--------
- **client** directory is the Android Library project for the WebSocket client.
- **test_app** directory is an Android Application project which contains the test application.
- **test_node_server** directory contains a simple Node.js WebSocket server using the
  great Worlizer [websocket server module](https://github.com/Worlize/WebSocket-Node.git).
  This server is used during the tests.


Installation of the test application
------------------------------------

To install the test application using Eclipse 

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


More documentation coming soon...
----------------------------------


