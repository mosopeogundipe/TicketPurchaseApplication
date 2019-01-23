/**
 * 
 */
package cs601.project4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletResponse;

/**
 * @author DELL
 *
 */
public class Utilities {

	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    int d = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static String getHttpPostBody(InputStream stream)
	{
		StringBuffer response = new StringBuffer();
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					stream));
			String inputLine;			

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			LoggerHelper.makeInfoLog("Http Post Body: " + response.toString());
		}
		catch(IOException iex)
		{
			LoggerHelper.makeSevereLog("Error in getHttpPostBody: " + iex.getMessage());
		}
		return response.toString();
	}
	
	public HttpServletResponse setFailedResponse(HttpServletResponse response) throws IOException
	{
		response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return response;
	}
	
	public HttpServletResponse setOkResponse(HttpServletResponse response, String message) throws IOException
	{
		response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(message);
        return response;
	}
	
	public HttpServletResponse setOkResponse(HttpServletResponse response) throws IOException
	{
		response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        return response;
	}
	
	public static String formInternalAPIUrl(String urlSuffix)
	{
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(Constants.URL_PREFIX);
		urlBuilder.append(urlSuffix);
		return urlBuilder.toString();
	}
}
