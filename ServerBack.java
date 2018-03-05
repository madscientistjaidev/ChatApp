import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServerBack
{
    //Unique ID for each connection
    private static int ConnID;

    //ArrayList to keep a list of Clients
    private ArrayList<ClientThread> ClientList;

    //Connection to GUI
    private ServerFront SFrontEnd;

    //To get time
    private SimpleDateFormat sdf;

    //Listening for connections on this port
    private int PortNo;

    //Activates and deactivates server
    private boolean KeepAlive;

    public ServerBack(int port, ServerFront sg)
    {           
        this.SFrontEnd = sg;
        this.PortNo = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        ClientList = new ArrayList<>();
    }

    public void start()
    {
        KeepAlive = true;

        try 
        {
            //Opens socket
            ServerSocket serverSocket = new ServerSocket(PortNo);

            //Loops as long as server is alive
            while(KeepAlive) 
            {
                display("Listening for Clients on port " + PortNo + ".");

                //Accepts new connections from clients
                Socket socket = serverSocket.accept();

                //Stops server
                if(!KeepAlive) break;

                //Creates threads for new clients
                ClientThread t = new ClientThread(socket);

                //Adds new clients to list
                ClientList.add(t);									

                t.start();
            }

            //Closes sockets upon deactivation
            try
            {
                serverSocket.close();
                for(int i = 0; i < ClientList.size(); ++i)
                {
                    ClientThread tc = ClientList.get(i);
                    try
                    {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }

                    catch(IOException ioE) {}
                }
            }

            catch(Exception e)
            {
                    display("Server shutting down" + e);
            }
        }

        //Other Errors
        catch (IOException e)
        {
            display(sdf.format(new Date()) + "Unknown Error: " + e + "\n");
        }
    }		

    //Stops Server
    protected void stop()
    {
        KeepAlive = false;

        //To check if server has stopped
        try
        {
            new Socket("localhost", PortNo);
        }
        catch(Exception e) {}
    }

    //Prints message to Event Log
    private void display(String msg)
    {
        SFrontEnd.PrintToEvent(sdf.format(new Date()) + " " + msg + "\n");
    }

    //Broadcasts message
    private synchronized void broadcast(String message)
    {
        //add HH:mm:ss and \n to the message
        String messageLf = sdf.format(new Date()) + " " + message + "\n";

        SFrontEnd.PrintToChat(messageLf + " " + message + "\n");     //append in the room window

        for(int i = ClientList.size(); --i >= 0;)
        {
            ClientThread ct = ClientList.get(i);

            //Removes client from list if it fails to receive message
            if(!ct.writeMsg(messageLf))
            {
                ClientList.remove(i);
                display(ct.username + " disconnected.");
            }
        }
    }

    //To handle client logoff
    synchronized void remove(int id)
    {
        //Find client and remove it form list
        for(int i = 0; i < ClientList.size(); ++i)
        {
            ClientThread ct = ClientList.get(i);

            if(ct.ClientID == id)
            {
                ClientList.remove(i);
                return;
            }
        }
    }

    //Clients run in threads of this class
    class ClientThread extends Thread
    {
        //Socker and I/O streams
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        
        //Unique ID
        int ClientID;
        
        //Username
        String username;
        
        //Holds messages
        Message ClientMessage;
        
        //Timestamp of connection
        String ConnTime;

        ClientThread(Socket socket)
        {
            ClientID = ++ConnID;
            this.socket = socket;
            
            //Creating I/O streams
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                
                username = (String) sInput.readObject();
                display(username + " connected.");
            }
            
            catch (IOException e)
            {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            
            catch (ClassNotFoundException e) {}

            ConnTime = new Date().toString() + "\n";
        }

        @Override
        public void run()
        {
            //Keeps thread alive
            boolean keepGoing = true;
            
            while(keepGoing)
            {
                //Polls Input Stream
                try
                {
                    ClientMessage = (Message) sInput.readObject();
                }
                
                catch (IOException e)
                {
                    display(username + " Exception reading Streams: " + e);
                    break;				
                }
                catch(ClassNotFoundException e2)
                {
                    break;
                }
                
                //Gets message
                String message = ClientMessage.getMsgString();

                //Checks Message type
                switch(ClientMessage.getMsgType())
                {

                    case Normal:
                            broadcast(username + ": " + message);
                            break;
                    case Disconnect:
                            display(username + " disconnected with a LOGOUT message.");
                            keepGoing = false;
                            break;
                    case ClientList:
                            writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                            //Returns all lients in list
                            for(int i = 0; i < ClientList.size(); ++i)
                            {
                                ClientThread ct = ClientList.get(i);
                                writeMsg((i+1) + ") " + ct.username + " since " + ct.ConnTime);
                            }
                            break;
                }
            }
            
            //Remove client from list
            remove(ClientID);
            close();
        }

        //Closes I/O streams
        private void close()
        {        
            try
            {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}

            try
            {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};

            try
            {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        //Sends message to client
        private boolean writeMsg(String msg)
        {
            //Checks if client is connected
            if(!socket.isConnected())
            {
                close();
                return false;
            }
            
            //Writes message
            try
            {
                sOutput.writeObject(msg);
            }
            
            catch(IOException e)
            {
                display(username + " not connected.");
                display(e.toString());
            }
            return true;
        }
    }
}