/*
 * 
 * This code is developed by other person, I only include to have the project complete,
 * you can get more info at:
 * http://thistleshrub.net/Joomla/index.php?option=com_content&view=article&id=115:displaying-streamed-mjpeg-in-java&catid=43:robotics&Itemid=64
 * 
 * 
 * I only modify a little to include the MJPEGViewer class (this is a bypass control GUI to show the stream)
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Given an extended JPanel and URL read and create BufferedImages to be displayed from a MJPEG stream
 * @author shrub34 Copyright 2012
 * Free for reuse, just please give me a credit if it is for a redistributed package
 */
public class MjpegRunner implements Runnable
{
	private static final String CONTENT_LENGTH = "Content-Length: ";
	private static final String CONTENT_TYPE = "Content-Type: image/jpeg";
        private static final String CONTENT_TIME_STAMP= "X-Timestamp:";
	private MJPEGViewer viewer;
	private InputStream urlStream;
	private StringWriter stringWriter;
	private boolean processing = true;
        private JLabel errorMsgOut;
        private URLConnection urlConn;
	
	public MjpegRunner(MJPEGViewer viewer, URL url, JLabel errorMsg) throws IOException
	{
		this.viewer = viewer;
		urlConn = url.openConnection();
		// change the timeout to taste, I like 1 second
		urlConn.setReadTimeout(2000);
		urlConn.connect();
		urlStream = urlConn.getInputStream();
		stringWriter = new StringWriter(128);
                errorMsgOut=errorMsg;
	}

	/**
	 * Stop the loop, and allow it to clean up
	 */
	public synchronized void stop()
	{
		processing = false;
	}
	
	/**
	 * Keeps running while process() returns true
	 * 
	 * Each loop asks for the next JPEG image and then sends it to our JPanel to draw
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() 
	{
		while(processing)
		{
			try
			{
				byte[] imageBytes = retrieveNextImage();
				ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
				
				BufferedImage image = ImageIO.read(bais);
                                
                                //(viewer.getGraphics()).drawImage(image, 0, 0, null);
				//viewer.setBufferedImage(image);
                                
                                viewer.setImage(image); 
                                viewer.revalidate();
				viewer.repaint();
			}catch(SocketTimeoutException ste){
				System.err.println("Failed stream read: " + ste);
				//viewer.setFailedString("Lost Camera connection: " + ste);
                                errorMsgOut.setText("Failed stream read." +ste);
				viewer.repaint();
				stop();
			}catch(IOException e){
				System.err.println("failed stream read: " +e);
                                errorMsgOut.setText("Failed stream read.");
				stop();
			}
		}
		
		// close streams
		try
		{
			urlStream.close();
		}catch(IOException ioe){
			System.err.println("Failed to close the stream: " + ioe);
                        errorMsgOut.setText("Failed to close the stream");
		}
                
                //viewer.setImage(null); 
                //viewer.revalidate();
                //viewer.repaint();
	}
	
	/**
	 * Using the <i>urlStream</i> get the next JPEG image as a byte[]
	 * @return byte[] of the JPEG
	 * @throws IOException
	 */
	private byte[] retrieveNextImage() throws IOException
	{
		boolean haveHeader = false; 
		int currByte = -1;
                int flagLenght=0;
                int flagNewline=0;
                int indexOf=0;
		
		String header = null;
		// build headers
		// the DCS-930L stops it's headers, Compatible with C170 Logitech
		while((currByte = urlStream.read()) > -1 && !haveHeader)
		{
			stringWriter.write(currByte);
			
			String tempString = stringWriter.toString(); 
                        //System.out.println(tempString+"\n");
			
                        if(flagLenght==0)
                        {
                            
                            indexOf = tempString.indexOf(CONTENT_LENGTH);
                            
                            if(indexOf>0)
                            {
                                flagLenght=1;
                            }
                        }
                        
                        if(flagLenght==1)
                        {
                            indexOf = (tempString.substring(tempString.indexOf(CONTENT_LENGTH))).indexOf("\n");
                            
                            if(indexOf>0)
                            {
                                flagNewline=1;
                            }
                        }
                        
                        
                        if(flagNewline==1 && flagLenght==1)
                        {
                             haveHeader = true;
                             header = tempString;
                        }
		}		
		
		// 255 indicates the start of the jpeg image
		while((urlStream.read()) != 255)
		{
			// just skip extras
		}
		
		// rest is the buffer
		int contentLength = contentLength(header);
		byte[] imageBytes = new byte[contentLength + 1];
		// since we ate the original 255 , shove it back in
		imageBytes[0] = (byte)255;
		int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
               && (numRead=urlStream.read(imageBytes, offset, imageBytes.length-offset)) >= 0) {
            offset += numRead;
        }       
		
		stringWriter = new StringWriter(128);
		
		return imageBytes;
	}

	// dirty but it works content-length parsing
	private static int contentLength(String header)
	{
		int indexOfContentLength = header.indexOf(CONTENT_LENGTH);
		int valueStartPos = indexOfContentLength + CONTENT_LENGTH.length();
		int indexOfEOL = header.indexOf('\n', indexOfContentLength);
		
		String lengthValStr = header.substring(valueStartPos, indexOfEOL).trim();
		
		int retValue = Integer.parseInt(lengthValStr);
		
		return retValue;
	}
}