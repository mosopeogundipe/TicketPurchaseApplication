/**
 * 
 */
package cs601.project4.FrontEndAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cs601.project4.ConfigurationHelper;
import cs601.project4.Constants;
import cs601.project4.GetEventsResponse;
import cs601.project4.HttpClient;
import cs601.project4.LoggerHelper;
import cs601.project4.PurchaseTicketsFrontEndReq;
import cs601.project4.ServerResponse;
import cs601.project4.TransferUserTicketsReq;
import cs601.project4.Utilities;
import cs601.project4.UserAPI.GetUserResponse.EventsResponse;

/**
 * @author DELL
 *
 */
public class FeUserServletHelper {
	private Gson gson;	
	private String userAPIHostName;
	private int userAPIPort;
	private String eventAPIHostName;
	private int eventAPIPort;
	
	public FeUserServletHelper(String userAPIHostName, int userAPIPort, String eventAPIHostName, int eventAPIPort)
	{
		gson = new Gson();
		this.userAPIHostName = userAPIHostName;
		this.userAPIPort = userAPIPort;
		this.eventAPIHostName = eventAPIHostName;
		this.eventAPIPort = eventAPIPort;
	}
	
	public void getUserById(HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered GET users/{userid} in front end API");
		String apiUrl = Utilities.formInternalAPIUrl(userAPIHostName + ":" + userAPIPort + pathSuffix);
		HttpClient client = new HttpClient(apiUrl, Constants.HTTP_GET);
		ServerResponse servResponse = client.makeHttpRequest(null);
		if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
		{
			//return 200 and JSON string
			cs601.project4.UserAPI.GetUserResponse userAPIResponse = gson.fromJson(servResponse.getResponseBody(), cs601.project4.UserAPI.GetUserResponse.class);
			GetUserResponse frontEndResponse = new GetUserResponse(userAPIResponse.getUserid(), userAPIResponse.getUsername(), new ArrayList<GetEventsResponse>());
			apiUrl = Utilities.formInternalAPIUrl(eventAPIHostName + ":" + eventAPIPort + Constants.GET_EVENTS_LIST_INTERNAL);
			client = new HttpClient(apiUrl, Constants.HTTP_GET);
			ServerResponse eventResponse = client.makeHttpRequest(null);
			if(eventResponse.getResponseBody() == null || eventResponse.getResponseBody().isEmpty())
			{
				response = new Utilities().setOkResponse(response, gson.toJson(frontEndResponse));
				return;
			}
			GetEventsResponse [] allEvents = gson.fromJson(eventResponse.getResponseBody(), GetEventsResponse[].class);
			List<GetEventsResponse> allEventsList = new ArrayList<GetEventsResponse>(Arrays.asList(allEvents));
			//HINT: Optimize this bit of code later
			for(EventsResponse eventRep: userAPIResponse.getTickets())
			{
				int id = eventRep.getEventid();
				for(GetEventsResponse event: allEventsList)
				{
					if(id == event.getEventid())
					{
						frontEndResponse.addEvent(event);
					}
				}
			}
			response = new Utilities().setOkResponse(response, gson.toJson(frontEndResponse));
	        return;
		}
		else
		{
			//return 400
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void postUserCreate(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered POST users/create in front end API");
		String apiUrl = Utilities.formInternalAPIUrl(userAPIHostName + ":" + userAPIPort + pathSuffix);
		HttpClient client = new HttpClient(apiUrl, Constants.HTTP_POST);
		ServerResponse servResponse = client.makeHttpRequest(Utilities.getHttpPostBody(request.getInputStream()));
		if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
		{
			//return 200 and JSON string
			response = new Utilities().setOkResponse(response, servResponse.getResponseBody());
		}
		else
		{
			//return 400
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void postUserTransferTicket(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered POST users/{userid}/tickets/transfer in front end API");
		String [] split = pathSuffix.substring(pathSuffix.indexOf(Constants.PATH_SEPARATOR) + 1).split(Constants.PATH_SEPARATOR);
		TransferUserTicketsReq req = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), TransferUserTicketsReq.class);
		if(split.length == 3 && Utilities.isNumeric(split[0]) && split[1].equals("tickets") && split[2].equals("transfer") && req.getTickets() > 0)
		{
			int userId = Integer.parseInt(split[0]);
			if(userId > 0)
			{
				String apiUrl = Utilities.formInternalAPIUrl(userAPIHostName + ":" + userAPIPort + "/" + userId + Constants.POST_TRANSFER__USER_TICKETS_INTERNAL);
				HttpClient client = new HttpClient(apiUrl, Constants.HTTP_POST);
				ServerResponse servResponse = client.makeHttpRequest(gson.toJson(req));
				if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
				{
					//return 200 and JSON object
					LoggerHelper.makeInfoLog("Tickets Transferred Successfully from userid: " + userId + " to userId: " + req.getTargetuser());
					response = new Utilities().setOkResponse(response);	//no response body even in OK responses here, based on requirements
				}
				else
				{
					//return 400
					response = new Utilities().setFailedResponse(response);
				}
			}
			else
			{
				response = new Utilities().setFailedResponse(response);
			}
		}
		else
		{
			//return 400 -- URL isn't well formed
			response = new Utilities().setFailedResponse(response);
		}
	}
}
