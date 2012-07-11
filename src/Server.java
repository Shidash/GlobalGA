import java.io.*;
import java.net.*;
import java.util.*;
import ca.uwaterloo.crysp.otr.*;
import ca.uwaterloo.crysp.otr.iface.*;


public class Server
{
    //For accepting new connections
    private ServerSocket ss;    
    private Hashtable outputStreams = new Hashtable();
    private Hashtable users = new Hashtable();
    private Hashtable regnames = new Hashtable();
    private Hashtable nameStreams = new Hashtable();
    String name;
    int numuser = 0;
    String room;
    int roomnum;
    private String userpass;
    private String[] userpassarray;
    private String password;
    private ArrayList<Hashtable<String, Object>> rooms = new ArrayList<Hashtable<String, Object>>();
    Hashtable roomlist = new Hashtable();
    UserState us = new UserState(new ca.uwaterloo.crysp.otr.crypt.jca.JCAProvider());
    OTRCallbacks callback;
    
    //Starts listening
    public Server(int port) throws IOException{
	listen(port);
    }

    //Listener that accepts connections and autoassigns nicknames                                                
    private void listen(int port) throws IOException{
	ss = new ServerSocket(port);
	System.out.println("Listening on "+ss);

	while(true){
	    Socket socket = ss.accept();
	    System.out.println("Connection from "+socket);
	    DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
	    outputStreams.put(socket, dout);
	    name = "nick" + numuser;
	    users.put(name, name);
	    nameStreams.put(socket, name);
	    numuser++;
	    callback = new LocalCallback(socket);
	    System.out.println(socket);
	    new ServerThread(this, socket);
	}
    }

    //Get all output streams (one per client)
    Enumeration getOutputStreams() {
	return outputStreams.elements();
    }

    //Send a message to all clients (the main channel)
    void sendToAll(String message){
	synchronized(outputStreams){
	    //For each client
	    for(Enumeration e = getOutputStreams(); e.hasMoreElements(); ){
		DataOutputStream dout = (DataOutputStream)e.nextElement();
	       
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
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		dout.writeUTF("The name " + name + " is already in use or registered. Please choose a different name.");
	    } catch(IOException ie) {
		System.out.println(ie);
	    }
	    return oldname;
	}
	//Checks if the user is using an illegal name
	else if(name.startsWith("nick")){
	    try{
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                dout.writeUTF("You cannot use other names starting with nick, please choose another name.");
	    } catch(IOException ie) {
		System.out.println(ie);
	    }
	    return oldname;
	}
	//Assigns the user the name they chose
	else if(!users.containsKey(name) && !regnames.containsKey(name)){
	    users.remove(oldname);
	    users.put(name, name);
	    nameStreams.remove(socket);
	    nameStreams.put(socket, name);
	    sendToAll(oldname + " has changed their name to " + name);
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
		users.put(name, name);
		nameStreams.remove(socket);
		nameStreams.put(socket, name);
		sendToAll(oldname + " has changed their name to " + userpassarray[0]);
		return userpassarray[0];
	    }
	    //Checks if the password is correct
	    else{
		try{
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
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                dout.writeUTF("The room " + room + " has already been created. Please join this room or choose a different name for the room you are creating.");
            } catch(IOException ie) {
                System.out.println(ie);
            }
	}
	else if(!roomlist.containsKey(room)){
	    roomnum++;
	    roomlist.put(room, roomnum);
	    rooms.add(new Hashtable());
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

		(rooms.get(roomnum-1)).put(currentname, outputStreams.get(socket));
		sendToChannel(currentname + " has joined the channel " + room + "." , roomnum, currentname, socket, room);
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
		sendToChannel(currentname + " has parted the channel " + room + "." , roomnum, currentname, socket, room);
		(rooms.get(roomnum-1)).remove(currentname);
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
	if((rooms.get(roomnumber-1)).containsKey(currentname)){
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
	    try{
		DataOutputStream dout2 = new DataOutputStream(socket.getOutputStream());
                dout2.writeUTF("You are not in the channel you are trying to send a message to. You cannot send a message to a channel you are not in.");
            } catch(IOException ie) {
                System.out.println(ie);
            }
   
	}
    }

    //Removes the name of someone who has left from the userlist
    public void removeUser(String name, Socket socket){
	users.remove(name);
	nameStreams.remove(socket);
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
