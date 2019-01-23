/**
 * 
 */
package cs601.project4;

/**
 * Stores the response server gives to client for this request
 * @author mosopeogundipe
 *
 */
public class ServerResponse {

	private int responseCode;
	private String responseBody;
	
	public ServerResponse(int responseCode, String responseBody)
	{
		this.responseCode = responseCode;
		this.responseBody = responseBody;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
	
}
