package uk.co.azquelt.slackstacker.versionedFile;

/**
 * Responsible for upgrading a specific version of a file to a newer version.
 * 
 * @param <T> The class which corresponds to the version of the data format which this processor can read
 */
abstract public class VersionedFormatProcessor <T extends VersionedFormat> {
	
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
	 * This must match <code>&lt;T></code> and correspond to the value returned by {@link #getVersionNumber()}
	 * 
	 * @return the data class
	 */
	public abstract Class<T> getDataClass();

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
