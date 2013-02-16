package SocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * ServerModel for the Super Gomoku server. accepts client connections and
 * spawns threads to handle and process communtication. Currently handles authentication
 * through a database controller
 * @author geldshot, Timothy Jones
 *
 */

public class ServerModel extends Thread {
	private ArrayList<ServerThread> team; //arraylist for holding user threads
	private ServerSocket serverSocket = null;//the socket for accepting new connections
	private boolean listening = true; //boolean for tracking if the server is accepting connections
	private DatabaseController databaseController;//database controller for interacting with server
	
	/**
	 * constructor for ServerModel that initializes the arrays, constructs the database
	 * controller and creates a socket for listening.
	 * @param url url for connecting to database
	 * @param dbuser username used for connecting to database
	 * @param dbpass password used for connecting to database
	 */
	
	public ServerModel(String url, String dbuser, String dbpass){
		System.out.println("url: " + url);
		System.out.println("user: " + dbuser);
		System.out.println("password: " + dbpass);
		databaseController = new DatabaseController(url, dbuser, dbpass);
		team = new ArrayList<ServerThread>();
		try {
			serverSocket = new ServerSocket(4445);//socket for accepting connections
		} catch(IOException e){
			System.err.println("Could not listen on port: 4445.");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}
	
	/**
	 * function for removing threads from the arraylists in the model.
	 * @param thread thread to be removed
	 * @return true if thread removal is successful
	 */
	
	public boolean removeUser(ServerThread thread){
		return team.remove(thread);//removes user thread
	}
	
	/**
	 * function that handles the login interaction with the database controller. 
	 * calls userCheck in the database controller.
	 * @param name username to be logged in with
	 * @param pass password to be logged in with
	 * @return true if username and password combination exist in database.
	 */
	
	public boolean loginUser(String name, String pass){
		if(databaseController.userCheck(name,pass) &&  getServerThread(name) == null)
			return true;	// attempts to login through database
		return false;
	}
	
	/**
	 * function that handles the registration interaction with the database controller.
	 * calls register in the database controller.
	 * @param name user name to be registered
	 * @param pass password to be registered
	 * @return true if username was not present in database and username/password pair
	 * was successfully added
	 */
	
	public boolean registerUser(String name, String pass){
		return databaseController.register(name, pass);//attempts to register through database
	}
	
	/**
	 * Thread that listens for new connections and accepts them.
	 */
	
	public void run(){
		System.out.println("listening for messages");
		
		while (listening){//listen loop for 
			try{
			ServerThread user = new ServerThread(serverSocket.accept(), this);
			team.add(user);//adds user to the matchmaking list
			user.start();//starts thread
			}catch(SocketException e){
				System.out.print("socket failed: ");
				e.printStackTrace();
			}catch(IOException e){
				System.out.print("IOException failure: ");
				e.printStackTrace();
			}
		}
		
		try {
			serverSocket.close();//closes new connection socket
		} catch (IOException e) {
			System.out.print("IOException failure: ");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * matchmakingList function for returning the list of current ServerThreads held
	 * in team attribute and are available for client invites.
	 * @return String list of usernames delimited by ":"
	 */
	
	public String matchmakingList() {
		String s = "";
		if(team.size() >= 1)
			s += team.get(0).getUserName();
		
		for(int i = 1; i < team.size(); i++){
			s+= ":" + team.get(i).getUserName();
		}
		return s;
	}
	
	/**
	 * getServerThread function for getting the ServerThread object with matching user_name
	 * attribute matching the String passed in.
	 * @param username String to be matched to user_name attribute
	 * @return ServerThread that has matching user_name to username, if none then null
	 */
	
	public ServerThread getServerThread(String username){
		for(int i = 0; i < team.size() ; i++){
			if(team.get(i).getUserName().equals(username))
				return team.get(i);
			
		}
		return null;
	}
	
	/**
	 * quit function for exiting out of the server, calls quit for all ServerThreads and clears
	 * team, sets listening to false and closes serverSocket. Then calls System.exit(0)
	 */
	
	public void quit() {
		for(int i = 0; i < team.size() ; i++){
			team.get(i).quit();
		}
		team.clear();
		listening = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("serverSocket close failed: ");
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * getIpAddress function for retrieving an client address for a ServerThread with 
	 * user_name matching the given String username. otherwise returns fail.
	 * @param username String for matching to user_name attributes
	 * @return String of external socket address from ServerThread or "fail"
	 */
	
	public String getIpAddress(String username) {
		ServerThread temp = getServerThread(username);
		if(temp != null)
			return temp.getSocketAddress();
		return "fail";
	}
	
	
	
}
