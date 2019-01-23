/**
 * 
 */
package cs601.project4.FrontEndAPI;

import java.util.ArrayList;
import java.util.List;

import cs601.project4.GetEventsResponse;

/**
 * @author DELL
 *
 */
public class GetUserResponse {

	private int userid;
	private String username;
	private List<GetEventsResponse> tickets;	
	
	public GetUserResponse(int userid, String username, List<GetEventsResponse> tickets)
	{
		this.userid = userid;
		this.username = username;
		this.tickets = tickets;
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
	public List<GetEventsResponse> getTickets() {
		return tickets;
	}
	public void setTickets(List<GetEventsResponse> tickets) {
		this.tickets = tickets;
	}
	public void addEvent(GetEventsResponse event)
	{
		if(tickets == null)
			tickets = new ArrayList<GetEventsResponse>();
		tickets.add(event);
	}
}
