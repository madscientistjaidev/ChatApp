
# ChatApp
This is a simple Team Chat application. It is written in Java, and uses Java Sockets for communication. Complete documentation is available in the javadoc folder.

## Build Instructions
No special build instructions are needed. The exact commands are, of course, platform dependent, and one will not need to use these if one is using an IDE. Assuming that one has navigated to the folder containing the files, one can compile and run the application with these commands.

    javac *.java
    java <ClassName>

The choice of ClassName depends on whether one wishes to run the client or the server. In an IDE, it is simply a matter of choosing which of these classes is the main one. One uses ServerFront.java if ones wishes to run the server, and ClientFront.java if one wishes to run the client. 

Additionally, If one wishes to create an executable jar file, so as to run the application with a simple mouse click, one can use this command.

    jar cvfe <ClassName>.jar <ClassName> *.class 

JAR files for both client and server are available in the jars folder on the Git repository. 

## Architecture
This project is based on a Client Server Architecture. Many clients connect to a single server, and exchange messages through it.

The client and server are both divided into a front end and a back end. The front end GUIs provide a simple user interface, while the back ends perform the actual communications. In this sense, it is also a very simple Layered Architecture.
