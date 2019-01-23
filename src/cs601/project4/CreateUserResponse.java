package cs601.project4;

/**
 * JSON output for POST /user/create API response as specified
 * @author DELL
 *
 */
public class CreateUserResponse {

	private int userid;

	public CreateUserResponse(int userId)
	{
		this.userid = userId;
	}
	
	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}
	
}
