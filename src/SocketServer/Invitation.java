package SocketServer;

/**
 * Invitation class meant to keep track of ServerThread that originated a
 * matchmaking invite.
 * @author Timothy Jones, A.K.A. geldshot
 *
 */

public class Invitation {
	private ServerThread inviter;
	
	/**
	 * constructor for Invitation class
	 * @param inviter ServerThread object to set inviter attribute to
	 */
	
	public Invitation(ServerThread inviter){
		this.inviter = inviter;
	}
	
	/**
	 * setter for inviter ServerThread object
	 * @param inviter ServerThread object to set inviter to
	 */
	
	public void setInviter(ServerThread inviter){ this.inviter = inviter; }
	
	/**
	 * getter for inviter ServerThread object
	 * @return returns inviter ServerThread object
	 */
	
	public ServerThread getInviter(){ return this.inviter;}
}
