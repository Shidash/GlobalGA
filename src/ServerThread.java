import java.io.*;
import java.net.*;
import ca.uwaterloo.crysp.otr.*;
import ca.uwaterloo.crysp.otr.iface.*;

public class ServerThread extends Thread
{
    private Server server;
    private Socket socket;
    String name;
    String[] chaninfo;
    int roomnum;
    int messagelen;
    String tempmessage;
    private UserState us;
    private OTRCallbacks callback;

    //Initialize a server thread
    public ServerThread(Server server, Socket socket){
        this.server = server;
        this.socket = socket;
        start();
	name = server.name;
	us = server.us;
	callback = server.callback;
    }

    public void run(){
	try{
	    DataInputStream din = new DataInputStream(socket.getInputStream());

	    while(true){
		//Read the next message                                           
		String message = din.readUTF();
		int portname = socket.getPort();
		try{
		    StringTLV stlv = us.messageReceiving("Server", "GlobalGA", Integer.toString(portname), message, callback);
		
		if(stlv!=null){
		//Process commands
		    if((stlv.msg).charAt(0) == '/'){
		    if((stlv.msg).startsWith("/nick ")){
			name = server.addUser(stlv.msg, name, socket);
		    }
		    else if((stlv.msg).startsWith("/register ")){
			name = server.registerUser(stlv.msg, name, socket);
		    }
		    else if((stlv.msg).startsWith("/identify ")){
			name = server.identUser(stlv.msg, name, socket);
		    }
		    else if((stlv.msg).startsWith("/create ")){
			server.createRoom(stlv.msg, socket);
		    }
		    else if((stlv.msg).startsWith("/join ")){
			server.joinRoom(stlv.msg, name, socket);
		    }
		    else if((stlv.msg).startsWith("/part ")){
			server.partRoom(stlv.msg, name, socket);
		    }
		    //Pre-process a message to a room
		    else if((stlv.msg).startsWith("/message ")){
			message = (stlv.msg).substring(9);
			chaninfo = message.split(" ");
			roomnum = Integer.parseInt((server.roomlist.get(chaninfo[0])).toString());
			messagelen = chaninfo.length;
			for(int i = 1; i < messagelen; i++){
			    if(i == 1){
				tempmessage = chaninfo[1];
			    }
			    else{
				tempmessage = tempmessage + " " + chaninfo[i];
			    }
			}
			server.sendToChannel(name + ": " + tempmessage, roomnum, name, socket, chaninfo[0]);
		    }
		}
		//Send a message to the main channel
		else{
		    message = name+ ": " + stlv.msg;
		    System.out.println("Sending "+message);
		    server.sendToAll(message);
		}
		}
		} catch (Exception e){
		    e.printStackTrace();
		}
	    }
	} catch(EOFException ie) {
	} catch(IOException ie) {
	    ie.printStackTrace();
	} finally {
	    //Cleanup
	    server.removeUser(name);
	    server.removeConnection(socket);
	}
    }
}
