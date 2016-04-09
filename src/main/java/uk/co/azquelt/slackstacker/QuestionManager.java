package uk.co.azquelt.slackstacker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.co.azquelt.slackstacker.stack.Question;

public class QuestionManager {

	/**
	 * Filter out questions which have been seen before, returning only those which are new
	 * 
	 * @param questions the list of questions to filter
	 * @param oldState the state from the last run
	 * @return the list of questions which are new since the last run
	 */
	public List<Question> filterQuestions(List<Question> questions, State state) {
		ArrayList<Question> results = new ArrayList<>();
		for (Question question : questions) {
			if (isQuestionNew(question, state)) {
				results.add(question);
			}
		}
		return results;
	}
	
	/**
	 * Create a new state, given an old state and a list of questions received
	 * 
	 * @param newQuestions complete list of questions received from the API
	 * @param oldState the old state
	 * @return the new state
	 */
	public State computeNewState(List<Question> newQuestions, State oldState) {
		Map<String, Calendar> questionsSeen = new HashMap<>(oldState.questionsSeen);

		// Add the new questions to the map of seen questions
		// and find the creation date of the oldest new question
		Calendar oldestQuestion = null;
		for (Question question : newQuestions) {
			questionsSeen.put(question.question_id, question.creation_date);
			if (oldestQuestion == null || question.creation_date.before(oldestQuestion)) {
				oldestQuestion = question.creation_date;
			}
		}
		
		// Compute the new cutoff
		// The new cutoff must never be earlier than either the old cutoff or the oldest new question
		Calendar oldCutoff = oldState.questionsSeenCutoff;
		Calendar newCutoff = oldestQuestion == null || oldestQuestion.before(oldCutoff) ? oldCutoff : oldestQuestion;
		
		// Prune any seen questions which are older than the new cutoff point
		for (Iterator<Entry<String, Calendar>> i = questionsSeen.entrySet().iterator(); i.hasNext(); ) {
			Entry<String, Calendar> e = i.next();
			if (e.getValue().before(newCutoff)) {
				i.remove();
			}
		}
		
		State newState = new State();
		newState.questionsSeenCutoff = newCutoff;
		newState.questionsSeen = questionsSeen;
		
		return newState;
	}
	
	/**
	 * Create an initial state for the first run
	 * 
	 * @param now the current time
	 * @return the new state
	 */
	public State createDefaultState(Calendar now) {
		State newState = new State();
		newState.questionsSeen = Collections.emptyMap();
		newState.questionsSeenCutoff = now;
		return newState;
	}
	
	private boolean isQuestionNew(Question question, State state) {
		return !question.creation_date.before(state.questionsSeenCutoff)
				&& !state.questionsSeen.containsKey(question.question_id);
	}
}
