import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServerFront extends JFrame implements ActionListener, WindowListener
{
    //Start button
    private JButton ActionButton;

    //Chat Log and Event Log
    private JTextArea ChatLog, EventLog;

    //Port to listen on
    private JTextField ServerPort;

    //Creates an instance of the Back end.
    private ServerBack SBackEnd;


    //Constructor accepts the Port to listen on
    ServerFront(int ChatPort)
    {
        super("Chat Server");
        SBackEnd = null;
        //Text Labels
        JPanel UpperPanel = new JPanel();
        UpperPanel.add(new JLabel("Port number: "));
        ServerPort = new JTextField("  " + ChatPort);
        UpperPanel.add(ServerPort);
        
        //Start Button
        ActionButton = new JButton("Start");
        ActionButton.addActionListener(this);
        UpperPanel.add(ActionButton);
        add(UpperPanel, BorderLayout.SOUTH);

        //Sets Window layout
        JPanel center = new JPanel(new GridLayout(1,2));
        
        //Chat Log
        ChatLog = new JTextArea(80,40);
        ChatLog.setEditable(false);
        PrintToChat("Chat Log.\n");
        center.add(new JScrollPane(ChatLog));
        
        //Event Log
        EventLog = new JTextArea(80,40);
        EventLog.setEditable(false);
        PrintToEvent("Event log.\n");
        center.add(new JScrollPane(EventLog));	
        add(center);

        //To detect OS UI close button
        addWindowListener(this);
        
        //Sets window size
        setSize(1000, 800);
        setVisible(true);
    }		

    //Writes messages to logs
    void PrintToChat(String str)
    {
        ChatLog.append(str);
        ChatLog.setCaretPosition(ChatLog.getText().length() - 1);
    }
    void PrintToEvent(String str)
    {
        EventLog.append(str);
        EventLog.setCaretPosition(ChatLog.getText().length() - 1);
    }

    //Toggles start and stop
    @Override
    public void actionPerformed(ActionEvent e)
    {
        //To stop
        if(SBackEnd != null)
        {
            SBackEnd.stop();
            SBackEnd = null;
            ServerPort.setEditable(true);
            ActionButton.setText("Start");
            return;
        }

        //To start
        int port;
        try
        {
            port = Integer.parseInt(ServerPort.getText().trim());
        }

        catch(Exception er)
        {
            PrintToEvent("Invalid port number");
            return;
        }

        //Starts Back end
        SBackEnd = new ServerBack(port, this);
        new ServerRunning().start();
        ActionButton.setText("Stop");
        ServerPort.setEditable(false);
    }

    //Detects close using OS UI button
    @Override
    public void windowClosing(WindowEvent e)
    {
        //Stops Back end if it is still active
        if(SBackEnd != null)
        {
            try
            {
                SBackEnd.stop();
            }

            catch(Exception eClose) {}

            SBackEnd = null;
        }

        //Closes GUI
        dispose();

        System.exit(0);
    }

    //Dummy methods
    @Override
    public void windowClosed(WindowEvent e) {}
    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}

    //A thread to run the Back end
    class ServerRunning extends Thread
    {
        @Override
        public void run()
        {
            SBackEnd.start();

            ActionButton.setText("Start");
            ServerPort.setEditable(true);
            PrintToEvent("Server crashed\n");
            SBackEnd = null;
        }
    }

    public static void main(String[] arg)
    {
        new ServerFront(25351);
    }
}