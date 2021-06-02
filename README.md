# Java Projects
**Random collection of Java projects I have developed to show some of my skills!**

![Lakkienet logo](https://uploads.lakkie.net/lakkienet-logo-dark-solid.png)

## Current applications

--------------------------------

## Chatter

### What is Chatter?



> When people talk, listen completely. Most people never listen.



Simple application for text communications using TCP. Server is operated via command line



### Starting the server and client



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


--------------------------------

## Crossword

```
O S U Y T R Q J A C
E Q P I B N C I S U
M W S R N B O W H L
O R Q I O V N Y Z K
S N D W H J Y J W C
E W A C I T E E R I
W V V V H G B C P S
A U G N R Q W D T Z
I G H C M O N K J G
D W Q R X E L G I Q
```

### What is Crossword?

Crossword generates a random crossword with a specified size, in number of rows and columns, and allows you to specify which words to add, or pick a certain amount of random words.

Usage is through a command line interface, you can also save to a file, which follows the [INI format]([https://link](https://en.wikipedia.org/wiki/INI_file)).


--------------------------------


## Pong

Ah, the classic game of pong. Super easy run, just double click on `pong.jar` or run:

```sh
java -jar pong.jar
```

In the same directory as the pong jar.

This does not include any special AI, or special effects. Just a normal pong game!

(No, I'm not lying)