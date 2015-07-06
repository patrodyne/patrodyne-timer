package org.patrodyne.timer.httptask;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

public class RunJetty
{
    // Add Jetty Plus for some extra J2EE-like features: JNDI, etc.
    // All except org.eclipse.jetty.plus.webapp.* are default. 
    // We need to org.eclipse.jetty.plus.webapp.EnvConfiguration
    // to register resource-ref entries and to FIX 
	// 'NameNotFoundException; remaining name env...'.
    private static final String[] PLUS_CONFIG =
        new String[]
        {
         "org.eclipse.jetty.plus.webapp.EnvConfiguration",
         "org.eclipse.jetty.plus.webapp.PlusConfiguration",
         "org.eclipse.jetty.webapp.FragmentConfiguration",
         "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
         "org.eclipse.jetty.webapp.MetaInfConfiguration",
         "org.eclipse.jetty.webapp.WebInfConfiguration",
         "org.eclipse.jetty.webapp.WebXmlConfiguration",
         "org.eclipse.jetty.webapp.TagLibConfiguration"
        };
    
    public static void main(String[] args) throws Exception
	{
        Server server = new Server(8080);
        String[] configLocators = {"file:src/test/jetty/webapps/TimerHttpTask.xml"};  
        for(String configLocator : configLocators)
        {  
        	URL configurationURL = new URL(configLocator);
        	XmlConfiguration configuration = new XmlConfiguration(configurationURL);  
        	WebAppContext wac = (WebAppContext) configuration.configure();  
            wac.setWar(null);
            wac.setResourceBase("src/main/webapp");
            wac.setConfigurationClasses(PLUS_CONFIG);
            server.setHandler(wac);
        }   
        server.start();
        URI locator = server.getURI();
        System.out.println("Locator: "+locator);
		Desktop.getDesktop().browse(locator);
        server.join();
    }
}
