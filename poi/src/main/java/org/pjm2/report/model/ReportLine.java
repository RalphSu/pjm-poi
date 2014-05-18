package org.pjm2.report.model;

import java.util.HashMap;
import java.util.Map;

public class ReportLine {
	private Long itemId;
//	private String template_type;
	private String date;
	private Map<String, Object> columns = new HashMap<String, Object>();

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Map<String, Object> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, Object> columns) {
		this.columns = columns;
	}

//	public String getTemplate_type() {
//		return template_type;
//	}
//
//	public void setTemplate_type(String template_type) {
//		this.template_type = template_type;
//	}

}
