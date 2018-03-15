import java.io.Serializable;
import java.text.SimpleDateFormat;

//This class acts as a container for messages.

public class Message implements Serializable
{
	//MsgType denotes the purpose of the message.
        private Type MsgType;
        
        //Stores timestamp of the message.
        private SimpleDateFormat MsgTime;
	
        //MsgString stores the contents of the message.
        private String MsgString;
	
	Message(String MT, String MsgString)
        {
		switch(MT)
                {
                    case "Disconnect": this.MsgType = Type.Disconnect; break;
                    case "Normal": this.MsgType = Type.Normal; break;
                    case "ClientList": this.MsgType = Type.ClientList; break;
                }
                this.MsgType = MsgType;
		this.MsgString = MsgString;
                MsgTime = new SimpleDateFormat("HH:mm:ss");
	}
	
	Type getMsgType() {return MsgType;}
        String getMsgString() {return MsgString;}
        SimpleDateFormat getTimeStamp() {return MsgTime;}
}

//Denotes the type of message
enum Type{ClientList, Normal, Disconnect}