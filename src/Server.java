import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
    //For accepting new connections
    private ServerSocket ss;    
    private Hashtable outputStreams = new Hashtable();
    private Hashtable users = new Hashtable();
    private Hashtable regnames = new Hashtable();
    private ArrayList<ArrayList<Hashtable<String, Double>>> temptable = new ArrayList<ArrayList<Hashtable<String, Double>>>();
    private ArrayList<Hashtable<String, String>> votetable = new ArrayList<Hashtable<String, String>>();
    private ArrayList<ArrayList<Hashtable<String, String>>> polltable = new ArrayList<ArrayList<Hashtable<String, String>>>();
    HashMap<String, ArrayList> userroomlist = new HashMap<String, ArrayList>();
    String name;
    int numuser = 0;
    String room;
    int roomnum;
    private String userpass;
    private String[] userpassarray;
    private String password;
    private ArrayList<Hashtable<String, Object>> rooms = new ArrayList<Hashtable<String, Object>>();
    Hashtable<String, Integer> roomlist = new Hashtable<String, Integer>();
    double tempavg;

    //Starts listening
    public Server(int port) throws IOException{
	listen(port);
    }

    //Listener that accepts connections and autoassigns nicknames                                                
    private void listen(int port) throws IOException{
	ss = new ServerSocket(port);
	System.out.println("Listening on "+ss);

	//Initiate objects for votes
	temptable.add(new ArrayList<Hashtable<String, Double>>());
	polltable.add(new ArrayList<Hashtable<String, String>>());
        votetable.add(new Hashtable<String, String>());

	while(true){
	    Socket socket = ss.accept();
	    System.out.println("Connection from "+socket);
	    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
	    outputStreams.put(socket, dout);
	    name = "nick" + numuser;

	    //Track users
	    users.put(name, name);
	    userroomlist.put(name, new ArrayList<String>());
	    numuser++;

	    //Start new server thread
	    new ServerThread(this, socket);
	    sendListAll();
	}
    }

    //Get all output streams (one per client)
    Enumeration getOutputStreams() {
	return outputStreams.elements();
    }

    //Create a new temp check
    void newtempcheck(String message, String name, Socket socket, int roomnum){
	String tempstring = message.substring(14);
	String strarray[] = tempstring.split(" ", 2);

	//Add and send info to channels
	if(roomnum == 0){
	    (temptable.get(0)).add(new Hashtable());
	    (votetable.get(0)).put("t" + Integer.toString((temptable.get(0)).size()-1), Integer.toString((votetable.get(0)).size()));
	    sendToAll(";newtemp " + Integer.toString((temptable.get(0)).size()-1) + " " + Integer.toString((votetable.get(0)).size()-1) + " " + strarray[1]); 
	}	 
	else{
	    (temptable.get(roomnum)).add(new Hashtable());
	    (votetable.get(roomnum)).put("t" + Integer.toString((temptable.get(roomnum)).size()-1), Integer.toString((votetable.get(roomnum)).size()));
	    sendToChannel(";newtemp " + Integer.toString((temptable.get(roomnum)).size()-1) + " " + Integer.toString((votetable.get(roomnum)).size()-1) + " " + strarray[1], roomnum, "tempcheck", socket, strarray[0]);
	}
    }
        
    //Updates temp check temperature and does calculations
    void trackTemp(String message, String name, Socket socket, int roomnum){
	String tempstring = message.substring(12);
	String strarray[] = tempstring.split(" ");
	double temp = Double.parseDouble(strarray[2]);
	double numtemp;
	
	//Check if the person is already in the table
	if(!((temptable.get(roomnum)).get(Integer.parseInt(strarray[1]))).containsKey(name)){
	    //Add user if this is their first time voting
	    ((temptable.get(roomnum)).get(Integer.parseInt(strarray[1]))).put(name, temp);
	}
	else{
	    //Update a user's vote
	    ((temptable.get(roomnum)).get(Integer.parseInt(strarray[1]))).remove(name);
	    ((temptable.get(roomnum)).get(Integer.parseInt(strarray[1]))).put(name, temp);
	}
	
	//Calcuate the average temperature
	tempavg = 0;
	for(Enumeration e = ((temptable.get(roomnum)).get(Integer.parseInt(strarray[1]))).elements(); e.hasMoreElements();){
	    tempavg = tempavg + (Double)e.nextElement();
	}
	tempavg = tempavg/(temptable.size());

	//Send results to users
	if(strarray[0].startsWith("bluh")){
	    sendToAll(";tempup " + strarray[1] + " " + Double.toString(tempavg));
	}
	else{
	    sendToChannel(";tempup " + strarray[1] + " " + Double.toString(tempavg), roomnum, "tempcheck", socket, strarray[0]);
	}
    }

    //Create a new poll
    void newpoll(String message, String name, Socket socket, int roomnum){
	String tempstring = message.substring(9);
        String strarray[] = tempstring.split(" ", 2);

	//Initiate and send info to the client
	if(roomnum == 0){
	    (polltable.get(0)).add(new Hashtable<String, String>());
	    (votetable.get(0)).put("p" + Integer.toString((polltable.get(0)).size()-1), Integer.toString((votetable.get(0)).size()));
            sendToAll(";newpoll " + Integer.toString((polltable.get(0)).size()-1) + " " + Integer.toString((votetable.get(0)).size()-1) + " " + strarray[1]);
        }
        else{
	    (polltable.get(roomnum)).add(new Hashtable<String, String>());
            (votetable.get(roomnum)).put("p" + Integer.toString((polltable.get(roomnum)).size()-1), Integer.toString((votetable.get(roomnum)).size()));
            sendToChannel(";newpoll " + Integer.toString((polltable.get(roomnum)).size()-1) + " " + Integer.toString((votetable.get(roomnum)).size()-1) + " " + strarray[1], roomnum, "tempcheck", socket, strarray[0]);
        }
    }

    //Track the results of votes
    void trackPoll(String message, String name, Socket socket, int roomnum){
	String pollstring = message.substring(15);
	String strarray[] = pollstring.split(" ");
	int pollnum = Integer.parseInt(strarray[1]);

	//Go through the string and increment votes
	for(int l = 2; l < strarray.length; l++){
	    //Add a new result
	    if(!((polltable.get(roomnum)).get(pollnum)).containsKey(strarray[l])){
		((polltable.get(roomnum)).get(pollnum)).put(strarray[l], "1");
	    }
	    else{
		//Update a known result
		String value =((polltable.get(roomnum)).get(pollnum)).get(strarray[l]);
		String newval = Integer.toString(Integer.parseInt(value) + 1);
		((polltable.get(roomnum)).get(pollnum)).put(strarray[l], newval);
	    }
	}
    
	//Create string to send to server
	String send = ";pollup " + Integer.toString(pollnum) + " " + Integer.toString(votetable.get(roomnum).size()-1);
	for(Enumeration e = ((polltable.get(roomnum)).get(pollnum)).keys(); e.hasMoreElements(); ){
	    String currkey = (String)e.nextElement();
	    send = send + " " + currkey + " " + ((polltable.get(roomnum)).get(pollnum)).get(currkey);
	}
	
	//Send updated count to server
	if(roomnum==0){
	    sendToAll(send);
	}
	else{
	    sendToChannel(send, roomnum, "tempcheck", socket, strarray[0]);
	}
    }

    //Send an updated user list to the main channel
    void sendListAll(){
	String message = ";updatelist";
	Enumeration userlist = users.keys();

	//Go through the list and form a message
	while(userlist.hasMoreElements()){
	    message = message + " " + userlist.nextElement();
	}
	sendToAll(message);
    }

    //Send and update user list to a specific channel
    void sendListChannel(int roomnum, String name, Socket socket, String channame){
        String message = ";updatelist";
        Enumeration userlist =  (rooms.get(roomnum-1)).keys();

	//Form and send a message
	while(userlist.hasMoreElements()){
            message = message + " " + userlist.nextElement();
        }
        sendToChannel(message, roomnum, name, socket, channame);
    }

    //Send a message to all clients (the main channel)
    void sendToAll(String message){
	synchronized(outputStreams){
	    //For each client. go through and send
	    Enumeration keys = outputStreams.keys();
	    for(Enumeration e = getOutputStreams(); e.hasMoreElements(); ){
		DataOutputStream dout = (DataOutputStream)e.nextElement();
		Socket socket = (Socket)keys.nextElement();
		String encrypted = "";

		try{
		    dout.writeUTF(message);
		} catch(IOException ie) {
		    System.out.println(ie);
		}
	    }
	}
    }
    
    //Removes a client connected to the server
    void removeConnection(Socket socket){
	synchronized(outputStreams){
	    System.out.println("Removing connection to "+socket);
	    outputStreams.remove(socket);

	    try{
		socket.close();
	    } catch(IOException ie){
		System.out.println("Error closing "+socket);
		ie.printStackTrace();
	    }
	}
    }

    //Allows a user to change their name
    public String addUser(String message, String oldname, Socket socket){
	name = message.substring(6);
	//Checks if the name is already in use
	if(users.containsKey(name) || regnames.containsKey(name)){
	    try{
		//Notify the user that name is in use
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		dout.writeUTF("The name " + name + " is already in use or registered. Please choose a different name.");
	    } catch(IOException ie) {
		System.out.println(ie);
	    }
	    return oldname;
	}
	//Checks if the user is using an illegal name
	else if(name.startsWith("nick") || name.contains(" ")){
	    try{
		//Notify user of banned names
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                dout.writeUTF("You cannot use other names starting with nick or names with spaces, please choose another name.");
	    } catch(IOException ie) {
		System.out.println(ie);
	    }
	    return oldname;
	}
	//Assigns the user the name they chose
	else if(!users.containsKey(name) && !regnames.containsKey(name)){
	    users.remove(oldname);
	    ArrayList<String> hold = userroomlist.get(oldname);

	    //Updates the name in lists
	    userroomlist.remove(oldname);
	    userroomlist.put(name, hold);
	    users.put(name, name);

	    //Notifies all rooms the user is in that their name has changed
	    for(int j = 0; j < hold.size(); j++){
		int roomnum = roomlist.get(hold.get(j));
		(rooms.get(roomnum-1)).remove(oldname);
		(rooms.get(roomnum-1)).put(name, outputStreams.get(socket));
		sendToChannel(oldname + " has changed their name to " + name, roomnum, name, socket, hold.get(j));
		sendListChannel(roomnum, name, socket, hold.get(j));
	    }
	    sendToAll(oldname + " has changed their name to " + name);
	    sendListAll();
	    return name;
	}
	return oldname;
    }

    //Lets a user register a name
    public String registerUser(String message, String oldname, Socket socket){
	userpass = message.substring(10);
	userpassarray = userpass.split(" ");
	//Reigsters the name
	if(userpassarray[0].compareToIgnoreCase(oldname) == 0){
	    regnames.put(userpassarray[0], userpassarray[1]);
	    return oldname;
	}
	//Changes the name then registers the name
	else{
	    name = addUser("/nick " + userpassarray[0], oldname, socket);
	    if(name.compareToIgnoreCase(oldname) != 0){
		regnames.put(userpassarray[0], userpassarray[1]);
	    }
	    return name;
	}
    }

    //Lets a user identify
    public String identUser(String message, String oldname, Socket socket){
	userpass = message.substring(10);
	userpassarray = userpass.split(" ");
	//Checks if the name is registered
	if(regnames.containsKey(userpassarray[0])){
	    //Identifies the user
	    if(userpassarray[1].compareTo((regnames.get(userpassarray[0])).toString()) == 0){
		users.remove(oldname);
		ArrayList<String> hold = userroomlist.get(oldname);
		userroomlist.remove(oldname);
		userroomlist.put(userpassarray[0], hold);
		users.put(userpassarray[0], userpassarray[0]);

		//Notifies all rooms the user is in that they have changed their name
		for(int j = 0; j < hold.size(); j++){
		    int roomnum = roomlist.get(hold.get(j));
		    (rooms.get(roomnum-1)).remove(oldname);
		    (rooms.get(roomnum-1)).put(userpassarray[0], outputStreams.get(socket));
		    sendToChannel(oldname + " has changed their name to " + userpassarray[0], roomnum, userpassarray[0], socket, hold.get(j));
		    sendListChannel(roomnum, userpassarray[0], socket, hold.get(j));
		}
		sendToAll(oldname + " has changed their name to " + userpassarray[0]);
		sendListAll();
		return userpassarray[0];
	    }
	    //Checks if the password is correct
	    else{
		try{
		    //Tells the user they entered the wrong password
		    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		    dout.writeUTF("The password you entered was incorrect.");
		} catch(IOException ie) {
		    System.out.println(ie);
		}
		return oldname;
	    }
	}
	//The name is not registered
	else if(!regnames.containsKey(userpassarray[0])){
	    try{
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                dout.writeUTF("This name is not registered. Please register this name before authenticating with it.");
            } catch(IOException ie) {
                System.out.println(ie);
            }
	    return oldname;
	}
	return oldname;
    }
    
    //Creates a new room
    public void createRoom(String message, Socket socket){
	room = message.substring(8);
	//Checks if the room exists
	if(roomlist.containsKey(room)){
	    try{
		//Tells the user the room already exists
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                dout.writeUTF("The room " + room + " has already been created. Please join this room or choose a different name for the room you are creating.");
            } catch(IOException ie) {
                System.out.println(ie);
            }
	}
	else if(!roomlist.containsKey(room)){
	    //Adds a new room
	    roomlist.put(room, roomlist.size()+1);
	    rooms.add(new Hashtable());
	    temptable.add(new ArrayList<Hashtable<String, Double>>());
	    polltable.add(new ArrayList<Hashtable<String, String>>());
	    votetable.add(new Hashtable<String, String>());
	}
    }

    //Lets a user join a channel
    public void joinRoom(String message, String currentname, Socket socket){
	room = message.substring(6);
	if(roomlist.containsKey(room)){
	    roomnum = Integer.parseInt((roomlist.get(room)).toString());
	    //Checks if a user is in the room already
	    if((rooms.get(roomnum-1)).containsKey(currentname)){
		try{
		    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		    dout.writeUTF("You are already in the room " + room + ". You cannot rejoin a room you are already in.");
		} catch(IOException ie) {
		    System.out.println(ie);
		}

	    }
	    //Joins the room
	    else{
		try{
                    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                    dout.writeUTF("join;" + room);
                } catch(IOException ie) {
                    System.out.println(ie);
                }

		//Adds the user to lists in the room
		(userroomlist.get(currentname)).add(room);
		(rooms.get(roomnum-1)).put(currentname, outputStreams.get(socket));

		//Notifies others that the user has joined
		sendToChannel(currentname + " has joined the channel " + room + "." , roomnum, currentname, socket, room);
		sendListChannel(roomnum, currentname, socket, room);
	    }
	}
	//The room does not exist, create it
	else if(!roomlist.containsKey(room)){
	    createRoom("/create " + room, socket);
	    joinRoom(message, currentname, socket);
	}
    }

    //Leave the room
    public void partRoom(String message, String currentname, Socket socket){
	int arrsize;
	int num;
	String roomname;
	room = message.substring(6);
	if(roomlist.containsKey(room)){
	    roomnum = Integer.parseInt((roomlist.get(room)).toString());
	    //Removes the user from the channel
	    if((rooms.get(roomnum-1)).containsKey(currentname)){
		(rooms.get(roomnum-1)).remove(currentname);
		(userroomlist.get(currentname)).remove((userroomlist.get(currentname)).indexOf(room));

		//Remove room if it is empty
		if((rooms.get(roomnum-1)).size() == 0){
		    rooms.remove(roomnum-1);
		    roomlist.remove(room);
		    temptable.remove(roomnum);

		    //Reindex the rooms
		    Enumeration keys = roomlist.keys();
		    while(keys.hasMoreElements()){
			String currkey = (String)keys.nextElement();
			if((int)roomlist.get(currkey) >= roomnum+1){
			    int save = (int)roomlist.get(currkey);
			    roomlist.remove(currkey);
			    roomlist.put(currkey, save-1);
			} 
		    }
		}
		else{
		    //Notifies others the user has left
		    sendToChannel("/parted " + currentname + " has parted the channel " + room + "." , roomnum, currentname, socket, room);
		    sendListChannel(roomnum, "tempcheck", socket, room);
		}
	    }
	    //The user is not in the channel
	    else{
		try{
		    DataOutputStream dout2 = new DataOutputStream(socket.getOutputStream());
		    dout2.writeUTF("You are not in the channel " + room + ". You cannot leave a channel you are not in.");
		} catch(IOException ie) {
		    System.out.println(ie);
		}
	    }
	}
	//The room does not exist
	else{
	    try{
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		dout.writeUTF("The channel " + room + " does not exist. You cannot leave a channel that does not exist.");
	    } catch(IOException ie) {
		System.out.println(ie);
	    }
	}
    }

    //Gets the output streams of all users in a channel
    Enumeration getChannelStreams(int roomnumber){
	return (rooms.get(roomnumber-1)).elements();
    }

    //Sends a message to the channel
    public void sendToChannel(String message, int roomnumber, String currentname, Socket socket, String roomname){
	if((rooms.get(roomnumber-1)).containsKey(currentname) || currentname.startsWith("tempcheck")){
	    synchronized(rooms.get(roomnumber-1)){
		//Get all streams and write to them
		for(Enumeration e = getChannelStreams(roomnumber); e.hasMoreElements(); ){
		    DataOutputStream dout = (DataOutputStream)e.nextElement();
		    
		    try{
			dout.writeUTF(roomname + " " + message);
		    } catch(IOException ie){
			System.out.println(ie);
		    }
		}
	    }
	}
	//Stop users from writing to a channel they are not in
	else{
	    if(message.startsWith("/parted ")){
		message = message.substring(8);
		synchronized(rooms.get(roomnumber-1)){
		    //Get all streams and write to them        
		    for(Enumeration e = getChannelStreams(roomnumber); e.hasMoreElements(); ){
			DataOutputStream dout = (DataOutputStream)e.nextElement();
			try{
			    dout.writeUTF(roomname + " " + message);
			} catch(IOException ie){
			    System.out.println(ie);
			}
		    }
		}
	    }
	    else{
		try{
		    DataOutputStream dout2 = new DataOutputStream(socket.getOutputStream());
		    dout2.writeUTF("You are not in the channel you are trying to send a message to. You cannot send a message to a channel you are not in.");
		} catch(IOException ie) {
		    System.out.println(ie);
		}
	    }
	}
    }

    //Removes the name of someone who has left from the userlist
    public void removeUser(String name){
	users.remove(name);
	userroomlist.remove(name);
    }

    //Checks if the userlist has a particular name in it
    public boolean containsKey(String name){
	return users.containsKey(name);
    }

    static public void main(String args[]) throws Exception{
	//Gets the port specified in command line                                                
	int port = Integer.parseInt(args[0]);

	//Make server object as specified above which starts listening                              
	new Server(port);
    }
}
