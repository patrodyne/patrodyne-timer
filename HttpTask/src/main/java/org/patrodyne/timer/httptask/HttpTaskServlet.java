package org.patrodyne.timer.httptask;
 
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet to initiate Timer managed tasks and invoke HTTP endpoints
 * on a periodic schedule. Tasks are configured from properties provided
 * at the JNDI path, java:comp/env/timer/httptask. Each JDNI property
 * provides a key (job name) and value TYPE|DELAY|PERIOD|URL where
 * 
 *   TYPE = FD (fixed delay) or FR (fixed rate)
 *   DELAY = initial offset in milliseconds
 *   PERIOD = time between invocations in milliseconds
 *   URL = an HTTP endpoint.
 *
 * @author Rick O'Sullivan
 */
public class HttpTaskServlet extends HttpServlet 
{
	private static final long serialVersionUID = 20150701L;

	// Represents the job schedules.
	private Properties jobs;
	/**
	 * Get the task properties.
	 * @return the jobs properties.
	 * @throws NamingException 
	 */
	public Properties getJobs() throws NamingException
	{
		if ( jobs == null )
		{
            InitialContext ic = new InitialContext();
            jobs = (Properties) ic.lookup("java:comp/env/timer/httptask");
		}
		return jobs;
	}
	/**
	 * Set the task properties.
	 * @param jobs the jobs to set
	 */
	public void setJobs(Properties jobs)
	{
		this.jobs = jobs;
	}

	// Represents a facility to schedule jobs.
	private Timer timer;
	/** 
	 * Get a facility to schedule jobs.
	 * @return The timer facility.
	 */
	public Timer getTimer()
	{
		if ( timer == null )
			timer = new Timer();
		return timer;
	}

	// Represents the list of HTTP tasks.
	private List<HttpTask> tasks;
	/**
	 * Get the list of HTTP tasks.
	 * @return the HTTP tasks
	 */
	public List<HttpTask> getTasks()
	{
		if ( tasks == null )
			setTasks(new ArrayList<HttpTask>());
		return tasks;
	}
	/**
	 * Set the list of HTTP tasks.
	 * @param tasks the HTTP tasks to set
	 */
	public void setTasks(List<HttpTask> tasks)
	{
		this.tasks = tasks;
	}
	
	// Represents the next task taskIndex, 
	private int taskIndex = -1;
	/** Next circular task taskIndex. */
	public int nextTaskIndex()
	{
		if ( !getTasks().isEmpty() )
			taskIndex = ( ++taskIndex % getTasks().size() );
		return taskIndex;
	}
	
    /**
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being placed into service.  See {@link Servlet#init}.
     * 
     * This implementation schedules jobs configured in JNDI.
     * 
     * @param config the configuration information for this servlet.
     */
	public void init(ServletConfig config) throws ServletException
	{
		try
		{
			for ( Entry<Object, Object> job : getJobs().entrySet() )
			{
				String title = (String) job.getKey();
				String value = (String) job.getValue();
				String[] parms = value.split("\\|");
				if ( parms.length == 4)
				{
					String method = parms[0];
					int delay = Integer.parseInt(parms[1]);
					int period = Integer.parseInt(parms[2]);
					URL url = new URL(parms[3]);
					
					HttpTask httpTask = new HttpTask(title, url); 
					getTasks().add(httpTask);
					
					switch (method)
					{
						case "FD":
							getTimer().schedule(httpTask, delay, period);
							break;
						case "FR":
							getTimer().scheduleAtFixedRate(httpTask, delay, period);
							break;
					}
				}
			}
		}
		catch ( NamingException nex )
		{
			throw new ServletException("Cannot resolve JNDI path.", nex);
		}
		catch (MalformedURLException mue)
		{
			throw new ServletException("Cannot parse job locator.", mue);
		}
		super.init();
	}

	/**
	 * Called by the server (via the <code>service</code> method) to
     * allow a servlet to handle a GET request. 
	 */
	@Override 
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws IOException
	{
		HttpTask httpTask = getTasks().get(nextTaskIndex());
		response.setContentType("text/html");
		response.setStatus(httpTask.getResponseCode());
		response.getOutputStream().print(render(httpTask));
	}
	
	/**
	 * Render a HttpTask for presentation.
	 * @param httpTask The HTTP task to be rendered.
	 * @return A presentation of the HTTP task.
	 */
	private String render(HttpTask task)
	{
		StringBuilder page = new StringBuilder();
		page.append("<!DOCTYPE html>\n");
		page.append("<html lang=\"en\">\n");
		page.append("<head>\n");
		page.append("  <meta charset=\"UTF-8\">");
		page.append("  <title>"+task.getTitle()+"</title>\n");
		page.append("</head>\n");
		page.append("<body>\n");
		page.append("  <pre>"+escape(task.getResponse())+"</pre>\n");
		page.append("</body>\n");
		page.append("</html>\n");
		return page.toString();
	}

	/**
	 * Replace special characters with HTTP escape codes.
	 * @param text Text to be escaped.
	 * @return Text after replacement.
	 */
	private String escape(String text)
	{
		if ( text != null )
			return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		else
			return text;
	}

    /**
     * Called by the servlet container to indicate to a servlet that the
     * servlet is being taken out of service. Cancels and purges tasks
     * managed by the Timer.
     */
	public void destroy()
	{
		getTimer().cancel();
		getTimer().purge();
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
