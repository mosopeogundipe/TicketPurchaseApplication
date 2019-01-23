/**
 * 
 */
package cs601.project4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author DELL
 * Contains queries and connections for tables in the user database
 */
public class UserDBManager {

	private Connection con = null;
	private static String userTable = null;
	private ResultSet rs = null;
	
	public UserDBManager()
	{
		if(userTable == null)
		{
			userTable = ConfigurationHelper.getConfiguration().getUserTableName();
		}
		loadDriver();		
		//createConnection();
	}
	
	private void loadDriver()
	{
		try {
			// load driver
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		}
		catch (Exception e) {
			LoggerHelper.makeSevereLog("Can't find driver");
			//System.err.println("Can't find driver");
		}
	}
	
	public void createConnection()
	{
		// format "jdbc:mysql://[hostname][:port]/[dbname]"
		//note: if connecting through an ssh tunnel make sure to use 127.0.0.1 and
		//also to that the ports are set up correctly
		String hostName = ConfigurationHelper.getConfiguration().getUserAPIDBHostName();
		String dbName = ConfigurationHelper.getConfiguration().getUserAPIDatabaseName();
		String dbUser = ConfigurationHelper.getConfiguration().getUserAPIDBUserName();
		String dbPassword = ConfigurationHelper.getConfiguration().getUserAPIDBPassword();
		int port = ConfigurationHelper.getConfiguration().getUserAPIDBPort();
		String urlString = "jdbc:mysql://" + hostName + ":" + port + "/"+ dbName;
		//Must set time zone explicitly in newer versions of mySQL.
		String timeZoneSettings = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
		try {
			con = DriverManager.getConnection(urlString+timeZoneSettings,
					dbUser,
					dbPassword);
		} catch (SQLException e) {
			LoggerHelper.makeSevereLog("Can't connect to SQL Database");	
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a user in database, and returns id of created user.
	 * Returns id of -1 if user could not be created 
	 */
	public int createUser(String username)
	{
		LoggerHelper.makeInfoLog("Entered DBMgr create user");
		int id = -1;		
		try {
			if(con == null)
				createConnection();
			PreparedStatement updateStmt = con.prepareStatement("INSERT INTO users (username) VALUES (?)");
			updateStmt.setString(1, username);
			updateStmt.execute();
			PreparedStatement stmt = con.prepareStatement(Constants.GET_USER_ID_BY_USERNAME);
			stmt.setString(1, username);
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("id: %d\n", id));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error creating user: " + e.getMessage());
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * Fetches the id of the user from database. Returns -1 if id doesn't exist
	 * @param username
	 * @return
	 */
	public int getUserId(String username)
	{
		int id = -1;
		try {
			if(con == null)
				createConnection();
			PreparedStatement stmt = con.prepareStatement(Constants.GET_USER_ID_BY_USERNAME);
			stmt.setString(1, username);
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("id: %d\n", id));
			}
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error getting user id: " + e.getMessage());
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * Fetches the id of the user from database. Returns -1 if id doesn't exist
	 * @param username
	 * @return
	 */
	public int getUserId(int userid)
	{
		int id = -1;
		try {
			if(con == null)
				createConnection();
			String selectStmt = "SELECT * FROM users where id=?"; 
			PreparedStatement stmt = con.prepareStatement(selectStmt);
			stmt.setInt(1, userid);
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("id: %d\n", id));
			}
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error getting user id: " + e.getMessage());
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * Fetches the details for GET User internal API call
	 * @param username
	 * @return
	 */
	public cs601.project4.UserAPI.GetUserResponse getUserInternalAPI(int userid)
	{
		cs601.project4.UserAPI.GetUserResponse response = new cs601.project4.UserAPI.GetUserResponse();
		try {
			if(con == null)
				createConnection();
			String selectStmt = "Select u.id, u.username, t.eventid, t.num_tickets  FROM users u LEFT OUTER JOIN tickets t ON u.id=t.userid where u.id = ?"; 
			PreparedStatement stmt = con.prepareStatement(selectStmt);
			stmt.setInt(1, userid);
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				if(result.isFirst())
				{
					response.setUserid(result.getInt("id"));
					response.setUsername(result.getString("username"));
				}
				if(result.getInt("eventid") > 0)
				{
					for(int i=0; i<result.getInt("num_tickets"); i++)
					{
						response.addEventId(result.getInt("eventid"));
					}					
				}								
			}
			LoggerHelper.makeInfoLog(String.format("id: %d\n", response.getUserid()));
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error getting user id: " + e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Creates a record for user, events and tickets in database.
	 * Returns false if record could not be created 
	 */
	public synchronized boolean addTickets(PurchaseEventTicketsReq tickets)
	{
		LoggerHelper.makeInfoLog("Entered DBMgr add tickets");
		boolean areTicketsAdded = false;	
		int id = -1;
		try {
			if(con == null)
				createConnection();
			con.setAutoCommit(false);
			UserTicket ticketFromDB = getUserTicket(tickets.getUserid(), tickets.getEventid());
			if(ticketFromDB == null)	//INSERT if no previous record exists with same user and event
			{				
				PreparedStatement updateStmt = con.prepareStatement(Constants.PURCHASE_USER_TICKETS);
				updateStmt.setInt(1, tickets.getUserid());
				updateStmt.setInt(2, tickets.getEventid());
				updateStmt.setInt(3, tickets.getTickets());
				updateStmt.execute();				
			}
			else				//UPDATE if previous record exists with same user and event
			{
				PreparedStatement updateStmt = con.prepareStatement(Constants.UPDATE_USER_TICKETS);
				updateStmt.setInt(1, tickets.getTickets());
				updateStmt.setInt(2, ticketFromDB.getId());
				updateStmt.execute();
			}
			con.commit();
			PreparedStatement stmt = con.prepareStatement(Constants.GET_JUST_BOUGHT_USER_TICKETS);
			stmt.setInt(1, tickets.getUserid());
			stmt.setInt(2, tickets.getEventid());
			stmt.setInt(3, ticketFromDB == null ? tickets.getTickets() : ticketFromDB.getTickets() + tickets.getTickets());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("id: %d\n", id));
			}
			if(id > 0)
			{
				LoggerHelper.makeSevereLog("Ticket created successfully. id is: " + id);
				areTicketsAdded = true;
			}
		} catch (SQLException e) {
			try {
				LoggerHelper.makeSevereLog("Transaction is being rolled back");
				con.rollback();
			} catch (SQLException excep) {
				LoggerHelper.makeSevereLog("Error is rolling back:" + excep.getMessage());
				excep.printStackTrace();
			}
			LoggerHelper.makeSevereLog("Error adding tickets to user in USER API: " + e.getMessage() + "userid: " + tickets.getUserid());
			e.printStackTrace();
		}
		finally {			
			try {
				if(con != null)
					con.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LoggerHelper.makeSevereLog("Error re-setting auto commit");
			}
		}
		return areTicketsAdded;
	}
	
	public void insertUserTickets(int userId, int eventId, int numTickets) throws SQLException
	{
		PreparedStatement updateStmt = con.prepareStatement(Constants.PURCHASE_USER_TICKETS);
		updateStmt.setInt(1, userId);
		updateStmt.setInt(2, eventId);
		updateStmt.setInt(3, numTickets);
		updateStmt.execute();
	}
	
	public void increaseUserTickets(int numTickets, int id) throws SQLException
	{
		PreparedStatement updateStmt = con.prepareStatement(Constants.UPDATE_USER_TICKETS);
		updateStmt.setInt(1, numTickets);
		updateStmt.setInt(2, id);
		updateStmt.execute();
	}
	
	public void decreaseUserTickets(int numTickets, int id) throws SQLException
	{
		PreparedStatement updateStmt = con.prepareStatement(Constants.UPDATE_REDUCE_USER_TICKETS);
		updateStmt.setInt(1, numTickets);
		updateStmt.setInt(2, id);
		updateStmt.execute();
	}
	/**
	 * returns all valid results from tickets table.
	 * @param userId
	 * @param eventId
	 * @param num
	 * @return
	 */
	public UserTicket getUserTicket(int userId, int eventId)
	{
		UserTicket ticket = null;
		//fetch valid userId and eventId combo from tickets table here
		;
		try {
			if(con == null)
				createConnection();
			PreparedStatement stmt = con.prepareStatement(Constants.GET_USER_TICKETS);
			stmt.setInt(1, userId);
			stmt.setInt(2, eventId);
			ResultSet result = stmt.executeQuery();			
			while (result.next()) {
				//for each result, get the value of the columns name and id
				ticket = new UserTicket(result.getInt("id"),result.getInt("userid"),result.getInt("eventid"),result.getInt("num_tickets"));
				LoggerHelper.makeInfoLog(String.format("user id: %d\n", ticket.getUserid()));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ticket;
	}
	
	/**
	 * Transfers tickets from a user to another in database.
	 * Returns false if ticket could not be transferred 
	 */
	public synchronized boolean transferTickets(TransferUserTicketsReq request, int fromUserId)
	{
		LoggerHelper.makeInfoLog("Entered DBMgr transfer tickets");
		boolean areTicketsAdded = false;	
		int id = -1;
		try {
			if(con == null)
				createConnection();
			if(!doesUserExist(getUserId(fromUserId)) || !doesUserExist(getUserId(request.getTargetuser())))
			{
				LoggerHelper.makeInfoLog("Either the targetuser or user transferring ticket do not exist");
				return areTicketsAdded;
			}				
			UserTicket fromUser = getUserTicket(fromUserId, request.getEventid());
			UserTicket toUser = getUserTicket(request.getTargetuser(), request.getEventid());
			if(fromUser == null)
			{
				LoggerHelper.makeInfoLog("User " + fromUserId + " has not bought any tickets for this event");
				return areTicketsAdded;
			}
			if(fromUser.getTickets() < request.getTickets())
			{
				LoggerHelper.makeInfoLog("User " + fromUser.getUserid() + " does not have up to " + request.getTickets() + " tickets to transfer");
				return areTicketsAdded;
			}
			con.setAutoCommit(false);
			if(toUser == null)			//INSERT if no previous record exists with same user and event
			{
				//INSERT toUser, UPDATE (decrease ticket) of fromUser
				insertUserTickets(request.getTargetuser(), request.getEventid(), request.getTickets());
				decreaseUserTickets(request.getTickets(), fromUser.getId());
			}
			else						//UPDATE if previous record exists with same user and event
			{
				//UPDATE (decrease ticket) of toUser, UPDATE (decrease ticket) of fromUser
				increaseUserTickets(request.getTickets(), toUser.getId());
				decreaseUserTickets(request.getTickets(), fromUser.getId());
			}
			con.commit();
			
			PreparedStatement stmt = con.prepareStatement(Constants.GET_JUST_BOUGHT_USER_TICKETS);
			stmt.setInt(1, request.getTargetuser());
			stmt.setInt(2, request.getEventid());
			stmt.setInt(3, toUser == null ? request.getTickets() : toUser.getTickets() + request.getTickets());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("id: %d\n", id));
			}
			if(id > 0)
			{
				LoggerHelper.makeSevereLog("Ticket transferred successfully. id is: " + id);
				areTicketsAdded = true;
			}
		} catch (SQLException e) {
			try {
				LoggerHelper.makeSevereLog("Transaction is being rolled back");
				con.rollback();
			} catch (SQLException excep) {
				LoggerHelper.makeSevereLog("Error is rolling back:" + excep.getMessage());
				excep.printStackTrace();
			}
			LoggerHelper.makeSevereLog("Error adding tickets to user in USER API: " + e.getMessage() + "userid: " + request.getTargetuser());
			e.printStackTrace();
		}
		finally {			
			try {
				if(con != null)
					con.setAutoCommit(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LoggerHelper.makeSevereLog("Error re-setting auto commit");
			}
		}
		return areTicketsAdded;
	}
	
	public boolean doesUserExist(int id)
	{
		if(id <= 0)
			return false;
		return true;
	}
	
	/**
	 * Call this method when done with a query or sequence of queries
	 */
	public void closeConnection()
	{
		try{
	         if(con!=null)
	            con.close();
	      }catch(SQLException se){
	    	  LoggerHelper.makeSevereLog("Error closing connection: " + se.getMessage());
	         se.printStackTrace();
	      }
	}
	
}
