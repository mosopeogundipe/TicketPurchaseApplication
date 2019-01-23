/**
 * 
 */
package cs601.project4;

/**
 * @author DELL
 *
 */
public class CreateEventResponse {

	private int eventid;

	public CreateEventResponse(int eventId)
	{
		this.eventid = eventId;
	}
	
	public int getEventid() {
		return eventid;
	}

	public void setEventid(int eventid) {
		this.eventid = eventid;
	}
	
}
