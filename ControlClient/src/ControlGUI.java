
/*
 * This software is part of the Scrambot project, for more info go to:
 * https://github.com/tejonbiker/Scrambot
 * 
 * All source code of the project is MIT License
 * 
 * The workflow is as follow:
 *      ControlGUI()
 *          PiClient()
 *          MJPEGViewer()
 * 
 * From here the GUI events takes the control
 * 
 *      keypressed/keyReleased: control command user input (socket transfer via
 *                              PiClient)
 *      actionPerformed: Botton actions
 *              -Conectar: Open the sockets with the text on IP Jlabel,
 *                         Aditionaly init the MJPEG capture
 *              -Desconectar: close all connections
 *              -Apagar: Sends a special character to the server in
 *                       Scrambot that shutdown the Raspberry Pi
 *              -Scanear Red: Init the scanIPRange function
 * 
 *       scanIPRange: based on the values of the textbox "IP Base" and 
 *                  "# de IPs a Probar" scan a range of IP for the scrambot
 *                  and perform a full connection behavior if the scrambot
 *                  is found
 */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tejon
 */
public class ControlGUI extends javax.swing.JFrame implements KeyListener, ActionListener {

    /**
     * Creates new form ControlGUI
     */
    
    //Number of max transfered bytes in socket communication
    public static final int MAX_OUT_COMMANDS=8;
    public static final int MAX_IN_COMMANDS=8;
    
    //Buffer of the output/input bytes transfered by sockets
    char cmdOutArray[];
    char cmdInArray[];
    
    //For coloring the JLabel in GUI
    Color redColor;
    Color blackColor;
    
    //The Class that opens the socket for control commands
    PiClient client;
    
    //Required for MJPEGRunner to avoid lock the main
    //thread in the http petition of MJPEG stream
    Thread thread;
    
    //MJPEG Decoder
    MjpegRunner mjpeg_runner;
    
    //MJPEG GUI element
    MJPEGViewer mjpeg_viewer;
    
    //Classes to manage the repaint of the MJPEG GUI element
    JPanel JStream;
    RepaintManager myRepaintManager;
    
    //For Scrambot IP search in LAN
    Inet4Address current_addr;
    String IPBase;
    int    submask;
    int    IPToTest;
    String cutBase;
    
    //Image for showing in the MJPEG GUI element when the stream is closed
    Image blackScramImg;
    
    public ControlGUI()  {
        Component comp;
        int z;
        
        StringBuilder BuilderBaseIP= new StringBuilder();
        
        //Init all GUI components (by default)
        initComponents();
        
        //Add listeners (keys, buttons) to the apropiate controls
        BConect.addActionListener(this);
        BHalt.addActionListener(this);
        InputField.addKeyListener(this);
        ScanButton.addActionListener(this);
        BDisconnect.addActionListener(this);
        
        cmdOutArray =  new char[MAX_OUT_COMMANDS];
        cmdInArray  =  new char[MAX_IN_COMMANDS];
        client = new PiClient();
        
        //Setup some miscellaneous stuff
        StateLabel.setText("Sin Conectar...");
        StreamLabel.setText("...");
        
        for(int i=0;i<cmdOutArray.length ;i++)
        {
            cmdOutArray[i]='0';
        }
        
        redColor = new Color(255,0,0);
        blackColor= new Color(0,0,0);
        
        //Create the GUI element for MJPEG stream show
        mjpeg_viewer= new MJPEGViewer();
        
        //Load the stand-by element of the MJPEG stream show
        blackScramImg = new ImageIcon(getClass().getResource("/blackscram.jpg")).getImage();
        
        //Force dimentions and paint for the stand-by image
        mjpeg_viewer.setPreferredSize(new Dimension(320,240));
        mjpeg_viewer.setBackground(redColor);
        mjpeg_viewer.setImage(blackScramImg);
        mjpeg_viewer.revalidate();
        mjpeg_viewer.repaint();
        
        //We prefed IPV4, yes I know this is old, but is easy to manage
        System.setProperty("java.net.preferIPv4Stack","true");
        
        //Add the controller to the main grid layout
        comp =this.add(mjpeg_viewer); 
        
        /*  The scrambot have a little section that search the Scrambot IP in a range
            of IP, to help to the user we propose a base IP based in the IP of the
          * computer that run this App
        */
        try{
            
            //Get some data of local machine
            current_addr = (Inet4Address)Inet4Address.getLocalHost();
            IPBase = current_addr.getHostAddress();
            
            //This is only for emergencies, need more accurate code to identify IPv4
            InetAddress localHost = Inet4Address.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
            System.out.println(networkInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength());        
            submask=networkInterface.getInterfaceAddresses().get(1).getNetworkPrefixLength();
            
            //Try to propose the search range based on the network submask
            IPToTest=(int)Math.pow(2,32-submask);
            
            //Clean the string, I have this habit from ANSI C          
            BuilderBaseIP.delete(0, BuilderBaseIP.length());
            BuilderBaseIP.append(IPBase);
            BuilderBaseIP = BuilderBaseIP.reverse();
        
            //From the local current IP we delete the last number (remember we reversed
            //the string) finding the first point
            cutBase=BuilderBaseIP.substring(BuilderBaseIP.indexOf("."));
        
            BuilderBaseIP.delete(0, BuilderBaseIP.length());
            BuilderBaseIP.append(cutBase);
            BuilderBaseIP = BuilderBaseIP.reverse();
        
            //Finally we append a 0 to keep the a valid IP
            IPBase = BuilderBaseIP.toString() + "0";
        
            //for debuggin print out in the console the IP
             System.out.println(IPBase);
            
        }catch(UnknownHostException  e)
        {
            IPBase = "Net error";
            IPToTest=-1;
            
        }catch(SocketException e)
        {
            IPBase = "Net error";
            IPToTest=-1;
        }
        
        //print out in the GUI
        InputIPBase.setText(IPBase);
        InputBlockTest.setText(Integer.toString(IPToTest));
        StatusScan.setText("...");
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        PortText = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        IPText = new javax.swing.JTextField();
        InputField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        StateLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        StreamLabel = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        BConect = new javax.swing.JButton();
        BHalt = new javax.swing.JButton();
        BDisconnect = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        ScanButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        InputIPBase = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        InputBlockTest = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        StatusScan = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(2, 2));

        PortText.setText("7000");
        PortText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PortTextActionPerformed(evt);
            }
        });

        jLabel6.setText("Puerto:");

        jLabel5.setText("IP:");

        IPText.setText("192.168.0.31");
        IPText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IPTextActionPerformed(evt);
            }
        });

        InputField.setText(".....");
        InputField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InputFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Presione las teclas en la parte de arriba");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                        .add(55, 55, 55)
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(PortText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(31, 31, 31)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(IPText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(56, 56, 56)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel7)
                            .add(InputField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(74, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(IPText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5)
                    .add(PortText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(InputField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jLabel7)
                .add(32, 32, 32))
        );

        getContentPane().add(jPanel3);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setText(" Controles: \n  4 - \tMotor Izq Adelante\n  5 - \tMotor Der Adelante  \n  1 - \tMotor Izq Atras\n  2 - \tMotor Der Atras\n  Arriba - \tSubir Brazo\n  Abajo  - \tBajar Brazo\n  Derecha - \tCerrar Pinzas\nIzquierda-       Abrir Pinzas");
        jScrollPane1.setViewportView(jTextArea1);

        jLabel1.setText("Estado:");

        StateLabel.setText("jLabel2");

        jLabel3.setText("MJPEG:");

        StreamLabel.setText("jLabel4");

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(StateLabel))
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(StreamLabel))
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(109, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(23, 23, 23)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(StateLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(StreamLabel))
                .add(20, 20, 20))
        );

        getContentPane().add(jPanel7);

        BConect.setText("Conectar");
        BConect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BConectActionPerformed(evt);
            }
        });

        BHalt.setText("Apagar");

        BDisconnect.setText("Desconectar");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(BConect)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(BDisconnect))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(56, 56, 56)
                        .add(BHalt)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(BConect)
                    .add(BDisconnect))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(BHalt)
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(240, 240, 240));

        ScanButton.setText("Scanear Red");

        jLabel2.setText("IP Base:");

        InputIPBase.setText("jTextField1");

        jLabel4.setText("# de IPs a Probar:");

        InputBlockTest.setText("jTextField2");
        InputBlockTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InputBlockTestActionPerformed(evt);
            }
        });

        jLabel8.setText("Estado de Prueba:");

        StatusScan.setText("jLabel9");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(95, 95, 95)
                        .add(ScanButton))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel8)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(StatusScan)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel4)
                                    .add(jLabel2))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(InputIPBase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(0, 6, Short.MAX_VALUE))
                                    .add(InputBlockTest))))))
                .addContainerGap(31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(ScanButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(InputIPBase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(InputBlockTest, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(StatusScan))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(74, 74, 74)
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel8);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void IPTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IPTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IPTextActionPerformed

    private void PortTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PortTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PortTextActionPerformed

    private void BConectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BConectActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BConectActionPerformed

    private void InputFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InputFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_InputFieldActionPerformed

    private void InputBlockTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InputBlockTestActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_InputBlockTestActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ControlGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ControlGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ControlGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ControlGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ControlGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BConect;
    private javax.swing.JButton BDisconnect;
    private javax.swing.JButton BHalt;
    private javax.swing.JTextField IPText;
    private javax.swing.JTextField InputBlockTest;
    private javax.swing.JTextField InputField;
    private javax.swing.JTextField InputIPBase;
    private javax.swing.JTextField PortText;
    private javax.swing.JButton ScanButton;
    private javax.swing.JLabel StateLabel;
    private javax.swing.JLabel StatusScan;
    private javax.swing.JLabel StreamLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void keyTyped(KeyEvent ke) {
        
    }
    
    

    @Override
    public void keyPressed(KeyEvent ke) {
        //To know the code of some special keys
        //System.out.println(ke.getKeyCode());
        //System.out.println(ke.getKeyChar() + "\n");
        
        //Clear the textbox
        InputField.setText(null);
        
        /*  (Spanish)
        Controles: 
        4 - 	Motor Izq Adelante
        5 - 	Motor Der Adelante  
        1 - 	Motor Izq Atras
        2 - 	Motor Der Atras
        Arriba - 	Subir Brazo
        Abajo  - 	Bajar Brazo
        Derecha - 	Cerrar Pinzas
        Izquierda-       Abrir Pinzas
        * 
        * Ten encuenta que esto depende de las conexiones en el robot
        */
        
        /* Controls (English)
        4 - 	Motor Left Forward
        5 - 	Motor Right Forward  
        1 - 	Motor Left Backward
        2 - 	Motor Right Backward
        Up - 	Up Arm
        Down  - Down Arm
        Right - Close Claw
        Left-   Open Claw
        * 
        * Take into account this depends of the connections of the robot
         */
        
        /*Select the command based on the key pressed, the command 
         * is quite easye, depending wheres the "1" are is the bit that is
         * sended to the L293 (H-bridge), the cmdOutArray Have 8 bytes, 
         * this is and exmaple:
        * cmdOutArra[]={'1','0','0','0','0','0','0','0'}
        * If we send this array, correspond to left motor to forward, the next
        * Array is left motor to backward
        * cmdOutArra[]={'0','1','0','0','0','0','0','0'}
        * and so on
        */
        switch(ke.getKeyCode())
        {
            case 100: //Tecla 4
            case 'Q':
                cmdOutArray[0]='1';
                InputField.setText("Motor Izq Adelante");
                break;       
            case 101: //Tecla 5
            case 'W':
                cmdOutArray[2]='1';
                InputField.setText("Motor Der Adelante");
                break;          
            case 97: //Tecla 1
            case 'A':
                cmdOutArray[1]='1';
                InputField.setText("Motor Izq Atras");
                break;       
            case 98: //Tecla 2
            case 'S':
                cmdOutArray[3]='1';
                InputField.setText("Motor Der Atras");
                break;
            case 38: //Arriba
                cmdOutArray[4]='1';
                InputField.setText("Subir Brazo");
                break;   
            case 40: //Abajo
                cmdOutArray[5]='1';
                InputField.setText("Bajar Brazo");
                break;
            case 39: //Derecha.
                cmdOutArray[6]='1';
                InputField.setText("Cerrar Brazo");
                break;
            case 37: //Izquierda
                cmdOutArray[7]='1';
                InputField.setText("Abrir Brazo");
                break;
        }
        
        this.SendCommand();
       
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        //See keyPressed for more info
        
        //System.out.println(ke.getKeyCode());
        //System.out.println(ke.getKeyChar() + "\n");
        
        InputField.setText(null);
        
        /*
        Controles: 
        4 - 	Motor Izq Adelante
        5 - 	Motor Der Adelante  
        1 - 	Motor Izq Atras
        2 - 	Motor Der Atras
        Arriba - 	Subir Brazo
        Abajo  - 	Bajar Brazo
        Derecha - 	Cerrar Pinzas
        Izquierda-       Abrir Pinzas
        */
        
        switch(ke.getKeyCode())
        {
            case 100: //Tecla 4
            case 'Q':
                cmdOutArray[0]='0';
                break;       
            case 101: //Tecla 5
            case 'W':
                cmdOutArray[2]='0';
                break;          
            case 97: //Tecla 1
            case 'A':
                cmdOutArray[1]='0';
                break;       
            case 98: //Tecla 2
            case 'S':
                cmdOutArray[3]='0';
                break;
            case 38: //Arriba
                cmdOutArray[4]='0';
                break;   
            case 40: //Abajo
                cmdOutArray[5]='0';
                break;
            case 39: //Derecha.
                cmdOutArray[6]='0';
                break;
            case 37: //Izquierda
                cmdOutArray[7]='0';
                break;
        }
        
        this.SendCommand();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        
        try {
            
            //Send the special character to shutdown the Raspberry Pi
            if(ae.getSource()==BHalt)
            {
                cmdOutArray[0]=255;
                client.sendCommands(cmdOutArray);
                cmdOutArray[0]='0';
            }
            
            //try to connect to the Scrambot
            else if(ae.getSource() == BConect)
            {
                
                //Stop the thread (if this is start)
                //The way in this is maded seems dangerous, need a fix
                if(thread!=null)
                    thread.stop();
                
                //Try to connect to the control server
                client.conect( IPText.getText() , Integer.parseInt(PortText.getText()));
                
                //Try yo open the MJPEG stream
                mjpeg_runner = new MjpegRunner(mjpeg_viewer, new URL("http://"+IPText.getText()+":8090/?action=stream") , StateLabel);
                
                //Setup the thread to avoid the lock for the MJPEG stream
                thread= new Thread(mjpeg_runner);
                thread.start();
                
                //Yay, we have the control!!!!!
                InputField.setText("Listo");
                
            }else if(ae.getSource()==ScanButton)
            {
                //Start to scan the range
                scanIPRange();
                return;
            }else if(ae.getSource()==BDisconnect)
            {
                //Close the control connection
                client.closeConection();
                
                //Stop the MJPEG thread, one more time unsafe
                if(thread!=null)
                    thread.stop();
                
                //Enable and diable some buttons
                BConect.setEnabled(true);
                ScanButton.setEnabled(true);
                
                //Set text to know the state of the program
                StateLabel.setText("Not Connected");
                StreamLabel.setText("...");
                
                InputField.setText("No conectado");
                
                //Setup stand-by MJPEG image
                mjpeg_viewer.setImage(blackScramImg);
                mjpeg_viewer.revalidate();
                mjpeg_viewer.repaint();
                
                return;
            }
            
            
         //Some error? show it
        } catch (UnknownHostException ex) {
            StateLabel.setText("Imposible conectar: "+ex);
            StateLabel.setForeground(redColor);         
            StreamLabel.setText("...");
            return;
        } catch (IOException ex) {
            StateLabel.setText("Imposible conectar: "+ex);
            StateLabel.setForeground(redColor);         
            StreamLabel.setText("...");
            return;
        }
        
        //All is their place, we have connection, show  some messages
        StateLabel.setText("Conectado");
        StateLabel.setForeground(blackColor); 
        
        if(client.MJPEGAlive()==false)
        {
            StreamLabel.setText("Not Active");
            StreamLabel.setForeground(redColor);
        }
        else
        {
            StreamLabel.setText("Ready");
            StreamLabel.setForeground(blackColor);
        }
        
        //Disable/Enable some buttons
        BConect.setEnabled(false);
        ScanButton.setEnabled(false);
    
    }
    

    public void scanIPRange()
    {
        String BoxIP;
        URL testIP;
        int a,b,c,d;
        String IPMod;
        int nIPTest;
        int i,j,k;
        String buildIP=null;
        int found;
        
        //Get the IP base
        BoxIP=InputIPBase.getText();
        
        //Check if the IP Base is correct in syntax
        try{
        testIP =  new URL("http://"+BoxIP);
        }catch(MalformedURLException ex)
        {
            StatusScan.setText("IP malformada");
            return;
        }
        
        try{
            
            //Split the IP into integers to apply an "increment" during testing
            IPMod = BoxIP;
            a = Integer.parseInt(IPMod.substring(0,IPMod.indexOf(".")));
            IPMod = IPMod.substring(IPMod.indexOf(".")+1);
            
            b = Integer.parseInt(IPMod.substring(0,IPMod.indexOf(".")));
            IPMod = IPMod.substring(IPMod.indexOf(".")+1);
            
            c = Integer.parseInt(IPMod.substring(0,IPMod.indexOf(".")));
            IPMod = IPMod.substring(IPMod.indexOf(".")+1);
            
            d = Integer.parseInt(IPMod);
            
        }catch(Exception e)
        {
            StatusScan.setText("Error en IP");
            return;
        }
        
        //Get the numbers of IP to test
        try{
            nIPTest = Integer.parseInt( InputBlockTest.getText() );
            
            if(nIPTest<=0)
                throw new Exception();
            
        }catch(Exception e)
        {
            StatusScan.setText("Error en número de IPs");
            return;
        }
        
        //Search starting with IP base number
        found=-1;
        for(i=0;i<nIPTest;i++)
        {
            //Build the IP from splited integers
            buildIP = Integer.toString(a)+"."+Integer.toString(b)+"."+Integer.toString(c)+"."+Integer.toString(d);
            StatusScan.setText(buildIP);
            
            //try to connect
           try{
            client.conect(buildIP, 7000);
            found=1;
           }catch(Exception e)
           {
               
           }
           
           //We found the robot? break the loop
           if(found==1)
               break;
           
           //Increment for the next IP
           d++;
           if(d>255)
               c++;
           
           if(c>255)
               b++;

           if(b>255)
               a++;
           
           if(a>255)
               a=0;
           
        }
        
        //if we don't find the robot only show a message
        if(found==-1)
        {
            StatusScan.setText("Robot no encontrado");
            return;
        }
        
        //We have found the robot, close the connection for the moment
        try{
            client.closeConection();
        }catch(Exception e)
        {
            StatusScan.setText("Error al cerrar IP");
        }
        
        IPText.setText(buildIP);
        
        //Perform a fully behavior connection (simulating we hit the "Conectar"
       //button)
        for(ActionListener actions: BConect.getActionListeners()) {
            actions.actionPerformed( new ActionEvent(BConect, 100, "Blas" ));
            
}
        
    }
    
    public void SendCommand()
    {
        //Send the arrays that have the control commands
   
       if(client.StillConected()==false)
       {
           InputField.setText("No Conectado");
           return;
       }
       
       try {
            //here is the core of the function, all other stuff is to 
            //detect and show messages in case of disconnection
            client.sendCommands(cmdOutArray);
        } catch (UnknownHostException ex) {
            StateLabel.setText("Imposible conectar: " +ex);
            StateLabel.setForeground(redColor);         
            StreamLabel.setText("...");
            BConect.setEnabled(true);
            ScanButton.setEnabled(true);
            return;
        } catch (IOException ex) {
            StateLabel.setText("Imposible conectar: "+ex);
            StateLabel.setForeground(redColor);         
            StreamLabel.setText("...");
            BConect.setEnabled(true);
            ScanButton.setEnabled(true);
            return;
        }
    }
}
