package uk.co.azquelt.slackstacker;

import java.util.List;

import uk.co.azquelt.slackstacker.slack.SlackMessage;
import uk.co.azquelt.slackstacker.stack.Question;

/**
 * Builds slack messages from StackOverflow questions
 */
public class MessageBuilder {

	/**
	 * Builds a SlackMessage from a list of StackOverflow questions
	 * 
	 * @param question a question
	 * @return a message which includes the given question
	 */
	public static SlackMessage buildMessage(Question question) {
		StringBuilder sb = new StringBuilder();
		appendQuestion(sb, question);
		
		SlackMessage message = new SlackMessage();
		message.text = sb.toString();
		
		return message;
	}
	
	/**
	 * Format a SO question and append it to a string builder
	 * 
	 * @param sb the string builder
	 * @param question the SO question
	 */
	private static void appendQuestion(StringBuilder sb, Question question) {
		sb.append("<");
		sb.append(question.link);
		sb.append("|");
		sb.append(question.title);
		sb.append(">");
		
		appendTags(sb, question.tags);
		
		sb.append("\n");
	}
	
	/**
	 * Format a list of tags and append it to a string builder
	 * 
	 * @param sb the string builder
	 * @param tags the list of tags
	 */
	private static void appendTags(StringBuilder sb, List<String> tags) {
		if (tags == null) {
			return;
		}
		for (String tag : tags) {
			sb.append(" [");
			sb.append(tag);
			sb.append("]");
		}
	}

}
