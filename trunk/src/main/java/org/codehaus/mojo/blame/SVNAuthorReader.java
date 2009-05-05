package org.codehaus.mojo.blame;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * @author Michenux
 *
 */
public class SVNAuthorReader {

	// SVN Log client to connect to svn server
	private SVNLogClient logClient;

	// Cache of files already blame analysed
	private Map<String, Map<String, String>> cache;

	private Map<String, Integer> mapErrorCountPerDev;
	
	private Map<String, Integer> mapLineCountPerDev ;

	/**
	 * @param pSvnUserName
	 * @param pSvnUserPassword
	 * @throws SVNException 
	 */
	@SuppressWarnings("unchecked")
	public void init(final String pSvnUserName, final String pSvnUserPassword,
			final boolean pFullBlame, String pSourceDirectory ) throws SVNException {
		DAVRepositoryFactory.setup();

		if (pSvnUserName != null && pSvnUserName.length() > 0) {
			this.logClient = SVNClientManager.newInstance(
					SVNWCUtil.createDefaultOptions(true), pSvnUserName,
					pSvnUserPassword).getLogClient();
		} else {
			this.logClient = SVNClientManager.newInstance(
					SVNWCUtil.createDefaultOptions(true)).getLogClient();
		}
		this.cache = new HashMap<String, Map<String, String>>();
		this.mapErrorCountPerDev = new HashMap<String, Integer>();

		if (pFullBlame) {
			
			this.mapLineCountPerDev = new HashMap<String, Integer>();
			
			List<File> listFiles = (List<File>) FileUtils.listFiles(new File(
				pSourceDirectory), new String[] { "java" }, true);
			for( File oFile : listFiles ) {
				blameFile( FilenameUtils.normalize(oFile.getAbsolutePath()));
			}
		}
	}

	/**
	 * 
	 */
	public void release() {
		this.cache.clear();
	}

	/**
	 * @param checkstyleFile
	 * @param pLineNumber
	 * @return
	 */
	protected String getAuthor(String pFileName, int pLineNumber)
			throws Exception {

		String sFileName = FilenameUtils.normalize(pFileName);
		
		Map<String, String> mapLinePerAuthor = this.cache.get(sFileName);
		if (mapLinePerAuthor == null) {
			mapLinePerAuthor = blameFile( sFileName );
		}

		String r_sAuthor = (String) mapLinePerAuthor.get(Integer
				.toString(pLineNumber));
		Integer errorCount = mapErrorCountPerDev.get(r_sAuthor);
		if (errorCount == null) {
			mapErrorCountPerDev.put(r_sAuthor, 1);
		} else
			mapErrorCountPerDev.put(r_sAuthor, ++errorCount);

		return r_sAuthor;
	}
	
	/**
	 * @throws SVNException 
	 * 
	 */
	private Map<String, String> blameFile( String pFileName ) throws SVNException {
		
		BlameAnnotationHandler oBlameAnnotationHandler = new BlameAnnotationHandler(
				false, false, this.logClient.getOptions(), mapLineCountPerDev );
		this.logClient.doAnnotate(new File(pFileName),
				SVNRevision.UNDEFINED, SVNRevision.create(1),
				SVNRevision.HEAD, oBlameAnnotationHandler);

		Map<String, String> mapLinePerAuthor = oBlameAnnotationHandler.getMapAuthorPerLine();
		this.cache.put(pFileName, mapLinePerAuthor);
		
		return mapLinePerAuthor ;
	}

	/**
	 * @return
	 */
	public Map<String, Integer> getMapErrorCountPerDev() {
		return mapErrorCountPerDev;
	}

	/**
	 * @return
	 */
	public Map<String, Integer> getLineCountPerDev() {
		return mapLineCountPerDev;
	}
}
