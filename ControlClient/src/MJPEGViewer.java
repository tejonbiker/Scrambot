
/*
 * This software is part of the Scrambot project, for more info go to:
 * https://github.com/tejonbiker/Scrambot
 * 
 * All source code of the project is MIT License
 * 
 * This class only provide a bypass GUI element to show the rendered image of 
 * MJPEG viewer
 * 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tejon
 */
public class MJPEGViewer extends JPanel {
    
    Image imgViewer;
    
    MJPEGViewer()
    {
    }
    
    public void setImage(Image img)
    {
        imgViewer=img;
    }
    
    
    //We override this function, this image is seted in MjpegRunner class
    @Override
    public void paint(Graphics g)
    {
         //super.paintComponent(g);
         g.setColor(Color.black);
         g.fillRect(0, 0, 320, 240);
         
         if(imgViewer!=null)
            g.drawImage(imgViewer, 0, 0, null);
         
    }
    
}
