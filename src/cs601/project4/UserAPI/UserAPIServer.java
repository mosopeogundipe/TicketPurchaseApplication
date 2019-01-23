/**
 * 
 */
package cs601.project4.UserAPI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import cs601.project4.ConfigurationHelper;
import cs601.project4.LoggerHelper;
import cs601.project4.FrontEndAPI.FrontEndEventServlet;
import cs601.project4.FrontEndAPI.FrontEndUserServlet;

/**
 * @author DELL
 *
 */
public class UserAPIServer {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new LoggerHelper();			//Logger can be accessed statically in program after this instance is created
		LoggerHelper.makeInfoLog("Reading from Config file...");
		new ConfigurationHelper("Config.json");
		LoggerHelper.makeInfoLog("Starting User API Server...");
		Server server = new Server(ConfigurationHelper.getConfiguration().getUserAPIPort());
		ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(UserServlet.class, "/*");
        server.start();
        server.join();
	}

}
