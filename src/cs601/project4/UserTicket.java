/**
 * 
 */
package cs601.project4;

/**
 * @author DELL
 *
 */
public class UserTicket {

	private int id;
	private int userid;
	private int eventid;
	private int tickets;
	
	public UserTicket(int id, int userId, int eventId, int tickets)
	{
		this.id = id;
		this.userid = userId;
		this.eventid = eventId;
		this.tickets = tickets;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public int getEventid() {
		return eventid;
	}

	public void setEventid(int eventid) {
		this.eventid = eventid;
	}

	public int getTickets() {
		return tickets;
	}

	public void setTickets(int tickets) {
		this.tickets = tickets;
	}
	
	
}
