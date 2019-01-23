package cs601.project4.UserAPI;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import cs601.project4.Constants;
import cs601.project4.CreateUserRequest;
import cs601.project4.CreateUserResponse;
import cs601.project4.EventDBManager;
import cs601.project4.HttpClient;
import cs601.project4.LoggerHelper;
import cs601.project4.PurchaseEventTicketsReq;
import cs601.project4.PurchaseUserTicketsReq;
import cs601.project4.ServerResponse;
import cs601.project4.TransferUserTicketsReq;
import cs601.project4.UserDBManager;
import cs601.project4.Utilities;

public class UserServletHelper {

	private Gson gson;
	private String eventAPIHostName;
	private int eventAPIPort;
	
	public UserServletHelper(String eventAPIHostName, int eventAPIPort)
	{
		gson = new Gson();
		this.eventAPIHostName = eventAPIHostName;
		this.eventAPIPort = eventAPIPort;
	}
	
	public void getUserById(HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered GET users/{userid} in user API");
		UserDBManager db = new UserDBManager();
		db.createConnection();
		int idCheck = db.getUserId(Integer.valueOf(pathSuffix));
		if(!db.doesUserExist(idCheck))
		{
			db.closeConnection();
			response = new Utilities().setFailedResponse(response);
			return;
		}
		GetUserResponse resp = db.getUserInternalAPI(idCheck);
		db.closeConnection();
		response = new Utilities().setOkResponse(response, gson.toJson(resp));
	}
	
	public void postUserCreate(HttpServletRequest request, HttpServletResponse response) throws IOException, JsonSyntaxException
	{
		//Process request by saving to Database, and send correct response back as JSON string
		//check that username isn't null or empty before saving to DB
		LoggerHelper.makeInfoLog("Entered POST /create in user API");
		CreateUserRequest req = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), CreateUserRequest.class);
		if(req == null || req.getUsername() ==  null || req.getUsername().isEmpty() || Utilities.isNumeric(req.getUsername()))
		{
			//N.B: I don't allow purely numeric usernames
			//return 400
			response = new Utilities().setFailedResponse(response);
			return;
		}

		UserDBManager db = new UserDBManager();
		db.createConnection();
		int idCheck = db.getUserId(req.getUsername());
		if(db.doesUserExist(idCheck)) //don't create already existing user
		{
			db.closeConnection();
			response = new Utilities().setFailedResponse(response);
			return;
		}
		int id = db.createUser(req.getUsername());
		db.closeConnection();
		if(id <= 0)
		{
			//return 400
			response = new Utilities().setFailedResponse(response);
			return;
		}
		CreateUserResponse resp = new CreateUserResponse(id);
		response = new Utilities().setOkResponse(response, gson.toJson(resp));
	}
	
	public void postUserAddTickets(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered POST /{userid}/tickets/add in user API");
		String [] split = pathSuffix.substring(pathSuffix.indexOf(Constants.PATH_SEPARATOR) + 1).split(Constants.PATH_SEPARATOR);
		if(split.length == 3 && Utilities.isNumeric(split[0]) && split[1].equals("tickets")  && split[2].equals("add"))
		{
			int userId = Integer.parseInt(split[0]);
			PurchaseUserTicketsReq userReq = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), PurchaseUserTicketsReq.class);
			UserDBManager db = new UserDBManager();
			db.createConnection();
			int idCheck = db.getUserId(userId);
			if(!db.doesUserExist(idCheck)) //don't add tickets for non existent user
			{
				LoggerHelper.makeInfoLog("user does not exist. userid: " + userId + " eventId: " + userReq.getEventid());
				db.closeConnection();
				response = new Utilities().setFailedResponse(response);
				return;
			}
			if(userReq.getEventid() <= 0 || userReq.getTickets() <= 0)
			{
				LoggerHelper.makeInfoLog("Purchase API request for user not properly formed. userid: " + userId + " eventId: " + userReq.getEventid());
				db.closeConnection();
				response = new Utilities().setFailedResponse(response);
				return;
			}
			//call purchase on events api to reduce tickets
			PurchaseEventTicketsReq eventReq = new PurchaseEventTicketsReq(userId, userReq.getEventid(), userReq.getTickets());
			String apiUrl = Utilities.formInternalAPIUrl(eventAPIHostName + ":" + eventAPIPort + Constants.POST_PURCHASE__EVENTS_TICKETS_INTERNAL + "/" + eventReq.getEventid());
			HttpClient client = new HttpClient(apiUrl, Constants.HTTP_POST);
			ServerResponse servResponse = client.makeHttpRequest(gson.toJson(eventReq));
			if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
			{
				LoggerHelper.makeInfoLog("Event API call successful, now saving entries in user database. userid: " + userId + " eventId: " + userReq.getEventid());
				boolean successful = db.addTickets(eventReq);
				if(successful)
				{
					LoggerHelper.makeInfoLog("User database tickets save successful. userid: " + userId + " eventId: " + userReq.getEventid());
					response = new Utilities().setOkResponse(response);	//no response body even in OK responses here, based on requirements	
				}				
				else
				{
					response = new Utilities().setFailedResponse(response);
				}
			}
			else
			{
				//return 400
				LoggerHelper.makeInfoLog("Event API call unsuccessful. userid: " + userId + " eventId: " + userReq.getEventid());
				response = new Utilities().setFailedResponse(response);
			}	
			db.closeConnection();
		}
		else
		{
			//return 400 -- URL isn't well formed
			LoggerHelper.makeInfoLog("Purchase API request for user not properly formed.");
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void postUserTransferTickets(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		String [] split = pathSuffix.substring(pathSuffix.indexOf(Constants.PATH_SEPARATOR) + 1).split(Constants.PATH_SEPARATOR);
		if(split.length == 3 && Utilities.isNumeric(split[0]) && split[1].equals("tickets") && split[2].equals("transfer"))
		{
			int fromUserId = Integer.parseInt(split[0]);
			TransferUserTicketsReq req = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), TransferUserTicketsReq.class);
			if(fromUserId <= 0 || req.getEventid() <= 0 || req.getTickets() <= 0 || req.getTargetuser() <= 0)
			{
				LoggerHelper.makeInfoLog("Transfer API for user not properly formed. userid: " + fromUserId);
				response = new Utilities().setFailedResponse(response);
		        return;
			}
			if(fromUserId == req.getTargetuser())
			{
				LoggerHelper.makeInfoLog("Transfer of ticket to yourself is not allowed. userid: " + fromUserId);
				response = new Utilities().setFailedResponse(response);
		        return;
			}
			UserDBManager db = new UserDBManager();
			db.createConnection();
			boolean updated = db.transferTickets(req, fromUserId);
			if(updated)
			{
				response = new Utilities().setOkResponse(response);
			}
			else
			{
				response = new Utilities().setFailedResponse(response);
			}
			db.closeConnection();
		}
		else
		{
			//return 400 -- URL isn't well formed
			response = new Utilities().setFailedResponse(response);
		}
	}
}
