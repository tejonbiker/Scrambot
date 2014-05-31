/*  Some initial testing with sockets in Java
 */

import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            echoSocket = new Socket("192.168.0.115", 7000);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                                        echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: taranis.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: taranis.");
            System.exit(1);
        }

	BufferedReader stdIn = new BufferedReader(
                                   new InputStreamReader(System.in));
	String userInput;
        
        out.println("Hola desde Java!!!");

        /*
	while ((userInput = stdIn.readLine()) != null) {
	    out.println(userInput);
	    System.out.println("echo: " + in.readLine());
	}*/

	out.close();
	in.close();
	stdIn.close();
	echoSocket.close();
    }
}