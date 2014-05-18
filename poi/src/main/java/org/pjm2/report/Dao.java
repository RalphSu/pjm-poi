package org.pjm2.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.jpa.QueryHints;
import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTask.Status;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.internal.BlogDao;
import org.pjm2.report.internal.ForumDao;
import org.pjm2.report.internal.IEntityDao;
import org.pjm2.report.internal.NewsDao;
import org.pjm2.report.internal.SummaryDao;
import org.pjm2.report.internal.WeiboDao;
import org.pjm2.report.internal.WeixinDao;
import org.pjm2.report.model.ReportLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liasu
 * 
 */
public class Dao {
	public	static final String NEWS_TEMPLATE_TYPE = "新闻类模板";
	public 	static final String WEIBO_TEMPLATE_TYPE = "微博类模板";
	public 	static final String BLOG_TEMPLATE_TYPE = "博客类模板";
	public  static final String FORUM_TEMPLATE_TYPE = "论坛类模板";
	public  static final String WEIXIN_TEMPLATE_TYPE = "微信类模板";
	public  static final String SUMMARY_TEMPLATE_TYPE = "汇总数据类模板";
	public  static final Integer MAX_GENERATION_COUNT = 10;

	private static Logger logger = LoggerFactory.getLogger(Dao.class);

	public EntityManagerFactory buildSessionFactory(Map<String, String> props) {
		try {
			EntityManagerFactory sf = Persistence.createEntityManagerFactory("pjmUnit", props);
			return sf;
		} catch (Exception e) {
			logger.error("Failed to load connection!", e);
			throw new RuntimeException(e);
		}
	}

	private EntityManagerFactory managerFactory;
	private EntityManager manager;
	private final Map<String, IEntityDao> daos = new HashMap<String, IEntityDao>();

	public void init() {
		managerFactory = buildSessionFactory(new HashMap<String, String>());
        manager = managerFactory.createEntityManager();
		initDaos();
	}

	private void initDaos() {
		EntityManager manager = this.managerFactory.createEntityManager();
		IEntityDao dao = new NewsDao(manager);
		daos.put(NEWS_TEMPLATE_TYPE, dao);
		dao = new BlogDao(manager);
		daos.put(BLOG_TEMPLATE_TYPE, dao);
		dao = new ForumDao(manager);
		daos.put(FORUM_TEMPLATE_TYPE, dao);
		dao = new WeiboDao(manager);
		daos.put(WEIBO_TEMPLATE_TYPE, dao);
		dao = new WeixinDao(manager);
		daos.put(WEIXIN_TEMPLATE_TYPE, dao);
		dao = new SummaryDao(manager);
		daos.put(SUMMARY_TEMPLATE_TYPE, dao);
	}
	
	// for test only
	public void setEntityManagerFactory(EntityManagerFactory mgrfct) {
		this.managerFactory = mgrfct;
		manager = managerFactory.createEntityManager();
		initDaos();
	}

	@SuppressWarnings("unchecked")
	public List<ReportTask> findTODOTasks() {
		String sql = "select * from report_tasks where status in ('%s', '%s') and ( gen_count IS NULL or gen_count <= %d )";
		sql = String.format(sql, Status.planned, Status.inprogress,
				MAX_GENERATION_COUNT);
		Query query = manager.createNativeQuery(sql, ReportTask.class);
		query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
		List<?> result = query.getResultList();
		List<ReportTask> tasks = (List<ReportTask>) result;

		StringBuffer idSbs = new StringBuffer();
		// fill project identifier
		if (!tasks.isEmpty()) {
			List<Long> pId = new ArrayList<Long>();
			for (ReportTask task : tasks) {
				pId.add(task.getProjectId());
			}
			Map<Long, String> projects = new HashMap<Long, String>();
			sql = "select id, name from projects where id in ( %s )";
			query = manager.createNativeQuery(String.format(sql,StringUtils.join(pId, ',')));
			query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
			result = query.getResultList();
			for (Object o : result) {
				Object[] objs = (Object[]) o;
				projects.put(((Number) objs[0]).longValue(), objs[1].toString());
			}
			for (ReportTask t : tasks) {
				t.setProjectName(projects.get(t.getProjectId()));
				idSbs.append(t.getId().toString()).append(",");
			}
		}

		logger.info("Find " + tasks.size() + " tasks! :: " + idSbs);
		return tasks;
	}

	@SuppressWarnings("unchecked")
	public List<ReportTemplate> findReportTemplates(Long project_id) {
		String sql = "select * from report_templates where project_id = " + project_id + " order by position ASC ";
		Query query = manager.createNativeQuery(sql, ReportTemplate.class);
		query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
		List<?> result = query.getResultList();
		return (List<ReportTemplate>) result;
	}
	
	public List<String> sortedTempalteTypes(Long project_id) {
		String sql = "select template_type from report_templates where project_id = " + project_id + " group by template_type order by position ASC ";
		Query query = manager.createNativeQuery(sql);
		query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
		List<?> queryResults = query.getResultList();
		List<String> results = new ArrayList<String>();
		for (Object o : queryResults) {
			if (o instanceof String) {
				results.add(o.toString());
			} else if (o instanceof Object[]) {
				for (Object so : (Object[]) o) {
					if (so instanceof String) {
						results.add(so.toString());
					}
				}
			}
		}
		return results;
	}

	public List<ReportLine> findReportLine(ReportTemplate template, long pid, Date starTime, Date endTime) {
		String type = template.getTemplate_type();
		IEntityDao dao = daos.get(type);
		if (dao != null) {
			return dao.findReportLine(template, pid, starTime, endTime);
		} else {
			throw new IllegalArgumentException("unkonw tempalte" + template.getTemplate_type());
		}
	}
	
	public long findReportLineCount(ReportTemplate template, long pid, Date starTime, Date endTime) {
		 List<ReportLine> results = findReportLine(template, pid, starTime,endTime);
		 return results.size();
	}
	
	public void save(Object entity) {
		EntityManager manager = managerFactory.createEntityManager();
		EntityTransaction tx = manager.getTransaction();
		try{
			tx.begin();
			manager.merge(entity);
			tx.commit();
		}catch (PersistenceException e) {
			logger.error("error in update",e);
			if(e instanceof OptimisticLockException)
				throw new RuntimeException("Your data is not newest, someone else change it already ,please reselect and update");
			else 
				throw new RuntimeException(e.getCause().getMessage());
		}catch (Throwable t) {
			logger.error("error in update",t);
			throw new RuntimeException(t.getMessage());
		}
		finally {
			try {
				if (tx.isActive())
					tx.rollback();
			} catch (Exception e) {
				logger.error("error in update",e);
			}
			try {
				manager.close();
			} catch (Exception e) {
				logger.error("Could not close entitymanager",e);
			}
			
		}
		
    }

    public List<String> findImagePathByUrl(String body, String date) {
        List<String> paths = new ArrayList<String>();
        try {
            String sql = "select file_path from images where url = '%s' and image_date = '%s' ";
            Query query = manager.createNativeQuery(String.format(sql, body, date));
            query.setHint(QueryHints.HINT_CACHE_MODE, CacheMode.IGNORE);
            List<?> sqlResult = query.getResultList();
            for (Object o : sqlResult) {
                if (o != null) {
                    paths.add(o.toString());
                }
            }
        } catch (Exception e) {
            logger.warn(String.format("Cannot query for image path : %s, date : %s !", body, date), e);
        }
        return paths;
    }

}
