package uk.co.azquelt.slackstacker;

import java.util.Calendar;
import java.util.Map;

public class State {
	
	/**
	 * The cutoff time for remembering which questions we have seen.
	 * <p>
	 * We assume that we have seen any question which was created before this
	 * time. For any question created after this time, we need to consult
	 * {@link #questionsSeen}
	 */
	public Calendar questionsSeenCutoff;
	
	/**
	 * A map of ID to created date for all questions we have seen which were
	 * created after the {@link #questionsSeenCutoff}
	 */
	public Map<String, Calendar> questionsSeen;
	
	/**
	 * If we've been told to back off, this is the time we are next allowed to run again
	 */
	public Calendar backoffUntil;
	
}
