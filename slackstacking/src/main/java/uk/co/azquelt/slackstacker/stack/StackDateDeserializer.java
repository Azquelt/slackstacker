package uk.co.azquelt.slackstacker.stack;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Deserializer for dates in the Stack Exchange API
 * <p>
 * Stack Exchange uses epoch seconds, Java expects epoch milliseconds
 * 
 * @see <a href="https://api.stackexchange.com/docs/dates">https://api.stackexchange.com/docs/dates</a>
 */
public class StackDateDeserializer extends JsonDeserializer<Calendar> {
	
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Override
	public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		long dateString = p.readValueAs(Long.class);
		dateString *= 1000; // Convert seconds -> ms
		
		Calendar cal = Calendar.getInstance(UTC);
		cal.setTimeInMillis(dateString);
		return cal;
	}

}
