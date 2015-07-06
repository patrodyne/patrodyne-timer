// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.timer.httptask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TimerTask to call HTTP services periodically.
 * 
 * @author Rick O'Sullivan
 */
public class HttpTask
	extends TimerTask
{
	private static Logger log = LoggerFactory.getLogger(HttpTask.class);
	
	// Represents the task title.
	private String title;
	/**
	 * Get the task title.
	 * @return the task title
	 */
	public String getTitle()
	{
		return title;
	}
	/**
	 * Set the task title.
	 * @param title the task title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	// Represents the HTTP service locator.
	private URL service;
	/**
	 * Get an HTTP service.
	 * @return the HTTP service.
	 */
	public URL getService()
	{
		return service;
	}
	/**
	 * Set a HTTP service.
	 * @param service the HTTP service to set
	 */
	public void setService(URL service)
	{
		this.service = service;
	}

	// Represents the recent response time.
	private Date responseTime;
	/**
	 * Get the recent response time.
	 * @return the recent response time
	 */
	public Date getResponseTime()
	{
		return responseTime;
	}
	/**
	 * Set the recent response time.
	 * @param responseTime the recent response time to set
	 */
	public void setResponseTime(Date responseTime)
	{
		this.responseTime = responseTime;
	}

	// Represents the recent response code.
	private int responseCode = 200;
	/**
	 * Get the recent response code.
	 * @return the recent response code
	 */
	public int getResponseCode()
	{
		return responseCode;
	}
	/**
	 * Set recent response code.
	 * @param responseCode the recent response code to set
	 */
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}

	// Represents the recent response.
	private String response = "";
	/**
	 * Get the recent response.
	 * @return the recent response
	 */
	public String getResponse()
	{
		return response;
	}
	/**
	 * Set the recent response.
	 * @param response the recent response to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}
	
	/**
	 * Construct with a title and HTTP service locator.
	 * 
	 * @param title The task title.
	 * @param url The task locator.
	 */
	public HttpTask(String title, URL service)
	{
		setTitle(title);
		setService(service);
	}

	/* Run the task periodically.
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		try
		{
			setResponseTime(new Date());
			HttpURLConnection http = (HttpURLConnection) getService().openConnection();
			setResponseCode(http.getResponseCode());
			StringBuilder response = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream())))
			{
				String responseLine;
				while ((responseLine = reader.readLine()) != null) 
					response.append(responseLine);
			}
			setResponse(response.toString());
			log.info(getResponseCode()+"|"+getTitle()+"|"+getResponse());
		}
		catch (IOException ex)
		{
			log.error("Failure: ", ex);
		}
	}
}

// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
