package cs601.project4;

public class PurchaseEventTicketsReq {
	
	private int userid;
	private int eventid;
	private int tickets;
	
	public PurchaseEventTicketsReq(int userId, int eventId, int tickets)
	{
		this.userid = userId;
		this.eventid = eventId;
		this.tickets = tickets;
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
