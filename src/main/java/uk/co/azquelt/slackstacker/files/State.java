package uk.co.azquelt.slackstacker.files;

import java.util.Calendar;
import java.util.List;

import uk.co.azquelt.slackstacker.versionedFile.VersionedFormat;

public class State extends VersionedFormat {
	
	/**
	 * The time that we last successfully retrieved and posted questions
	 */
	public Calendar lastUpdated;
	
	/**
	 * The IDs we saw last time we requested questions (to avoid posting the same ones twice if there's any overlap)
	 */
	public List<String> idsSeen;
	
	/**
	 * If we've been told to back off, this is the time we are next allowed to run again
	 */
	public Calendar backoffUntil;

}
