/**
 * 
 */
package org.pjm2.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	private volatile boolean stop = false;
	private final Dao dao = new Dao();

	// daemon thread factory
	private static class DaemonThreadFactory implements ThreadFactory {
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix = "Report genearte [Thread-";
		final String nameSuffix = "]";

		public Thread newThread(Runnable r) {
			Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, namePrefix
					+ threadNumber.getAndIncrement() + nameSuffix, 0);
			t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	private ExecutorService executors = Executors.newCachedThreadPool(new DaemonThreadFactory());
	private ConcurrentHashMap<Long, Job> runningTaskId = new ConcurrentHashMap<Long, Job>();

	public void startLoop() {
		stop = false;

		while (!stop) {
			List<Job> jobs = getJob();
			// submit jobs
			for (Job job : jobs) {
				runningTaskId.put(job.task.getId(), job);
				executors.submit(job);
			}
			slientWait();
		}
	}

	private void slientWait() {
		try {
			// current no job, sleep for 5 minutes
			Thread.sleep(1000 * 60 * 5);
		} catch (InterruptedException e) {
			logger.info("Generate wait!", e);
		}
	}

	private List<Job> getJob() {
		List<ReportTask> tasks = dao.findTODOTasks();
		List<Job> jobs = new ArrayList<ReportGenerator.Job>();
		for (ReportTask task : tasks) {
			jobs.add(new Job(task));
		}
		return jobs;
	}

	public void stopLoop() {
		stop = true;
		try {
			executors.shutdown();
			executors.awaitTermination(3, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			executors.shutdownNow();
		}
	}

	private class Job implements Runnable {
		private final ReportTask task;

		public Job(ReportTask task) {
			this.task = task;
		}

		public void run() {
			try {
				// mark start
				task.setStatus(Status.inprogress.toString());
				dao.save(task);

				// on-progress
				List<ReportTemplate> templates = dao.findReportTemplates(task.getProjectId());
				Map<ReportTemplate, List<ReportLine>> allReportData = new HashMap<ReportTemplate, List<ReportLine>>();
				for (ReportTemplate template : templates) {
					List<ReportLine> lines = dao.findReportLine(template, task.getProjectId(),
							task.getReportStartTime(), task.getReportEndTime());
					allReportData.put(template, lines);
				}

				if (writeToFile(allReportData)) {
					// mark end
					task.setStatus(Status.generated.toString());
					dao.save(task);
				}
			} catch (Exception e) {
				logger.error("Generation failed!", e);
			} finally {
				// remove from set
				ReportGenerator.this.runningTaskId.remove(task.getId());
			}
		}

		private boolean writeToFile(Map<ReportTemplate, List<ReportLine>> reportData) {
			POIWriter writer = new POIWriter();
			return writer.write(task, reportData);
		}

	}

}
