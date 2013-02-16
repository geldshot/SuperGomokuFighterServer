package SocketServer;

import java.sql.*;

import javax.sql.*;

/**
 * Database Controller built for interacting with the database set up on
 * the local server. Currently assumes Server application and database
 * would be on the same computer.
 * @author geldshot Timothy Jones
 *
 */

public class DatabaseController {
	private String connectionURL;//connection Url
	private String HostName; //database login name
	private String HostPass; //database login password
	
	/**
	 * constructs the Database Controller with the connectionURL set to
	 * the given string.
	 * @param connectionurl String to set the connectionURL to
	 * @param hostname String to set HostName to for connections to database
	 * @param hostpass String to set HostPass to for connections to database
	 */
	
	public DatabaseController(String connectionurl, String hostname, String hostpass){
		connectionURL = "jdbc:mysql://" + connectionurl;
		HostName = hostname;
		HostPass = hostpass;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("driver creation failed: ");
			e.printStackTrace();
		}
	}
	
	/**
	 * creates a connection object and returns it to the called function. later
	 * functionality will use something other than root to create the connection.
	 * @return connection object for interacting with the database.
	 */
	
	private Connection connect(){
		Connection connection = null;//sets connection to null
		try {
			connection = DriverManager.getConnection(connectionURL, HostName, HostPass); //starts connection
		} catch (SQLException e) {
			System.out.println("connection failed: ");
			e.printStackTrace();
		}
		return connection;
	}
	
	/**
	 * function for closing connection objects to keep other functions clean.
	 * @param connection connection object to be closed.
	 */
	
	private void close(Connection connection){
		if(connection != null){//makes sure connection isn't null
			try {
				connection.close();//closes connection
			} catch (SQLException e) {
				System.out.println("connection close failed: ");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * userCheck function for checking to see if a user exists with the
	 * given username and password. works for logging in at the moment.
	 * @param name username to be checked in database
	 * @param pass password to be checked in database
	 * @return returns true if the username and password are in the database together
	 */
	
	public boolean userCheck(String name, String pass){
		System.out.println("usercheck called.");
		Connection connection;//calls connection and statement creation
		if((connection = connect()) == null)
			return false;
		PreparedStatement statement;
		ResultSet resultset = null;
		try {
			statement = connection.prepareStatement("SELECT * FROM supergomoku.users WHERE (username = ? AND password = ?);");
			statement.setString(1, name);
			statement.setString(2, pass);
			resultset = statement.executeQuery();
			
			if(resultset.first()){
				System.out.println("user id: " + resultset.getString(1));
				return true;	
			}
		} catch (SQLException e1) {
			System.out.println("select failed: ");
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		System.out.println("login failed");
		close(connection);//closes connection
		return false;
	}
	
	/**
	 * register function for checking to see if a user is present already in
	 * the database and adding their username and password if not.
	 * @param name username to be added
	 * @param pass password to be added
	 * @return returns true only if the insert to the database was successful
	 */
	
	public boolean register(String name, String pass){
		System.out.println("register called.");
		Connection connection;//calls connection and statement creation
		if((connection = connect()) == null)
			return false;
		PreparedStatement statement;
		ResultSet resultset = null;
		try {
			statement = connection.prepareStatement("SELECT username FROM supergomoku.users WHERE username = ?;");
			statement.setString(1, name);
			resultset = statement.executeQuery();
		} catch (SQLException e1) {
			System.out.println("select failed: ");
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		try {
			if(resultset != null && resultset.first()){
				System.out.println(resultset.getString(1));
				close(connection);
				return false;
			}
			else{
			statement = connection.prepareStatement("INSERT INTO supergomoku.users(username, password) VALUES (?, ?);");
			statement.setString(1, name);
			statement.setString(2, pass);
			statement.executeUpdate();
			}
		} catch (SQLException e1) {
			System.out.println("insert failed: ");
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		close(connection); //closes connection
		return true;
	}
	
	public boolean tableSetup(){
		System.out.println("table setup called.");
		Connection connection;
		if((connection = connect()) == null)
			return false;
		PreparedStatement statement;
		ResultSet resultset = null;
		try{
			statement = connection.prepareStatement("");
			resultset = statement.executeQuery();
		}catch (SQLException e1){
			System.out.println("schema creation failed");
			e1.printStackTrace();
			return false;
		}
		
		
		
		return false;
		
	}
	
}
