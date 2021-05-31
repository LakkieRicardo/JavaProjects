# Chatter



> When people talk, listen completely. Most people never listen.



## What is Chatter?



Simple application for text communications using TCP. Server is operated via command line



## Starting the server and client



The server must be started via command line as such:
```sh
java -jar ./server.jar [port]
```
The port is optional; if not specified you will be asked.

In order to start the client, you can either start with certain connection details specified as arguments, such as this:
```sh
java -jar ./client.jar -details <host address> <port> <username>
```

Alternatively, you can use a CLI interface to input details:
```sh
java -jar ./client.jar cli
```

Starting w/o command line arguments will give you a GUI to enter in the details instead.

*Client-side, information about commands can be accessed with `/help`*