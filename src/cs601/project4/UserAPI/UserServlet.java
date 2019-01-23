/**
 * 
 */
package cs601.project4.UserAPI;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import cs601.project4.ConfigurationHelper;
import cs601.project4.Constants;
import cs601.project4.CreateUserRequest;
import cs601.project4.CreateUserResponse;
import cs601.project4.HttpClient;
import cs601.project4.LoggerHelper;
import cs601.project4.ServerResponse;
import cs601.project4.UserDBManager;
import cs601.project4.Utilities;

/**
 * @author DELL
 *
 */
public class UserServlet extends HttpServlet{

	private Gson gson = new Gson();
	private UserServletHelper helper = null;
	private String eventAPIHostName = ConfigurationHelper.getConfiguration().getEventAPIHostName();
	private int eventAPIPort = ConfigurationHelper.getConfiguration().getEventAPIPort();
	
	public UserServlet()
	{
		helper = new UserServletHelper(eventAPIHostName, eventAPIPort);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try
		{
			//GET /users/*
			String pathSuffix = request.getPathInfo();
			pathSuffix = pathSuffix.replaceFirst(Constants.PATH_SEPARATOR, "");
			if(pathSuffix == null || pathSuffix.isEmpty())
			{
				//return 400 here. Any POST request with no path suffix is invalid according to API spec
				response = new Utilities().setFailedResponse(response);
			}
			else if(Utilities.isNumeric(pathSuffix))	//contains an number, so it's fine for this stage
			{
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
			LoggerHelper.makeSevereLog("Error in User API do Get: " + iex.getMessage());
			iex.printStackTrace();
		}
		catch(JsonSyntaxException jex)
		{
			LoggerHelper.makeSevereLog("Json Syntax Exception in User API do Get: " + jex.getMessage());
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
			else if(pathSuffix.equals(Constants.POST_CREATE)) // -----> POST /create
			{
				helper.postUserCreate(request, response);
			}
			else if(pathSuffix.endsWith("/add"))  // ----> POST /{userid}/tickets/add
			{
				helper.postUserAddTickets(request, response, pathSuffix);
			}
			else if(pathSuffix.endsWith("/transfer"))  // ----> POST /{userid}/tickets/transfer
			{
				helper.postUserTransferTickets(request, response, pathSuffix);
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
			LoggerHelper.makeSevereLog("Error in User API do Post: " + iex.getMessage());
			iex.printStackTrace();
		}
		catch(JsonSyntaxException jex)
		{
			LoggerHelper.makeSevereLog("Json Syntax Exception in User API do Post: " + jex.getMessage());
			try {
				response = new Utilities().setFailedResponse(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
