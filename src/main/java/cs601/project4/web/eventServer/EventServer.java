package cs601.project4.web.eventServer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import cs601.project4.dao.dbtools.DbHelper;
import cs601.project4.tools.PropertyReader;

/**
 * User Service - The user service will manage the user account information, 
 * including the events for which a user has purchased tickets. 
 * The API will support the following operations:
 * @author yangzun
 */
public class EventServer {
	private static PropertyReader reader = new PropertyReader("./config","eventServer.properties");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int PORT = reader.readIntValue("eventPort");
		//Example from http://www.eclipse.org/jetty/documentation/current/embedding-jetty.html
		
		// Create a basic jetty server object that will listen on port 8080.
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(PORT);
 
        // The ServletHandler is a dead simple way to create a context handler
        // that is backed by an instance of a Servlet.
        // This handler then needs to be registered with the Server object.
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        
        // Passing in the class for the Servlet allows jetty to instantiate an
        // instance of that Servlet and mount it on a given context path.
 
        // IMPORTANT:
        // This is a raw Servlet, not a Servlet that has been configured
        // through a web.xml @WebServlet annotation, or anything similar.
        handler.addServletWithMapping(EventServlet.class, "/*");
 
        // Start things up!
        server.start();
        System.out.println("listenning on port: "+PORT);
        DbHelper.init(reader);
        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See
        // http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        server.join();

		
	}

}
