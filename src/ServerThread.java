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
		    message = stlv.msg;
		    if(message.charAt(0) == '/'){
			if(message.startsWith("/nick ")){
			    name = server.addUser(message, name, socket);
			}
			else if(message.startsWith("/register ")){
			    name = server.registerUser(message, name, socket);
			}
			else if(message.startsWith("/identify ")){
			    name = server.identUser(message, name, socket);
			}
			else if(message.startsWith("/create ")){
			    server.createRoom(message, socket);
			}
			else if(message.startsWith("/join ")){
			    server.joinRoom(message, name, socket);
			}
			else if(message.startsWith("/part ")){
			    server.partRoom(message, name, socket);
			}
		    //Pre-process a message to a room
			else if(message.startsWith("/message ")){
			    message = message.substring(9);
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
			message = name+ ": " + message;
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
