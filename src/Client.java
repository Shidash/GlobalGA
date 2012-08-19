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
    private ArrayList<ArrayList<JLabel>> picture = new ArrayList<ArrayList<JLabel>>(); 
    private ArrayList<ArrayList<JPanel>> pollarray = new ArrayList<ArrayList<JPanel>>();
    private ArrayList<ArrayList<JLabel>> avgtemp = new ArrayList<ArrayList<JLabel>>();
    private ArrayList<ArrayList<JSlider>> tempcheck = new ArrayList<ArrayList<JSlider>>();
    private ArrayList<ArrayList<ArrayList<JRadioButton>>> radiolist = new ArrayList<ArrayList<ArrayList<JRadioButton>>>();
    private ArrayList<ArrayList<ArrayList<JCheckBox>>> checklist = new ArrayList<ArrayList<ArrayList<JCheckBox>>>();
    private ArrayList<ArrayList<ArrayList<String>>> pollanswers = new ArrayList<ArrayList<ArrayList<String>>>();
    private ArrayList<ArrayList<ArrayList<JLabel>>> pollresults = new ArrayList<ArrayList<ArrayList<JLabel>>>();
    private ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<ArrayList<ArrayList<String>>>();
    private ArrayList<ArrayList<JButton>> submitvote = new ArrayList<ArrayList<JButton>>();
    private ArrayList<ArrayList<JPanel>> pollPanel = new ArrayList<ArrayList<JPanel>>();
    private ArrayList<String[]> users = new ArrayList<String[]>();
    private ArrayList<JList> userlist = new ArrayList<JList>();
    private ArrayList<JPanel> infopanel = new ArrayList<JPanel>();
    private ArrayList<JSplitPane> infopane = new ArrayList<JSplitPane>();
    private ArrayList<JButton> registerbutton = new ArrayList<JButton>();
    private ArrayList<JButton> identifybutton = new ArrayList<JButton>();
    private ArrayList<JButton> newnickbutton = new ArrayList<JButton>();
    private ArrayList<TextField> pollquestion = new ArrayList<TextField>();
    private ArrayList<ArrayList<TextField>> pollresponse = new ArrayList<ArrayList<TextField>>();
    private ArrayList<Button> submitpoll = new ArrayList<Button>();
    private ArrayList<Button> addresponse = new ArrayList<Button>();
    private ArrayList<JPanel> responsepanel = new ArrayList<JPanel>();
    private ArrayList<JCheckBox> pollcheck = new ArrayList<JCheckBox>();
    TextField rpassfield;
    TextField rname;
    TextField ipassfield;
    TextField iname;
    TextField cname;
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
	pollPanel.add(new ArrayList<JPanel>());

	radiolist.add(new ArrayList<ArrayList<JRadioButton>>());
	checklist.add(new ArrayList<ArrayList<JCheckBox>>());
	pollanswers.add(new ArrayList<ArrayList<String>>());
	pollresults.add(new ArrayList<ArrayList<JLabel>>());
	results.add(new ArrayList<ArrayList<String>>());
	submitvote.add(new ArrayList<JButton>());

	//Vote list
	votelist.add(new String[99999]);
	listarray.add(new JList(votelist.get(index)));
	(listarray.get(index)).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	(listarray.get(index)).setSelectedIndex(0);
	incards.add(new JPanel(new CardLayout()));
	(listarray.get(index)).setFixedCellHeight(20);
	(listarray.get(index)).addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e){
		    for(int x = 0; x < listarray.size(); x++){
			JList theList = (JList)e.getSource();
			if(theList == listarray.get(x)){
			    theList.ensureIndexIsVisible(theList.getSelectedIndex());
			    
			    if (e.getValueIsAdjusting())
				return;
			    
			    if (theList.isSelectionEmpty()) {
				
			    } else {
				int index = theList.getSelectedIndex();
				incl = (CardLayout)((incards.get(x)).getLayout());
				incl.show(incards.get(x), Integer.toString(index));
			    }
			}
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
	pollquestion.add(new TextField());
	pollresponse.add(new ArrayList<TextField>());
	submitpoll.add(new Button("Submit"));
	addresponse.add(new Button("Add Response"));
	responsepanel.add(new JPanel());
	pollcheck.add(new JCheckBox());
	(pollresponse.get(index)).add(new TextField());
	(pollresponse.get(index)).add(new TextField());

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

	//Submit poll
        (submitpoll.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    for(int x = 0; x < submitpoll.size(); x++){
                        if(event.getSource() == submitpoll.get(x)){
			    String question = (pollquestion.get(x)).getText();
			    String responses = "";
			    String numrep;
			    if((pollcheck.get(x)).isSelected()){
				numrep = "2";
			    }
			    else{
				numrep = "1";
			    }
			    for(int p = 0; p < (pollresponse.get(x)).size(); p++){
				responses = responses + "," + ((pollresponse.get(x)).get(p)).getText();
				((pollresponse.get(x)).get(p)).setText("");
			    }	    
                            if(x == 0){
                                processMessage("/newpoll " + "bluh " + numrep + " " + question + responses, 0);
                            }
                            else{
                                processMessage("/newpoll " + roomlist[x-1] + " " + numrep + " " + question + responses, x);
                            }
			    for(int q = 0; q < (pollresponse.get(x)).size(); q++){
				(responsepanel.get(x)).remove((pollresponse.get(x)).get(q));
				if(q > 1){
				    (pollresponse.get(x)).remove(q);
				}
			    }
			    (responsepanel.get(x)).validate();
			    if((pollcheck.get(x)).isSelected()){
                                (pollcheck.get(x)).setSelected(false);
			    }
                            (pollquestion.get(x)).setText("");
                            cl.first(cards.get(x));
                        }
                    }
                }
            });

	
        (addresponse.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    for(int x = 0; x < addresponse.size(); x++){
                        if(event.getSource() == addresponse.get(x)){
                            (pollresponse.get(x)).add(new TextField());
			    (responsepanel.get(x)).add((pollresponse.get(x)).get((pollresponse.get(x)).size()-1));
			    (responsepanel.get(x)).validate();
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
			    JPanel backpanel = new JPanel();
			    (back.get(x)).setPreferredSize(new Dimension(100, 30));
			    (submitpoll.get(x)).setPreferredSize(new Dimension(100, 30));
			    (pollquestion.get(x)).setPreferredSize(new Dimension(200, 30));
			    (addresponse.get(x)).setPreferredSize(new Dimension(100, 30));
			    JPanel question =new JPanel();
			    question.add(new JLabel("Poll Question: "));
			    question.add(pollquestion.get(x));
			    (responsepanel.get(x)).setLayout(new BoxLayout((responsepanel.get(x)), BoxLayout.PAGE_AXIS));
			    (responsepanel.get(x)).add((pollresponse.get(x)).get(0));
			    (responsepanel.get(x)).add((pollresponse.get(x)).get(1));
			    (responsepanel.get(x)).validate();
			    back.add(new Button("Back"));
			    backpanel.add(back.get(x));
			    backpanel.add(submitpoll.get(x));
			    JPanel checkpanel = new JPanel();
			    checkpanel.add(pollcheck.get(x));
			    checkpanel.add(new JLabel(" Allow Multiple Responses"));
			    pollpanel.setLayout(new BoxLayout(pollpanel, BoxLayout.PAGE_AXIS));
			    pollpanel.add(question);
			    JPanel titlepanel = new JPanel();
			    titlepanel.add(new JLabel("Responses"));
			    pollpanel.add(titlepanel);
			    pollpanel.add((responsepanel.get(x)));
			    pollpanel.add(addresponse.get(x));
			    pollpanel.add(checkpanel);
			    pollpanel.add(backpanel);
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
	
	//Create infopane
	users.add(new String[99999]);
	userlist.add(new JList(users.get(index)));
	(userlist.get(index)).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	(userlist.get(index)).setSelectedIndex(0);
	(userlist.get(index)).setFixedCellHeight(20);
	(userlist.get(index)).addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e){
                    JList theList = (JList)e.getSource();
                    theList.ensureIndexIsVisible(theList.getSelectedIndex());

                    if (e.getValueIsAdjusting())
                        return;

                    if (theList.isSelectionEmpty()) {

                    } else {
                      
                    }
                }
            });

	infopanel.add(new JPanel());
	(infopanel.get(index)).setLayout(new BoxLayout(infopanel.get(index), BoxLayout.PAGE_AXIS));
	JPanel regpanel = new JPanel();
	GridLayout reglayout = new GridLayout(4, 2);
	regpanel.setLayout(reglayout);
	regpanel.add(new JLabel("Register- "));
	regpanel.add(new JLabel(""));
	rname = new TextField();
	regpanel.add(new JLabel("Nickname: "));
	regpanel.add(rname);
	rpassfield = new TextField();
	regpanel.add(new JLabel("Password: "));
	regpanel.add(rpassfield);
	regpanel.add(new JLabel(""));
	registerbutton.add(new JButton("Submit"));
	regpanel.add(registerbutton.get(index));

	rname.setMinimumSize(new Dimension(100, 20));
        rname.setPreferredSize(new Dimension(100, 20));
	rpassfield.setMinimumSize(new Dimension(100, 20));
        rpassfield.setPreferredSize(new Dimension(100, 20));
	(registerbutton.get(index)).setMinimumSize(new Dimension(50, 20));
        (registerbutton.get(index)).setPreferredSize(new Dimension(50, 20));

	//Listens for the user pressing the button                                                                                       
        (registerbutton.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    String name = rname.getText();
		    String password = rpassfield.getText();
		    processMessage("/register " + name + " " + password, 0);
		    rname.setText("");
		    rpassfield.setText("");
                }
            } );
	(infopanel.get(index)).add(regpanel);

	JPanel identpanel = new JPanel();
	identpanel.setLayout(reglayout);
        identpanel.add(new JLabel("Login- "));
	identpanel.add(new JLabel(""));
        iname = new TextField();
        identpanel.add(new JLabel("Nickname: "));
        identpanel.add(iname);
        ipassfield = new TextField();
        identpanel.add(new JLabel("Password: "));
        identpanel.add(ipassfield);
	identpanel.add(new JLabel(""));
        identifybutton.add(new JButton("Submit"));
        identpanel.add(identifybutton.get(index));

	iname.setMinimumSize(new Dimension(100, 20));
        iname.setPreferredSize(new Dimension(100, 20));
        ipassfield.setMinimumSize(new Dimension(100, 20));
        ipassfield.setPreferredSize(new Dimension(100, 20));
        (identifybutton.get(index)).setMinimumSize(new Dimension(50, 20));
        (identifybutton.get(index)).setPreferredSize(new Dimension(50, 20));

        //Listens for the user pressing the button                                                                                       
        (identifybutton.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {  
                    String name = iname.getText();
                    String password = ipassfield.getText();
                    processMessage("/identify " + name + " " + password, 0);
		    iname.setText("");
		    ipassfield.setText("");                                                                      
                }
            } );

	(infopanel.get(index)).add(identpanel);

	JPanel changenick = new JPanel();
	GridLayout changelayout = new GridLayout(3, 2);
	changenick.setLayout(changelayout);
	changenick.add(new JLabel("Change Nick- "));
	changenick.add(new JLabel(""));
	cname = new TextField();
	changenick.add(new JLabel("New Nick: "));
	changenick.add(cname);
	changenick.add(new JLabel(""));
	newnickbutton.add(new JButton("Submit"));
	changenick.add(newnickbutton.get(index));

	cname.setMinimumSize(new Dimension(100, 20));
        cname.setPreferredSize(new Dimension(100, 20));
        (newnickbutton.get(index)).setMinimumSize(new Dimension(50, 20));
        (newnickbutton.get(index)).setPreferredSize(new Dimension(50, 20));

	//Listens for the user pressing the button                                                                                       
        (newnickbutton.get(index)).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {                                                                             
                    String name = cname.getText();
                    processMessage("/nick " + name, 0);
                    cname.setText("");                                                                             
                }
            } );
	 (infopanel.get(index)).add(changenick);
	

	infopane.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userlist.get(index), infopanel.get(index)));
	(infopane.get(index)).setDividerLocation(200);
        (userlist.get(index)).setMinimumSize(new Dimension(100, 50));
        (infopanel.get(index)).setMinimumSize(new Dimension(100, 50));
        (userlist.get(index)).setPreferredSize(new Dimension(100, 50));
        (infopanel.get(index)).setPreferredSize(new Dimension(100, 50));

	//Setup tabbar                                                                                                 
        tabbararray.add(new JTabbedPane());
        (tabbararray.get(index)).addTab("Info", infopane.get(index));
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
				(tfarray.get(x)).setText("");
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
				(tfarray.get(x)).setText("");
                            }
                            else{
                                processMessage("/message " + roomlist[x-1] + " " + text, x);
                            }
                        }
                    }
                }
            } );
    }

    public JPanel pollObject(ArrayList<JPanel> pollarray, int index, int pollnum, int num, String pollq, ArrayList<String> responses, int ansnum){
	pollarray.add(new JPanel());
	(pollarray.get(num)).setLayout(new BoxLayout((pollarray.get(num)), BoxLayout.PAGE_AXIS));
	votelist.get(index)[num] = pollq;
	(pollarray.get(num)).add(new JLabel(pollq));
	(pollPanel.get(index)).add(new JPanel(new GridLayout(0, 1)));

	(submitvote.get(index)).add(new JButton("Submit"));
	(radiolist.get(index)).add(new ArrayList<JRadioButton>());
	(checklist.get(index)).add(new ArrayList<JCheckBox>());
	(results.get(index)).add(new ArrayList<String>());
	(pollanswers.get(index)).add(responses);
	(pollresults.get(index)).add(new ArrayList<JLabel>());
	
	for(int q = 0; q < responses.size(); q++){
	    ((pollresults.get(index)).get(pollnum)).add(new JLabel(" (0 votes)"));
	}

	//List radio buttons
	if(ansnum > 1){
	    for(int s = 0; s < responses.size(); s++){
                ((checklist.get(index)).get(pollnum)).add(new JCheckBox(responses.get(s)));
		((pollPanel.get(index)).get(pollnum)).add(((checklist.get(index)).get(pollnum)).get(s));
		(((checklist.get(index)).get(pollnum)).get(s)).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    for(int x = 0; x < checklist.size(); x++){
				for(int y = 0; y < (checklist.get(x)).size(); y++){
				    for(int z = 0; z < ((checklist.get(x)).get(y)).size(); z++){
					if(e.getSource() == ((checklist.get(x)).get(y)).get(z)){
					    if((((checklist.get(x)).get(y)).get(z)).isSelected()){
						((results.get(x)).get(y)).add(Integer.toString(z));
					    }
					    else{
						((results.get(x)).get(y)).remove(((results.get(x)).get(y)).indexOf(Integer.toString(z)));
					    }
					}
				    }
				}
			    }
			}
		    });
	    }
	}
	else{
	    ButtonGroup buttongroup = new ButtonGroup();
	    for(int s = 0; s < responses.size(); s++){
	       ((radiolist.get(index)).get(pollnum)).add(new JRadioButton(responses.get(s)));
	       buttongroup.add(((radiolist.get(index)).get(pollnum)).get(s));
	       ((pollPanel.get(index)).get(pollnum)).add(((radiolist.get(index)).get(pollnum)).get(s));
	       (((radiolist.get(index)).get(pollnum)).get(s)).addActionListener(new ActionListener() {
		       public void actionPerformed(ActionEvent e) {
			   for(int x = 0; x < radiolist.size(); x++){
			       for(int y = 0; y < (radiolist.get(x)).size(); y++){
				   for(int z = 0; z < ((radiolist.get(x)).get(y)).size(); z++){
				       if(e.getSource() == ((radiolist.get(x)).get(y)).get(z)){
					   if((((radiolist.get(x)).get(y)).get(z)).isSelected()){
					       ((results.get(x)).get(y)).clear();
					       ((results.get(x)).get(y)).add(Integer.toString(z));
					   }
				       }
				   }
			       }
			   }
		       }
		   });
	    }
	}
	
	((submitvote.get(index)).get(pollnum)).addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			for(int x = 0; x < submitvote.size(); x++){
			    for(int z = 0; z < (submitvote.get(x)).size(); z++){
				if(e.getSource() == ((submitvote.get(x)).get(z))){
				    String send;
				    if(x == 0){
					send = "/trackresponse " + "bluh " + z + " ";
				    }
				    else{
					send = "/trackresponse " + roomlist[x-1] + " " + z + " ";
				    }

				    for(int k = 0; k < ((results.get(x)).get(z)).size(); k++){
					send = send + ((results.get(x)).get(z)).get(k) + " ";
				    }
				    processMessage(send, x);
				    ((pollPanel.get(x)).get(z)).removeAll();
				    ((pollPanel.get(x)).get(z)).setLayout(new GridLayout(0, 1));
				    for(int w = 0; w < ((pollanswers.get(x)).get(z)).size(); w++){
					JPanel respage = new JPanel();
					respage.add(new JLabel(((pollanswers.get(x)).get(z)).get(w)));
					respage.add(((pollresults.get(x)).get(z)).get(w));
					((pollPanel.get(x)).get(z)).add(respage);
				    }
				    ((pollPanel.get(x)).get(z)).updateUI();
				}
			    }
			}
		    }
		});

	((pollPanel.get(index)).get(pollnum)).add((submitvote.get(index)).get(pollnum));
	(pollarray.get(num)).add((pollPanel.get(index)).get(pollnum));
	return pollarray.get(num);
    }

    public JPanel tempObject(ArrayList<JPanel> pollarray, int index, int num, int tempnum, String tempq, ArrayList<JLabel> avgtemp){
	pollarray.add(new JPanel());
	int temp = 50;
	(tempcheck.get(index)).add(new JSlider(JSlider.VERTICAL, 0, 100, temp));
	((tempcheck.get(index)).get(tempnum)).setMajorTickSpacing(10);
	((tempcheck.get(index)).get(tempnum)).setPaintTicks(true);
	((tempcheck.get(index)).get(tempnum)).setPreferredSize(new Dimension(300, 225));
	Hashtable labelTable = new Hashtable();
	labelTable.put(1, new JLabel("   Strongly Disagree"));
	labelTable.put(50, new JLabel("   Neutral") );
	labelTable.put(99, new JLabel("   Strongly Agree") );
	((tempcheck.get(index)).get(tempnum)).setLabelTable( labelTable );
	((tempcheck.get(index)).get(tempnum)).setPaintLabels(true);

	((tempcheck.get(index)).get(tempnum)).addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent event){
		    for(int x = 0; x < tempcheck.size(); x++){
			for(int z = 0; z < (tempcheck.get(x)).size(); z++){
			    if(event.getSource() == ((tempcheck.get(x)).get(z))){
				if(x==0){
				    processMessage("/changetemp " + "bluh " + z + " " + Integer.toString(((tempcheck.get(x)).get(z)).getValue()), x);
				}
				else{
				    processMessage("/changetemp " + roomlist[x-1] + " " + z + " " + Integer.toString(((tempcheck.get(x)).get(z)).getValue()), i-1);
				}
			    }
			}
		    }
		}
	    });

      	
	(pollarray.get(num)).setLayout(new BoxLayout((pollarray.get(num)), BoxLayout.PAGE_AXIS));
	votelist.get(index)[num] = tempq;
	JPanel tempane = new JPanel();
	tempane.add((tempcheck.get(index)).get(tempnum));
	JLabel question = new JLabel(tempq);
	JPanel qpanel = new JPanel();
	qpanel.add(question);
	JPanel avgpane = new JPanel();
	avgtemp.add(new JLabel("Average Temperature: 50\n"));
	(picture.get(index)).add(new JLabel(normal));
	avgpane.add(avgtemp.get(tempnum));
	avgpane.add((picture.get(index)).get(tempnum));
	(pollarray.get(num)).add(qpanel);
	(pollarray.get(num)).add(tempane);
	(pollarray.get(num)).add(avgpane);
	return pollarray.get(num);
    }

    //Processes messages
    public void processMessage(String message, int index){
	try{
	    if(message.equalsIgnoreCase("")){
	    }
	    else if(message.startsWith("/part ")){
		
		dout.writeUTF(message);
		exitRoom(message);
	    }
	    else{
		dout.writeUTF(message);
		(tfarray.get(index)).setText("");
	    }
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
		pollarray.remove(j+1);
		votelist.remove(j+1);
		listarray.remove(j+1);
		votePanearray.remove(j+1);
		toparray.remove(j+1);
		tempbutton.remove(j+1);
		pollbutton.remove(j+1);
		buttongroup.remove(j+1);
		radiopanel.remove(j+1);
		registerbutton.remove(j+1);
		identifybutton.remove(j+1);
		newnickbutton.remove(j+1);
		picture.remove(j+1);
		cards.remove(j+1);
		back.remove(j+1);
		submit.remove(j+1);
		tfcreate.remove(j+1);
		incards.remove(j+1);
		avgtemp.remove(j+1);
		tempcheck.remove(j+1);
		sendarray.remove(j+1);
		sparray.remove(j+1);
		panelarray.remove(j+1);
		users.remove(j+1);
		userlist.remove(j+1);
		infopanel.remove(j+1);
		infopane.remove(j+1);
		results.remove(j+1);
		checklist.remove(j+1);
		radiolist.remove(j+1);
		submitvote.remove(j+1);
		pollresponse.remove(j+1);
		pollcheck.remove(j+1);
		responsepanel.remove(j+1);
		addresponse.remove(j+1);
		submitpoll.remove(j+1);
		pollquestion.remove(j+1);
		pollanswers.remove(j+1);
		pollresults.remove(j+1);
		pollPanel.remove(j+1);
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
				    else if(messarray[1].startsWith(";pollup ")){
					String[] mess2array;
					mess2array = messarray[1].split(" ");

					for(int jk = 3; jk < mess2array.length; jk++){
					    (((pollresults.get(j+1)).get(Integer.parseInt(mess2array[1]))).get(Integer.parseInt(mess2array[jk]))).setText(" (" + mess2array[jk+1] + " votes)");
					    jk = jk+1;
					}
				    }
				    else if(messarray[1].startsWith(";newpoll ")){
					String[] mess2array;
					mess2array = messarray[1].split(" ", 5);
					String[] qresparray = mess2array[4].split(",");
					ArrayList<String> responsearray = new ArrayList<String>();
					for(int d = 1; d < qresparray.length; d++){
					    responsearray.add(qresparray[d]);
					}
   
					(incards.get(j+1)).add(pollObject(pollarray.get(j+1), j+1, Integer.parseInt(mess2array[1]), Integer.parseInt(mess2array[2]), qresparray[0], responsearray, Integer.parseInt(mess2array[3])), mess2array[2]);
				    }
				    else if(messarray[1].startsWith(";newtemp ")){
					String[] mess2array;
					mess2array = messarray[1].split(" ", 4);
					(incards.get(j+1)).add(tempObject(pollarray.get(j+1), j+1, Integer.parseInt(messarray[1]), Integer.parseInt(messarray[2]), messarray[3], avgtemp.get(j+1)), mess2array[2]);
				    }
				    else if(messarray[1].startsWith(";updatelist")){
					String[] mess2array;
					mess2array = messarray[1].split(" ");
					for(int p = 1; p < mess2array.length; p++){
					    (users.get(j+1))[p-1] = mess2array[p];
					}
					if((users.get(j+1)).length > (mess2array.length-1)){
					    for(int b = (mess2array.length-1); b < (users.get(j+1)).length; b++){
						(users.get(j+1))[b] = null;
					    }
					}
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
				else if(message.startsWith(";pollup ")){
				    messarray = message.split(" ");
				    
				    for(int jk = 3; jk < messarray.length; jk++){
					(((pollresults.get(0)).get(Integer.parseInt(messarray[1]))).get(Integer.parseInt(messarray[jk]))).setText(" (" + messarray[jk+1] + " votes)");
					jk = jk+1;
				    }
				}
				else if(message.startsWith(";newtemp ")){
				    messarray = message.split(" ", 4);
				    (incards.get(0)).add(tempObject(pollarray.get(0), 0, Integer.parseInt(messarray[1]), Integer.parseInt(messarray[2]), messarray[3], avgtemp.get(0)), messarray[2]);                 
				}
				else if(message.startsWith(";newpoll ")){
				    messarray = message.split(" ", 5);
				    String[] qresparray = messarray[4].split(",");
				    ArrayList<String> responsearray = new ArrayList<String>();
				    for(int d = 1; d < qresparray.length; d++){
					responsearray.add(qresparray[d]);
				    }
				    (incards.get(0)).add(pollObject(pollarray.get(0), 0, Integer.parseInt(messarray[1]), Integer.parseInt(messarray[2]), qresparray[0], responsearray, Integer.parseInt(messarray[3])), messarray[2]);
				}
				else if(message.startsWith(";updatelist")){
				    messarray = message.split(" ");
				    for(int p = 1; p < messarray.length; p++){
					(users.get(0))[p-1] = messarray[p];
				    } 
				    if((users.get(0)).length > (messarray.length-1)){
					for(int b = (messarray.length-1); b < (users.get(0)).length; b++){
					    (users.get(0))[b] = null;
					}
				    }
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
