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
import cs601.project4.PurchaseUserTicketsReq;
import cs601.project4.ServerResponse;
import cs601.project4.Utilities;

/**
 * @author DELL
 * Helper Methods used in the FrontEndEventServlet class
 */
public class FeEventServletHelper {

	private Gson gson;
	private String eventAPIHostName;
	private int eventAPIPort;
	private String userAPIHostName;
	private int userAPIPort;
	
	public FeEventServletHelper(String eventAPIHostName, int eventAPIPort, String userAPIHostName, int userAPIPort)
	{
		gson = new Gson();
		this.eventAPIHostName = eventAPIHostName;
		this.eventAPIPort = eventAPIPort;
		this.userAPIHostName = userAPIHostName;
		this.userAPIPort = userAPIPort;
	}
	
	public void getAllEvents(HttpServletResponse response) throws IOException, JsonSyntaxException
	{
		String apiUrl = Utilities.formInternalAPIUrl(eventAPIHostName + ":" + eventAPIPort + Constants.GET_EVENTS_LIST_INTERNAL);
		HttpClient client = new HttpClient(apiUrl, Constants.HTTP_GET);
		ServerResponse eventResponse = client.makeHttpRequest(null);
		if(eventResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
		{
			if(eventResponse.getResponseBody() == null || eventResponse.getResponseBody().isEmpty())
			{					
				response = new Utilities().setFailedResponse(response);
				return;
			}
			GetEventsResponse [] allEvents = gson.fromJson(eventResponse.getResponseBody(), GetEventsResponse[].class);
			List<GetEventsResponse> allEventsList = new ArrayList<GetEventsResponse>(Arrays.asList(allEvents));
			response = new Utilities().setOkResponse(response, gson.toJson(allEventsList));
		}
		else
		{
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void getEventById(HttpServletResponse response, String eventId) throws IOException, JsonSyntaxException
	{
		LoggerHelper.makeInfoLog("Entered GET events/{eventid} in event API");
		String apiUrl = Utilities.formInternalAPIUrl(eventAPIHostName + ":" + eventAPIPort + Constants.PATH_SEPARATOR + eventId);
		HttpClient client = new HttpClient(apiUrl, Constants.HTTP_GET);
		ServerResponse eventResponse = client.makeHttpRequest(null);
		if(eventResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
		{
			if(eventResponse.getResponseBody() == null || eventResponse.getResponseBody().isEmpty())
			{
				response = new Utilities().setFailedResponse(response);
				return;
			}
			GetEventsResponse event = gson.fromJson(eventResponse.getResponseBody(), GetEventsResponse.class);
			response = new Utilities().setOkResponse(response, gson.toJson(event));
		}
		else
		{
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void postEventCreate(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		//Send HttpRequest to internal user creation API
		LoggerHelper.makeInfoLog("Entered POST events/create in front end API");
		String apiUrl = Utilities.formInternalAPIUrl(eventAPIHostName + ":" + eventAPIPort + pathSuffix);
		HttpClient client = new HttpClient(apiUrl, Constants.HTTP_POST);
		ServerResponse servResponse = client.makeHttpRequest(Utilities.getHttpPostBody(request.getInputStream()));
		if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
		{
			//return 200 and JSON object
			response = new Utilities().setOkResponse(response, servResponse.getResponseBody());
		}
		else
		{
			//return 400
			response = new Utilities().setFailedResponse(response);
		}
	}
	
	public void postEventsPurchase(HttpServletRequest request, HttpServletResponse response, String pathSuffix) throws IOException, JsonSyntaxException
	{
		String [] split = pathSuffix.substring(pathSuffix.indexOf(Constants.PATH_SEPARATOR) + 1).split(Constants.PATH_SEPARATOR);
		PurchaseTicketsFrontEndReq req = gson.fromJson(Utilities.getHttpPostBody(request.getInputStream()), PurchaseTicketsFrontEndReq.class);
		if(split.length == 3 && Utilities.isNumeric(split[0]) && split[1].equals("purchase") && Utilities.isNumeric(split[2]) && req.getTickets() > 0)
		{
			LoggerHelper.makeInfoLog("Entered POST events/{eventid}/purchase/{userid} in front end API");			
			String userId = split[2];
			int eventid = Integer.parseInt(split[0]);
			PurchaseUserTicketsReq userReq = new PurchaseUserTicketsReq(eventid, req.getTickets());
			String apiUrl = Utilities.formInternalAPIUrl(userAPIHostName + ":" + userAPIPort + "/" + userId + Constants.POST_PURCHASE_USER_TICKETS_INTERNAL);
			HttpClient client = new HttpClient(apiUrl, Constants.HTTP_POST);
			ServerResponse servResponse = client.makeHttpRequest(gson.toJson(userReq));
			if(servResponse.getResponseCode() == Constants.HTTP_OK_RESPONSE)
			{
				//return 200 and JSON object
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
			//return 400 -- URL isn't well formed
			response = new Utilities().setFailedResponse(response);
		}
	}
	
}
