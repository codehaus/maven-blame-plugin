package org.codehaus.mojo.blame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class FindBugsReportUpdater extends AbstractReportUpdater {

	/**
	 * @param authorReader the svn author reader
	 * @param logger
	 */
	public FindBugsReportUpdater( final SVNAuthorReader authorReader, final Log logger ) {
		super(authorReader, logger );
	}

	/**
	 * @see org.codehaus.mojo.blame.AbstractReportUpdater#updateReport(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public void updateReport( final String pFindBugsReportFile, final String pEncoding ) throws Exception {

		getLog().info("Blamizing findbugs report...");

		File oFindBugsFile = new File( pFindBugsReportFile);
		if (oFindBugsFile.exists() == false) {
			getLog().error(
					"can't find findbugs result file : "
							+ oFindBugsFile.getAbsolutePath());
		} else {
			SAXReader oSAXReader = new SAXReader();
			InputStream oFileInputStream = new FileInputStream(oFindBugsFile);
			try {
				InputStreamReader oInputStreamReader = new InputStreamReader(
						oFileInputStream, pEncoding );
				try {
					Document xFindBugs = oSAXReader.read(oInputStreamReader);
					String sSourceDir = xFindBugs.getRootElement().element(
							"Project").elementText("SrcDir");
					List<Element> listFiles = xFindBugs.getRootElement()
							.elements("file");
					for (Element xFile : listFiles) {
						String sClassName = xFile.attributeValue("classname");
						String sFileName = sSourceDir + '/'
								+ sClassName.replace('.', '/') + ".java";
						getLog().debug(sFileName);
						List<Element> listBugInstances = xFile
								.elements("BugInstance");
						for (Element xBugInstance : listBugInstances) {
							int iLineNumber = Integer.parseInt(xBugInstance
									.attributeValue("lineNumber"));
							String sAuthor = getAuthor(sFileName, iLineNumber);
							xBugInstance.addAttribute("author", sAuthor);
						}
					}
					OutputFormat xFormat = OutputFormat.createPrettyPrint();
					XMLWriter xWriter = new XMLWriter(new FileWriter(
							oFindBugsFile), xFormat);
					try {
						xWriter.write(xFindBugs);
					} finally {
						xWriter.close();
					}
				} finally {
					oInputStreamReader.close();
				}
			} finally {
				oFileInputStream.close();
			}
		}
	}
}
