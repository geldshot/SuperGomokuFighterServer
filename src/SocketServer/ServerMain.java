package SocketServer;

import java.util.Scanner;
import java.io.*;

public class ServerMain {
	
	public static void main(String[] args){
		Scanner keyboardScanner = new Scanner(System.in);
		System.out.println("enter database url (usually localhost:3306/): ");
		String DBurl = keyboardScanner.nextLine();
		System.out.println("enter database user: ");
		String DBuser = keyboardScanner.nextLine();
		System.out.println("enter database password: ");
		String DBpass = keyboardScanner.nextLine();
		
		
		ServerModel server_model = new ServerModel(DBurl, DBuser, DBpass);//constructs server
		server_model.start();	//starts thread
								//will be an idle piece here that calls updates for
								//clients periodically
		String command = "";
		while(!command.equalsIgnoreCase("bye")){//loop for controlling when to shut down server and some basic commands
			System.out.println("Enter command: ( \"bye\" to exit and \"list threads\" for all of the current users connected");
			command = keyboardScanner.nextLine();
			if(command.equalsIgnoreCase("list threads"))
				System.out.println(server_model.matchmakingList());
		}
		
		server_model.quit();
	}
	
}
