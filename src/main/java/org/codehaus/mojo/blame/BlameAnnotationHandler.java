package org.codehaus.mojo.blame;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;
import org.tmatesoft.svn.core.wc.ISVNOptions;

public class BlameAnnotationHandler implements ISVNAnnotateHandler {

	// private boolean myIsUseMergeHistory;
	// private boolean myIsVerbose;
	// private ISVNOptions myOptions;
	private Map<String, String> authorPerLine;
	private Map<String, Integer> lineCountPerDev;

	public BlameAnnotationHandler(boolean useMergeHistory, boolean verbose,
			ISVNOptions options, Map<String, Integer> pLineCountPerDev) {
		// myIsUseMergeHistory = useMergeHistory;
		// myIsVerbose = verbose;
		// myOptions = options;
		this.authorPerLine = new HashMap<String, String>();
		this.lineCountPerDev = pLineCountPerDev ;
	}

	/**
	 * Deprecated.
	 */
	public void handleLine(Date date, long revision, String author, String line)
			throws SVNException {
		handleLine(date, revision, author, line, null, -1, null, null, 0);
	}

	/**
	 * Formats per line information and prints it out to the console.
	 */
	public void handleLine(Date date, long revision, String p_sAuthor,
			String line, Date mergedDate, long mergedRevision,
			String mergedAuthor, String mergedPath, int p_iLineNumber)
			throws SVNException {

		// String mergedStr = "";
		// if (myIsUseMergeHistory) {
		// if (revision != mergedRevision) {
		// mergedStr = "G ";
		// } else {
		// mergedStr = "  ";
		// }
		//
		// date = mergedDate;
		// revision = mergedRevision;
		// author = mergedAuthor;
		// }

		this.authorPerLine.put(Integer.toString(p_iLineNumber), p_sAuthor);
		
		if ( this.lineCountPerDev != null ) {
			Integer lineCount = lineCountPerDev.get(p_sAuthor);
			if ( lineCount == null )
				this.lineCountPerDev.put(p_sAuthor, 1);
			else
				this.lineCountPerDev.put(p_sAuthor, ++lineCount);
		}
	}

	/* (non-Javadoc)
	 * @see org.tmatesoft.svn.core.wc.ISVNAnnotateHandler#handleRevision(java.util.Date, long, java.lang.String, java.io.File)
	 */
	public boolean handleRevision(Date date, long revision, String author,
			File contents) throws SVNException {
		/*
		 * We do not want our file to be annotated for each revision of the
		 * range, but only for the last revision of it, so we return false
		 */
		return false;
	}

	/**
	 * 
	 */
	public void handleEOF() {
	}

	/**
	 * @param p_iLineNumber
	 * @return
	 */
	public String getAuthorOfLine(int p_iLineNumber) {
		return (String) authorPerLine.get(Integer.toString(p_iLineNumber));
	}

	/**
	 * @return
	 */
	public Map<String, String> getMapAuthorPerLine() {
		return this.authorPerLine;
	}
}
