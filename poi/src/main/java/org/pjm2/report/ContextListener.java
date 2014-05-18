/**
 * 
 */
package org.pjm2.report;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author liasu
 * 
 */
public class ContextListener implements ServletContextListener {

	private final ReportGenerator generator = new ReportGenerator();

	public void contextInitialized(ServletContextEvent sce) {
		generator.startLoop();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		generator.startLoop();
	}

}
