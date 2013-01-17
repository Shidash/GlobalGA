/*Copyright (C) 2012 M. C. McGrath

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>*/

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
