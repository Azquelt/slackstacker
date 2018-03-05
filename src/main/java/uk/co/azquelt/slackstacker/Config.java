package uk.co.azquelt.slackstacker;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {
	
	public Map<String, List<String>> tags;
	
	@JsonProperty("slack-webhook-url")
	public String slackWebhookUrl;
	
	@JsonProperty("state-file")
	public String stateFile;
	
	@JsonProperty("stackoverflow-key")
	public String stackoverflowKey;

}
