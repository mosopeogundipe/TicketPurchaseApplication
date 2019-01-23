package cs601.project4.FrontEndAPI;

import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import com.google.gson.Gson;

import cs601.project4.ConfigurationHelper;
import cs601.project4.GetEventsResponse;
import cs601.project4.LoggerHelper;

public class FrontEndServer {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		new LoggerHelper();			//Logger can be accessed statically in program after this instance is created
		LoggerHelper.makeInfoLog("Reading from Config file...");
		new ConfigurationHelper("Config.json");
		LoggerHelper.makeInfoLog("Starting Front End API Server...");
		Server server = new Server(ConfigurationHelper.getConfiguration().getFrontEndAPIPort());
		ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(FrontEndUserServlet.class, "/users/*");
        handler.addServletWithMapping(FrontEndEventServlet.class, "/events/*");
        server.start();
        server.join();
	}

}
