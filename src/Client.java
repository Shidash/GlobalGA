import java.applet.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class Client extends Panel implements Runnable
{
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private int i = 1;
    private String[] roomlist = new String[99999];
    JTabbedPane channels = new JTabbedPane();

    //Arrays for new tabs
    private ArrayList<JTextArea> taarray = new ArrayList<JTextArea>();
    private ArrayList<JPanel> panelarray = new ArrayList<JPanel>();
    private ArrayList<JScrollPane> sparray = new ArrayList<JScrollPane>();
    private ArrayList<JTabbedPane> tabbararray = new ArrayList<JTabbedPane>();
    private ArrayList<Button> sendarray = new ArrayList<Button>();
    private ArrayList<TextField> tfarray = new ArrayList<TextField>();
    private ArrayList<String[]> votelist = new ArrayList<String[]>();
    private ArrayList<JList> listarray = new ArrayList<JList>();
    private ArrayList<JSplitPane> votePanearray = new ArrayList<JSplitPane>();
    private ArrayList<JSplitPane> toparray = new ArrayList <JSplitPane>();
    private ArrayList<JRadioButton> tempbutton = new ArrayList<JRadioButton>();
    private ArrayList<JRadioButton> pollbutton = new ArrayList<JRadioButton>();
    private ArrayList<ButtonGroup> buttongroup = new ArrayList<ButtonGroup>();
    private ArrayList<JPanel> radiopanel = new ArrayList<JPanel>();
    private ArrayList<JPanel> cards = new ArrayList<JPanel>();
    private ArrayList<Button> back = new ArrayList<Button>();
    private ArrayList<Button> submit = new ArrayList<Button>();
    private ArrayList<TextField> tfcreate = new ArrayList<TextField>();
    private ArrayList<JPanel> incards = new ArrayList<JPanel>();
    private ArrayList<ArrayList<JPanel>> pollarray = new ArrayList<ArrayList<JPanel>>();
    private ArrayList<ArrayList<JLabel>> avgtemp = new ArrayList<ArrayList<JLabel>>();
    private ArrayList<ArrayList<JLabel>> picture = new ArrayList<ArrayList<JLabel>>();
    private ArrayList<ArrayList<JSlider>> tempcheck = new ArrayList<ArrayList<JSlider>>();
    CardLayout cl;
    CardLayout incl;
    ClassLoader cldr = this.getClass().getClassLoader();
    java.net.URL happyURL = cldr.getResource("happy.png");
    ImageIcon happy = new ImageIcon(happyURL);
    java.net.URL sadURL = cldr.getResource("sad.png");
    ImageIcon sad = new ImageIcon(sadURL);
    java.net.URL normalURL = cldr.getResource("normal.png");
    ImageIcon normal = new ImageIcon(normalURL);

    public Client(String host, int port){
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

	pollarray.add(new ArrayList<JPanel>());
	avgtemp.add(new ArrayList<JLabel>());
	picture.add(new ArrayList<JLabel>());
	incards.add(new JPanel(new CardLayout()));
	tempcheck.add(new ArrayList<JSlider>());

	//Vote list
	votelist.add(new String[99999]);
	listarray.add(new JList(votelist.get(index)));
	(listarray.get(index)).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	(listarray.get(index)).setSelectedIndex(0);
	incards.add(new JPanel(new CardLayout()));
	(listarray.get(index)).setFixedCellHeight(20);
	(listarray.get(index)).addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e){
		    JList theList = (JList)e.getSource();
		    theList.ensureIndexIsVisible(theList.getSelectedIndex());

		    if (e.getValueIsAdjusting())
			return;
		    
		    if (theList.isSelectionEmpty()) {
			
		    } else {
			int index = theList.getSelectedIndex();
			incl = (CardLayout)((incards.get(i-1)).getLayout());
			incl.show(incards.get(i-1), Integer.toString(index));
		    }
		}
	    });

	cards.add(new JPanel(new CardLayout()));

	//Setup vote pane
	votePanearray.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listarray.get(index), incards.get(index)));
        (votePanearray.get(index)).setDividerLocation(200);
	(listarray.get(index)).setMinimumSize(new Dimension(100, 50));
	(incards.get(index)).setMinimumSize(new Dimension(100, 50));

	//Top object
	tempbutton.add(new JRadioButton("Temperature Check"));
        (tempbutton.get(index)).setMnemonic(KeyEvent.VK_B);
        (tempbutton.get(index)).setActionCommand("Temperature Check");

        pollbutton.add(new JRadioButton("Poll"));
        (pollbutton.get(index)).setMnemonic(KeyEvent.VK_C);
        (pollbutton.get(index)).setActionCommand("Poll");

	buttongroup.add(new ButtonGroup());
	(buttongroup.get(index)).add(tempbutton.get(index));
	(buttongroup.get(index)).add(pollbutton.get(index));

	back.add(new Button("Back"));
	tfcreate.add(new TextField());
	submit.add(new Button("Submit"));

	(back.get(index)).addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event){
		    for(int x = 0; x < back.size(); x++){
			if(event.getSource() == back.get(x)){
			    cl.first(cards.get(x));
			}
		    }
		}
	    });
	
	(tfcreate.get(index)).addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event){
		    for(int x = 0; x < tfcreate.size(); x++){
			if(event.getSource() == tfcreate.get(x)){
			    String text = event.getActionCommand();
			    if(x == 0){
				processMessage("/newtempcheck " + "bluh " + text, 0);
			    }
			    else{
				processMessage("/newtempcheck " + roomlist[x-1] + " " + text, x);
			    }
			    (tfcreate.get(x)).setText("");
			    cl.first(cards.get(x));
			}
		    };
		}
	    });

	(submit.get(index)).addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event){
		    for(int x = 0; x < submit.size(); x++){
			if(event.getSource() == submit.get(x)){
			    String text = (tfcreate.get(x)).getText();
			    if(x == 0){
				processMessage("/newtempcheck " + "bluh " + text, 0);
			    }
			    else{
				processMessage("/newtempcheck " + roomlist[x-1] + " " + text, x);
			    }
			    (tfcreate.get(x)).setText("");
			    cl.first(cards.get(x));
			}
		    }
		}
	    });

	
	//Tempcheck creation
	(tempbutton.get(index)).addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
		    for(int x = 0; x < tempbutton.size(); x++){
			if(e.getSource() == tempbutton.get(x)){
			    JPanel temppanel = new JPanel();
			    (cards.get(x)).add(temppanel, "Tempcheck");
			    cl = (CardLayout)((cards.get(x)).getLayout());
			    cl.show(cards.get(x), "Tempcheck");
			    JPanel backpanel = new JPanel();
			    (back.get(x)).setPreferredSize(new Dimension(100, 30));
			    (submit.get(x)).setPreferredSize(new Dimension(100, 30));
			    (tfcreate.get(x)).setPreferredSize(new Dimension(200, 30));
			    JPanel sendpanel = new JPanel();
			    JLabel temptopic = new JLabel("Topic of Temperature Check: ");
			    JPanel sendbutton = new JPanel();
			    backpanel.add(back.get(x));
			    backpanel.add(submit.get(x));
			    sendpanel.add(temptopic);
			    sendpanel.add(tfcreate.get(x));
			    temppanel.setLayout(new BoxLayout(temppanel, BoxLayout.PAGE_AXIS));
			    temppanel.add(Box.createHorizontalGlue());
			    temppanel.add(Box.createRigidArea(new Dimension(40, 100)));
			    temppanel.add(sendpanel);
			    temppanel.add(backpanel);
			}
		    }
		}
	    });

	//Poll creation
	(pollbutton.get(index)).addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
		    for(int x = 0; x < pollbutton.size(); x++){
			if(e.getSource() == pollbutton.get(x)){
			    JPanel pollpanel = new JPanel();
			    (cards.get(x)).add(pollpanel, "poll");
			    cl = (CardLayout)((cards.get(x)).getLayout());
			    cl.show(cards.get(x), "poll");
			    back.add(new Button("Back"));
			    pollpanel.add(back.get(x));
			    (back.get(x)).addActionListener(new ActionListener() {
				    public void actionPerformed(ActionEvent event){
					for(int w = 0; w < back.size(); w++){
					    if(event.getSource() == submit.get(w)){
						cl.first(cards.get(w));
					    }
					}
				    }
				});
			}
		    }
		}
	    });

	//JPanel radio panel
	radiopanel.add(new JPanel());
	JLabel question = new JLabel("Create a new vote:\n");
	(radiopanel.get(index)).add(question);
	(radiopanel.get(index)).add(tempbutton.get(index));
	(radiopanel.get(index)).add(pollbutton.get(index));

	//Setup top of pane
	toparray.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, radiopanel.get(index), votePanearray.get(index)));
	(toparray.get(index)).setDividerLocation(100);
	(votePanearray.get(index)).setMinimumSize(new Dimension(100, 50));
	(radiopanel.get(index)).setMinimumSize(new Dimension(100, 15));
	(radiopanel.get(index)).setPreferredSize(new Dimension(100, 30));
        (toparray.get(index)).setPreferredSize(new Dimension(500, 400));

	(cards.get(index)).add(toparray.get(index), "Home");

	//Setup tabbar                                                                                                 
        tabbararray.add(new JTabbedPane());
        (tabbararray.get(index)).addTab("Info", new TextArea());
        (tabbararray.get(index)).addTab("Decide", cards.get(index));

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

    public JPanel tempObject(ArrayList<JPanel> pollarray, int index, int num, String tempq, ArrayList<JLabel> avgtemp){
	pollarray.add(new JPanel());
	int temp = 50;
	(tempcheck.get(index)).add(new JSlider(JSlider.VERTICAL, 0, 100, temp));
	((tempcheck.get(index)).get(num)).setMajorTickSpacing(10);
	((tempcheck.get(index)).get(num)).setPaintTicks(true);
	((tempcheck.get(index)).get(num)).setPreferredSize(new Dimension(300, 225));
	Hashtable labelTable = new Hashtable();
	labelTable.put(1, new JLabel("   Strongly Disagree"));
	labelTable.put(50, new JLabel("   Neutral") );
	labelTable.put(99, new JLabel("   Strongly Agree") );
	((tempcheck.get(index)).get(num)).setLabelTable( labelTable );
	((tempcheck.get(index)).get(num)).setPaintLabels(true);

	((tempcheck.get(index)).get(num)).addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent event){
		    for(int x = 0; x < tempcheck.size(); x++){
			for(int z = 0; z < (tempcheck.get(x)).size(); z++){
			    if(event.getSource() == ((tempcheck.get(x)).get(z))){
				if(x==0){
				    processMessage("/changetemp " + "bluh " + z + " " + Integer.toString(((tempcheck.get(x)).get(z)).getValue()), x);
				}
				else{
				    processMessage("/changetemp " + roomlist[x] + " " + z + " " + Integer.toString(((tempcheck.get(x)).get(z)).getValue()), i-1);
				}
			    }
			}
		    }
		}
	    });

      	
	(pollarray.get(num)).setLayout(new BoxLayout((pollarray.get(num)), BoxLayout.PAGE_AXIS));
	votelist.get(index)[num] = tempq;
	JPanel tempane = new JPanel();
	tempane.add((tempcheck.get(index)).get(num));
	JLabel question = new JLabel(tempq);
	JPanel qpanel = new JPanel();
	qpanel.add(question);
	JPanel avgpane = new JPanel();
	avgtemp.add(new JLabel("Average Temperature: 50\n"));
	(picture.get(index)).add(new JLabel(normal));
	avgpane.add(avgtemp.get(num));
	avgpane.add((picture.get(index)).get(num));
	(pollarray.get(num)).add(qpanel);
	(pollarray.get(num)).add(tempane);
	(pollarray.get(num)).add(avgpane);
	return pollarray.get(num);
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
				    if(messarray[1].startsWith(";tempup ")){
					//send update to the right temp in the right room
					String[] mess2array;
					mess2array = messarray[1].split(" ", 3);
					((avgtemp.get(j+1)).get(Integer.parseInt(mess2array[1]))).setText("Average Temperature: " + mess2array[2]);
					if(Double.parseDouble(mess2array[2]) > 66){
					    ((picture.get(j+1)).get(Integer.parseInt(mess2array[1]))).setIcon(happy);
					}
					else if(Double.parseDouble(mess2array[2]) < 33){
					    ((picture.get(j+1)).get(Integer.parseInt(mess2array[1]))).setIcon(sad);
					}
					else{
					    ((picture.get(j+1)).get(Integer.parseInt(mess2array[1]))).setIcon(normal);
					    }
				    }
				    else if(messarray[1].startsWith(";newtemp ")){
					String[] mess2array;
					mess2array = messarray[1].split(" ", 3);
					(incards.get(j+11)).add(tempObject(pollarray.get(j+1), j+1, Integer.parseInt(messarray[1]), messarray[2], avgtemp.get(j+1)), messarray[1]);
				    }
				    else{
					(taarray.get(j+1)).append(messarray[1]+"\n");
				    }
				    flag = 1;
				    break;
				}
			    }
			    if(flag == 0){
				if(message.startsWith(";tempup ")){
				    //send update to the right temp in the right room
				    messarray = message.split(" ", 3);
				    ((avgtemp.get(0)).get(Integer.parseInt(messarray[1]))).setText("Average Temperature: " + messarray[2]);
				    
				    if(Double.parseDouble(messarray[2]) > 66){
					((picture.get(0)).get(Integer.parseInt(messarray[1]))).setIcon(happy);
				    }
				    else if(Double.parseDouble(messarray[2]) < 33){
                                        ((picture.get(0)).get(Integer.parseInt(messarray[1]))).setIcon(sad);
				    } 
				    else{
                                        ((picture.get(0)).get(Integer.parseInt(messarray[1]))).setIcon(normal);
				    }
				}
				else if(message.startsWith(";newtemp ")){
				    messarray = message.split(" ", 3);
				    (incards.get(0)).add(tempObject(pollarray.get(0), 0, Integer.parseInt(messarray[1]), messarray[2], avgtemp.get(0)), messarray[1]);                 
				}
				else{
				    (taarray.get(0)).append(message+"\n");
				}
			    }
			}
		    }
	    }
	} catch(IOException ie) {
	    System.out.println(ie);
	}
    }
}
