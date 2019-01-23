/**
 * 
 */
package cs601.project4;

/**
 * @author DELL
 *
 */
public class Constants {

	public static final String PATH_SEPARATOR = "/";
	public static final String URL_PREFIX = "http://";
	public static final String HTTP_GET = "GET";
	public static final String HTTP_POST = "POST";
	public static final int HTTP_OK_RESPONSE = 200;
	public static final String GET_EVENTS_LIST = "/events";
	public static final String POST_CREATE = "/create";
	public static final String POST_PURCHASE_TICKETS = "purchase";
	public static final String POST_TRANSFER_TICKETS = "/tickets/transfer";
	public static final String ZERO = "0";
	
	
	//************************  INTERNAL API URLS BEGIN ****************************//
	public static final String GET_EVENTS_LIST_INTERNAL = "/list";
	public static final String POST_PURCHASE_USER_TICKETS_INTERNAL = "/tickets/add";
	public static final String POST_PURCHASE__EVENTS_TICKETS_INTERNAL = "/purchase";
	public static final String POST_TRANSFER__USER_TICKETS_INTERNAL = "/tickets/transfer";	
	//************************  INTERNAL API URLS END ****************************//
	
	
	//*********************** DB CONSTANTS BEGIN ************************************//
	public static final String GET_EVENT_ID_BY_CREATOR = "SELECT id FROM events where name=? AND creatorid=?"; 
	public static final String GET_USER_ID_BY_USERNAME = "SELECT id FROM users where username=?";
	public static final String GET_EVENT_BY_ID = "SELECT * FROM events where id=?";
	public static final String PURCHASE_EVENT_TICKETS = "UPDATE events SET ticketsSold=(ticketsSold + ?), ticketsAvailable=(ticketsAvailable - ?) WHERE id=?";
	public static final String DB_TRUE = "true";
	public static final String PURCHASE_USER_TICKETS = "INSERT INTO tickets (userid,eventid,num_tickets) VALUES (?,?,?)";
	public static final String GET_JUST_BOUGHT_USER_TICKETS = "SELECT * FROM user40.tickets WHERE userid=? AND eventid=? AND num_tickets=?";
	public static final String GET_USER_TICKETS = "SELECT * FROM tickets WHERE userid=? AND eventid=? LIMIT 1";
	public static final String UPDATE_USER_TICKETS = "UPDATE tickets SET num_tickets=(num_tickets + ?) WHERE id=?";
	public static final String UPDATE_REDUCE_USER_TICKETS = "UPDATE tickets SET num_tickets=(num_tickets - ?) WHERE id=?";
	//*********************** DB CONSTANTS END ************************************//
}
