/**
 * 
 */
package cs601.project4;

/**
 * @author DELL
 *
 */
public class GetEventsResponse {

	private int eventid;
	private String eventname;
	private int userid;
	private int avail;
	private int purchased;
	public GetEventsResponse()
	{
		
	}
	public GetEventsResponse(int eventid, String eventname, int userid, int avail, int purchased)
	{
		this.eventid = eventid;
		this.eventname = eventname;
		this.userid = userid;
		this.avail = avail;
		this.purchased = purchased;
	}
	public int getEventid() {
		return eventid;
	}
	public void setEventid(int eventid) {
		this.eventid = eventid;
	}
	public String getEventname() {
		return eventname;
	}
	public void setEventname(String eventname) {
		this.eventname = eventname;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public int getAvail() {
		return avail;
	}
	public void setAvail(int avail) {
		this.avail = avail;
	}
	public int getPurchased() {
		return purchased;
	}
	public void setPurchased(int purchased) {
		this.purchased = purchased;
	}
	
}
