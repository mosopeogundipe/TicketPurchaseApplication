/**
 * 
 */
package cs601.project4.FrontEndAPI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cs601.project4.*;
import cs601.project4.UserAPI.GetUserResponse.EventsResponse;

/**
 * @author DELL
 *
 */
public class FrontEndUserServlet extends HttpServlet {
	
	private Gson gson;	
	private String userAPIHostName = ConfigurationHelper.getConfiguration().getUserAPIHostName();
	private int userAPIPort = ConfigurationHelper.getConfiguration().getUserAPIPort();
	private String eventAPIHostName = ConfigurationHelper.getConfiguration().getEventAPIHostName();
	private int eventAPIPort = ConfigurationHelper.getConfiguration().getEventAPIPort();
	private FeUserServletHelper helper = null;
	
	public FrontEndUserServlet ()
	{
		gson = new Gson();	
		helper = new FeUserServletHelper(userAPIHostName, userAPIPort, eventAPIHostName, eventAPIPort);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try
		{
			//GET /users/*
			String pathSuffix = request.getPathInfo();
			String userId = (pathSuffix == null) ? null : pathSuffix.replaceFirst(Constants.PATH_SEPARATOR, "");
			if(pathSuffix == null)
			{
				//return 400 here. Any POST request with no path suffix is invalid according to API spec
				response = new Utilities().setFailedResponse(response);
			}
			else if(Utilities.isNumeric(userId))	//contains an number, so it's fine for this stage
			{
				//Send HttpRequest to internal user creation API
				helper.getUserById(response, pathSuffix);
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
			LoggerHelper.makeSevereLog("Error in Front End User API do Get: " + iex.getMessage());
			iex.printStackTrace();
		}
		catch(JsonSyntaxException jex)
		{
			LoggerHelper.makeSevereLog("Json Syntax Exception in Front End User API do Get: " + jex.getMessage());
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
			//POST /users/*
			String pathSuffix = request.getPathInfo();
			if(pathSuffix == null)
			{
				//return 400 here. Any POST request with no path suffix is invalid according to API spec
				response = new Utilities().setFailedResponse(response);
			}
			else if(pathSuffix.equals(Constants.POST_CREATE))	// -----> POST /create
			{
				//Send HttpRequest to internal user creation API
				helper.postUserCreate(request, response, pathSuffix);
			}
			else if(pathSuffix.endsWith("/transfer"))
			{
				helper.postUserTransferTicket(request, response, pathSuffix);
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
			LoggerHelper.makeSevereLog("Error in Front End User API do Post: " + iex.getMessage());
			iex.printStackTrace();
		}
		catch(JsonSyntaxException jex)
		{
			LoggerHelper.makeSevereLog("Json Syntax Exception in Front End User API do Post: " + jex.getMessage());
			try {
				response = new Utilities().setFailedResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	
}
