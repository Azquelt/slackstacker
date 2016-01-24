package uk.co.azquelt.slackstacker.test.versionedFile;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.co.azquelt.slackstacker.versionedFile.VersionedFormat;
import uk.co.azquelt.slackstacker.versionedFile.VersionedFormatProcessor;
import uk.co.azquelt.slackstacker.versionedFile.VersionedFormatReader;

public class VersionedFormatReaderTest {
	
	private static class DataV1 extends VersionedFormat {
		public String data;
	}
	
	private static class DataV2 extends VersionedFormat {
		public String data;
		public String data2;
	}
	
	private static class DataV3 extends VersionedFormat {
		public String data;
		public List<String> data2;
	}
	
	private static class DataV1Processor extends VersionedFormatProcessor<DataV1> {

		@Override
		public int getVersionNumber() {
			return 1;
		}

		@Override
		protected VersionedFormat doUpgrade(DataV1 data) {
			DataV2 dataV2 = new DataV2();
			dataV2.version = 2;
			dataV2.data = data.data;
			dataV2.data2 = "default";
			return dataV2;
		}

		@Override
		public Class<DataV1> getDataClass() {
			return DataV1.class;
		}
		
	}
	
	private static class DataV2Processor extends VersionedFormatProcessor<DataV2> {

		@Override
		public int getVersionNumber() {
			return 2;
		}

		@Override
		protected VersionedFormat doUpgrade(DataV2 data) {
			DataV3 dataV3 = new DataV3();
			dataV3.version = 3;
			
			dataV3.data = data.data;
			
			dataV3.data2 = new ArrayList<>();
			dataV3.data2.add(data.data2);
			
			return dataV3;
		}

		@Override
		public Class<DataV2> getDataClass() {
			return DataV2.class;
		}
		
	}
	
	private static class DataReader extends VersionedFormatReader<DataV3> {
		
		public DataReader(ObjectMapper mapper) {
			super(mapper);
		}

		@Override
		protected Class<DataV3> getDataClass() {
			return DataV3.class;
		}

		@Override
		protected int getCurrentVersion() {
			return 3;
		}

		@Override
		protected Collection<VersionedFormatProcessor<?>> getFormatProcessors() {
			return Arrays.asList(new DataV1Processor(), new DataV2Processor());
		}
		
	}
	
	private InputStream createInputStream(String input) {
		return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Test reading the current version
	 */
	@Test
	public void testReadCurrent() throws Exception {
		DataReader reader = new DataReader(new ObjectMapper());
		InputStream stream = createInputStream("{\"version\": 3, \"data\": \"test data\", \"data2\": [\"a\", \"b\"]}");
		DataV3 data = reader.read(stream, 3);
		
		assertEquals(3, data.version);
		assertEquals("test data", data.data);
		assertEquals(Arrays.asList("a", "b"), data.data2);
	}
	
	/**
	 * Test reading the previous version
	 */
	@Test
	public void testReadOld1() throws Exception {
		DataReader reader = new DataReader(new ObjectMapper());
		InputStream stream = createInputStream("{\"version\": 2, \"data\": \"test data\", \"data2\": \"test data 2\"}");
		DataV3 data = reader.read(stream, 2);
		
		assertEquals(3, data.version);
		assertEquals("test data", data.data);
		assertEquals(Arrays.asList("test data 2"), data.data2);
	}
	
	/**
	 * Test reading a file from two versions ago
	 */
	@Test
	public void testReadOld2() throws Exception {
		DataReader reader = new DataReader(new ObjectMapper());
		InputStream stream = createInputStream("{\"version\": 1, \"data\": \"test data\"}");
		DataV3 data = reader.read(stream, 1);
		
		assertEquals(3, data.version);
		assertEquals("test data", data.data);
		assertEquals(Arrays.asList("default"), data.data2);
	}
	
	/**
	 * Test reading a legacy v1 file which had no version
	 */
	@Test
	public void testReadNoVersion() throws Exception {
		DataReader reader = new DataReader(new ObjectMapper());
		InputStream stream = createInputStream("{\"data\": \"test data\"}");
		DataV3 data = reader.read(stream, 1);
		
		assertEquals(3, data.version);
		assertEquals("test data", data.data);
		assertEquals(Arrays.asList("default"), data.data2);
	}
}
