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
 *
 */
public class EventDBManager {

	private Connection con = null;
	private String eventTable = null;
	private ResultSet rs = null;	
	
	public EventDBManager()
	{
		loadDriver();
		eventTable = ConfigurationHelper.getConfiguration().getEventTableName();
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
		String hostName = ConfigurationHelper.getConfiguration().getEventAPIDBHostName();
		String dbName = ConfigurationHelper.getConfiguration().getEventDatabaseName();
		String dbUser = ConfigurationHelper.getConfiguration().getEventAPIDBUserName();
		String dbPassword = ConfigurationHelper.getConfiguration().getEventAPIDBPassword();
		int port = ConfigurationHelper.getConfiguration().getEventAPIDBPort();
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
	
	/**
	 * Creates an event in database, and returns id of created event.
	 * Returns id of -1 if user could not be created 
	 */
	public int createEvent(CreateEventRequest event)
	{
		LoggerHelper.makeInfoLog("Entered DBMgr create user");
		int id = -1;		
		try {
			if(con == null)
				createConnection();
			PreparedStatement updateStmt = con.prepareStatement("INSERT INTO events (name, ticketsCreated, ticketsSold, ticketsAvailable, creatorid) VALUES (?,?,?,?,?)");
			updateStmt.setString(1, event.getEventname());
			updateStmt.setInt(2, event.getNumtickets());
			updateStmt.setInt(3, 0);
			updateStmt.setInt(4, event.getNumtickets());	//initially all tickets created are available for sale.
			updateStmt.setInt(5, event.getUserid());
			updateStmt.execute();
			
			PreparedStatement stmt = con.prepareStatement(Constants.GET_EVENT_ID_BY_CREATOR);
			stmt.setString(1, event.getEventname());
			stmt.setInt(2, event.getUserid());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("event id: %d\n", id));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error creating event: " + e.getMessage());
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * Fetches the id of the event from database. Returns the id if event was created by same user. Returns -1 if event otherwise
	 * @param username
	 * @return
	 */
	public int getEventId(CreateEventRequest event)
	{
		int id = -1;
		try {
			if(con == null)
				createConnection();
			PreparedStatement stmt = con.prepareStatement(Constants.GET_EVENT_ID_BY_CREATOR);
			stmt.setString(1, event.getEventname());
			stmt.setInt(2, event.getUserid());
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				//for each result, get the value of the columns name and id
				id = result.getInt("id");
				LoggerHelper.makeInfoLog(String.format("event id: %d\n", id));
			}
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error getting event id: " + e.getMessage());
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * Fetches the id of the event from database. Returns -1 if id doesn't exist
	 * @param username
	 * @return
	 */
	public int getEventId(int eventid)
	{
		int id = -1;
		try {
			if(con == null)
				createConnection(); 
			PreparedStatement stmt = con.prepareStatement(Constants.GET_EVENT_BY_ID);
			stmt.setInt(1, eventid);
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
	
	public boolean doesEventExist(int id)
	{
		if(id == -1)
			return false;
		return true;
	}
	
	/**
	 * Fetches the details for GET Events List
	 * @param username
	 * @return
	 */
	public List<GetEventsResponse> getEventsList()
	{
		List<GetEventsResponse> eventsList = new ArrayList<GetEventsResponse>();
		try {
			if(con == null)
				createConnection();
			String selectStmt = "Select * FROM  events"; 
			PreparedStatement stmt = con.prepareStatement(selectStmt);
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				GetEventsResponse element = new GetEventsResponse(result.getInt("id"), result.getString("name"), result.getInt("creatorid"), 
						result.getInt("ticketsAvailable"), result.getInt("ticketsSold"));
				eventsList.add(element);								
			}
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error getting user id: " + e.getMessage());
			e.printStackTrace();
		}
		return eventsList;
	}
	
	/**
	 * Fetches the details for GET Events List
	 * @param username
	 * @return
	 */
	public GetEventsResponse getEventById(int eventid)
	{
		GetEventsResponse event = null;
		try {
			if(con == null)
				createConnection();
			PreparedStatement stmt = con.prepareStatement(Constants.GET_EVENT_BY_ID);
			stmt.setInt(1, eventid);
			ResultSet result = stmt.executeQuery();
			while (result.next()) {
				event = new GetEventsResponse(result.getInt("id"), result.getString("name"), result.getInt("creatorid"), 
						result.getInt("ticketsAvailable"), result.getInt("ticketsSold"));								
			}
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LoggerHelper.makeSevereLog("Error getting event id: " + e.getMessage());
			e.printStackTrace();
		}
		return event;
	}
	
	/**
	 * updates the number of tickets purchased for an event
	 * @param username
	 * @return
	 */
	public synchronized boolean purchaseTickets(EventDBManager db, int numTickets, int eventId)
	{
		GetEventsResponse updatedEvent = null;
		boolean purchaseSuccessful = false;
		try {
			if(con == null)
				createConnection();
			GetEventsResponse event = db.getEventById(eventId);
			if(event == null)
			{
				LoggerHelper.makeInfoLog("No events found with id: " + eventId);
				return purchaseSuccessful;
			}
			else if(event.getAvail() == 0)
			{
				LoggerHelper.makeInfoLog("No tickets available for event with id: " + eventId);
				return purchaseSuccessful;
			}
			else if(numTickets > event.getAvail())
			{
				LoggerHelper.makeInfoLog(numTickets + " Tickets Requested are more than " +event.getAvail() + " Tickets Available for event with id: " + eventId);
				return purchaseSuccessful;
			}
			else
			{
				con.setAutoCommit(false);
				PreparedStatement updateStmt = con.prepareStatement(Constants.PURCHASE_EVENT_TICKETS);
				updateStmt.setInt(1, numTickets);
				updateStmt.setInt(2, numTickets);
				updateStmt.setInt(3, eventId);
				updateStmt.execute();
				con.commit();
				
				PreparedStatement stmt = con.prepareStatement(Constants.GET_EVENT_BY_ID);
				stmt.setInt(1, eventId);
				ResultSet result = stmt.executeQuery();
				while (result.next()) {
					updatedEvent = new GetEventsResponse(result.getInt("id"), result.getString("name"), result.getInt("creatorid"), 
							result.getInt("ticketsAvailable"), result.getInt("ticketsSold"));								
				}
				//If there's any discrepancy between no of tickets in updated event and the expected number, roll back!
				if(updatedEvent.getAvail() != (event.getAvail() - numTickets) || updatedEvent.getPurchased() != (event.getPurchased() + numTickets))
				{
					LoggerHelper.makeSevereLog("Updated number of tickets doesn't match expected no." + "updatedAvail: " + updatedEvent.getAvail() + "expectedAvail: " + 
				(event.getAvail() - numTickets) + "updatedPurchased: " + updatedEvent.getPurchased() + "expectedPurchased: " + (event.getPurchased() + numTickets));
					LoggerHelper.makeSevereLog("Rolling back...");
					con.rollback();
				}
				else
				{
					purchaseSuccessful = true;
				}
			}			
		} catch (SQLException e) {
			// Roll back here
			LoggerHelper.makeSevereLog("Error getting purchasing event tickets in EVENT DB: " + e.getMessage() + "EVENT ID: " + eventId);
			e.printStackTrace();
			 if (con != null) {
		            try {
		            	LoggerHelper.makeSevereLog("Transaction is being rolled back");
		                con.rollback();
		            } catch(SQLException excep) {
		            	LoggerHelper.makeSevereLog("Error is rolling back:" + excep.getMessage());
		            }
			 }
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
		return purchaseSuccessful;
	}
}
