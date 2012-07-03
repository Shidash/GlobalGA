import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientApplet extends Applet
{
    //Initializes the applet and grabs info from the html
    public void init(){
	String host = getParameter("host");
	int port = Integer.parseInt(getParameter("port") );
	setLayout(new BorderLayout());
	add("Center", new Client(host, port));
    }
}
