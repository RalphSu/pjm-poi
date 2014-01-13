package org.pjm2.report.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "report_tasks")
public class ReportTask {
	
	public enum Status {
		planned,
		inprogress,
		generated,
		reviewed,
		published;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "project_id")
	private Long projectId;

	@Column
	private String status;

	@Column(name = "gen_start_time")
	private Date genStartTime;

	@Column(name = "gen_end_time")
	private Date genEndTime;
	@Column(name = "report_start_time")
	private Date reportStartTime;
	@Column(name = "report_end_time")
	private Date reportEndTime;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	@Column(name = "report_path")
	private String reportPath;
	
	@Column(name="gen_count")
	private Integer gen_count;

	@Transient
	private String project_identifier;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getGenStartTime() {
		return genStartTime;
	}

	public void setGenStartTime(Date genStartTime) {
		this.genStartTime = genStartTime;
	}

	public Date getGenEndTime() {
		return genEndTime;
	}

	public void setGenEndTime(Date genEndTime) {
		this.genEndTime = genEndTime;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getReportPath() {
		return reportPath;
	}

	public void setReportPath(String reportPath) {
		this.reportPath = reportPath;
	}

	public Date getReportStartTime() {
		return reportStartTime;
	}

	public void setReportStartTime(Date reportStartTime) {
		this.reportStartTime = reportStartTime;
	}

	public Date getReportEndTime() {
		return reportEndTime;
	}

	public void setReportEndTime(Date reportEndTime) {
		this.reportEndTime = reportEndTime;
	}

	public String getProject_identifier() {
		return project_identifier;
	}

	public void setProject_identifier(String project_identifier) {
		this.project_identifier = project_identifier;
	}

    public Integer getGen_count() {
        return gen_count;
    }

    public void setGen_count(Integer gen_count) {
        this.gen_count = gen_count;
    }
    
    public void addGen_count() {
        if (this.gen_count == null) {
            this.gen_count = 1;
        } else  {
            this.gen_count++;
        }
    }

}
