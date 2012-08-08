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
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private int i = 1;
    private String[] roomlist = new String[99999];
    int temp;
    private JSlider tempcheck = new JSlider(JSlider.VERTICAL, 0, 100, temp);
    JTabbedPane channels = new JTabbedPane();

    //Arrays for new tabs
    private ArrayList<JTextArea> taarray = new ArrayList<JTextArea>();
    private ArrayList<JPanel> panelarray = new ArrayList<JPanel>();
    private ArrayList<JScrollPane> sparray = new ArrayList<JScrollPane>();
    private ArrayList<JTabbedPane> tabbararray = new ArrayList<JTabbedPane>();
    private ArrayList<Button> sendarray = new ArrayList<Button>();
    private ArrayList<TextField> tfarray = new ArrayList<TextField>();

    public Client(String host, int port){
	//Temp check
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
			//processMessage("/changetemp " + Integer.toString(tempcheck.getValue()));
		    }
		}
	    });

	//Create main tab	
	newTab(0, "Main");
	channels.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

	//Setup layout
	BorderLayout container = new BorderLayout();
	setLayout(container);
	add(channels, BorderLayout.CENTER);

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

    public void newTab(int index, String channame){
	//Make a gridbaglayout in a JPanel
	panelarray.add(new JPanel(new GridBagLayout()));
        GridBagConstraints gbc = new GridBagConstraints();

	//Initialize the text field and send button
        tfarray.add(new TextField());
        sendarray.add(new Button("Send"));

        //Adds main text area                                                                                         
        taarray.add(new JTextArea());
        sparray.add(new JScrollPane(taarray.get(index)));
        (taarray.get(index)).setLineWrap(true);
        (taarray.get(index)).setWrapStyleWord(true);
	
	//Setup tabbar                                                                                                 
        tabbararray.add(new JTabbedPane());
        (tabbararray.get(index)).addTab("Info", new TextArea());
        (tabbararray.get(index)).addTab("Decide", null);
        (tabbararray.get(index)).addTab("Think", null);

        //Adds tabbedarea to the layout                                                                                
	gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 1;
        gbc.weightx = 50.0;
        gbc.weighty = 99.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(0,0,0,0);
        (panelarray.get(index)).add(sparray.get(index), gbc);

        //Adds the sidebar tabbar to the layout                                                                        
	gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 2;
        gbc.weightx = 50.0;
        gbc.weighty = 100.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(0,0,0,0);
        (panelarray.get(index)).add(tabbararray.get(index), gbc);

	//Adds the text field tf to the layout                                                                         
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = 45.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(0,0,0,0);
        (panelarray.get(index)).add(tfarray.get(index), gbc);

        //Adds the send button to the layout                                                                           
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 5.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.insets = new Insets(0,0,0,0);
        (panelarray.get(index)).add(sendarray.get(index), gbc);

	//Add the tab setup above
	channels.addTab(channame, panelarray.get(index));
	//Add button to tab
	if(index != 0){
	    channels.setTabComponentAt(index, new ButtonTabComponent(channels));
	}

        //Listens for the user hitting enter on tf                                                                     
        (tfarray.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    for(int x = 0; x < tfarray.size(); x++){
			if(e.getSource() == tfarray.get(x)){
			    String text = e.getActionCommand();
			    if(x == 0 || text.startsWith("/")){
				processMessage(text, 0);
			    }
			    else{
				processMessage("/message " + roomlist[x-1] + " " + text, x);
			    }
			}
		    }
                }
	    } );

        //Listens for the user pressing the button                                                                     
        (sendarray.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    for(int x = 0; x < sendarray.size(); x++){
                        if(e.getSource() == sendarray.get(x)){
			    String text = (tfarray.get(x)).getText();
                            if(x == 0 || text.startsWith("/")){
                                processMessage(text, 0);
                            }
                            else{
                                processMessage("/message " + roomlist[x-1] + " " + text, x);
                            }
                        }
                    }
                }
            } );
    }

    //Processes messages
    public void processMessage(String message, int index){
	try{
	    if(message.startsWith("/part ")){
		exitRoom(message);
	    }
	    dout.writeUTF(message);
	    (tfarray.get(index)).setText("");
	} catch(IOException ie) {
	    System.out.println(ie);
	}
    }

    //Removes tabs when exiting room
    public void exitRoom(String message){
	for(int j = 0; j < 99999; j++){
	    if(message.startsWith("/part " + roomlist[j])){
	        channels.removeTabAt(j+1);
		taarray.remove(j+1);
		tabbararray.remove(j+1);
		tfarray.remove(j+1);
		sendarray.remove(j+1);
		sparray.remove(j+1);
		panelarray.remove(j+1);
		for(int k = j+1; k < roomlist.length; k++){
		    roomlist[k-1] = roomlist[k];
		}
	    }
	}
    }

    //Processes received messages
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
			    newTab(i-1, roomlist[i-2]);
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
