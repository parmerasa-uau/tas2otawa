/**
 * 
 */
package eu.parmerasa.sik.xmlconverter.converter;

import org.junit.Test;

/**
 * @author jahrralf
 * 
 */
public class ThreadUsageTest extends AbstractTasTest {

	@Test
	public void testBauer() throws Exception {
		runTest("src/test/resources/bauer_tas.xml");
	}

	@Test
	public void nacheinanderTest() throws Exception {
		runTest("src/test/resources/nacheinander.xml");
	}

	@Test
	public void nacheinanderMitIniFinTest() throws Exception {
		runTest("src/test/resources/nacheinander_with_init_finalize.xml");
	}

	@Test
	public void verschachteltTest() throws Exception {
		runTest("src/test/resources/verschachtelt.xml");
	}

	@Test
	public void verschachteltMitIniFinTest() throws Exception {
		runTest("src/test/resources/verschachtelt_with_init_finalize.xml");
	}

	@Test
	public void stegmeierTas() throws Exception {
		runTest("src/test/resources/stegmeier_tas.xml");
	}

	@Test
	public void fooTest() throws Exception {
		runTest("C:/temp/Versions/12_Cores/bauer/__otawa__/bauer_tas.xml");
	}

}
