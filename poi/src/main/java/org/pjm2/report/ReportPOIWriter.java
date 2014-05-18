/**
 * 
 */
package org.pjm2.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlink;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTColumns;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STSectionMark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.pjm2.report.ReportGenerator.TempalteSorter;
import org.pjm2.report.db.model.ReportTask;
import org.pjm2.report.db.model.ReportTemplate;
import org.pjm2.report.model.ReportLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author liasu
 * 
 */
public class ReportPOIWriter {
	private static final String 截图 = "截图";
	private static final String 图片 = "图片";
	private static final String 所属话题 = "所属话题";
	private static final String 微博直发 = "微博直发";
	private static final String 达人直发="达人直发";
	private static final String 微博类模板 = "微博类模板";
	private static final Logger logger = LoggerFactory.getLogger(ReportPOIWriter.class);
	private final Dao dao;
	private final ReportTask task;
	private static final int CHART_WIDTH=400;
	private static final int CHART_HEIGHT=280;
	private final Map<String, Integer> picturesMap = new HashMap<String, Integer>();
	private final TempalteSorter sorter;
	private static int IN_TABLE_FONT_SIZE = 8;
	private static int TABLE_MAX_WIDTH = 8100;
	private static int LANDSACPE_TABLE_MAX_WIDTH = 13000;
	private static int IMAGE_MAX_WIDTH = 500;

    private static final Map<String, Integer> FIXED_COLUMN_WIDTH = new HashMap<String, Integer>();
    static {
        FIXED_COLUMN_WIDTH.put("标题", 1428);
        FIXED_COLUMN_WIDTH.put("日期", 1065);
        FIXED_COLUMN_WIDTH.put("链接", 2131);
        FIXED_COLUMN_WIDTH.put("微博内容", 2131);
        FIXED_COLUMN_WIDTH.put("分享链接内容", 737);
        FIXED_COLUMN_WIDTH.put("主题", 1360);
        FIXED_COLUMN_WIDTH.put("位置", 1054);
        FIXED_COLUMN_WIDTH.put("内容", 2131);
        FIXED_COLUMN_WIDTH.put("热门微博排名", 793);
        FIXED_COLUMN_WIDTH.put("是否推荐", 566);
    }

    private static final Set<String> NON_CENTER_ALIGN_COLUMN = new HashSet<String>();
    static {
        NON_CENTER_ALIGN_COLUMN.add("标题");
        NON_CENTER_ALIGN_COLUMN.add("链接");
        NON_CENTER_ALIGN_COLUMN.add("微博内容");
        NON_CENTER_ALIGN_COLUMN.add("分享链接内容");
        NON_CENTER_ALIGN_COLUMN.add("内容");
    }

	public ReportPOIWriter(Dao dao, ReportTask task, TempalteSorter sorter) {
		this.dao = dao;
		this.task = task;
		this.sorter = sorter;
	}
	
	@SuppressWarnings("unchecked")
	public boolean write(Map<ReportTemplate, List<ReportLine>> reportData) {
		try {
			CustomXWPFDocument doc = createDocOfStartingPages();
			
			Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> sortedData = shuffle(reportData);
			{
				String key = "新闻类模板";
				List<Entry<ReportTemplate, List<ReportLine>>> data = sortedData.get(key);
				if (data != null) {
					writeTemplateType(doc, key, data);
				}
			}
			{
				String key = 微博类模板;
				List<Entry<ReportTemplate, List<ReportLine>>> data = sortedData.get(key);
				if (data != null) {
					// pre-process of weiboDirect and darenDirect
					boolean moreThanDirect = false;
					List<Entry<ReportTemplate, List<ReportLine>>> filteredWeibo = new ArrayList<Map.Entry<ReportTemplate, List<ReportLine>>>();
					for (Entry<ReportTemplate, List<ReportLine>> module : data) {
						if (key.equals(微博类模板) && 微博直发.equals(module.getKey().getClassified())) {
							// special handling for weibo direct
							weibo_Direct = module;
						} else if (key.equals(微博类模板) && 达人直发.equals(module.getKey().getClassified())) {
							daren_Direct = module;
						} else {
							moreThanDirect = true;
							filteredWeibo.add(module);
						}
					}
					if (moreThanDirect) {
						writeTemplateType(doc, key, filteredWeibo);
					}
				}
            }
			{
				String key = "微信类模板";
				List<Entry<ReportTemplate, List<ReportLine>>> data = sortedData.get(key);
				if (data != null) {
					writeTemplateType(doc, key, data);
				}
            }
			{
				String key = "博客类模板";
				List<Entry<ReportTemplate, List<ReportLine>>> data = sortedData.get(key);
				if (data != null) {
					writeTemplateType(doc, key, data);
				}
			}
			{
				String key = "论坛类模板";
				List<Entry<ReportTemplate, List<ReportLine>>> data = sortedData.get(key);
				if (data != null) {
					writeTemplateType(doc, key, data);
				}
			}
			{
				String key = "汇总数据类模板";
				List<Entry<ReportTemplate, List<ReportLine>>> data = sortedData.get(key);
				if (data != null) {
					writeTemplateType(doc, key, data);
				}
			}
			writeImageAnaylysis(doc, reportData);
			save(doc, "");
			
			// special handling for weibo_direct and daren_direct
			{
				if (weibo_Direct != null) {
					try {
						CustomXWPFDocument weiboDirectDoc = createDocOfStartingPages();
						writeTemplateType(weiboDirectDoc, 微博类模板, Arrays.asList(weibo_Direct));
						save(weiboDirectDoc, "_微博直发");
					} catch (Exception e) {
						logger.error("Write weibo direct document failed!", e);
					}
				}
				if (daren_Direct != null) {
					try {
						CustomXWPFDocument darenDirectDoc = createDocOfStartingPages();
						writeTemplateType(darenDirectDoc, 微博类模板, Arrays.asList(daren_Direct));
						save(darenDirectDoc, "_达人直发");
					} catch (Exception e) {
						logger.error("Write da_ren direct document failed!", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Fail to write the template!", e);
			return false;
		}
		return true;
	}
	
	private void writeSectionPageBreak(CustomXWPFDocument doc, boolean landscape) {
		XWPFParagraph breakParagraph = doc.createParagraph();
		CTP ctp = breakParagraph.getCTP();
		CTPPr pr = ctp.addNewPPr();
		CTSectPr secPr = pr.addNewSectPr();
		// grid
		CTDocGrid grid = secPr.addNewDocGrid();
		grid.setType(STDocGrid.LINES);
		grid.setLinePitch(BigInteger.valueOf(312));
		// copy from template generated
		CTColumns columns = secPr.addNewCols();
		columns.setSpace(BigInteger.valueOf(425));
		// set type=continuous at any time, let the word handling the mis-match of continuous
		CTSectType type = secPr.addNewType();
		type.setVal(STSectionMark.CONTINUOUS);
		CTPageSz pageSz = secPr.addNewPgSz();
		if (landscape) {
			pageSz.setOrient(STPageOrientation.LANDSCAPE);
			pageSz.setW(BigInteger.valueOf(16838));
			pageSz.setH(BigInteger.valueOf(11906));
			// page margin
			CTPageMar mar = secPr.addNewPgMar();
			mar.setTop(BigInteger.valueOf(1440));
			mar.setRight(BigInteger.valueOf(1800));
			mar.setBottom(BigInteger.valueOf(1440));
			mar.setLeft(BigInteger.valueOf(1800));
			mar.setHeader(BigInteger.valueOf(851));
			mar.setFooter(BigInteger.valueOf(992));
			mar.setGutter(BigInteger.valueOf(0));
		} else {
			pageSz.setW(BigInteger.valueOf(11906));
			pageSz.setH(BigInteger.valueOf(16836));
			// page margin
			CTPageMar mar = secPr.addNewPgMar();
			mar.setTop(BigInteger.valueOf(1800));
			mar.setRight(BigInteger.valueOf(1440));
			mar.setBottom(BigInteger.valueOf(1800));
			mar.setLeft(BigInteger.valueOf(1440));
			mar.setHeader(BigInteger.valueOf(851));
			mar.setFooter(BigInteger.valueOf(992));
			mar.setGutter(BigInteger.valueOf(0));
		}
	}

	private CustomXWPFDocument createDocOfStartingPages() throws IOException {
		CustomXWPFDocument doc = new CustomXWPFDocument(ReportPOIWriter.class.getResourceAsStream("/Template.docx"));
		{
			XWPFParagraph p1 = doc.createParagraph();
			XWPFRun title = p1.createRun();
			p1.setAlignment(ParagraphAlignment.CENTER);
			p1.setVerticalAlignment(TextAlignment.TOP);
			p1.setStyle("Title");
			title.setBold(true);
			title.setFontSize(20);
			title.setText(String.format("%s", task.getProjectName()));
		}
		XWPFParagraph p2 = doc.createParagraph();
		{
			String type = "日报";
			if (ReportTask.TASKTYPE.weekly.name().equalsIgnoreCase(task.getTaskType())) {
				type = "周报";
			} else if (ReportTask.TASKTYPE.summary.name().equalsIgnoreCase(task.getTaskType())) {
				type = "结案报告";
			}
			{
				XWPFRun title1 = p2.createRun();
				p2.setAlignment(ParagraphAlignment.CENTER);
				p2.setVerticalAlignment(TextAlignment.TOP);
				title1.setBold(false);
				title1.setFontSize(14);
				title1.setText(type);
			}
			
			{
				XWPFRun title2 = p2.createRun();
				p2.setStyle("Subtitle");
				p2.setAlignment(ParagraphAlignment.CENTER);
				p2.setVerticalAlignment(TextAlignment.TOP);
				title2.setBold(false);
				title2.setFontSize(14);
				// title1.setFontFamily("微软雅黑");
				SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
				String startDate = format.format(task.getReportStartTime());
				if (type == "周报" || type == "结案报告") {
					if (task.getReportEndTime() != null) {
						String endDate = format.format(task.getReportEndTime());
						title2.setText(String.format("（%s - %s）", startDate, endDate));
					} else {
						title2.setText(String.format("(%s) ", startDate));
					}
				} else {
					title2.setText(String.format("(%s) ", startDate));
				}
				
			}
			
		}
		
		// insert a page break
		{
			XWPFRun breakRun = p2.createRun();
			breakRun.addBreak(BreakType.PAGE);
		}
		return doc;
	}

	private String getReportFilePath(){
		String prefix = System.getenv("PJM_HOME");
		if(prefix==null)
			prefix = System.getProperty("PJM_HOME");
		if(prefix==null) {
		    throw new RuntimeException("can not find PJM_HOME system property! please set correctly.");
		}
		String path = prefix + "/reports/" + this.task.getProjectName() + "/";
		// check parent directory
		try{
			FileUtils.forceMkdir(new File(path));	
		}catch(Throwable t){
			logger.error("create path error "+path, t);
			throw new RuntimeException(t);
		}
		return path;
	}

	private void save(XWPFDocument doc, String suffix) {
		FileOutputStream out = null;
		try {
			String path = getReportFilePath();
			String file = path + task.getId() + suffix + ".docx";
            logger.info(" write to file path:" + file);
			out = new FileOutputStream(file);
			doc.write(out);
			out.flush();
			task.setGen_path("/reports/" + this.task.getProjectName() + "/"+task.getId() + ".docx");
		} catch (FileNotFoundException e) {
			logger.error("Can not open doc file for write!", e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("Write doc file failed!", e);
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Close doc file failed! Ignore this failure!", e);
				}
			}
		}
	}

	private void writeTemplateType(CustomXWPFDocument doc, String key,
	        List<Entry<ReportTemplate, List<ReportLine>>> data) {
		XWPFParagraph templateParagraph = doc.createParagraph();
		templateParagraph.setStyle("Heading1");
		XWPFRun templateRun = templateParagraph.createRun();
		templateRun.setFontSize(20);
		templateRun.setText(key.substring(0, key.length() - 2)); // assume last two word is"模板"

		for (Entry<ReportTemplate, List<ReportLine>> module : data) {
			writeModule(doc, module.getKey(), module.getValue());
		}
	}

	private Entry<ReportTemplate, List<ReportLine>> weibo_Direct;
	private Entry<ReportTemplate, List<ReportLine>> daren_Direct;//达人直发
	
	public void writeImageAnaylysis( CustomXWPFDocument doc, Map<ReportTemplate, List<ReportLine>> reportData) {
		{
			XWPFParagraph templateParagraph = doc.createParagraph();
			templateParagraph.setStyle("Heading1");
			XWPFRun templateRun = templateParagraph.createRun();
			templateRun.setFontSize(20);
			templateRun.setText("图表类");
		}
		
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setBold(false);
			templateRun.setText("网络與情信息每日與情走势");
		}
		String trendCountFileName = createTrendCountFile(reportData.keySet());
		try{
			FileInputStream fis=new FileInputStream(trendCountFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			 doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);  
		}catch(Throwable t){
			logger.error("write trendCountFileName ",t);
		}
		
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("监测信息平台分布图");
		}
		String distroPieChartFileName = createDistroPieChartFileName(reportData);
		try{
			FileInputStream fis=new FileInputStream(distroPieChartFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write distroPieChartFileName ",t);
		}
	
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("视频信息比重");
		}
		String videoPieChartFileName = createVideoPieChartFileName(reportData);
		try{
			FileInputStream fis=new FileInputStream(videoPieChartFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write distroPieChartFileName ",t);
		}
		
		// //4.	网络关注情况分布
		
		//5.	网络新闻信息每日舆情走势
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("网络新闻信息每日舆情走势");
		}
		String newsTrendCountFileName = createNewsTrendCountFile(reportData.keySet());
		try{
			FileInputStream fis=new FileInputStream(newsTrendCountFileName);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write newsTrendCountFileName ",t);
		}
		
		{
			XWPFParagraph labelParagraph = doc.createParagraph();
			labelParagraph.setStyle("Heading2");
			XWPFRun templateRun = labelParagraph.createRun();
			templateRun.setFontSize(14);
			templateRun.setText("网络新闻信息媒体覆盖情况");
		}
		String newsPlatformDistro = createNewsPlatformDistro(reportData);
		try{
			FileInputStream fis=new FileInputStream(newsPlatformDistro);
			doc.addPictureData(fis,  XWPFDocument.PICTURE_TYPE_PNG);	
			doc.createPicture(doc.getAllPictures().size()-1, CHART_WIDTH, CHART_HEIGHT);
		}catch(Throwable t){
			logger.error("write newsPlatformDistro ",t);
		}
	}
	
    private String createNewsPlatformDistro(
			Map<ReportTemplate, List<ReportLine>> reportData) {
    	List<ReportLine> newsReportLine = new ArrayList<ReportLine>();
    	for(ReportTemplate reportTemplate:reportData.keySet()){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(reportTemplate.getTemplate_type())){
				newsReportLine.addAll(reportData.get(reportTemplate));
			}
		}
    	Map<String, Integer> platformDistro = new HashMap<String, Integer>();
    	for(ReportLine reportLine:newsReportLine){
    		if(reportLine.getColumns()==null) continue;
    		for(String columnName:reportLine.getColumns().keySet()){
    			if("发布平台".equals(columnName)){
    				Object platform = reportLine.getColumns().get(columnName);
    				if(platform instanceof String){
    					String platformCatalog="其他";
    					if(((String)platform).contains("新浪")){
    						platformCatalog="新浪";
    					}else if(((String)platform).contains("搜狐")){
    						platformCatalog="搜狐";
    					}else if(((String)platform).contains("网易")){
    						platformCatalog="网易";
    					}else if(((String)platform).contains("腾讯")){
    						platformCatalog="腾讯";
    					}else if(((String)platform).contains("凤凰")){
    						platformCatalog="凤凰";
    					}else if(((String)platform).contains("人民网")){
    						platformCatalog="人民网";
    					}else if(((String)platform).contains("东方网")){
    						platformCatalog="东方网";
    					}else if(((String)platform).contains("新华网")){
    						platformCatalog="新华网";
    					}
    					
    					if(platformDistro.containsKey(platformCatalog)){
    						int count = platformDistro.get(platformCatalog);
    						count++;
    						platformDistro.put(platformCatalog, count);
    					}else{
    						platformDistro.put(platformCatalog, 1);
    					}
    				}else continue;
    			}
    		}
    	}
    	DefaultCategoryDataset mDataset = new DefaultCategoryDataset(); 
    	for(String platformName:platformDistro.keySet()){
    		mDataset.addValue(platformDistro.get(platformName), "", platformName);
    	}
    	
    	JFreeChart chart = ChartFactory.createBarChart(
    			"新闻发布平台分布", // 图表标题
    			"新闻发布平台", // 目录轴的显示标签
    			"信息", // 数值轴的显示标"
    			mDataset, // 数据
    			PlotOrientation.VERTICAL , // 图表方向：垂�    			
    			false, // 是否显示图例(对于简单的柱状图必须是false)
    			false, // 是否生成工具
    			false // 是否生成URL链接
    			); 
         chart.setTitle(getTextTile("新闻发布平台分布"));
         
    	chart.setBackgroundPaint(Color.WHITE);   
         CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();   
         categoryplot.setBackgroundPaint(Color.WHITE);   
         categoryplot.setDomainGridlinePaint(Color.white);   
         categoryplot.setDomainGridlinesVisible(true);   
         //x� 
         CategoryAxis mDomainAxis = categoryplot.getDomainAxis();  
         //设置x轴标题的字体  
         mDomainAxis.setLabelFont(new Font("宋体", Font.PLAIN, 15));  
         //设置x轴坐标字� 
         mDomainAxis.setTickLabelFont(new Font("宋体", Font.PLAIN, 15));  
         //y� 
         ValueAxis mValueAxis = categoryplot.getRangeAxis();  
         //设置y轴标题字� 
         mValueAxis.setLabelFont(new Font("宋体", Font.PLAIN, 15));  
         //设置y轴坐标字� 
         mValueAxis.setTickLabelFont(new Font("宋体", Font.PLAIN, 15));  
         categoryplot.setRangeGridlinePaint(Color.white);   
         return chartToFile(chart, "platformdistro");
		
	}

	private String createNewsTrendCountFile(Set<ReportTemplate> keySet) {
		Set<ReportTemplate> newsReportTemplate = new HashSet<ReportTemplate>();
		for(ReportTemplate reportTemplate:keySet){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(reportTemplate.getTemplate_type())){
				newsReportTemplate.add(reportTemplate);
			}
		}
		//截止报告生成日前每日舆情走势，类似图.但纵坐标只统计新闻类的信息量
		TimeSeriesCollection timeSeriesCollection = getTimeSeriesCollection(newsReportTemplate);
		JFreeChart chart = createChartPress(timeSeriesCollection, "网络新闻信息日走势");  
		return chartToFile(chart, "news_trend");
		
	}

	private String createVideoPieChartFileName(
			Map<ReportTemplate, List<ReportLine>> reportData) {
    	int nonVideoCount=0;
    	int videoCount=0;
    	for(ReportTemplate template:reportData.keySet()){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
				String classified = template.getClassified();
				if(classified!=null&&classified.contains("视频")){
					videoCount+=reportData.get(template).size();
				}else{
					nonVideoCount+=reportData.get(template).size();
				}
		    }
		}
    	 DefaultPieDataset dataset = new DefaultPieDataset();  
    	 dataset.setValue("视频信息",videoCount);  
     	 dataset.setValue("非视频网络信息", nonVideoCount);
     	 
     	JFreeChart chart = ChartFactory.createPieChart3D("视频信息分布", dataset, true, true, true);  
        
        Font font = new Font("", Font.BOLD, 14);  
        chart.setTitle(getTextTile("视频信息分布"));
        chart.getLegend().setItemFont(font);  
        
        PiePlot3D  piePlot = (PiePlot3D ) chart.getPlot();  
        piePlot.setBackgroundPaint(new Color(255, 255, 255)); 
        piePlot.setLabelLinksVisible(false);
        piePlot.setLabelFont(font);  
        piePlot.setLabelBackgroundPaint(new Color(255, 255, 255));

        piePlot.setForegroundAlpha(1.0F);    
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}")); 
        piePlot.setSectionPaint("视频信息", new Color(79, 129, 189));    
        piePlot.setSectionPaint("非视频网络信息", new Color(192,80,77));     
        piePlot.setStartAngle(10.0); 
        return chartToFile(chart, "videodistro");
	}

	private String createDistroPieChartFileName(Map<ReportTemplate, List<ReportLine>> reportData) {
    	 DefaultPieDataset dataset = new DefaultPieDataset();  
         int newsCount=0;
         int blogCount=0;
         int forumCount=0;
         int weiboCount=0;
          
		for(ReportTemplate template:reportData.keySet()){
			if(Dao.NEWS_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
				newsCount+= reportData.get(template).size();
		    }else if(Dao.BLOG_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
		    	blogCount+= reportData.get(template).size();
		    }else if(Dao.FORUM_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
		    	forumCount+= reportData.get(template).size();
		    }else if(Dao.WEIBO_TEMPLATE_TYPE.equalsIgnoreCase(template.getTemplate_type())){
		    	weiboCount+= reportData.get(template).size();
		    }
		}
		dataset.setValue("新闻",newsCount);  
    	dataset.setValue("博客", blogCount);
    	dataset.setValue("论坛", forumCount);
       	dataset.setValue("微博", weiboCount);
		 
    	
		JFreeChart chart = ChartFactory.createPieChart3D("网络信息平台分布", dataset, true, true, true);  
		chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
	        	    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	 
        Font font = new Font("", Font.BOLD, 18);  
        chart.setTitle(getTextTile("网络信息平台分布"));
        chart.getLegend().setItemFont(font);  
        PiePlot3D  piePlot = (PiePlot3D ) chart.getPlot();  
        piePlot.setBackgroundPaint(new Color(255, 255, 255));  
        piePlot.setLabelFont(font);  
        piePlot.setLabelBackgroundPaint(new Color(255, 255, 255));
        piePlot.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        piePlot.setForegroundAlpha(1.0F);    
        piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {2}")); 
        piePlot.setSectionPaint("新闻", new Color(0, 112, 192));    
        piePlot.setSectionPaint("博客", new Color(146,208,80));    
        piePlot.setSectionPaint("论坛", new Color(192, 80, 77));    
        piePlot.setSectionPaint("微博", new Color(75, 172, 198));    
        piePlot.setStartAngle(10.0); 
        return chartToFile(chart, "distro");
        
	}
    
    private String chartToFile(JFreeChart chart, String type){
    	 try{
    		 String fileName = this.getReportFilePath()+ task.getId()+type+".png";
    		 logger.info("generate charts file name is "+fileName);
         	ChartUtilities.saveChartAsPNG(new File(fileName), chart, 650, 380);  
         	return fileName; 
 		 }catch(Exception e){
 			 logger.info("chart to file fail type "+type,e);
 			 throw new RuntimeException(e);
 		 }
    }

	private String createTrendCountFile(Set<ReportTemplate> reportData) {
    	//1.	网络舆情信息每日舆情走势 纵坐标：新闻类，微博类，论坛类，博客类，微信类所有信息条数总和
		TimeSeriesCollection timeSeriesCollection = getTimeSeriesCollection(reportData);
		JFreeChart chart = createChartPress(timeSeriesCollection, "网络信息监测日走势");  
		return chartToFile(chart, "trend");
	}
	
	@SuppressWarnings("deprecation")
    private TimeSeriesCollection getTimeSeriesCollection(Set<ReportTemplate> reportData){
		//default get previous 2 weeks data 
		 TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        TimeSeries timeseries = new TimeSeries("ffffff", org.jfree.data.time.Day.class);
        Date reportbase= this.task.getReportEndTime();
        if(reportbase==null)
        	reportbase=this.task.getReportStartTime();
        
		for(int m=15;m>0;m--){
			Calendar cal = Calendar.getInstance();
			cal.setTime(reportbase);
			cal.add(Calendar.DAY_OF_MONTH, (0-m));
			Date calStartDate = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			Date calEndDate = cal.getTime();
			long totalcount =0;
			for(ReportTemplate template:reportData){
				long count = dao.findReportLineCount(template, this.task.getProjectId(),
						calStartDate, calEndDate);
				totalcount +=count;
			}
			
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(calStartDate);
			Day days = new Day(cal2.get(Calendar.DAY_OF_MONTH),cal2.get(Calendar.MONTH)+1, cal2.get(Calendar.YEAR));
			
			if(totalcount==0) continue;
			 timeseries.add(days, (int)totalcount); 
			 
			 
		}
		timeSeriesCollection.addSeries(timeseries);
		return timeSeriesCollection;
	}

	@SuppressWarnings("deprecation")
    private  JFreeChart createChartPress(XYDataset xydataset,  
            String title) {  
 
     
          
        if (xydataset != null) {  
            int counts = xydataset.getItemCount(0);  
            if (counts == 0) {  
                xydataset = null;  
            }  
        }  
 
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title, "",  
                "", xydataset, false, false, false);  
        jfreechart.setBackgroundPaint(Color.white);  
        jfreechart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
        	    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 
        TextTitle text = getTextTile(title);
        jfreechart.setTitle(text);  
        jfreechart.setBorderVisible(false);
        
        XYPlot xyplot = jfreechart.getXYPlot();  
        xyplot.setBackgroundPaint(new Color(255, 255, 255));  
        
        ValueAxis vaxis = xyplot.getDomainAxis();  
        vaxis.setAxisLineStroke(new BasicStroke(1.0f)); // 坐标轴粗� 
        vaxis.setAxisLinePaint(new Color(10, 10, 10)); // 坐标轴颜� 
        
        vaxis.setLabelPaint(new Color(10, 10, 10)); // 坐标轴标题颜� 
        vaxis.setLowerMargin(0.06d);// 分类轴下（左）边� 
        vaxis.setUpperMargin(0.14d);// 分类轴下（右）边�防止最后边的一个数据靠近了坐标轴� 
          
        //X轴为日期格式，这里是专门的处理日期的类，  
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");  
        DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();  
        dateaxis.setTickUnit(new DateTickUnit(DateTickUnit.DAY, 1, format));  
        dateaxis.setVerticalTickLabels(true); // 设为true表示横坐标旋转到垂直� 
        dateaxis.setTickMarkPosition(DateTickMarkPosition.START);  
 
        ValueAxis valueAxis = xyplot.getRangeAxis();  
        valueAxis.setAutoRange(true);
        valueAxis.setAxisLineStroke(new BasicStroke(1.0f)); // 坐标轴粗� 
        valueAxis.setAxisLinePaint(new Color(10,10, 10)); // 坐标轴颜� 
        valueAxis.setLabelPaint(new Color(10, 10, 10)); // 坐标轴标题颜� 
        (( NumberAxis)valueAxis).setAutoRangeStickyZero(true);
        (( NumberAxis)valueAxis).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        xyplot.setRangeGridlinesVisible(true);  
        xyplot.setDomainGridlinesVisible(false);  
        xyplot.setRangeGridlinePaint(Color.LIGHT_GRAY);  
        xyplot.setRangeGridlineStroke(new BasicStroke(1.0f));
        
        
        
        xyplot.setNoDataMessageFont(new Font("", Font.BOLD, 14));//字体的大小，粗体� 
        xyplot.setNoDataMessagePaint(new Color(87, 149, 117));//字体颜色  
        xyplot.setAxisOffset(new RectangleInsets(0d, 0d, 0d, 5d)); //  
 
     
      
 
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot  
                .getRenderer();  
        //第一条折线的颜色  
        xylineandshaperenderer.setBaseItemLabelsVisible(false);  
        xylineandshaperenderer.setSeriesFillPaint(0, new Color(51, 102, 255));  
        xylineandshaperenderer.setSeriesPaint(0, new Color(51, 102, 255));  
 
      
    
        //折线的粗细调  
        StandardXYToolTipGenerator xytool = new StandardXYToolTipGenerator();  
        xylineandshaperenderer.setToolTipGenerator(xytool);  
        xylineandshaperenderer.setStroke(new BasicStroke(1.5f));  
   
 
        return jfreechart;  
	}

	private TextTitle getTextTile(String title) {
		// 设置标题的颜�
		TextTitle text = new TextTitle(title);
		text.setPaint(new Color(255, 255, 255));
		text.setBackgroundPaint(new Color(0, 112, 192));
		text.setExpandToFitSpace(true);
		
		text.setFont(new Font("", Font.BOLD, 18));
		return text;
	}

	private void writeModule(CustomXWPFDocument doc, ReportTemplate template, List<ReportLine> lines) {
		XWPFParagraph nameParagraph = doc.createParagraph();
		nameParagraph.setStyle("Heading2");
		XWPFRun moduleName = nameParagraph.createRun();
		moduleName.setBold(false);
		moduleName.setFontSize(14);
		moduleName.setText(template.getClassified());
		
		// weibo direct related handling structure
		final boolean needTopicAggregation = 微博类模板.equals(template.getTemplate_type())
		        && (微博直发.equals(template.getClassified()) || 达人直发.equals(template.getClassified()));
		Map<String, List<ReportLine>> weiboDirectByTopic = null;

		logger.info(String.format(" for report tempalte %s, classified %s, size are %s", template.getTemplate_type(),
				template.getClassified(),lines.size()));

		// table
		List<String> headers = new ArrayList<String>(template.getColumnHeaders());
		List<Integer> widths = new ArrayList<Integer>(headers.size());
		int tableWidth = TABLE_MAX_WIDTH;
		{
			// detect non table column, remove from table columns
			if (needTopicAggregation && (headers.indexOf(所属话题) >= 0)) {
				headers.remove(所属话题);
				// aggregate the report line based on 所属话题
				weiboDirectByTopic = aggregateWeiboDirectByTopic(template, lines);
			}
			// remove image column
			if (headers.indexOf(图片) >= 0) {
				headers.remove(图片);
			}
			// remove image column
			if (headers.indexOf(截图) >= 0) {
				headers.remove(截图);
			}
			tableWidth = negotiateHeaderWidth(headers, widths);
			logger.info(String.format(" Width array for template %s, classified %s, headers are: %s, widths: %s ",
			        template.getTemplate_type(), template.getClassified(), headers.toString(), widths.toString()));
		}
		
		if (weiboDirectByTopic == null) {
			XWPFParagraph labelParagraph = doc.createParagraph();
			XWPFRun moduleNumber = labelParagraph.createRun();
			moduleNumber.setFontSize(10);
			writeModuleTable(doc, template, lines, moduleNumber, headers, widths, tableWidth);
		} else {
			// multiple tables when the lines are aggregated
			for (Entry<String, List<ReportLine>> topicEntry : weiboDirectByTopic.entrySet()) {
				XWPFParagraph topicParagraph = doc.createParagraph();
				topicParagraph.setStyle("Heading3");
				XWPFRun topicRun = topicParagraph.createRun();
				topicRun.setText(所属话题 + " : " + topicEntry.getKey());
				topicRun.setFontSize(12);
				XWPFParagraph labelParagraph = doc.createParagraph();
				XWPFRun moduleNumber = labelParagraph.createRun();
				moduleNumber.setFontSize(10);
				writeModuleTable(doc, template, topicEntry.getValue(), moduleNumber, headers, widths, tableWidth);
			}
		}
		writeSectionPageBreak(doc, tableWidth > TABLE_MAX_WIDTH);
	}

	private void writeModuleTable(CustomXWPFDocument doc,
			ReportTemplate template, List<ReportLine> lines,
			XWPFRun moduleNumber, List<String> headers, List<Integer> widths, int tableWidth) {

		XWPFTable table = doc.createTable(lines.size() + 1, headers.size());
		CTTbl ctTbl = table.getCTTbl();
		CTTblPr tblPr = ctTbl.getTblPr();
		if (tblPr == null) {
			tblPr = ctTbl.addNewTblPr();
		}
		CTTblGrid grid = ctTbl.addNewTblGrid();
		for (Integer i : widths) {
			CTTblGridCol col = grid.addNewGridCol();
			col.setW(BigInteger.valueOf(i));
		}
		CTTblLayoutType layout = tblPr.addNewTblLayout();
		layout.setType(STTblLayoutType.FIXED);
		CTTblWidth width = tblPr.getTblW();
		if (width == null) {
			width = tblPr.addNewTblW();
		}
		width.setType(STTblWidth.DXA);
		width.setW(BigInteger.valueOf(tableWidth));

		XWPFTableRow headRow = table.getRow(0);
		List<XWPFTableCell> headerCells = headRow.getTableCells();
		for (int i = 0; i < headers.size(); i++) {
		    final XWPFTableCell headerCell = headerCells.get(i);
			{
				// center based sort
                XWPFParagraph graph = getOrCreateParaGraph(headerCell);
				graph.setAlignment(ParagraphAlignment.CENTER);
				XWPFRun run = graph.createRun();
				run.setText(headers.get(i));
				run.setFontSize(IN_TABLE_FONT_SIZE);
				CTTblWidth colWidth = headerCell.getCTTc().addNewTcPr().addNewTcW();
				colWidth.setW(BigInteger.valueOf(widths.get(i)));
				colWidth.setType(STTblWidth.DXA);
			}
		}
		int splitnumber = 160/headerCells.size();
		if(splitnumber>40) {
			splitnumber=40;
		} else if(splitnumber<20) {
			splitnumber=20;
		}
		// set value to the table cells
		// a map from image path to URL
		HashMap<String, String> imagePaths = new HashMap<String, String>();
		// a map from image path to date
		HashMap<String, String> imageDates = new HashMap<String, String>();
		final String[] IMAGE_FIELDS = new String[] { "链接", "日期" };
		int link_index = headers.indexOf(IMAGE_FIELDS[0]);
		int date_index = headers.indexOf(IMAGE_FIELDS[1]);
		final int LINE_SIZE = lines.size();
		int number_1=0;
		int number_2=0;
		int number_3=0;
		int number_4=0;
		int number_5 = 0; // click number
		for (int j = 0; j < LINE_SIZE; j++) {
			ReportLine line = lines.get(j);
			XWPFTableRow row = table.getRow(j+1);
			// fill one line
			for (int i = 0; i < headers.size(); i++) {
				final String headerName = headers.get(i);
                final Object obj = line.getColumns().get(headerName);
				if (obj != null) {
					String body = obj.toString();
					logger.info("header column name: " + headerName + " , value is: " + body);
                    if ("链接".equalsIgnoreCase(headerName)) {
						String displayText = body;
						if(body.length()>splitnumber){
						  String part1=body.substring(0,splitnumber);
						  part1+="\r\n";
						  String part2 = body.substring(splitnumber);
						  if(part2.length()>splitnumber){
							  String part2_1=part2.substring(0,splitnumber);
							  part2_1+="\r\n";
							  String part2_2 = part2.substring(splitnumber);
							  displayText = part1+part2_1+part2_2;
						  }else{
							  displayText = part1 + part2;
						  }
						} else {
							displayText = body;
						}
						// blocks to add hyper link
						{
							try {
								// 1. add a XWPFHyperlink to the doc. This code from @seeAlso XWPFDocument.initHyperlinks()
								String hypwLinkId = doc.getPackagePart().addExternalRelationship(body, XWPFRelation.HYPERLINK.getRelation()).getId();
								
								XWPFHyperlink link = new XWPFHyperlink(hypwLinkId, body);
								doc.addHyperLink(link);
	
								// 2. add a hyper link run at the right place.
								List<XWPFParagraph> graphs = row.getCell(i).getParagraphs();
								XWPFParagraph graph = null;
								if (graphs != null && graphs.size() >= 0) {
									graph = graphs.get(0);
								} else {
									graph = row.getCell(i).addParagraph();
								}
								CTHyperlink ctLink = graph.getCTP().addNewHyperlink();
								CTR ctr = ctLink.addNewR();// CRITICAL :: Need the CTR add on CTHyperLink to be used for the following linkRun. 
								XWPFHyperlinkRun linkRun = new XWPFHyperlinkRun(ctLink, ctr, graph);
								linkRun.setHyperlinkId(link.getId());
								linkRun.setText(displayText);
								linkRun.setFontSize(IN_TABLE_FONT_SIZE);
								linkRun.setItalic(true);
								graph.addRun(linkRun);
							} catch (Exception e) {
								logger.error("Write link failed with exception, set as normal link, exception is : ", e);
								row.getCell(i).setText(displayText);
							}
						}
                    } else {
                        XWPFParagraph graph = getOrCreateParaGraph(row.getCell(i));
                        if (!NON_CENTER_ALIGN_COLUMN.contains(headerName)) {
                            graph.setAlignment(ParagraphAlignment.CENTER);
                        }
						XWPFRun run = graph.createRun();
						run.setText(body);
						run.setFontSize(IN_TABLE_FONT_SIZE);
					}

					// accumulate total for numbers column
                    if("转发数".equalsIgnoreCase(headerName)){
                    	try{
                    		int m = Integer.parseInt(body);
                    		number_1+=m;
                    	}catch(Exception e){
                    		logger.error("parse number error "+body+e.getMessage());
                    	}
                    }else if ("评论数".equalsIgnoreCase(headerName)){
                    	try{
                    		int m = Integer.parseInt(body);
                    		number_2+=m;
                    	}catch(Exception e){
                    		logger.error("parse number error "+body+e.getMessage());
                    	}
                    }else if ("粉丝数".equalsIgnoreCase(headerName)){
                    	try{
                    		int m = Integer.parseInt(body);
                    		number_3+=m;
                    	}catch(Exception e){
                    		logger.error("parse number error "+body+e.getMessage());
                    	}
                    }else if ("回复数".equalsIgnoreCase(headerName)){
                    	try{
                    		int m = Integer.parseInt(body);
                    		number_4+=m;
                    	}catch(Exception e){
                    		logger.error("parse number error "+body+e.getMessage());
                    	}
                    } else if ("点击数".equalsIgnoreCase(headerName)) {
                    	try {
                    		int m = Integer.parseInt(body);
                    		number_5 +=m;
                    	} catch (Exception e) {
							logger.error("parse number error " + body, e);
						}
                    }
                    
                    
				} else {
					row.getCell(i).setText("");
				}
				logger.info("header column name: " + headerName + " , width is: " + widths.get(i));
				CTTblWidth colWidth = row.getCell(i).getCTTc().addNewTcPr().addNewTcW();
				colWidth.setW(BigInteger.valueOf(widths.get(i)));
				colWidth.setType(STTblWidth.DXA);
			}

			addImagePaths(imagePaths, imageDates, template, IMAGE_FIELDS, link_index, date_index, line);
			
		}
		String msg = "共计"+lines.size()+"条记录";
		if(number_1>0){
			msg+=";转发数:"+number_1;
		}
		if(number_2>0){
			msg+=";评论数:"+number_2;
		}
		if(number_3>0){
			msg+=";粉丝数:"+number_3;
		}
		if(number_4>0){
			msg+=";回复数:"+number_4;
		}
		if (number_5>0) {
			msg+=";点击数:"+number_5;
		}
		moduleNumber.setText(msg);
		// picture
		logger.info(String.format(" for report tempalte %s, classified %s,  report image paths are %s", template.getTemplate_type(),
				template.getClassified(), StringUtils.join(imagePaths, "\n")));
		writeModuleImage(doc, imagePaths, imageDates);
	}

    private XWPFParagraph getOrCreateParaGraph(final XWPFTableCell cell) {
        List<XWPFParagraph> graphs = cell.getParagraphs();
        XWPFParagraph graph = null;
        if (graphs != null && graphs.size() >= 0) {
            graph = graphs.get(0);
        } else {
            graph = cell.addParagraph();
        }
        return graph;
    }

	private Map<String, List<ReportLine>> aggregateWeiboDirectByTopic(ReportTemplate template, List<ReportLine> lines) {
		Map<String, List<ReportLine>> aggregates = new HashMap<String, List<ReportLine>>();
		for (ReportLine line : lines) {
			String topic = (String) line.getColumns().get(所属话题);
			List<ReportLine> topicLines = new ArrayList<ReportLine>();
			if (aggregates.containsKey(topic)) {
				topicLines = aggregates.get(topic);
			} else {
				topicLines = new ArrayList<ReportLine>();
				aggregates.put(topic, topicLines);
			}
			topicLines.add(line);
		}
		return aggregates;
	}

	private int negotiateHeaderWidth(List<String> headers, List<Integer> widths) {
		int base = 1060;// caoliu!!
		int tableWidth = TABLE_MAX_WIDTH; // preserve some over-head
		int fixColNumber = 0;
		int totalFixColWidth = 0;
		int dynamicColCharNum = 0;
		int minDynamicColCharNum = -1;
		for (int i = 0; i < headers.size(); i++) {
			if (FIXED_COLUMN_WIDTH.containsKey(headers.get(i))) {
				fixColNumber++;
				totalFixColWidth += FIXED_COLUMN_WIDTH.get(headers.get(i));
			} else {
				int headerWidth = headers.get(i).length();
                dynamicColCharNum += headerWidth;
                if (minDynamicColCharNum < 0 || minDynamicColCharNum > headerWidth) {
                    minDynamicColCharNum = headerWidth;
                }
			}
		}
		if ((totalFixColWidth + base * (headers.size() - fixColNumber)) > TABLE_MAX_WIDTH) {
			tableWidth = LANDSACPE_TABLE_MAX_WIDTH;
		}
		int leftWidth = tableWidth - totalFixColWidth;
		if (leftWidth <= 0) {
			logger.error("Negotiate header width encounter the sum(fix-column-width) bigger than table width! Header are : " + headers.toString() + 
					" Predefined width maps are : " + FIXED_COLUMN_WIDTH.toString());
		}
		if ((headers.size() - fixColNumber) > 0) {
			base = leftWidth / (headers.size() - fixColNumber);
		}
		for (int i = 0; i < headers.size(); i++) {
			if (FIXED_COLUMN_WIDTH.containsKey(headers.get(i))) {
				widths.add(FIXED_COLUMN_WIDTH.get(headers.get(i)));
			} else if (dynamicColCharNum > 0) {
				int width = (leftWidth * headers.get(i).length()) / dynamicColCharNum;
				if (width < 50) {
					width = 50;// minimum
				}
				widths.add(width);
			} else {
				// default case
				widths.add(base);
			}
		}
		
		logger.info("negotiated header column : " + headers + " , widths are : " + widths);		
		return tableWidth;
	}

	/*
	 * Add the images paths to the images paths set.
	 */
	private void addImagePaths(HashMap<String, String> imagePaths,
			HashMap<String, String> imageDate, ReportTemplate template, final String[] IMAGE_FIELDS, int link_index, int date_index,
			ReportLine line) {
		if (Dao.SUMMARY_TEMPLATE_TYPE.equals(template.getTemplate_type())) {
			final String IMAGE_FIELD_NAME = 截图;
			// summary doesn't have link, but the line has an screenshot field. Use this field value for the file path
			Object path = line.getColumns().get(IMAGE_FIELD_NAME);
			if ( path != null && !path.toString().isEmpty()) {
				imagePaths.put(path.toString(), null);
				imageDate.put(path.toString(), null);
			}
		} else {
			// find matched image for each line if any by matching the url and
			// the date
			if (link_index >= 0 && date_index >= 0) {
				Object link_obj = line.getColumns().get(IMAGE_FIELDS[0]);
				Object date_obj = line.getColumns().get(IMAGE_FIELDS[1]);
				if (link_obj != null && date_obj != null) {
					List<String> path = dao.findImagePathByUrl(
							link_obj.toString(), date_obj.toString());
					for (String p : path) {
					    imagePaths.put(p, link_obj.toString());
                        imageDate.put(p, date_obj.toString());
					}
				}
			}
		}
	}

	private void writeModuleImage(CustomXWPFDocument doc, HashMap<String, String> imagePaths, HashMap<String, String> imageDates) {
		logger.info("start write image for module... ");
		String prefix = System.getenv("PJM_HOME");
		if (prefix == null)
			prefix = System.getProperty("PJM_HOME");
		if (prefix == null)
			prefix = " ../../pjm2/";
		
		System.out.println("prefix is " + prefix);
		for (String combinedPath : imagePaths.keySet()) {
			logger.info(" find a combinbed image path: " + combinedPath);
			if (combinedPath == null ){
				continue;
			}
			String[] paths = StringUtils.split(combinedPath, ";");
			
			for (String path : paths) {
				FileInputStream fis = null;
				// detect extension
				int index = path.lastIndexOf('.');
				int fileType = XWPFDocument.PICTURE_TYPE_PNG;
				if (index >= 0) {
					String ext = path.substring(index);
					ext = ext.toUpperCase();
					if (ext.endsWith("JPEG")) {
						fileType = XWPFDocument.PICTURE_TYPE_JPEG;
					} else if (ext.endsWith("BMP")) {
						fileType = XWPFDocument.PICTURE_TYPE_BMP;
					} else if (ext.endsWith("GIF")) {
						fileType = XWPFDocument.PICTURE_TYPE_GIF;
					}
				}
				
				path = prefix + "/" + path;
				logger.info(" full image file_path is " + path);
				try {
					fis = new FileInputStream(path);
					String relationId=doc.addPictureData(fis, fileType);
					int imageid = doc.getAllPictures().size()-1;
					if(picturesMap.get(relationId)==null){
						picturesMap.put(relationId, imageid);
					}else{
						imageid = picturesMap.get(relationId);
					}
					
					Size size = getImageSize(path);
					logger.info(" relationId is " + relationId+" imageId="+imageid);
					doc.createPicture(imageid, size.hor, size.ver);
        // add image url and date
        createImageUrlRun(doc, imagePaths.get(combinedPath), imageDates.get(combinedPath));
				} catch (Throwable t) {
					logger.error("write module image failed. Path is " + path,
							t);
				} finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (Exception e) {
							// ignore
							logger.warn("close failure", e);
						}
					}
				}
			}
		}
    }
	private static class Size {
		int hor;
		int ver;
		Size(int hor, int ver) {
			this.hor = hor;
			this.ver = ver;
		}
	}
	private Size getImageSize(String path) {
		try {
			File picture = new File(path);
			BufferedImage sourceImg = ImageIO.read(new FileInputStream(picture));
			int width = sourceImg.getWidth();
			int height = sourceImg.getHeight();
			if (sourceImg.getWidth() > IMAGE_MAX_WIDTH) {
				width = IMAGE_MAX_WIDTH;
			}
			height = sourceImg.getHeight() * width / sourceImg.getWidth();
			return new Size(width, height);
		}catch (Exception e) {
			logger.error(" check image size error, use default : " + CHART_WIDTH + ", " + CHART_HEIGHT + "!", e);
			return new Size(CHART_WIDTH, CHART_HEIGHT);
		}
	}

    private void createImageUrlRun(CustomXWPFDocument doc, String url, String date) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        // 1. add a XWPFHyperlink to the doc. This code from @seeAlso XWPFDocument.initHyperlinks()
        XWPFHyperlink link = null;
        try {
            String hypwLinkId = doc.getPackagePart().addExternalRelationship(url, XWPFRelation.HYPERLINK.getRelation()).getId();
            link = new XWPFHyperlink(hypwLinkId, url);
            doc.addHyperLink(link);
        } catch (Exception e) {
            logger.error("add image url failed due to url is broken! ", e);
        }
        // 2. add a hyper link run at the right place.
        XWPFParagraph graph = doc.createParagraph();
        XWPFRun urlRun = null;
        if (link != null) {
            CTHyperlink ctLink = graph.getCTP().addNewHyperlink();
            CTR ctr = ctLink.addNewR();// CRITICAL :: Need the CTR add on
                                       // CTHyperLink to be used for the
                                       // following linkRun.
            XWPFHyperlinkRun linkRun = new XWPFHyperlinkRun(ctLink, ctr, graph);
            linkRun.setHyperlinkId(link.getId());
            String displayText = url;
            if (url.length() > 60) {
                displayText = url.substring(0, 60) + "...";
            }
            linkRun.setText(displayText);
            linkRun.setItalic(true);
            urlRun = linkRun;
        } else {
            urlRun = graph.createRun();
            urlRun.setText(url);
        }
        graph.addRun(urlRun);
        //: do we need date?
}

    private Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> shuffle(Map<ReportTemplate, List<ReportLine>> reportData) {
		// {template_type => [ report_template => List<ReportLine> ] }
    	
		Map<String, List<Entry<ReportTemplate, List<ReportLine>>>> sortedData = new TreeMap<String, List<Entry<ReportTemplate, List<ReportLine>>>>(sorter);
		
		for (Entry<ReportTemplate, List<ReportLine>> e :reportData.entrySet()) {
			List<Entry<ReportTemplate, List<ReportLine>>> entries = sortedData.get(e.getKey().getTemplate_type());
			if (entries == null) {
				entries = new LinkedList<Map.Entry<ReportTemplate,List<ReportLine>>>();
				sortedData.put(e.getKey().getTemplate_type(), entries);
			}
			entries.add(e);
		}
		
		// sort
		for (Entry<String, List<Entry<ReportTemplate, List<ReportLine>>>> e : sortedData.entrySet()) {
			Collections.sort(e.getValue(), new ReportComparator());
		}
		
		return sortedData;
	}
	
	private static class ReportComparator implements Comparator<Entry<ReportTemplate, List<ReportLine>>> {
		public int compare(Entry<ReportTemplate, List<ReportLine>> object1, Entry<ReportTemplate, List<ReportLine>> object2) {
			return ObjectUtils.compare(object1.getKey().getPosition(), object2.getKey().getPosition());
		}
	}

	public static String parseDateValue(Date d) {
		if (d == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
	}
	
	
    public static void main(String[] args) {
//    	
//    	DaoImpl daotest = new DaoImpl();
//    	ReportTask task = new ReportTask();
//    	task.setReportStartTime(new Date());
//    	task.setProjectId(1l);
//    	task.setProjectName("null");
//    	task.setId(2l);
//    	ReportPOIWriter writer = new ReportPOIWriter(daotest, task);
//    	Set<ReportTemplate> templates = new HashSet<ReportTemplate>();
//    	templates.add(new ReportTemplate());
//    	templates.add(new ReportTemplate());
//    	Map<ReportTemplate, List<ReportLine>> reportData = new HashMap<ReportTemplate, List<ReportLine>>();
//    	{
//    		// news
//    		ReportTemplate reportTemplate = new ReportTemplate();
//    		reportTemplate.setClassified("新闻稿发布");
//    		reportTemplate.setTemplate_type(Dao.NEWS_TEMPLATE_TYPE);
//    		reportTemplate.setColumnHeaders(new ArrayList<String>());
//    		reportTemplate.getColumnHeaders().add("发布平台");
//    		reportTemplate.getColumnHeaders().add("链接");
//    		reportTemplate.getColumnHeaders().add("日期");
//    		reportTemplate.getColumnHeaders().add("标题");
//    		reportTemplate.getColumnHeaders().add("推荐位置");
//    		reportTemplate.getColumnHeaders().add("排名 ");
//    		reportTemplate.getColumnHeaders().add("转发数 ");
//    		reportTemplate.getColumnHeaders().add("粉丝数");
//    		reportTemplate.getColumnHeaders().add("评论数");
//    		reportTemplate.getColumnHeaders().add("热门微博排名");
//    		reportTemplate.getColumnHeaders().add("点赞数");
//    		List<ReportLine> line1 = new ArrayList<ReportLine>();
//        	for(int m=0;m<80;m++){
//        		ReportLine line =  new ReportLine();
//        		line.setColumns(new HashMap<String, Object>());
//        		line.getColumns().put("发布平台", "人民网");
//        		line.getColumns().put("链接", "http://www.dzwww.com/yule/yulezhuanti/mtcbg/201402/t20140211_9375824.htm");
//        		line.getColumns().put("日期", "2013-12-12");
//        		line.getColumns().put("标题", "《汉字英雄》第二季苦情学霸张政竟敢讽刺马东？这是作死的节奏吗？");
//        		line.getColumns().put("推荐位置", "首页");
//        		line.getColumns().put("转发数", "1234567");
//        		line.getColumns().put("粉丝数", "12323312");
//        		line.getColumns().put("热门微博排名", "首页");
//        		line.getColumns().put("点赞数", "2121");
//        		
//        		line1.add(line);	
//        	}
//        	for(int m=0;m<60;m++){
//        		ReportLine line =  new ReportLine();
//        		line.setColumns(new HashMap<String, Object>());
//        		line.getColumns().put("发布平台", "搜狐");
//        		line1.add(line);	
//        	}
//        	for(int m=0;m<40;m++){
//        		ReportLine line =  new ReportLine();
//        		line.setColumns(new HashMap<String, Object>());
//        		line.getColumns().put("发布平台", "网易");
//        		line1.add(line);	
//        	}
//        	for(int m=0;m<30;m++){
//        		ReportLine line =  new ReportLine();
//        		line.setColumns(new HashMap<String, Object>());
//        		line.getColumns().put("发布平台", "凤凰");
//        		line1.add(line);	
//        	}
//        	for(int m=0;m<10;m++){
//        		ReportLine line =  new ReportLine();
//        		line.setColumns(new HashMap<String, Object>());
//        		line.getColumns().put("发布平台", "新浪");
//        		line1.add(line);	
//        	}
//        	
//        	for(int m=0;m<10;m++){
//        		ReportLine line =  new ReportLine();
//        		line.setColumns(new HashMap<String, Object>());
//        		line.getColumns().put("发布平台", "吉祥");
//        		line1.add(line);	
//        	}
//        	reportData.put(reportTemplate, line1);
//        	
//    	}
//    	
//    	{
//    		// blog
//    		ReportTemplate reportTemplate = new ReportTemplate();
//    		reportTemplate.setTemplate_type(Dao.BLOG_TEMPLATE_TYPE);
//    		reportTemplate.setClassified("名人博客");
//    		List<ReportLine> line1 = new ArrayList<ReportLine>();
//        	for(int m=0;m<300;m++){
//        		line1.add(new ReportLine());	
//        	}
//        	reportData.put(reportTemplate, line1);
//        	
//    	}
//    	
//    	{
//    		// forum
//    		ReportTemplate reportTemplate = new ReportTemplate();
//    		reportTemplate.setTemplate_type(Dao.FORUM_TEMPLATE_TYPE);
//    		reportTemplate.setClassified("论坛");
//    		List<ReportLine> line1 = new ArrayList<ReportLine>();
//        	for(int m=0;m<200;m++){
//        		line1.add(new ReportLine());	
//        	}
//        	reportData.put(reportTemplate, line1);
//        	
//    	}
//    	{
//    		// twitter
//    		ReportTemplate reportTemplate = new ReportTemplate();
//    		reportTemplate.setTemplate_type(Dao.WEIBO_TEMPLATE_TYPE);
//    		reportTemplate.setClassified("微博直发");
//    		List<ReportLine> line1 = new ArrayList<ReportLine>();
//        	for(int m=0;m<2500;m++){
//        		line1.add(new ReportLine());	
//        	}
//        	reportData.put(reportTemplate, line1);
//        	
//    	}
//    	
//    	writer.write(reportData);
    	try{
    		CustomXWPFDocument doc = new CustomXWPFDocument(ReportPOIWriter.class.getResourceAsStream("/Template.docx"));
    		FileInputStream fis=null;
    		try {
    			String path="c:\\number_4.png";
    			fis = new FileInputStream(path);
				doc.addPictureData(fis, XWPFDocument.PICTURE_TYPE_PNG);
				doc.createPicture(doc.getAllPictures().size() - 1,
						CHART_WIDTH, CHART_HEIGHT);
				
			} catch (Throwable t) {
				logger.error("write module image failed. Path is " ,
						t);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception e) {
						// ignore
						logger.warn("close failure", e);
					}
				}
			}
			
			try {
    			String path="c:\\number_4.png";
    			fis = new FileInputStream(path);
				doc.addPictureData(fis, XWPFDocument.PICTURE_TYPE_PNG);
				doc.createPicture(doc.getAllPictures().size() - 1,
						CHART_WIDTH, CHART_HEIGHT);
				
			} catch (Throwable t) {
				logger.error("write module image failed. Path is " ,
						t);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception e) {
						// ignore
						logger.warn("close failure", e);
					}
				}
			}
			
			try {
    			String path="c:\\number_3.png";
    			fis = new FileInputStream(path);
				doc.addPictureData(fis, XWPFDocument.PICTURE_TYPE_PNG);
				doc.createPicture(doc.getAllPictures().size() - 1,
						CHART_WIDTH, CHART_HEIGHT);
				
			} catch (Throwable t) {
				logger.error("write module image failed. Path is " ,
						t);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception e) {
						// ignore
						logger.warn("close failure", e);
					}
				}
			}
			
			FileOutputStream out = null;
			try {
				
	            out = new FileOutputStream("c:\\11.docx");
				doc.write(out);
				out.flush();
			}
			catch(Exception e){
				e.printStackTrace();
			}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
    
    public static  class DaoImpl extends Dao{
    	
    	@Override
    	public long findReportLineCount(ReportTemplate template,
    			long pid, Date startTime, Date endTime) {
    		long mili = System.currentTimeMillis();
    		return  (mili%1000) ;
    		
    	}
    }

}  
