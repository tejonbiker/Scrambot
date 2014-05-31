#!/bin/bash

duplicateDir="A folder was found, skipped"

echo "This script install all dependecies needed for"
echo "build and adjust WiringPi and mjpeg-streamer."
echo "The installation is silent."
echo "This script is part of the software designed"
echo "for ScramBot, IEEE student branch, University of"
echo "Guanajuato, Mexico."
echo "Ver 0.1"
echo "Federico Ramos"
echo "This is a MIT Lisence project see more at:"
echo "https://github.com/tejonbiker/Scrambot"
read -p "Pres [Enter] key to start..."

sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install libi2c-dev -y
sudo apt-get install git-core -y
sudo apt-get install tightvncserver -y

#store the dir execution (absolute path for MJPEG-Streamer)
dirServer="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


#Search for a folder for previous instalation of WiringPi
#https://projects.drogon.net/raspberry-pi/wiringpi/download-and-install/
dir="$HOME/wiringPi"
if [ -d $dir ]; then
	echo "$duplicateDir : $dir"
else
	cd $HOME
	git clone git://git.drogon.net/wiringPi
	cd wiringPi
	sudo ./build
fi

sudo apt-get install libv4l-dev -y
sudo apt-get install libjpeg8-dev -y
sudo apt-get install subversion -y
sudo apt-get install imagemagick -y

#start to install MJPEGStreamer
#http://www.justrobots.net/?p=97
dir="$HOME/mjpg-streamer"
if [ -d $dir ]; then
	echo "$duplicateDir : $dir"
else
	sudo ln -s /usr/include/linux/videodev2.h /usr/include/linux/videodev.h
	wget http://sourceforge.net/code-snapshots/svn/m/mj/mjpg-streamer/code/mjpg-streamer-code-182.zip
	unzip mjpg-streamer-code-182.zip
    cd mjpg-streamer-code-182/mjpg-streamer
	sudo make mjpg_streamer input_file.so output_http.so input_uvc.so output_udp.so
	sudo cp mjpg_streamer /usr/local/bin
	sudo cp input_file.so output_http.so input_uvc.so output_udp.so /usr/local/lib/
	sudo cp -R www /usr/local/www
fi

#Script to boot with MJPEGStreamer
cadena=$"#! /bin/sh
# /etc/init.d/webcam

# Carry out specific functions when asked to by the system
case \"\$1\" in
  start)
    echo \"Starting web\"
    export LD_LIBRARY_PATH=/usr/local/lib
    sudo $dirServer/mjpg-streamer-code-182/mjpg-streamer/mjpg_streamer  -i  \"/usr/local/lib/input_uvc.so -d /dev/video0  -r 320x240 -f 30\" -o \"/usr/local/lib/output_http.so -p 8090 -w /var/www/mjpg_streamer\" &
     ;;
  stop)
    echo \"Stopping webcam script\"
    killall mjpg_streamer
    ;;
  *)
    echo \"Usage: /etc/init.d/webcam {start|stop}\"
    exit 1
    ;;
    esac

exit 0"

sudo  rm -f /etc/init.d/webcam
#sudo echo "$cadena">/etc/init.d/webcam
echo "$cadena" | sudo tee -a /etc/init.d/webcam
sudo  chmod 755 /etc/init.d/webcam
sudo  update-rc.d webcam defaults

cd $dirServer

#Build GPIO program to drive the motors from TCP socket
#chmod 0777 buidlServerGPIO
#./buidlServerGPIO
gcc -o server3 server3.c -lwiringPi -lpthread -lm

#Script to boot with GPIO server
cadena=$"#! /bin/sh
# /etc/init.d/gpioserver

# Carry out specific functions when asked to by the system
case \"\$1\" in
  start)
    echo \"Starting GPIO\"
    sudo $dirServer/server3 7000 &
     ;;
  stop)
    echo \"Stopping gpio script\"
    killall server3
    ;;
  *)
    echo \"Usage: /etc/init.d/gpioserver {start|stop}\"
    exit 1
    ;;
    esac

exit 0"

sudo  rm -f /etc/init.d/gpioserver
echo "$cadena" | sudo tee -a /etc/init.d/gpioserver
sudo  chmod 755 /etc/init.d/gpioserver
sudo  update-rc.d gpioserver defaults




