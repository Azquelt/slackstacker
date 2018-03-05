package uk.co.azquelt.slackstacker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.cxf.transport.common.gzip.GZIPFeature;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import uk.co.azquelt.slackstacker.slack.SlackMessage;
import uk.co.azquelt.slackstacker.stack.Question;
import uk.co.azquelt.slackstacker.stack.QuestionResponse;

public class SlackStacker {
	
	private static ObjectMapper stateMapper;
	
	private static Client client = ClientBuilder.newBuilder()
			.register(JacksonJsonProvider.class) // Allow us to serialise JSON <-> POJO
			.register(GZIPFeature.class) // Allow us to understand GZIP compressed pages
			.build();

	public static void main(String[] args) throws IOException {
		
		try {
			stateMapper = new ObjectMapper();
			
			CommandLine arguments = CommandLine.processArgs(args);
			
			Config config = loadConfig(arguments.getConfigFile());
			
			State oldState = loadState(config.stateFile);
			Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			
			if (oldState != null) {
				
				if (oldState.backoffUntil != null && now.before(oldState.backoffUntil)) {
					// We've been asked by the StackExchange API to back off, don't run
					return;
				}
				
				State newState = createNewState(now);

				for (Map.Entry<String, List<String>> entry : config.tags.entrySet()) {
					QuestionResponse questions = getQuestions(entry.getKey(), entry.getValue(), config.stackoverflowKey);
					List<Question> newQuestions = filterOldQuestions(questions.items, oldState.lastUpdated, oldState.idsSeen);
					postQuestions(newQuestions, config.slackWebhookUrl);
					addToState(newState, questions.items);

					if (questions.backoff > 0) {
						Calendar backoffUntil = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						backoffUntil.add(Calendar.SECOND, questions.backoff);
						newState.backoffUntil = backoffUntil;
						newState.idsSeen.addAll(oldState.idsSeen);
						break;
					}
				}

				saveState(newState, config.stateFile);
			} else {
				System.out.println("No pre-existing state, setting up default state file");
				State newState = createDefaultState(now);
				saveState(newState, config.stateFile);
			}
		} catch (InvalidArgumentException e) {
			System.err.println(e.getMessage());
		}
		
	}

	private static void saveState(State newState, String stateFileName) throws JsonGenerationException, JsonMappingException, IOException {
		File stateFile = new File(stateFileName);
		stateMapper.writerWithDefaultPrettyPrinter().forType(State.class).writeValue(stateFile, newState);
	}
	
	private static State createDefaultState(Calendar now) {
		State newState = new State();
		newState.lastUpdated = now;
		newState.idsSeen = Collections.emptyList();
		return newState;
	}

	private static State createNewState(Calendar now) {
		State newState = new State();
		newState.lastUpdated = now;
		newState.idsSeen = new ArrayList<>();
		return newState;
	}

	private static void addToState(State state, List<Question> questions) {
		for (Question question : questions) {
			state.idsSeen.add(question.question_id);
		}
	}

	private static void postQuestions(List<Question> newQuestions, String webhookUrl) throws IOException {
		if (newQuestions.size() == 0) {
			return; //Nothing to post!
		}
		
		WebTarget target = client.target(webhookUrl);
		
		for (Question question : newQuestions) {
			SlackMessage message = MessageBuilder.buildMessage(question);
			
			Invocation.Builder builder = target.request();
	
			Response resp = builder.post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));
			if (resp.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
				throw new IOException("Error posting questions to slack: " + resp.getStatusInfo().getReasonPhrase());
			}
		}
	}

	private static List<Question> filterOldQuestions(List<Question> questions, Calendar lastUpdated, List<String> idsSeen) {
		
		// Sometimes questions don't appear in the API immediately
		// Add an additional 30 minutes of leeway
		Calendar cutoffTime = (Calendar) lastUpdated.clone();
		cutoffTime.add(Calendar.MINUTE, -30);
		
		List<Question> newQuestions = new ArrayList<>();
		for (Question question : questions) {
			if (!question.last_activity_date.before(cutoffTime) && !idsSeen.contains(question.question_id)) {
				newQuestions.add(question);
			}
		}
		return newQuestions;
	}

	private static QuestionResponse getQuestions(String site, List<String> tags, String apiKey) throws IOException {
		
		WebTarget target = client.target("http://api.stackexchange.com/2.2");
		WebTarget questionTarget = target.path("search")
				.queryParam("order", "desc")
				.queryParam("sort", "creation")
				.queryParam("site", site)
				.queryParam("tagged", joinTags(tags));
		
		if (apiKey != null) {
		    questionTarget = questionTarget.queryParam("key", apiKey);
		}
		
		Invocation.Builder builder = questionTarget.request();
		builder.accept(MediaType.APPLICATION_JSON);
		builder.acceptEncoding("UTF-8");
		
		Response response = builder.get();
		
		if (response.getStatus() == 200) {
			QuestionResponse questionResponse = response.readEntity(QuestionResponse.class);
			return questionResponse;
		} else {
			System.out.println("Response: " + response.getStatus());
			String string = response.readEntity(String.class);
			throw new IOException("Getting questions failed. RC: " + response.getStatus() + " Response: " + string);
		}
	}
	
	/**
	 * Joins a list of tags into a semi-colon separated string
	 */
	private static String joinTags(List<String> tags) {
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		for (String tag : tags) {
			if (!first) {
				sb.append(";");
			}
			first = false;
			sb.append(tag);
		}
		
		return sb.toString();
	}

	private static State loadState(String stateFileName) throws JsonProcessingException, IOException, InvalidArgumentException {
		if (stateFileName == null) {
			throw new InvalidArgumentException("State file location is not set in config file");
		}
		
		File stateFile = new File(stateFileName);
		
		State state = null;
		
		if (stateFile.exists()) {
			state = stateMapper.readerFor(State.class).readValue(stateFile);
		}
		
		return state;
	}
	
	private static Config loadConfig(File configFile) throws JsonProcessingException, IOException, InvalidArgumentException {
		if (configFile == null) {
			throw new InvalidArgumentException("Config file is not set");
		}
		
		if (!configFile.exists()) {
			throw new InvalidArgumentException("Config file [" + configFile + "] does not exist");
		}
		
		Config config = stateMapper.readerFor(Config.class).readValue(configFile);
		
		return config;
	}
	
}
