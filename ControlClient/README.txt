Scrambot Control Client

This is a desktop Java Application build on Netbeans, uses the GUI of the netbeans,
this means you only can compile with Netbeans.

The basics actions of the GUI are:
		-Open a socket connection to transfer control commands
		-Open a stream to capture the MJPEG from Scrambot
		-Capture the press/release keys 
		-Search the Scrambot in a range of IP


The application is entirely crossplatform, can be used (and develop) in any
OS that support Netbeans, currently I only tested in Linux/Win/Mac.

The executable is dist/ControlClient.jar

I highly recommend start with the file ControlGUI.java if you want inspect the code.