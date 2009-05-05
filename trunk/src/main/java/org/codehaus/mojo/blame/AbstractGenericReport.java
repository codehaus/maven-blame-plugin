package org.codehaus.mojo.blame;

import java.io.File;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;

/**
 * @author Michenux Generic Maven Report
 */
public abstract class AbstractGenericReport extends AbstractMavenReport {

	/**
	 * <i>Maven Internal</i>: The Project descriptor.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * <i>Maven Internal</i>: The Doxia Site Renderer.
	 * 
	 * @component
	 */
	private Renderer siteRenderer ;
	
	/**
	 * The output directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;
	
	/**
	 * outputEncoding
	 * 
	 * @parameter expression="${outputEncoding}"
	 *            default-value="${project.reporting.outputEncoding}"
	 */
	private String outputEncoding;
	
	/**
	 * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
	 * @return MavenProject
	 */
	protected final MavenProject getProject() {
		return project ;
	}
	
	/**
	 * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
	 * @return Renderer
	 */
	protected final Renderer getSiteRenderer() {
		return siteRenderer;
	}
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
	 * @return String the output directory
	 */
	protected final String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	/**
	 * @return
	 */
	public String getOutputEncoding() {
		if ( this.outputEncoding == null)
			this.outputEncoding = "UTF-8";
		return this.outputEncoding ;
	}
}
