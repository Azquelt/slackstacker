package com.ibm.ws.slackstacking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.transport.common.gzip.GZIPFeature;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.ws.slackstacking.stack.Question;
import com.ibm.ws.slackstacking.stack.QuestionResponse;

public class SlackStacker {
	
	private static ObjectMapper stateMapper;
	private static final File STATE_FILE = new File("slackstacker.state");

	public static void main(String[] args) throws IOException {
		
		stateMapper = new ObjectMapper();
		
		State oldState = loadState();
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if (oldState != null) {
			List<Question> questions = getQuestions();
			List<Question> newQuestions = filterOldQuestions(questions, oldState.lastUpdated, oldState.idsSeen);
			postQuestions(newQuestions);
			State newState = createNewState(now, questions);
			saveState(newState);
		} else {
			System.out.println("No pre-existing state, setting up default state file");
			State newState = createDefaultState(now);
			saveState(newState);
		}
		
	}

	private static void saveState(State newState) throws JsonGenerationException, JsonMappingException, IOException {
		stateMapper.writerWithDefaultPrettyPrinter().forType(State.class).writeValue(STATE_FILE, newState);
	}
	
	private static State createDefaultState(Calendar now) {
		State newState = new State();
		newState.lastUpdated = now;
		newState.idsSeen = Collections.emptyList();
		return newState;
	}

	private static State createNewState(Calendar now, List<Question> questions) {
		State newState = new State();
		newState.lastUpdated = now;
		newState.idsSeen = new ArrayList<>();
		for (Question question : questions) {
			newState.idsSeen.add(question.question_id);
		}
		return newState;
	}

	private static void postQuestions(List<Question> newQuestions) {
		// TODO: Just print for now
		for (Question question : newQuestions) {
			System.out.println(question.title);
			System.out.println(question.link);
			System.out.println();
		}
	}

	private static List<Question> filterOldQuestions(List<Question> questions, Calendar lastUpdated, List<String> idsSeen) {
		
		List<Question> newQuestions = new ArrayList<>();
		for (Question question : questions) {
			if (!question.creation_date.before(lastUpdated) && !idsSeen.contains(question.question_id)) {
				newQuestions.add(question);
			}
		}
		return newQuestions;
	}

	private static List<Question> getQuestions() throws IOException {
		Client client = ClientBuilder.newBuilder()
				.register(JacksonJsonProvider.class) // Allow us to serialise JSON <-> POJO
				.register(GZIPFeature.class) // Allow us to understand GZIP compressed pages
				.build();
		
		WebTarget target = client.target("http://api.stackexchange.com/2.2");
		WebTarget questionTarget = target.path("questions")
				.queryParam("order", "desc")
				.queryParam("sort", "creation")
				.queryParam("site", "stackoverflow");
		
		Invocation.Builder builder = questionTarget.request();
		builder.accept(MediaType.APPLICATION_JSON);
		builder.acceptEncoding("UTF-8");
		
		Response response = builder.get();
		
		if (response.getStatus() == 200) {
			QuestionResponse questions = response.readEntity(QuestionResponse.class);
			return questions.getItems();
		} else {
			System.out.println("Response: " + response.getStatus());
			String string = response.readEntity(String.class);
			throw new IOException("Getting questions failed. RC: " + response.getStatus() + " Response: " + string);
		}
	}

	private static State loadState() throws JsonProcessingException, IOException {
		State state = null;
		
		if (STATE_FILE.exists()) {
			state = stateMapper.readerFor(State.class).readValue(STATE_FILE);
		}
		
		return state;
	}
	
}
