import java.applet.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Panel implements Runnable
{
    private TextField tf = new TextField();
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private Button send = new Button("Send");
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JTabbedPane tabbar = new JTabbedPane();
    private int i = 1;
    private String[] roomlist = new String[99999];
    private ArrayList<JTextArea> taarray = new ArrayList<JTextArea>();
    int temp;
    private JSlider tempcheck = new JSlider(JSlider.VERTICAL, 0, 100, temp);

    public Client(String host, int port){
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();

	//Adds main text area
	taarray.add(new JTextArea());
	tabbedPane.addTab("Main", taarray.get(i-1));
	(taarray.get(i-1)).setLineWrap(true);
	(taarray.get(i-1)).setWrapStyleWord(true);
	
	tempcheck.setMajorTickSpacing(10);
	tempcheck.setPaintTicks(true);
	Hashtable labelTable = new Hashtable();
	labelTable.put(0, new JLabel(" Strongly Disagree"));
	labelTable.put(50, new JLabel(" Neutral") );
	labelTable.put(100, new JLabel(" Strongly Agree") );
	tempcheck.setLabelTable( labelTable );
	tempcheck.setPaintLabels(true);
	tempcheck.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent event){
		    JSlider tempcheck = (JSlider)event.getSource();
		    if(!tempcheck.getValueIsAdjusting()){
			processMessage("/changetemp " + Integer.toString(tempcheck.getValue()));
		    }
		}
	    });

	//Makes sidebar
	tabbar.addTab("Users", null);
	tabbar.addTab("Channels", null);
	tabbar.addTab("Vote", tempcheck);
	
	//Adds tabbedPane to the layout
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 80.0;
	gbc.weighty = 99;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(0,0,0,0);
	gbl.setConstraints(tabbedPane,gbc);
	add(tabbedPane);

	//Adds the sidebar tabbar to the layout
        gbc.gridx = 1;
        gbc.gridy = 0;
	gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 20.0;
        gbc.weighty = 99;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(0,0,0,0);
        gbl.setConstraints(tabbar,gbc);
        add(tabbar);

	//Adds the text field tf to the layout
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 80.0;
	gbc.weighty = 1.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(0,0,0,0);
	gbl.setConstraints(tf,gbc);
	add(tf);

	//Adds the send button to the layout
	gbc.gridx = 1;
	gbc.gridy = 1;
	gbc.gridwidth = 1;
	gbc.gridheight = 1;
	gbc.weightx = 20.0;
	gbc.weighty = 1.0;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.ipadx = 0;
	gbc.ipady = 0;
	gbc.insets = new Insets(0,0,0,0);
	gbl.setConstraints(send,gbc);
	add(send);

	//Listens for the user hitting enter on tf
	tf.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) {
		    processMessage(e.getActionCommand());
		}
	} );

	//Listens for the user pressing the button
        send.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    processMessage(tf.getText());
                }
	    } );

	//Connect to the server
	try{
	    socket = new Socket(host, port);
	    din = new DataInputStream(socket.getInputStream());
	    dout = new DataOutputStream(socket.getOutputStream());
	    new Thread(this).start();
	} catch(IOException ie) {
	    System.out.println(ie);
	}
    }

    //Processes messages
    public void processMessage(String message){
	try{
	    dout.writeUTF(message);
	    tf.setText("");
	} catch(IOException ie) {
	    System.out.println(ie);
	}
    }

    public void run(){
	try{
	    while(true){
		String message = din.readUTF();
		String[] messarray;
		int flag = 0;
		int portname = socket.getLocalPort();

		//Add a room to the user's room list if they join a room. Also make a tabbed pane.
		    if(message != null){
			if((message).startsWith("join;")){
			    messarray = (message).split(";", 2);
			    roomlist[i-1] = messarray[1];
			    i++;
			    taarray.add(new JTextArea());
			    (taarray.get(i-1)).setLineWrap(true);
			    (taarray.get(i-1)).setWrapStyleWord(true);
			    tabbedPane.addTab(roomlist[i-2], taarray.get(i-1));
			}
		//Process the message and send it to the appropriate room
			else{
			    for(int j = 0; j < i; j++){
				if(message.startsWith(roomlist[j] + " ")){
				    messarray = message.split(" ", 2);
				    (taarray.get(j+1)).append(messarray[1]+"\n");
				    flag = 1;
				    break;
				}
			    }
			    if(flag == 0){
				(taarray.get(0)).append(message+"\n");
			    }
			}
		    }
	    }
	} catch(IOException ie) {
	    System.out.println(ie);
	}
    }
}
