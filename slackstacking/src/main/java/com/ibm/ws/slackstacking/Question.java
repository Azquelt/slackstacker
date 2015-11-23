package com.ibm.ws.slackstacking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {

	public String question_id;
	public String link;
	public String title;
}
