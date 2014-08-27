package eu.parmerasa.sik.xmlconverter.converter;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.Format;

import eu.parmerasa.sik.tas2otawa.CommentVisitorStrategy;
import eu.parmerasa.sik.tas2otawa.SkeletonsConverter;
import eu.parmerasa.sik.tas2otawa.XmlIo;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaProgram;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasProgram;

public class XMLStructureTest {

	@Test
	public void testTaskParallelism() throws Exception {
		XmlIo io = new XmlIo();
		TasProgram tasProgram = io
				.readXml("src/test/resources/InputTaskParallelism.xml");

		// Transforming

		VisitorStrategy strategy = new CommentVisitorStrategy(null);
		Format formatter = new Format(XmlIo.TAS_PROLOG);
		Serializer serializer = new Persister(strategy, formatter);

		System.out.println("\n==========");
		System.out.println("Input:");
		System.out.println("==========\n");
		serializer.write(tasProgram, System.out);
		System.out.println();

		SkeletonsConverter factory = new SkeletonsConverter();
		OtawaProgram otawaProgram = factory.transformParallelism(tasProgram);

		formatter = new Format(XmlIo.OTAWA_PROLOG);
		serializer = new Persister(strategy, formatter);
		System.out.println("\n==========");
		System.out.println("Output:");
		System.out.println("==========\n");
		serializer.write(otawaProgram, System.out);
		System.out.println("\n");
	}

	@Test
	public void testDataParallelism() throws Exception {
		XmlIo io = new XmlIo();
		TasProgram tasProgram = io
				.readXml("src/test/resources/InputDataParallelism.xml");

		// Transforming

		VisitorStrategy strategy = new CommentVisitorStrategy(null);
		Format formatter = new Format(XmlIo.TAS_PROLOG);
		Serializer serializer = new Persister(strategy, formatter);

		System.out.println("\n==========");
		System.out.println("Input:");
		System.out.println("==========\n");
		serializer.write(tasProgram, System.out);
		System.out.println();

		SkeletonsConverter factory = new SkeletonsConverter();
		OtawaProgram otawaProgram = factory.transformParallelism(tasProgram);

		formatter = new Format(XmlIo.OTAWA_PROLOG);
		serializer = new Persister(strategy, formatter);
		System.out.println("\n==========");
		System.out.println("Output:");
		System.out.println("==========\n");
		serializer.write(otawaProgram, System.out);
		System.out.println("\n");
	}

	@Test
	public void testDataParallelismWithInitAndFinalize() throws Exception {
		XmlIo io = new XmlIo();
		TasProgram tasProgram = io
				.readXml("src/test/resources/InputDataParallelism_with_init_finalize.xml");

		// Transforming

		VisitorStrategy strategy = new CommentVisitorStrategy(null);
		Format formatter = new Format(XmlIo.TAS_PROLOG);
		Serializer serializer = new Persister(strategy, formatter);

		System.out.println("\n==========");
		System.out.println("Input:");
		System.out.println("==========\n");
		serializer.write(tasProgram, System.out);
		System.out.println();

		SkeletonsConverter factory = new SkeletonsConverter();
		OtawaProgram otawaProgram = factory.transformParallelism(tasProgram);

		formatter = new Format(XmlIo.OTAWA_PROLOG);
		serializer = new Persister(strategy, formatter);
		System.out.println("\n==========");
		System.out.println("Output:");
		System.out.println("==========\n");
		serializer.write(otawaProgram, System.out);
		System.out.println("\n");
	}

	@Test
	public void testPipelineParallelism() throws Exception {
		XmlIo io = new XmlIo();
		TasProgram tasProgram = io
				.readXml("src/test/resources/InputPipelineParallelism.xml");

		// Transforming

		VisitorStrategy strategy = new CommentVisitorStrategy(null);
		Format formatter = new Format(XmlIo.TAS_PROLOG);
		Serializer serializer = new Persister(strategy, formatter);

		System.out.println("\n==========");
		System.out.println("Input:");
		System.out.println("==========\n");
		serializer.write(tasProgram, System.out);
		System.out.println();

		SkeletonsConverter factory = new SkeletonsConverter();
		OtawaProgram otawaProgram = factory.transformParallelism(tasProgram);

		formatter = new Format(XmlIo.OTAWA_PROLOG);
		serializer = new Persister(strategy, formatter);
		System.out.println("\n==========");
		System.out.println("Output:");
		System.out.println("==========\n");
		serializer.write(otawaProgram, System.out);
		System.out.println("\n");
	}

	@Test
	public void testSerilization() throws Exception {
		String input = "src/test/resources/inputTaskParallelism.xml";
		String expectedOut = "src/test/resources/outputTaskParallelism.xml";
		checkGeneratedXML(input, expectedOut);

		input = "src/test/resources/inputDataParallelism.xml";
		expectedOut = "src/test/resources/outputDataParallelism.xml";
		checkGeneratedXML(input, expectedOut);

		input = "src/test/resources/InputDataParallelism_with_init_finalize.xml";
		expectedOut = "src/test/resources/OutputDataParallelism_with_init_finalize.xml";
		checkGeneratedXML(input, expectedOut);

		input = "src/test/resources/inputPipelineParallelism.xml";
		expectedOut = "src/test/resources/outputPipelineParallelism.xml";
		checkGeneratedXML(input, expectedOut);
	}

	/**
	 * Reads the given TAS XML file (input), generates an output and checks
	 * against a predefined OTAWA XML file (expectedOut)
	 * 
	 * @param input
	 *            the TAS XML input
	 * @param expectedOut
	 *            The predefined OTAWA XML output
	 * @throws FileNotFoundException
	 *             Thrown, if the files do not exist
	 * @throws IOException
	 *             Thrown, if an error occurs on reading the files
	 */
	private void checkGeneratedXML(String input, String expectedOut)
			throws FileNotFoundException, IOException {
		String out = "JUnitout.xml";

		XmlIo io = new XmlIo();
		TasProgram tasProgram = io.readXml(input);
		SkeletonsConverter factory = new SkeletonsConverter();
		OtawaProgram otawaProgram = factory.transformParallelism(tasProgram);
		io.writeXml(out, otawaProgram);

		File outFile = new File(out);
		BufferedReader reader = null;
		StringBuilder generatedOutput = new StringBuilder();
		StringBuilder expectedOutput = new StringBuilder();

		try {
			reader = new BufferedReader(new FileReader(outFile));
			while (reader.ready()) {
				generatedOutput.append(reader.readLine());
			}
		} finally {
			reader.close();
		}

		try {
			reader = new BufferedReader(new FileReader(expectedOut));
			while (reader.ready()) {
				expectedOutput.append(reader.readLine());
			}
		} finally {
			reader.close();
		}

		outFile.delete();

		System.out.println(expectedOutput);
		System.out.println(generatedOutput);
		assertEquals(expectedOutput.toString(), generatedOutput.toString());
	}
}
