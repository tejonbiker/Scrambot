/*
 * This software is part of the Scrambot project, for more info go to:
 * https://github.com/tejonbiker/Scrambot
 * 
 * All source code of the project is MIT License
 * 
 * This class provides some abstraction to connect to the scrambot and trasnfer
 * the controls via sockets
 * 
 * The workflow is as follow:
 *      PiClient()
 *      conect()
 *      sendCommands()
 * 
 * All other stuff is incomplete
 */



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tejon
 */
public class PiClient {
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        boolean StreamAlive=false;
        char aknow[];
        
        public PiClient()
        {
            //nothing goes here
        }
        
        public void closeConection() throws IOException, UnknownHostException 
        {
            if(echoSocket!=null)
            {
                echoSocket.close();
                
                if(out!=null)
                    out.close();
                if(in!=null)
                    in.close();
            }
        }
        
        public void conect(String IP,int port) throws IOException, UnknownHostException 
        {
            char inID[];
            
            StreamAlive=false;
            
            //if some remains open, close it!
            if(echoSocket!=null)
            {
                echoSocket.close();
                
                if(out!=null)
                    out.close();
                if(in!=null)
                    in.close();
            }
            

            //Open the socket and the streams
            echoSocket = new Socket();
            echoSocket.connect(new InetSocketAddress(IP, port), 50  );
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            
            inID= new char[14];
            aknow= new char[2];
            
            //We need to capture 14 bytes, this is automatically sent when
            //Scrambot detects a connection, this is not a fully configurable password!!
            this.getData(inID);
            
            String IDString;
            
            IDString = new String(inID);
            
            System.out.println(IDString);
            
            //Compare the string sent by the socket
            if(IDString.equals("TACOCABANA1989")==false)
            {
                throw new UnknownHostException();
            }
            
            //All is ok, let's continue
            this.getData(inID);
            
            if(inID[0]=='1')
            {
                StreamAlive=true;
            }
            else
            {
                StreamAlive=false;
            }
            
        }
        
        public boolean StillConected()
        {
            if(echoSocket==null)
                return false;
            
            return !echoSocket.isClosed();
        }
        
        public void getData(char outData[]) throws IOException
        {
            in.read(outData);
        }
        
        public void sendCommands(char cmd[]) throws IOException
        {
            String ak;
            

            aknow[0]=0;
            aknow[1]=0;
            
            //sent the commands
            out.println(cmd);
            
            //When the scrambot recibe a command, he reponds with "OK",
            //this help to know when a disconnection occurs
            in.read(aknow);
            
            ak = new String(aknow);
            
            //Compare the string sent by 
            if(!ak.equals("OK"))
            {
                throw new IOException();
            }
                
            
        }
        
        public boolean MJPEGAlive()
        {
            return (Boolean)StreamAlive;
        }
    
}
