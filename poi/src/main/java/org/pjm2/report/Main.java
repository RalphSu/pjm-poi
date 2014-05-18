/**
 * 
 */
package org.pjm2.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liasu
 * 
 */
public class Main {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		ReportGenerator generator = new ReportGenerator();
		generator.startLoop();
	}
}
