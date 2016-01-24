package uk.co.azquelt.slackstacker.test.versionedFile;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.co.azquelt.slackstacker.versionedFile.VersionedFormat;
import uk.co.azquelt.slackstacker.versionedFile.VersionedFormatProcessor;

public class VersionedFormatProcessorTest {

	class Data1 extends VersionedFormat {
		public String value;
	}
	
	class Data2 extends VersionedFormat {
		public String value;
		public String value2;
	}
	
	class TestProcessor extends VersionedFormatProcessor<Data1> {
		@Override
		public int getVersionNumber() {
			return 1;
		}

		@Override
		public Class<Data1> getDataClass() {
			return Data1.class;
		}
		
		@Override
		protected VersionedFormat doUpgrade(Data1 data) {
			Data2 data2= new Data2();
			data2.value = data.value;
			data2.value2 = "default";
			data2.version = 2;
			return data2;
		}
	}
	
	@Test
	public void testUpgrade() {
		TestProcessor testProcessor = new TestProcessor();
		
		Data1 input = new Data1();
		input.value = "testValue";
		
		Data2 data2 = (Data2) testProcessor.upgrade(input);
		assertEquals("testValue", data2.value);
		assertEquals("default", data2.value2);
		assertEquals(2, data2.version);
	}
	
	/**
	 * TestProcessor expects to upgrade a Data1, should fail with a Data2
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrongDataClass() {
		TestProcessor testProcessor = new TestProcessor();
		
		Data2 input = new Data2();
		input.value = "testValue";
		input.value2 = "testValue2";
		input.version = 1;
		
		testProcessor.upgrade(input);
	}
	
	/**
	 * TestProcessor expects input to be version 1, should fail if this is not the case
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWrongVersion() {
		TestProcessor testProcessor = new TestProcessor();
		
		Data1 input = new Data1();
		input.value = "testValue";
		input.version = 2;
		
		testProcessor.upgrade(input);
	}
	
	/**
	 * Upgrade should throw exception if doUpgrade does not result in a data
	 * object with a higher version number
	 */
	@Test(expected = RuntimeException.class)
	public void testNoUpgrade() {
		TestProcessor badProcessor = new TestProcessor() {
			@Override
			protected VersionedFormat doUpgrade(Data1 data) {
				Data2 data2 = new Data2();
				data2.value = data.value;
				data2.value2 = "default";
				data2.version = 1; // BAD
				return data2;
			}
		};
		
		Data1 input = new Data1();
		input.value = "testValue";
		input.version = 1;
		
		badProcessor.upgrade(input);
	}
}
