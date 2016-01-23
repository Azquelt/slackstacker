package uk.co.azquelt.slackstacker.versionedFile;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Responsible for reading a specific version of a versioned data format and upgrading it to a newer version.
 * 
 * @param <T> The class which corresponds to the version of the data format which this processor can read
 */
abstract public class VersionedFormatProcessor <T extends VersionedFormat> {
	
	protected ObjectMapper mapper;
	
	protected VersionedFormatProcessor(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	/**
	 * @return the version of the versioned format that this processor can read and upgrade
	 */
	public abstract int getVersionNumber();

	/**
	 * Upgrades a <code>&lt;T></code> data object to some newer version
	 * 
	 * @param data the data object to upgrade
	 * @return the upgraded data object
	 */
	protected abstract VersionedFormat doUpgrade(T data);

	/**
	 * Returns the data type understood by this processor
	 * <p>
	 * This must match <code>&lt;T></code>
	 * 
	 * @return the data class
	 */
	public abstract Class<T> getDataClass();

	/**
	 * Read a versioned file.
	 * <p>
	 * This method will return a subclass of VersionedFile which corresponds to the version of the file which was read.
	 * 
	 * @param file the file to read from
	 * @return the data from the versioned file
	 * @throws IOException if a problem was encountered reading the file
	 * @throws JsonProcessingException if a problem was encountered parsing the file
	 */
	public VersionedFormat readFile(File file) throws JsonProcessingException, IOException {
		return mapper.readerFor(getDataClass()).readValue(file);
	}
	
	/**
	 * Upgrades a VersionedFormat object to a newer format with a higher version number.
	 * <p>
	 * Subclasses must not override this method but override {@link #doUpgrade(VersionedFormat)} instead.
	 * <p>
	 * This method does some validation of the argument and then calls {@link #doUpgrade(VersionedFormat)}.
	 * 
	 * @param data a VersionedFile of the type understood by this class
	 * @return a VersionedFile of another type
	 * @throws IllegalArgumentException if versionedFormat is not a version understood by this processor
	 */
	public final VersionedFormat upgrade(VersionedFormat data) throws IllegalArgumentException {
		if (!getDataClass().isAssignableFrom(data.getClass())) {
			throw new IllegalArgumentException(
					"VersionedFormat object is not of class " + getDataClass().getName() + ". " 
					+ "Actual type: " + data.getClass().getName()
					);
		}
		
		if (data.version != getVersionNumber()) {
			throw new IllegalArgumentException(
					"VersionedFormat object has the right type but the wrong version number"
					);
		}
		
		VersionedFormat upgradedData = doUpgrade(getDataClass().cast(data));
		
		if (upgradedData.version <= data.version) {
			throw new RuntimeException(
					"VersionedFormat upgrade by " + this.getClass().getName() + " did not result in an object with a higher version"
					);
		}
		
		return upgradedData;
	}
	
}
