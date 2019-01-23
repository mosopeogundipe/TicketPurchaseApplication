/**
 * 
 */
package cs601.project4.EventAPI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
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
import cs601.project4.ServerResponse;
import cs601.project4.UserDBManager;
import cs601.project4.Utilities;
import cs601.project4.UserAPI.GetUserResponse;

/**
 * @author DELL
 *
 */
public class EventServlet extends HttpServlet{

	private Gson gson = new Gson();
	private String userAPIHostName = ConfigurationHelper.getConfiguration().getUserAPIHostName();
	private int userAPIPort = ConfigurationHelper.getConfiguration().getUserAPIPort();
	private EventServletHelper helper = null;
	
	public EventServlet()
	{
		helper = new EventServletHelper(userAPIHostName, userAPIPort);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try
		{
			//GET /events/*
			String pathSuffix = request.getPathInfo();
			String eventId = pathSuffix.replaceFirst(Constants.PATH_SEPARATOR, "");
			if(pathSuffix == null || pathSuffix.isEmpty())
			{
				//return 400 here. Any POST request with no path suffix is invalid according to API spec
				response = new Utilities().setFailedResponse(response);
			}
			else if(pathSuffix.equals(Constants.GET_EVENTS_LIST_INTERNAL)) // GET /list api in Events Service
			{
				helper.getAllEvents(response);
			}
			else if(Utilities.isNumeric(eventId))
			{
				helper.getEventById(response, eventId);
			}
			else
			{
				response = new Utilities().setFailedResponse(response);
			}
		}
		catch(IOException iex)
		{
			try {
				response = new Utilities().setFailedResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LoggerHelper.makeSevereLog("Error in Events API do Get: " + iex.getMessage());
			iex.printStackTrace();
		}
		catch(JsonSyntaxException jex)
		{
			LoggerHelper.makeSevereLog("Json Syntax Exception in Events API do Get: " + jex.getMessage());
			try {
				response = new Utilities().setFailedResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try
		{
			//POST /events/*
			String pathSuffix = request.getPathInfo();
			if(pathSuffix == null)
			{
				//return 400 here. Any POST request with no path suffix is invalid according to API spec
				response = new Utilities().setFailedResponse(response);
			}
			else if(pathSuffix.equals(Constants.POST_CREATE)) // -----> POST /create
			{
				helper.postEventCreate(request, response, pathSuffix);
			}
			else if(pathSuffix.contains("/purchase"))
			{
				helper.postEventsPurchase(request, response, pathSuffix);
			}
			else
			{
				//return 400 here -- invalid url
				response = new Utilities().setFailedResponse(response);
			}
		}
		catch(IOException iex)
		{
			try {
				response = new Utilities().setFailedResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LoggerHelper.makeSevereLog("Error in Events API do Post: " + iex.getMessage());
			iex.printStackTrace();
		}
		catch(JsonSyntaxException jex)
		{
			LoggerHelper.makeSevereLog("Json Syntax Exception in Events API do Post: " + jex.getMessage());
			try {
				response = new Utilities().setFailedResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
