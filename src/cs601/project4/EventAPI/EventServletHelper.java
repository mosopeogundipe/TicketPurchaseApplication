/**
 * 
 */
package cs601.project4.EventAPI;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cs601.project4.ConfigurationHelper;
import cs601.project4.Constants;
import cs601.project4.CreateEventRequest;
import cs601.project4.CreateEventResponse;
import cs601.project4.EventDBManager;
import cs601.project4.GetEventsResponse;
import cs601.project4.HttpClient;
import cs601.project4.LoggerHelper;
import cs601.project4.PurchaseEventTicketsReq;
import cs601.project4.PurchaseUserTicketsReq;
import cs601.project4.ServerResponse;
import cs601.project4.Utilities;

/**
 * @author DELL
 *
 */
public class EventServletHelper {

	private Gson gson;
	private String userAPIHostName;
	private int userAPIPort;
	
	public EventServletHelper(String userAPIHostName, int userAPIPort)
	{
		gson = new Gson();
		this.userAPIHostName = userAPIHostName;
		this.userAPIPort = userAPIPort;
	}
	
	public void getAllEvents(HttpServletResponse response) throws IOException, JsonSyntaxException
	{
		//Send HttpRequest to internal user creation API
		LoggerHelper.makeInfoLog("Entered GET events/list in event API");
		EventDBManager db = new EventDBManager();
		db.createConnection();
		List<GetEventsResponse> resp = db.getEventsList();
		db.closeConnection();
		if(resp.size() == 0)
		{
			LoggerHelper.makeInfoLog("No events found");
			response = new Utilities().setFailedResponse(response);
		}
		else
		{
			response = new Utilities().setOkResponse(response, gson.toJson(resp));
		}			
	}
	
	public void getEventById(HttpServletResponse response, String eventId) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered GET events/{eventid} in event API");
		EventDBManager db = new EventDBManager();
		db.createConnection();
		int id = Integer.valueOf(eventId);
		GetEventsResponse resp = db.getEventById(id);
		db.closeConnection();
		if(resp == null)
		{
			LoggerHelper.makeInfoLog("No events found with id: " + id);
			response = new Utilities().setFailedResponse(response);
		}
		else
		{
			response = new Utilities().setOkResponse(response, gson.toJson(resp));
		}
	}
	
	public void postEventCreate(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		//Process request by saving to Database, and send correct response back as JSON string
		//check that username isn't null or empty before saving to DB
		LoggerHelper.makeInfoLog("Entered POST /create in event API");
		CreateEventRequest req = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), CreateEventRequest.class);
		if(req == null || req.getEventname() ==  null || req.getEventname().isEmpty() || req.getNumtickets() <= 0 || req.getNumtickets() > Integer.MAX_VALUE || req.getUserid() <= 0)
		{
			//return 400
			response = new Utilities().setFailedResponse(response);
	        return;
		}
		
		//NOTE: I check if user exists in calling User API DB GET /{userid} before saving the request here.
		String apiUrl = Utilities.formInternalAPIUrl(userAPIHostName + ":" + userAPIPort + Constants.PATH_SEPARATOR + req.getUserid());
		HttpClient client = new HttpClient(apiUrl, Constants.HTTP_GET);
		ServerResponse servResponse = client.makeHttpRequest(null);
		if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
		{
			cs601.project4.UserAPI.GetUserResponse userAPIResponse = gson.fromJson(servResponse.getResponseBody(), cs601.project4.UserAPI.GetUserResponse.class);
			if(userAPIResponse.getUserid() > 0)			//means user exists in DB
			{
				EventDBManager db = new EventDBManager();
				db.createConnection();
				int idCheck = db.getEventId(req);
				if(db.doesEventExist(idCheck)) //don't create already existing user
				{
					db.closeConnection();
					response = new Utilities().setFailedResponse(response);
			        return;
				}
				int id = db.createEvent(req);
				db.closeConnection();
				if(id <= 0)
				{
					response = new Utilities().setFailedResponse(response);
			        return;
				}
				CreateEventResponse resp = new CreateEventResponse(id);
				response = new Utilities().setOkResponse(response, gson.toJson(resp));
			}
			else
			{
				response = new Utilities().setFailedResponse(response);
			}
			
		}
		else
		{
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void postEventsPurchase(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		String [] split = pathSuffix.substring(pathSuffix.indexOf(Constants.PATH_SEPARATOR) + 1).split(Constants.PATH_SEPARATOR);
		if(split.length == 2 && split[0].equals("purchase") && Utilities.isNumeric(split[1]))
		{
			int eventId = Integer.parseInt(split[1]);
			PurchaseEventTicketsReq eventReq = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), PurchaseEventTicketsReq.class);
			if(eventReq.getEventid() <= 0 || eventReq.getTickets() <= 0 || eventReq.getUserid() <= 0 || eventReq.getEventid() != eventId)
			{
				LoggerHelper.makeInfoLog("Improperly formed API request for purchase with eventId: " + eventId);
				response = new Utilities().setFailedResponse(response);
		        return;
			}
			EventDBManager db = new EventDBManager();
			db.createConnection();
			boolean updated = db.purchaseTickets(db, eventReq.getTickets(), eventReq.getEventid());
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
