/*
 * This software is part of the Scrambot project, for more info go to:
 * https://github.com/tejonbiker/Scrambot
 *
 * All source code of the project is MIT License
 *
 * This code is a modified version of:
 * http://www.linuxhowtos.org/C_C++/socket.htm
 *
 *
 * The workflow is as follow:
        Main:
                -Init WiringPi
                -Shutdown pins
                -Init Socket
                -Wait for a connection
        doStuff:
                -Send String ID
                -Read commands
                -put the commands to the GPIO
                -Wait for a command
 */

/* A simple server in the internet domain using TCP
 The port number is passed as an argument */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

//For GPIO interface
#include <wiringPi.h>

//Flag that indicates we are in Raspberry Pi
//Added for some desktop Linux testings
#define RPI_SERVER

#define LED 0


//Debug output function
void error(const char *msg)
{
    perror(msg);
    exit(1);
}

//Function that children executes
void dostuff (int sock);

int main(int argc, char *argv[])
{
    //Paramters for socket connection
	int sockfd, newsockfd, portno;
	socklen_t clilen;
	int pid;
	char buffer[256];
	int i;
	struct sockaddr_in serv_addr, cli_addr;
	int n;

	//check for a port provided by user
	if (argc < 2) {
		fprintf(stderr,"ERROR, no port provided\n");
		exit(1);
	}

	printf("Ajustando GPIO...\n");

    //Setup WiringPi (and GPIOS)
	#ifdef RPI_SERVER
	//setup GPIO
	if(wiringPiSetup()==-1)
	{
		printf("Error al arrcancar WiringPi");
		return -2;
	}

    //Shutdown all pins, (for some reasen some pins boot)
    //with ones, until they are shutdown at this moment
	for(i=0;i<8;i++)
	{
		pinMode(i,OUTPUT);
		digitalWrite(i,0);
	}
	#endif

	printf("GPIO Listos\n");

    //let's begin with the sockets
	sockfd = socket(AF_INET, SOCK_STREAM, 0); //We want an internet socket
	if (sockfd < 0)
        error("ERROR opening socket");
	bzero((char *) &serv_addr, sizeof(serv_addr));


	portno = atoi(argv[1]);
	serv_addr.sin_family = AF_INET;         //We han a internet connection
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portno);     //Setup the port

	//Bind the socket to the parameters
	if (bind(sockfd, (struct sockaddr *) &serv_addr,
			 sizeof(serv_addr)) < 0)
		error("ERROR on binding");

    //only listen for 5 concurrently connections
	listen(sockfd,5);
	clilen = sizeof(cli_addr);

	while (1) {

		//Wair for a connection
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		if (newsockfd < 0)
			error("ERROR on accept");
        //We duplicate the program, to avoid quit and kill the program
		pid = fork();
		if (pid < 0)
			error("ERROR on fork");
		if (pid == 0)  {
		    //the children excecutes the task (dostuff), the father wait for other
            //connections
			close(sockfd);
			dostuff(newsockfd);
			exit(0);
		}
		else close(newsockfd);
		printf("Cerrando Conexion\n");
	} /* end of while */
	close(sockfd);

	return 0;
}
/*For more info of how works this function see
    ControlGUI.java in ControlClient folder
*/
void dostuff (int sock)
{
	int n,m;
	char buffer[256];
	int i;

    //When a connection is stablished, the Scrambot send a string for ID
    //This is not a password!!!!
	n = write(sock,"TACOCABANA1989",14);
	n = write(sock,"1",1);

    //In reality this is an infinite loop
	while(n>0)
	{
	    //Wait for a command
		n = read(sock,buffer,255);
		//When a command is received, send a acknowledge
		m = write(sock,"OK",2);


		//Read the array and on the corresponding motor
		for(i=0;i<8;i++)
		{

			if(buffer[i]=='1')
			{
				#ifdef RPI_SERVER
				digitalWrite(i,1);
				#endif
			}
			else if(buffer[i]==216)
			{
			    //if the program send a special character in
			    //any slot, we shutdown the Raspberry Pi
			    printf("Apagando...");
			    system("sudo halt");
			    exit(0);
			}
			else
			{

				#ifdef RPI_SERVER
				digitalWrite(i,0);
				#endif
			}
		}

        //Clean un the buffer, to avoid fake readings
		buffer[8]=0;
	    //    printf("%s \n",buffer);
		//printf("%i\n",n);
	}

}
