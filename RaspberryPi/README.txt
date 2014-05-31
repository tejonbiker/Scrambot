This folder includes all software related to the Raspbery Pi, as you can see the
is not much as you can think, the most large file is ScramConfig.sh
this script configures a Raspian OS to install all features of the Scrambot
and be recognized by GUI ControlClient.

The main problem is the MJPEG source, this code have some changes that need to be uptaed
in the script.

If you are in Windows please open or edit this files with CodeBlocks or some similar
IDE that recognizes Linux end lines, Visual Studio rewrites this to windows end lines.

server3.c uses WiringPi to acces to the Raspberry Pi GPIO, aditionally provides the service
(via sockets) that ControlClient uses to transfer the control commands, all is maded in ANSI C.