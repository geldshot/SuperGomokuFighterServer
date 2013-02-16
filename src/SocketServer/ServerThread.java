package SocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ServerThread for handling client connections. holds the socket
 * for client connections and processes incoming and outgoing messages.
 * @author geldshot, Timothy Jones
 *
 */

public class ServerThread extends Thread{
	private Socket 			socket = null; //incoming socket
	private boolean 		active = true; //keeps track of user's activity
	private String 			user_name; //user name associated with thread
	private String 			password; //password associated with thread
	private boolean 		ingame; //boolean for tracking availability of user for matchmaking
	private PrintWriter 	out; //printwriter for sending messages to user
	private ServerModel 	server_model; //server model
	private Invitation 		invitation = null;
	
	/**
	 * constructor for ServerThread, initializes ingame to false and sets socket and 
	 * server_model objects.
	 * @param socket socket connection to client
	 * @param server_model model that spawned this ServerThread.
	 */
	
	public ServerThread(Socket socket, ServerModel server_model){ 
		super("ServerThread"); //calls thread super constructor
		this.socket = socket; //sets incoming socket to the socket passed in
		boolean ingame = false; //default ingame value is false
		this.server_model = server_model; //server model set to the provided ServerModel
		user_name = "";
		try{
		out = new PrintWriter(socket.getOutputStream(), true); //creates output printwriter
		}catch(IOException e){
			System.out.print("PrintWriter out failed to initialize: ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Function for sending messages to the client
	 * @param message message to be sent to the client
	 * @return true if message was successfully sent
	 */
	
	public boolean send(String message){
		try{
		System.out.println( "Message sent: " + user_name + ">" + message);	
		out.println(message);//sends message to user
		
		}catch(Exception e){//catches error
			
			System.out.print("message failed to send: ");
			e.printStackTrace();
			
			return false;
		}
		return true;
	}
	
	/**
	 * function for processing messages after authentication. currently only echoes.
	 * @param message message to be processed
	 */
	
	public void process(String message){
		String[] pieces = message.split(":");
		
		if(message.charAt(0) == 'M'){
			send("C L " +server_model.matchmakingList());
		}
		
		else if(message.charAt(0) == 'C' && pieces.length == 2 ){
			send("C " + server_model.getIpAddress(pieces[1]) );
		}
		
		else if(pieces[0].equalsIgnoreCase("invite") && pieces.length == 2){
			
			ServerThread threadHolder;
			if((threadHolder = server_model.getServerThread(pieces[1])) == null){
				send("c fail");
				return;
			}
			threadHolder.sendInvite(this);
		}
		
		else if(pieces[0].equalsIgnoreCase("accept") && invitation != null){
			
			invitation.getInviter().send("g connect to:" + getSocketAddress());
			invitation = null;
		}
		
		else if(pieces[0].equalsIgnoreCase("deny") && invitation != null){
			invitation.getInviter().send("c deny");
			invitation = null;
		}
		
		else{
		send("c fail");//prepared function for processing messages later
		}
	}
	
	/**
	 * Authentication function that checks the nature of the message, registration or login, and 
	 * calls the appropriate message.
	 * @param message message for trying authentication
	 * @return true if authentication was successful
	 */
	
	public boolean authenticate(String message){
		//authentication messages have three parts, identifier:username:password
		String[] pieces = message.split(":");
		if(message.charAt(0) == 'U' && pieces.length == 3){//user login
			System.out.println("attempting login");
			if(server_model.loginUser(pieces[1], pieces[2])){//attempt to login
				this.user_name = pieces[1];
				this.password = pieces[2];
				return true; //authentication succeeded
			}
			
			
				
		}
		else if(message.charAt(0) == 'R' && pieces.length == 3){//user register
			System.out.println("attempting register");
			if(server_model.registerUser(pieces[1], pieces[2])){//attempt to register
				this.user_name = pieces[1];
				this.password = pieces[2];
				return true; //authentication succeeded
			}
		}
		System.out.println("authentication failure");
		return false;//authentication failed
	}
	
	/**
	 * ServerThread's listen loops for directing incoming communication to the
	 * appropriate functions
	 */
	
	public void run(){
		
		try {
			System.out.println("connection established");
			
			String s;
			BufferedReader in = new BufferedReader( 
				new InputStreamReader(
				socket.getInputStream()));
			
			boolean checkauth = false;
			while(!checkauth){//authentication loop
				s = in.readLine();
				System.out.println("Authenticating message: " + s);
				if(authenticate(s)){//performs authentication
					checkauth = true;
					System.out.println("Authentication successful: " + s);
					send("c success");
				}
				else if(s.equalsIgnoreCase("bye")){//for early exit from authentication loop
					checkauth = true;
					active = false;
				}
				else{
					send("c fail");//responds that authentication failed
				}
				
			}
			send("C L " +server_model.matchmakingList());
			//a matchmaking list dump will be here
			while(active){//message receiving loop
				
				System.out.print("reading input: ");
				s = in.readLine();
				System.out.println(user_name + ">" + s);
				if(s.equalsIgnoreCase("bye") || s.equals("\0"))
					break;
				process(s);//processes incoming messages
				
				
			}
			out.println("bye");//sends bye to 
			out.close();
			in.close();
			socket.close();
			if(!server_model.removeUser(this))
				System.out.println("thread removal failure: " + user_name);
			} catch(IOException e){
				System.out.println("Thread input failure: " + user_name);
				e.printStackTrace();
				if(!server_model.removeUser(this))
					System.out.println("thread removal failure");
		}
		
	}
	
	/**
	 * function for checking ingame value
	 * @return ingame's boolean value
	 */
	
	public boolean checkGameStatus(){
		return ingame;//ingame's value returned
	}
	
	/**
	 * function for setting ingame
	 * @param ingame boolean value to set ingame to
	 */
	
	public void setGameStatus(boolean ingame){
		this.ingame = ingame;//sets ingame's value
	}
	
	/**
	 * getSocketAddress that retrieves address of client connected to thread
	 * @return String of socket's remote address
	 */
	
	public String getSocketAddress(){
		return ("" + socket.getRemoteSocketAddress());
	}
	
	/**
	 * function for quitting out of sockets in preparation for closing thread
	 */
	
	public void quit(){
		out.println("bye");//sends bye to client, all purpose die message
		out.close();
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Thread quit failed: " + user_name);
			e.printStackTrace();
		}
	}
	
	/**
	 * matchUsername function for comparing user_name attribute to supplied String name
	 * @param name String to compare user_name attribute to
	 * @return true if user_name String matches name String
	 */
	
	public boolean matchUsername(String name){
		return this.user_name.equals(name);
	}
	
	/**
	 * getUserName function for getting user_name attribute
	 * @return String user_name attribute
	 */
	
	public String getUserName(){
		return this.user_name;
	}
	
	/**
	 * sendInvite function for creating Invitation object and sending invite message to client.
	 * will only allow setting invitation attribute and message sending if there is no outstanding
	 * invitation.
	 * @param inviter ServerThread that originated the invitation
	 */
	
	public void sendInvite(ServerThread inviter){
		if(invitation == null){
		invitation = new Invitation(inviter);
		send(("c Invite:" + inviter.getUserName() ));
		}
		else
			inviter.send("c deny");//tells client that the invitation wasn't possible
	}

}