import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ClientFront extends JFrame implements ActionListener
{
    //Box Label
    private JLabel label;
    
    //Text box contents
    private JTextField MessageBox;
    
    //Server address and port
    private JTextField ServerIP, ServerPort;
    
    //Buttons
    private JButton login, logout, GetList;
    
    //Chat Log
    private JTextArea TextArea;
    
    //Connection state
    private boolean connected;
    
    //Connection to Back end
    private ClientBack CBackEnd;
    
    //Default Settings
    private int defaultPort;
    private String defaultHost;

    //Constructor connection receiving a socket number
    ClientFront(String host, int ChatPort)
    {
        super("Chat Client");
        defaultPort = ChatPort;
        defaultHost = host;

        //Upper Panel layout
        JPanel UpperPanel = new JPanel(new GridLayout(3,1));
        
        //Server and Port boxes
        JPanel serverAndPort = new JPanel(new GridLayout(1,5,1,3));
        ServerIP = new JTextField(host);
        ServerPort = new JTextField("" + ChatPort);
        ServerPort.setHorizontalAlignment(SwingConstants.RIGHT);

        //Text Labels
        serverAndPort.add(new JLabel("Server IP:  "));
        serverAndPort.add(ServerIP);

        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(ServerPort);

        serverAndPort.add(new JLabel(""));
        UpperPanel.add(serverAndPort);

        //Default values
        label = new JLabel("Enter Username", SwingConstants.CENTER);
        UpperPanel.add(label);
        MessageBox = new JTextField("Anonymous");
        MessageBox.setBackground(Color.WHITE);
        UpperPanel.add(MessageBox);
        add(UpperPanel, BorderLayout.NORTH);

        //Chat Log
        TextArea = new JTextArea("Java Chat Application\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(TextArea));
        TextArea.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        //Buttons and default states
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);
        GetList = new JButton("Client List");
        GetList.addActionListener(this);
        GetList.setEnabled(false);

        JPanel LowerPanel = new JPanel();
        LowerPanel.add(login);
        LowerPanel.add(logout);
        LowerPanel.add(GetList);
        add(LowerPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        MessageBox.requestFocus();

    }

    //Writes text to Chat box
    void append(String str)
    {
        TextArea.append(str);
        TextArea.setCaretPosition(TextArea.getText().length() - 1);
    }
    
    //Resets Buttons on failure
    void connectionFailed()
    {
        login.setEnabled(true);
        logout.setEnabled(false);
        GetList.setEnabled(false);
        label.setText("Enter Username");
        MessageBox.setText("Anonymous");
        
        ServerPort.setText("" + defaultPort);
        ServerIP.setText(defaultHost);
        
        ServerIP.setEditable(true);
        ServerPort.setEditable(true);
        
        MessageBox.removeActionListener(this);
        connected = false;
    }

   //Listens for input
    public void actionPerformed(ActionEvent e)
    {
        Object o = e.getSource();

        //Logout
        if(o == logout)
        {
            CBackEnd.TransmitMessage(new Message("Disconnect", ""));
            return;
        }
        //Client List
        if(o == GetList)
        {
            CBackEnd.TransmitMessage(new Message("ClientList", ""));				
            return;
        }

        //Text box
        if(connected)
        {
            //Sends Message
            CBackEnd.TransmitMessage(new Message("Normal", MessageBox.getText()));				
            MessageBox.setText("");
            return;
        }

        //Login
        if(o == login)
        {
            String username = MessageBox.getText().trim();

            //Checks for invalid input
            if(username.length() == 0) return;
            String server = ServerIP.getText().trim();
            if(server.length() == 0) return;
            String portNumber = ServerPort.getText().trim();
            if(portNumber.length() == 0) return;
            
            int port = 0;
            try
            {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en)
            {
                return;
            }

            //Starts Back end
            CBackEnd = new ClientBack(server, port, username, this);
            if(!CBackEnd.start()) return;
            MessageBox.setText("");
            label.setText("Enter your message below");
            connected = true;

            //Toggles button states
            login.setEnabled(false);
            logout.setEnabled(true);
            GetList.setEnabled(true);
            ServerIP.setEditable(false);
            ServerPort.setEditable(false);
            MessageBox.addActionListener(this);
        }
    }

    public static void main(String[] args)
    {
        new ClientFront("127.0.0.1", 25351);
    }
}