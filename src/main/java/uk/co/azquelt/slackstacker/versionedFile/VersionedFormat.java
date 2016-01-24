package uk.co.azquelt.slackstacker.versionedFile;

/**
 * This is intended to be the base class for any data format which may change in
 * the future and needs to be backwards compatible.
 * <p>
 * A given format should have a data class and a VersionedFormatProcessor for
 * each old version of the format, and one VersionedFormatReader to read and
 * write all versions.
 * <p>
 * E.g. State might have
 * <ul>
 * <li>StateV1 and StateV1Processor for version 1
 * <li>StateV2 and StateV2Processor for version 2
 * <li>State for version 3
 * <li>StateManager
 * </ul>
 * <p>
 * In this example, StateV1Processor is responsible for reading version 1 state
 * files into StateV1 objects and converting StateV1 objects to StateV2 objects.
 */
public class VersionedFormat {
	
	/**
	 * The file format version number
	 * <p>
	 * Defaults to 1, to be compatible with old files without version numbers
	 */
	public int version = 1;

}
