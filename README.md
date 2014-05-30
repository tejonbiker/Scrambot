Scrambot
========

29/May/2014

University of Guanajuato

Division de Ingenierias Campus Irapuato Salamanca (DICIS)

Jose Federico Ramos Ortega

Scrambot is a differential mobile robot controlled via WiFi based
on a beetle robot from Steren as chassis and a Raspberry Pi as core of the controller.

This project begins with the ASME SPDC contest in 2013: "Remote Inspection Device":

https://www.asme.org/events/competitions/student-design-competition

The targets and the rules were quite simple:

    -The robot operator cannot see the stage
    -The robot need to establish (in any way) a real time video with the operator
    -The robot need to be controlled wirelessly
    -The robot need to accomplish three tasks:
      1.-Read a label with four numbers
      2.-Carry a small wood cylinder
      3.-Push a button 
          
The best balance between cost and flexibility was the Raspberry Pi with a lot of add-ons.
The result of the contest was this project (with a more little post work), this repo have the intention
to be a source of knowledge and contact that you require if you want to reconstruct/improve this project. 

Some topics/knowledge used in this robot are:

    -Remote connections
      -SSH
      -VNC
      -TCP sockets for server and client (ANSIC C and Java respectively)
      -MJPEG Server
      
    -Java Desktop GUI
      -Sockets
      -MJPEG Capture and show
      
    -PCB Design (with Eagle)
      -5V Switched Mode Power Suypply
      -Raspberry Pi GPIO and two L293 H-Bridge interface
      
    -Raspberry Pi GPIO management
      -WiringPi
      -I2C (for MPU6050 acelerometter)
      
    -Power Management 
      -Knowledge about batteries (NiCd - Lipo)
      -5V SMPS design
      -Power consumption analysis
      
    -Linux stuff
      -SSH and console coomands management
      -Bash scripting
        -Automatically start services
        -Automatically install software
      -Make for compilation in ANSI C
      
    -Wireless LAN magagement
      -Detecting the Scrambot in LAN
      -Scrambot as hotspot
  
(Breathe.......) and much more.

For any misspelling/correction/comment/idea don't doubt to contact me.

tejonbiker@gmail.com


We will be happy if you like the project. 

Don't forget this is a MIT licensed project.

Have fun!!!!! :D


  
