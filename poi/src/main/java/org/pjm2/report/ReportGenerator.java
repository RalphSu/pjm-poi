/**
 * 
 */
package org.pjm2.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTask.Status;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.model.ReportLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liasu
 * 
 */
public class ReportGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
//	private volatile boolean stop = false;
	private final Dao dao;

//	// daemon thread factory
//	private static class DaemonThreadFactory implements ThreadFactory {
//		final AtomicInteger threadNumber = new AtomicInteger(1);
//		final String namePrefix = "Report genearte [Thread-";
//		final String nameSuffix = "]";
//
//		public Thread newThread(Runnable r) {
//			Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, namePrefix
//					+ threadNumber.getAndIncrement() + nameSuffix, 0);
//			t.setDaemon(true);
//			if (t.getPriority() != Thread.NORM_PRIORITY)
//				t.setPriority(Thread.NORM_PRIORITY);
//			return t;
//		}
//	}

//	private ExecutorService executors = Executors.newFixedThreadPool(10, new DaemonThreadFactory());
	private ConcurrentHashMap<Long, Job> runningTaskId = new ConcurrentHashMap<Long, Job>();

    public ReportGenerator() {
        dao = new Dao();
        dao.init();
    }

	public void startLoop() {
//		stop = false;
//		while (!stop) {
			try {
				List<Job> jobs = getJobs();
				logger.info("get jobs @ " + new Date() + " with job number " + jobs.size());
				// submit jobs
				for (Job job : jobs) {
					try {
//						runningTaskId.put(job.task.getId(), job);
//						executors.submit(job);
						job.run();
					} catch (Throwable t) {
						logger.error("report genration encounter an error! catch and log this, then continue running!");
					}
				}
//				waitExecution();
//				slientWait();
			} catch (Throwable t) {
				logger.error("report genration encounter an error! catch and log this, then continue running!");
			}
//		}
	}

//	private void waitExecution() {
//		try {
//			executors.shutdown();
//			executors.awaitTermination(30, TimeUnit.MINUTES);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void slientWait() {
//		try {
//			// current no job, sleep wait
//			Thread.sleep(1000 * 60);
//		} catch (InterruptedException e) {
//			logger.info("Generate wait!", e);
//		}
//	}

	private List<Job> getJobs() {
        try {
            List<ReportTask> tasks = dao.findTODOTasks();
            List<Job> jobs = new ArrayList<ReportGenerator.Job>();
            for (ReportTask task : tasks) {
                jobs.add(new Job(task));
            }
            return jobs;
        } catch (Exception e) {
			logger.error("failed to find tasks!", e);
			return Collections.emptyList();
		}
	}

//	public void stopLoop() {
////		stop = true;
//		try {
//			executors.shutdown();
//			executors.awaitTermination(3, TimeUnit.MINUTES);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			executors.shutdownNow();
//		}
//	}

	private class Job implements Runnable {
		private final ReportTask task;

		public Job(ReportTask task) {
			this.task = task;
		}

		public void run() {
			try {
				// mark start
				logger.info("start generation for task : " + task.getId()
						+ "for project " + task.getProjectName());
				task.setStatus(Status.inprogress.toString());
				task.setGenStartTime(new Date());
				dao.save(task);
				// on-progress
				List<ReportTemplate> templates = dao.findReportTemplates(task
						.getProjectId());
				Map<ReportTemplate, List<ReportLine>> allReportData = new HashMap<ReportTemplate, List<ReportLine>>();
				for (ReportTemplate template : templates) {
					List<ReportLine> lines = dao.findReportLine(template,
							task.getProjectId(), task.getReportStartTime(),
							task.getReportEndTime());
					allReportData.put(template, lines);
				}

				if (writeToFile(allReportData)) {
					// mark end
					task.setStatus(Status.generated.toString());
					task.setGenEndTime(new Date());
					dao.save(task);
					logger.info("end generation for task : task id "
							+ task.getId() + " . For project "
							+ task.getProjectName()
							+ ". File write at $PJM_HOME/" + task.getGen_path());
				} else {
					logger.info("end generation for task : Generation failed : "
							+ task.getId()
							+ " . For project "
							+ task.getProjectName());
					task.setGenEndTime(new Date());
					dao.save(task);
				}
			} catch (Exception e) {
				logger.error("Generation failed!", e);
			} finally {
				// remove from set
				ReportGenerator.this.runningTaskId.remove(task.getId());
				try {
					task.addGen_count();
					dao.save(task);
					logger.info("update task!");
				} catch (Throwable t) {
					// ignore
					logger.warn("fail to increase gen-count for task" + task.getId(), t);
				}

			}
		}

		private boolean writeToFile(Map<ReportTemplate, List<ReportLine>> reportData) {
			List<String> sortedTemplates = dao.sortedTempalteTypes(task.getProjectId());
			TempalteSorter sorter = new TempalteSorter(sortedTemplates);
			ReportPOIWriter writer = new ReportPOIWriter(dao, task, sorter);
			return writer.write(reportData);
		}

	}
	
	
	public static class TempalteSorter implements Comparator<String>{
		final Map<String, Integer> definedOrders = new HashMap<String, Integer>();
		public TempalteSorter(List<String> sortedTemplates) {
			for (int i = 0; i < sortedTemplates.size(); i++) {
				this.definedOrders.put(sortedTemplates.get(i), i);
			}
		}
		
		@Override
		public int compare(String o1, String o2) {
			Integer index1 = definedOrders.get(o1);
			Integer index2 = definedOrders.get(o2);
			return ObjectUtils.compare(index1, index2);
		}

	}


}
