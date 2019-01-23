/**
 * 
 */
package cs601.project4.UserAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DELL
 *
 */
public class GetUserResponse {

	private int userid;
	private String username;
	private List<EventsResponse> tickets;	
	
	public GetUserResponse()
	{
		tickets = new ArrayList<EventsResponse>();
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public List<EventsResponse> getTickets() {
		return tickets;
	}
	public void setTickets(List<EventsResponse> tickets) {
		this.tickets = tickets;
	}
	public void addEventId(int eventid)
	{
		EventsResponse event = new EventsResponse(eventid);
		if(tickets == null)
			tickets = new ArrayList<EventsResponse>();
		tickets.add(event);
	}

	public class EventsResponse
	{
		private int eventid;
		public EventsResponse(int eventid)
		{
			this.eventid = eventid;
		}
		public int getEventid() {
			return eventid;
		}
		public void setEventid(int eventid) {
			this.eventid = eventid;
		}
	}
}
