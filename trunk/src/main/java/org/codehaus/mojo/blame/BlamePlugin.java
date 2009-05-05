package org.codehaus.mojo.blame;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.MavenReportException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Blame Plugin
 * 
 * @goal blame
 * @execute phase="compile"
 * @requiresDependencyResolution compile
 * @requiresProject
 */
public class BlamePlugin extends AbstractGenericReport {

	private static final String REPORT_BLAME_DESCRIPTION_KEY = "report.blame.description";

	private static final String REPORT_BLAME_NAME_KEY = "report.blame.name";

	private static final String BLAME_BUNDLE_NAME = "blame-report";

	/**
	 * enableCheckstyle
	 * 
	 * @parameter
	 */
	private boolean enableCheckstyle = true;

	/**
	 * enablePmd
	 * 
	 * @parameter
	 */
	private boolean enablePmd = true;

	/**
	 * enableFindBugs
	 * 
	 * @parameter
	 */
	private boolean enableFindBugs = true;

	/**
	 * sourceDirectory
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 * @readonly
	 */
	private String sourceDirectory;

	/**
	 * checkstyleXmlReport
	 * 
	 * @parameter 
	 *            default-value="${project.build.directory}/checkstyle-result.xml"
	 * @required
	 * @readonly
	 */
	private String checkstyleXmlReport;

	/**
	 * pmdXmlReport
	 * 
	 * @parameter default-value="${project.build.directory}/pmd.xml"
	 * @required
	 * @readonly
	 */
	private String pmdXmlReport;

	/**
	 * findbugsXmlReport
	 * 
	 * @parameter default-value="${project.build.directory}/findbugs.xml"
	 * @required
	 * @readonly
	 */
	private String findbugsXmlReport;

	/**
	 * svnUserName
	 * 
	 * @parameter expression="${maven.scm.username}"
	 * @readonly
	 */
	private String svnUserName;

	/**
	 * svnUserPassword
	 * 
	 * @parameter expression="${maven.scm.password}"
	 * @readonly
	 */
	private String svnUserPassword;

	/**
	 * fullBlame
	 * 
	 * @parameter
	 */
	private boolean fullBlame = false;

	/**
	 * blameXmlReport
	 * 
	 * @parameter default-value="${project.build.directory}/blame.xml"
	 * @required
	 * @readonly
	 */
	private String blameXmlReport;

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void executeReport(Locale pLocale) throws MavenReportException {

		try {
			getLog().info("Blame Plugin");
			getLog().info("full blame = " + this.fullBlame);
			getLog().info("enable checkstyle = " + this.enableCheckstyle);
			getLog().info("enable pmd = " + this.enablePmd);
			getLog().info("enable findbugs = " + this.enableFindBugs);
			getLog().info("source directory = " + this.sourceDirectory);
			getLog().info("svn username = " + this.svnUserName);
			getLog().info("report encoding = " + getOutputEncoding());

			// getLog().info("svn password = " + this.svnUserPassword);

			SVNAuthorReader oSVNAuthorReader = new SVNAuthorReader();
			oSVNAuthorReader.init(this.svnUserName, this.svnUserPassword,
					this.fullBlame, this.sourceDirectory);

			try {
				if (this.enableCheckstyle) {
					CSReportUpdater oCsUpt = new CSReportUpdater(
							oSVNAuthorReader, getLog());
					oCsUpt.updateReport(this.checkstyleXmlReport,
							getOutputEncoding());
				}

				if (this.enablePmd) {
					PmdReportUpdater oPmdUpt = new PmdReportUpdater(
							oSVNAuthorReader, getLog());
					oPmdUpt
							.updateReport(this.pmdXmlReport,
									getOutputEncoding());
				}

				if (this.enableFindBugs) {
					FindBugsReportUpdater oFbUpt = new FindBugsReportUpdater(
							oSVNAuthorReader, getLog());
					oFbUpt.updateReport(this.findbugsXmlReport,
							getOutputEncoding());
				}

				getLog().info("Blamizing terminated.");

				writeReport(oSVNAuthorReader, pLocale);

			} finally {
				oSVNAuthorReader.release();
			}
		} catch (SVNAuthenticationException e) {
			throw new MavenReportException("SVN Authentication failed !", e);
		} catch (Exception e) {
			throw new MavenReportException("Echec du plugin", e);
		}
	}

	/**
	 * @param arg0
	 * @throws IOException
	 * @throws MavenReportException
	 */
	protected void writeReport(SVNAuthorReader pSVNAuthorReader, Locale pLocale)
			throws IOException {
		Sink sink = getSink();
		sink.head();
		sink.title();
		sink.text("Blame Report Plugin");
		sink.title_();
		sink.head_();

		sink.body();
		sink.section1();

		sink.sectionTitle1();
		sink.text("Blame Report Plugin");
		sink.sectionTitle1_();
		sink.lineBreak();
		sink.lineBreak();

		Element xmlRoot = DocumentHelper.createElement("blame-report");
		Document xmlReport = DocumentHelper.createDocument(xmlRoot);
		Element xmlDevs = xmlRoot.addElement("developpers");

		Map<String, Integer> mapSVNAuthorReader = pSVNAuthorReader
				.getMapErrorCountPerDev();
		Map<String, Integer> lineCountPerDev = pSVNAuthorReader
				.getLineCountPerDev();

		if (mapSVNAuthorReader.isEmpty() == false) {

			sink.table();
			sink.tableRow();
			sink.tableHeaderCell();
			sink.text(getBundle(pLocale).getString(
					"report.blame.column.developper"));
			sink.tableHeaderCell_();
			sink.tableHeaderCell();
			sink.text(getBundle(pLocale).getString(
					"report.blame.column.errorcount"));
			sink.tableHeaderCell_();

			if (this.fullBlame) {
				sink.tableHeaderCell();
				sink.text(getBundle(pLocale).getString(
						"report.blame.column.linecount"));
				sink.tableHeaderCell_();
				sink.tableHeaderCell();
				sink.text(getBundle(pLocale).getString(
						"report.blame.column.pourcentoferror"));
				sink.tableHeaderCell_();
			}

			sink.tableRow_();
			long totalErrorCount = 0;
			long totalLineCount = 0;

			for (Entry<String, Integer> entry : mapSVNAuthorReader.entrySet()) {

				String author = entry.getKey();
				Integer errorCount = entry.getValue();
				totalErrorCount += errorCount;

				Element xmlDev = xmlDevs.addElement("developper");
				xmlDev.addAttribute("name", author);
				xmlDev.addAttribute("error-count", errorCount.toString());

				sink.tableRow();
				sink.tableCell();
				sink.text(author);
				sink.tableCell_();
				sink.tableCell();
				sink.text(errorCount.toString());
				sink.tableCell_();
				if (this.fullBlame) {
					Integer lineCount = lineCountPerDev.get(author);
					totalLineCount += lineCount;
					sink.tableCell();
					sink.text(lineCount.toString());
					sink.tableCell_();
					sink.tableCell();

					xmlDev.addAttribute("line-count", Long.toString(lineCount));
					long pourcent = Math.round((double) errorCount / lineCount
							* 100);
					xmlDev.addAttribute("pourcent", Long.toString(pourcent));
					sink.text(pourcent + "%");
					sink.tableCell_();
				}
			}

			xmlDevs.addAttribute("error-count", Long.toString(totalErrorCount));

			sink.tableRow();
			sink.tableHeaderCell();
			sink.text(getBundle(pLocale).getString("report.blame.line.total"));
			sink.tableHeaderCell_();
			sink.tableHeaderCell();
			sink.text(Long.toString(totalErrorCount));
			sink.tableHeaderCell_();
			if (this.fullBlame) {
				sink.tableHeaderCell();
				sink.text(Long.toString(totalLineCount));
				sink.tableHeaderCell_();
				sink.tableHeaderCell();
				xmlDevs.addAttribute("line-count", Long
						.toString(totalLineCount));
				long pourcent = Math.round((double) totalErrorCount
						/ totalLineCount * 100);
				xmlDevs.addAttribute("pourcent", Long.toString(pourcent));
				sink.text(pourcent + "%");
				sink.tableHeaderCell_();
			}
			sink.tableRow_();

			sink.table_();
		}

		sink.lineBreak();
		// makeLinks(sink);
		sink.section1_();
		sink.body_();
		sink.flush();
		sink.close();

		OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
		xmlFormat.setEncoding(getOutputEncoding());
		FileOutputStream fileOutputStream = new FileOutputStream( this.blameXmlReport );
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					fileOutputStream, getOutputEncoding());
			try {
				XMLWriter xmlWriter = new XMLWriter(outputStreamWriter,
						xmlFormat);
				try {
					xmlWriter.write(xmlReport);
				} finally {
					xmlWriter.close();
				}
			} finally {
				outputStreamWriter.close();
			}
		} finally {
			fileOutputStream.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.maven.reporting.AbstractMavenReport#getDescription(java.util
	 * .Locale)
	 */
	public String getDescription(Locale pLocale) {
		return getBundle(pLocale).getString(REPORT_BLAME_DESCRIPTION_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.maven.reporting.AbstractMavenReport#getName(java.util.Locale)
	 */
	public String getName(Locale pLocale) {
		return getBundle(pLocale).getString(REPORT_BLAME_NAME_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#getOutputName()
	 */
	public String getOutputName() {
		return BLAME_BUNDLE_NAME;
	}

	/**
	 * @param p_oLocale
	 * @return
	 */
	private ResourceBundle getBundle(Locale pLocale) {
		return ResourceBundle.getBundle(BLAME_BUNDLE_NAME, pLocale);
	}
}
