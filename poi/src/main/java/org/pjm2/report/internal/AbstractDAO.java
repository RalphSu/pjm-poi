package org.pjm2.report.internal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.jpa.QueryHints;
import org.pjm2.report.Dao;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.model.ReportLine;

public abstract class AbstractDAO  implements IEntityDao {

	public AbstractDAO(EntityManager manager) {
		this.manager = manager;
	}

	private EntityManager manager;
	
	public List<ReportLine> findReportLine(ReportTemplate template, long pid, Date starTime, Date endTime) {
		// fill template column names
		fillTemplateColumName(template);
		
		// check if has 日期 field
		long dateClassifiedId = -1; 
		String checkDateSql = getDateFieldSql();
		Query checkQuery = manager.createNativeQuery(String.format(checkDateSql, template.getClassified()));
		checkQuery.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
		if (!checkQuery.getResultList().isEmpty()) {
			dateClassifiedId = ((Number)checkQuery.getResultList().get(0)).longValue();
		}

		// find releases id
		List<?> ids;
		if (dateClassifiedId >= 0) {
			String start = AbstractDAO.parseDateValue(starTime);
			Date shift1DayLater = shiftOneDayTime(endTime);
			String end = AbstractDAO.parseDateValue(shift1DayLater);
			StringBuilder sql = new StringBuilder(getItemByDateSql());
			if(template.getTemplate_type().equals(Dao.WEIXIN_TEMPLATE_TYPE)||
					template.getTemplate_type().equals(Dao.SUMMARY_TEMPLATE_TYPE)){
				sql.append(" and n.projects_id = ").append(pid);
			}else{
				sql.append(" and n.project_id = ").append(pid);
			}
			
			if (start != null) {
				sql.append(" and f.body >= '").append(start).append("'");
			}
			if (end != null) {
				sql.append(" and f.body < '").append(end).append("'");
			}
			Query query = manager.createNativeQuery(String.format(sql.toString(), dateClassifiedId, template.getClassified()));
			ids = query.getResultList();
		} else {
			StringBuilder sql = new StringBuilder(getItemByClassifieldSql());
			if(template.getTemplate_type().equals(Dao.WEIXIN_TEMPLATE_TYPE)||
					template.getTemplate_type().equals(Dao.SUMMARY_TEMPLATE_TYPE)){
				sql.append(" and n.projects_id = ").append(pid);
			}else{
				sql.append(" and n.project_id = ").append(pid);
			}
			
			Query query = manager.createNativeQuery(String.format(sql.toString(), template.getClassified()));
			ids = query.getResultList();
		}

		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		
		// now select report column
		StringBuilder sb = new StringBuilder(getFiledsSql());
		Query query = manager.createNativeQuery(String.format(sb.toString(), StringUtils.join(ids, ',')));
		List<?> result = query.getResultList();
		return AbstractDAO.convertToLine(result);
	}


	private Date shiftOneDayTime(Date endTime) {
		// simply construct a new date which is a whole later than the given
		return new Date(endTime.getTime() + 24l * 3600l * 1000l);
	}

	private void fillTemplateColumName(ReportTemplate template) {
		String sql = getColumnNameSql();
		Query query = manager.createNativeQuery(String.format(sql, template.getClassified()));
		query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
		List<?> columns = query.getResultList();
		List<String> column_names = new ArrayList<String>();
		for (Object o : columns) {
			column_names.add(o.toString());
		}
		template.setColumnHeaders(column_names);
	}


	abstract protected String getFiledsSql();

	abstract protected String getItemByClassifieldSql();

	abstract protected String getItemByDateSql();

	abstract protected String getDateFieldSql();
	
	abstract protected String getColumnNameSql();


	public static String parseDateValue(Date d) {
		if (d == null) {
			return null;
		}
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
	}

	public static List<ReportLine> convertToLine(List<?> sqlResult) {
		if (sqlResult == null || sqlResult.isEmpty()) {
			return Collections.emptyList();
		}
		Map<Long, ReportLine> lines = new HashMap<Long, ReportLine>();
		for (Object l : sqlResult) {
			Object[] line = (Object[]) l;
			Long id = ((Number) line[0]).longValue();
			ReportLine reportLine = null;
			if (lines.containsKey(id)) {
				reportLine = lines.get(id);
			} else {
				// add one line
				String date = "";
				if (line[1] != null) {
					date = line[1].toString();
				}
				reportLine = new ReportLine();
				reportLine.setItemId(id);
				reportLine.setDate(date);
				lines.put(id, reportLine);
			}
			reportLine.getColumns().put((String) line[2], line[3]);
		}
		
		List<ReportLine> result = new ArrayList<ReportLine>(lines.values());
		Collections.sort(result, new Comparator<ReportLine>() {
			@Override
			public int compare(ReportLine o1, ReportLine o2) {
				return ObjectUtils.compare(o1.getDate(), o2.getDate());
			}
		});
		return result;
	}
}
