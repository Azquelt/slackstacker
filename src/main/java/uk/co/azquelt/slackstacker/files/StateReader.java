package uk.co.azquelt.slackstacker.files;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.co.azquelt.slackstacker.versionedFile.VersionedFormatProcessor;
import uk.co.azquelt.slackstacker.versionedFile.VersionedFormatReader;

public class StateReader extends VersionedFormatReader<State> {

	public StateReader(ObjectMapper mapper) {
		super(mapper);
	}

	@Override
	protected Class<State> getDataClass() {
		return State.class;
	}

	@Override
	protected int getCurrentVersion() {
		return 1;
	}

	@Override
	protected Collection<VersionedFormatProcessor<?>> getFormatProcessors() {
		return Collections.emptyList();
	}

}
