import java.net.*;
import java.io.*;
import ca.uwaterloo.crysp.otr.*;
import ca.uwaterloo.crysp.otr.iface.*;

class LocalCallback implements OTRCallbacks{

        Socket soc;
        PrintWriter out;

        public LocalCallback(Socket sock) throws IOException{
                soc=sock;
                out=new PrintWriter(soc.getOutputStream());
        }

        //Send message
        public void injectMessage(String accName, String prot, String rec, String msg){
	    if(msg==null){
		return;
	    }
            System.out.println("Sending message to the recipient:" +msg.length()+":"+msg);
            out.println(msg);
            out.flush();
        }

        //Returns policy for connection context
        public int getOtrPolicy(OTRContext conn) {
                return Policy.DEFAULT;
        }

       //Notifies users when the chat is secure
        public void goneSecure(OTRContext context) {
	        out.println("This chat is now secure.");
		out.flush();
                System.out.println("This chat is now secure.");
        }

       //Return true if the user is there
        public int isLoggedIn(String accountname, String protocol, String recipient) {
                return 1;
        }

       //Return limit in message length
        public int maxMessageSize(OTRContext context) {
                return 512;
        }
    
        //Notify users when a new fingerprint is created
        public void newFingerprint(OTRInterface us, String accountname, String protocol, String username, byte[] fingerprint) {
      	        out.println("A new fingerprint has been created for " + accountname);
		out.flush();
                System.out.println("A new fingerprint has been created for " + accountname);
        }
    
        //Check if the chat is still secure, this is the response
        public void stillSecure(OTRContext context, int is_reply) {
	    out.println("The chat is still secure.");
	    out.flush();
	    System.out.println("The chat is still secure.");
	}

        //ConnContexts has been updated
        public void updateContextList() {
	    System.out.println("Updating context list.");
	}

        //Fingerprint list has changed
        public void writeFingerprints() {
	    out.println("The list of known fingerprints has changed.");
	    out.flush();
	    System.out.println("The list of known fingerprints has changed.");
	}

        //Error
        public String errorMessage(OTRContext context, int err_code) {
	    if(err_code==OTRCallbacks.OTRL_ERRCODE_MSG_NOT_IN_PRIVATE){
		return "You sent an encrypted message, but we finished the private conversation.";
	    }
	    return null;
	}

       //Dealing with messages that shouldn't be encrypted
        public void handleMsgEvent(int msg_event, OTRContext context, String message) {
	    if(msg_event==OTRCallbacks.OTRL_MSGEVENT_CONNECTION_ENDED){
		out.println("The private connection has already ended.");
		System.out.println("The private connection has already ended.");
	    }
	    else if(msg_event==OTRCallbacks.OTRL_MSGEVENT_RCVDMSG_NOT_IN_PRIVATE){
		out.println("You received an encrypted message but the chat is not encrypted.");
		System.out.println("You received an encrypted message but the chat is not encrypted.");
	    }
	}

       //Socialist Millionaires Protocol (authentication)
        public void handleSmpEvent(int smpEvent, OTRContext context, int progress_percent, String question) {
	    if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_SECRET){
		System.out.println("The other side has initialized SMP. Please respond with /rs.");
	    }else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_ASK_FOR_ANSWER){
		System.out.println("The other side has initialized SMP, with question:" + question + ", Please respond with /rs.");
	    }else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_SUCCESS){
		System.out.println("SMP succeeded.");
	    }else if(smpEvent == OTRCallbacks.OTRL_SMPEVENT_FAILURE){
		System.out.println("SMP failed.");
	    }
	}
}
