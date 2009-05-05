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

public class CSReportUpdater extends AbstractReportUpdater {


	/**
	 * @param pAuthorReader the svn author reader
	 * @param pLog
	 */
	public CSReportUpdater( final SVNAuthorReader pAuthorReader, final Log pLog ) {
		super(pAuthorReader, pLog );
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void updateReport( final String pCSReportFile, final String pEncoding ) throws Exception {

		getLog().info("Blamizing checkstyle report...");

		File oCheckstyleFile = new File( pCSReportFile );
		if (oCheckstyleFile.exists() == false)
			getLog().error(
					"can't find checkstyle result file : "
							+ oCheckstyleFile.getAbsolutePath());
		else {
			SAXReader oSAXReader = new SAXReader();
			InputStream oFileInputStream = new FileInputStream(oCheckstyleFile);
			try {
				InputStreamReader oInputStreamReader = new InputStreamReader(
						oFileInputStream, pEncoding );
				try {
					Document xCheckStyle = oSAXReader.read(oInputStreamReader);
					List<Element> listElements = xCheckStyle.getRootElement()
							.elements("file");
					for (Element xFile : listElements) {
						String sFileName = xFile.attributeValue("name");
						if (sFileName.endsWith(".java")) {
							getLog().debug(sFileName);
							List<Element> listErrors = xFile.elements("error");
							for (Element xError : listErrors) {
								int iLineNumber = Integer.parseInt(xError
										.attributeValue("line"));
								String sAuthor = getAuthor(sFileName,
										iLineNumber);
								xError.addAttribute("author", sAuthor);
							}
						}
					}
					OutputFormat xFormat = OutputFormat.createPrettyPrint();
					XMLWriter xWriter = new XMLWriter(new FileWriter(
							oCheckstyleFile), xFormat);
					try {
						xWriter.write(xCheckStyle);
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
