/**
 * 
 */
package cs601.project4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Http Client Created to Mimic requests from an actual Http client (browser, postman, command line etc)
 * @author mosopeogundipe
 *
 */
public class HttpClient {
	private URL url;
	private HttpURLConnection connection;
	private String requestMethod;
	public HttpClient(String url, String httpRequestMethod)
	{
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			LoggerHelper.makeWarningLog("Error in forming url: " + e.getMessage());
		}
		this.requestMethod = httpRequestMethod;
	}

	/**
	 * Sends the http GET or POST request, and returns the response.
	 * If Request is a GET request, send in a null or empty string in request message
	 * @return
	 */
	public ServerResponse makeHttpRequest(String requestMessage)
	{
		ServerResponse response = null;
		try {
			if(requestMethod.equals(Constants.HTTP_GET))
				sendGETRequest();
			else if(requestMethod.equals(Constants.HTTP_POST))
				sendPOSTRequest(requestMessage);
			response = getResponse();
		} catch (IOException e) {
			LoggerHelper.makeWarningLog("Error in HTTP Request: " + e.getMessage());
		}
		return response;
	}

	private HttpURLConnection sendGETRequest() throws IOException
	{
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");			
		connection.connect();	
		return connection;
	}

	private HttpURLConnection sendPOSTRequest(String message) throws IOException
	{
		byte[] out = message.getBytes();
		int length = out.length;		
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("POST");		
		connection.setFixedLengthStreamingMode(length);
		connection.setDoOutput(true);

		connection.connect();

		OutputStream os = connection.getOutputStream();
		os.write(out);
		os.flush();
		os.close();
		return connection;
	}

	private ServerResponse getResponse() throws IOException
	{
		ServerResponse retVal = null;
		int responseCode = connection.getResponseCode();
		StringBuffer response = new StringBuffer();
		LoggerHelper.makeInfoLog("Server Response Code :: " + responseCode);

		if(responseCode == Constants.HTTP_OK_RESPONSE)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;			

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			LoggerHelper.makeInfoLog("Server Response:: " + response);
			retVal = new ServerResponse(responseCode, response.toString());
		}
		else
		{
			LoggerHelper.makeInfoLog("It's a 400 response, so response body can't be read from input stream");
			retVal = new ServerResponse(responseCode, response.toString());
		}
		//connection.disconnect();		
		return retVal;
	}
	
}
