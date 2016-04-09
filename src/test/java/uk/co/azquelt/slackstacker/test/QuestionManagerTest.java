package uk.co.azquelt.slackstacker.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import uk.co.azquelt.slackstacker.QuestionManager;
import uk.co.azquelt.slackstacker.State;
import uk.co.azquelt.slackstacker.stack.Question;

public class QuestionManagerTest {

	private static final DateFormat TEST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private Question question(String id, String title, String time) {
		Question result = new Question();
		result.question_id = id;
		result.creation_date = calendar(time);
		result.title = title;
		result.last_activity_date = result.creation_date;
		result.link = "http://www.example.com";
		result.tags = Collections.emptyList();
		return result;
	}

	private Calendar calendar(String time) {
		try {
			Calendar result = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			result.setTime(TEST_DATE_FORMAT.parse(time));
			return result;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private State state(String cutoffTime, Question... questions) {
		State state = new State();
		state.questionsSeenCutoff = calendar(cutoffTime);
		state.questionsSeen = new HashMap<>();

		for (Question question : questions) {
			state.questionsSeen.put(question.question_id, question.creation_date);
			if (question.creation_date.before(state.questionsSeenCutoff)) {
				throw new IllegalArgumentException(
						"Question " + question.question_id + " created before the state cutoff time");
			}
		}

		return state;
	}

	private Question q1 = question("1", "Question 1", "2016-01-01 12:00");
	private Question q2 = question("2", "Question 2", "2016-01-02 12:00");
	private Question q3 = question("3", "Question 3", "2016-01-03 12:00");
	private Question q4 = question("4", "Question 4", "2016-01-04 12:00");

	private QuestionManager qm = new QuestionManager();

	@Test
	public void testCreateDefaultState() {
		Calendar now = calendar("2016-01-01 00:00");
		State defaultState = qm.createDefaultState(now);

		assertThat(defaultState.backoffUntil, is(nullValue()));
		assertThat(defaultState.questionsSeenCutoff, is(now));
		assertThat(defaultState.questionsSeen.isEmpty(), is(true));
	}

	@Test
	public void testNoQuestionsBeforeCutoff() {
		State state = state("2016-01-02 12:00");

		List<Question> questionsReceived = Arrays.asList(q1, q2, q3);

		List<Question> newQuestions = qm.filterQuestions(questionsReceived, state);

		assertThat(newQuestions, contains(q2, q3));
	}

	@Test
	public void testOldQuestionsFilteredOut() {
		State state = state("2016-01-01 12:00", q1, q2);

		List<Question> newQuestions = qm.filterQuestions(Arrays.asList(q1, q2, q3, q4), state);

		assertThat(newQuestions, contains(q3, q4));
	}

	@Test
	public void testDeletedQuestionsRemainInState() {
		State state = state("2016-01-01 12:00", q1, q2, q3);

		State newState = qm.computeNewState(Arrays.asList(q1, q3), state);

		assertThat(newState.questionsSeen, hasKey("2"));
	}

	/**
	 * Test that we handle the case where, for some reason, no questions are
	 * returned
	 */
	@Test
	public void testNoQuestionsReturned() {
		State state = state("2016-01-01 12:00", q1, q2, q3);
		
		List<Question> receivedQuestions = Collections.emptyList();

		List<Question> newQuestions = qm.filterQuestions(receivedQuestions, state);
		assertThat(newQuestions, is(empty()));
		
		// If there are no questions, the new state should be the same as the old state
		State newState = qm.computeNewState(receivedQuestions, state);
		assertThat(newState.questionsSeenCutoff, is(calendar("2016-01-01 12:00")));
		assertThat(newState.questionsSeen.keySet(), containsInAnyOrder("1", "2", "3"));
	}
}
