package org.pjm2.report.internal;

import java.util.Date;
import java.util.List;

import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.model.ReportLine;

/**
 * TODO: One DAO with different runtime config instead of inheritance hierarchy?
 * 
 * @author liasu
 *
 */
public interface IEntityDao {

	public List<ReportLine> findReportLine(ReportTemplate template, long pid, Date starTime, Date endTime);
	
//	public int countReportData(ReportTemplate template, long pid, Date startTime, Date endTime);

}
