package uk.co.azquelt.slackstacker.versionedFile;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles reading and writing old and new versions of a versioned file format
 *
 * @param <T> the data class for the most recent version of the versioned format
 */
public abstract class VersionedFormatReader<T> {

	protected ObjectMapper mapper;
	
	public VersionedFormatReader(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	/**
	 * Returns the data class for the most recent version of the versioned format
	 * <p>
	 * This must be {@code <T>}
	 * 
	 * @return the data class for the current version
	 */
	protected abstract Class<T> getDataClass();
	
	/**
	 * Returns the most recent version number of the versioned format
	 * 
	 * @return the current format version number
	 */
	protected abstract int getCurrentVersion();

	/**
	 * Returns the {@link VersionedFormatProcessor}s which should be used for reading old versions of this versioned data format
	 * 
	 * @return a collection of format processors for older versions of the versioned data format
	 */
	protected abstract Collection<VersionedFormatProcessor<?>> getFormatProcessors();
	
	/**
	 * Read a versioned data format from an InputStream
	 * <p>
	 * If the stream contains an old version of the data format, it will be read
	 * and converted to the latest format.
	 * 
	 * @param stream
	 *            the stream to read from
	 * @param version the data format version of the data held by the stream
	 * @return the data from the versioned file
	 * @throws IOException
	 *             if a problem was encountered reading the file
	 * @throws JsonProcessingException
	 *             if a problem was encountered parsing the file
	 */
	public T read(InputStream stream, int version) throws JsonProcessingException, IOException {
		if (version == getCurrentVersion()) {
			return readCurrentVersion(stream);
		} else {
			return readOldVersion(stream, version);
		}
	}
	
	public T read(File file) throws JsonProcessingException, FileNotFoundException, IOException {
		int version = readVersion(new FileInputStream(file));
		return read(new FileInputStream(file), version);
	}
	
	/**
	 * Write a versioned data format to an OutputStream
	 * <p>
	 * Only the latest version of the format may be used for writing.
	 * 
	 * @param stream the stream to write to
	 * @param data the data object to write
	 * @throws JsonGenerationException if a problem is encountered generating the JSON for the data object
	 * @throws JsonMappingException if a problem is encountered generating the JSON for the data object
	 * @throws IOException if there is a problem writing to the stream
	 */
	public void write(OutputStream stream, T data) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writerFor(getDataClass()).writeValue(stream, data);
	}
	
	/**
	 * Read the input stream and find the version of the data stored within it
	 * 
	 * @param stream
	 *            the input stream
	 * @return the version of the data in the input stream
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public int readVersion(InputStream stream) throws JsonProcessingException, IOException {
		VersionedFormat versionedFormat = mapper.readerFor(VersionedFormat.class).without(FAIL_ON_UNKNOWN_PROPERTIES).readValue(stream);
		return versionedFormat.version;
	}

	private T readOldVersion(InputStream stream, int version) throws JsonProcessingException, IOException {
		VersionedFormatProcessor<?> processor = getProcessorForVersion(version);
		VersionedFormat data = mapper.readerFor(processor.getDataClass()).readValue(stream);
		while (version != getCurrentVersion()) {
			processor = getProcessorForVersion(version);
			data = processor.upgrade(data);
			version = data.version;
		}
		return getDataClass().cast(data);
	}
	
	private T readCurrentVersion(InputStream stream) throws JsonProcessingException, IOException {
		return mapper.readerFor(getDataClass()).readValue(stream);
	}
	
	private VersionedFormatProcessor<?> getProcessorForVersion(int version) {
		for (VersionedFormatProcessor<?> processor : getFormatProcessors()) {
			if (processor.getVersionNumber() == version) {
				return processor;
			}
		}
		throw new RuntimeException("Unknown file version: " + version);
	}

}
