package com.ibm.ws.slackstacking;

import java.util.Calendar;
import java.util.List;

public class State {
	
	/**
	 * The time that we last successfully retrieved and posted questions
	 */
	public Calendar lastUpdated;
	
	/**
	 * The IDs we saw last time we requested questions (to avoid posting the same ones twice if there's any overlap)
	 */
	public List<String> idsSeen;

}
