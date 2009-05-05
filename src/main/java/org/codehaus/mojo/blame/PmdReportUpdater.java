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

public class PmdReportUpdater extends AbstractReportUpdater {

	/**
	 * @param pSvnAuthorReader
	 * @param pLogger
	 */
	public PmdReportUpdater(SVNAuthorReader pSvnAuthorReader, Log pLogger) {
		super(pSvnAuthorReader, pLogger);
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void updateReport(final String pPmdReportFile, final String pEncoding)
			throws Exception {

		getLog().info("Blamizing pmd report...");

		File oPmdFile = new File(pPmdReportFile);
		if (oPmdFile.exists() == false) {
			getLog().error(
					"can't find pmd result file : "
							+ oPmdFile.getAbsolutePath());
		} else {
			SAXReader oSAXReader = new SAXReader();
			InputStream oFileInputStream = new FileInputStream(oPmdFile);
			try {
				InputStreamReader oInputStreamReader = new InputStreamReader(
						oFileInputStream, pEncoding);
				try {
					Document xPmd = oSAXReader.read(oInputStreamReader);
					List<Element> listElements = xPmd.getRootElement()
							.elements("file");
					for (Element xFile : listElements) {
						String sFileName = xFile.attributeValue("name");
						getLog().debug(sFileName);
						List<Element> listViolations = xFile
								.elements("violation");
						for (Element xError : listViolations) {
							int iBeginLineNumber = Integer.parseInt(xError
									.attributeValue("beginline"));
							// TODO: gérer la liste des auteurs
							String sAuthor = getAuthor(sFileName,
									iBeginLineNumber);
							xError.addAttribute("author", sAuthor);
						}
					}
					OutputFormat xFormat = OutputFormat.createPrettyPrint();
					XMLWriter xWriter = new XMLWriter(new FileWriter(oPmdFile),
							xFormat);
					try {
						xWriter.write(xPmd);
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
