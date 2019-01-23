/**
 * 
 */
package cs601.project4;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author mosopeogundipe
 * Stores Objects from the Configuration file.
 * Only needs to be instantiated once, after which the getConfiguration() method should be used to retrieve the configuration objects
 */
public class ConfigurationHelper {

	private static Configurations configuration;
	
	public static Configurations getConfiguration() {
		return configuration;
	}
	
	public ConfigurationHelper(String fileName)
	{
		initializeConfiguration(fileName);
	}
	
	public void initializeConfiguration(String fileName)
	{
		Charset charset = Charset.forName("ISO-8859-1");
		Path path = Paths.get (fileName);
		Gson gson = new Gson();
		try (BufferedReader reader = Files.newBufferedReader (path, charset))
		{	
			String line = null;
			while((line = reader.readLine()) != null)
			{
				try	{
					configuration = gson.fromJson(line, Configurations.class);
				}
				catch(JsonSyntaxException ex) {
					LoggerHelper.makeWarningLog("Skipping line..." + line);
					continue;
				}
			}
		}
		catch(IOException ex)
		{
			LoggerHelper.makeSevereLog(String.format("Config File '%s' could not be read. File doesn't exist in project directory", fileName,ex.getMessage()));
		}
	}
}
