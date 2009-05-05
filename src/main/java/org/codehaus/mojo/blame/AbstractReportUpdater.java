package org.codehaus.mojo.blame;

import org.apache.maven.plugin.logging.Log;

/**
 * @author Michenux
 *
 */
public abstract class AbstractReportUpdater {

	/**
	 * The author reader of lines in svn
	 */
	private final SVNAuthorReader svnAuthorReader ;
	
	/**
	 * Maven Logger
	 */
	private final Log log ;
	
	/**
	 * @param pSvnAuthorReader
	 * @param logger
	 */
	public AbstractReportUpdater( final SVNAuthorReader pSvnAuthorReader, final Log pLogger ) {
		this.svnAuthorReader = pSvnAuthorReader ;
		this.log = pLogger ;
	}
	
	/**
	 * @param pReportFile report to update
	 * @param pEncoding encoding of the report
	 * @throws Exception
	 */
	public abstract void updateReport( String pReportFile, String pEncoding ) throws Exception ;

	/**
	 * @param pFileName
	 * @param pLineNumber
	 * @return
	 * @throws Exception 
	 */
	protected String getAuthor( String pFileName, int pLineNumber) throws Exception {
		return this.svnAuthorReader.getAuthor( pFileName, pLineNumber );
	}

	/**
	 * @return
	 */
	public Log getLog() {
		return log;
	}
}
