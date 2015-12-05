import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientBack
{
    //Socket I/O objects
    private ObjectInputStream SocketIn;
    private ObjectOutputStream SocketOut;
    private Socket socket;

    //GUI Connection
    private ClientFront CFrontEnd;

    //the ServerIP, the ServerPort and the username
    private String ServerIP, username;
    private int ServerPort;

    ClientBack(String server, int port, String username, ClientFront cg)
    {
        this.ServerIP = server;
        this.ServerPort = port;
        this.username = username;
        this.CFrontEnd = cg;
    }

    public boolean start()
    {
        //Opens server connection
        try
        {
            socket = new Socket(ServerIP, ServerPort);
        } 

        catch(Exception e)
        {
            display("Error connectiong to server:" + e);
            return false;
        }

        String msg = "Connection opened " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        //Opens I/O streams
        try
        {
            SocketIn  = new ObjectInputStream(socket.getInputStream());
            SocketOut = new ObjectOutputStream(socket.getOutputStream());
        }

        catch (IOException eIO)
        {
            display("Could not create I/O streams.");
            return false;
        }

        //Listener thread
        new ListenFromServer().start();

        //Sends username
        try
        {
            SocketOut.writeObject(username);
        }
        catch (IOException eIO)
        {
            display("Could not login.");
            disconnect();
            return false;
        }
        //success we inform the caller that it worked
        return true;
    }

    //Writes messages to Chat Log
    private void display(String msg)
    {
        CFrontEnd.append(msg + "\n");
    }

    //Sends message
    void TransmitMessage(Message msg)
    {
        try
        {
            SocketOut.writeObject(msg);
        }
        catch(IOException e)
        {
            display("Error sending message.");
        }
    }

    //Closes connection
    private void disconnect()
    {
        try
        { 
            if(SocketIn != null) SocketIn.close();
        }
        catch(Exception e) {}

        try
        {
            if(SocketOut != null) SocketOut.close();
        }
        catch(Exception e) {}

        try
        {
            if(socket != null) socket.close();
        }
        catch(Exception e) {}

        CFrontEnd.connectionFailed();
    }

    //Thread that listens for server I/O
    class ListenFromServer extends Thread
    {
        public void run()
        {
            while(true)
            {
                try
                {
                    String msg = (String) SocketIn.readObject();
                    CFrontEnd.append(msg);
                }

                catch(IOException e)
                {
                    display("Disconnected from server.");
                    CFrontEnd.connectionFailed();
                    break;
                }

                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}