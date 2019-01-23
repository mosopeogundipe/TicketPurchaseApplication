/**
 * 
 */
package cs601.project4;

/**
 * @author DELL
 *
 */
public class TransferUserTicketsReq {
	private int eventid;
	private int tickets;
	private int targetuser;
	
	public TransferUserTicketsReq(int eventId, int tickets)
	{
		this.eventid = eventId;
		this.tickets = tickets;
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
	public int getTargetuser() {
		return targetuser;
	}
	public void setTargetuser(int targetuser) {
		this.targetuser = targetuser;
	}
	
}
