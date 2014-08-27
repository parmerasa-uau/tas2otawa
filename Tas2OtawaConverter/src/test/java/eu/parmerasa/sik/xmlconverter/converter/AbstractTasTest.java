package eu.parmerasa.sik.xmlconverter.converter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.Format;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import eu.parmerasa.sik.tas2otawa.CommentVisitorStrategy;
import eu.parmerasa.sik.tas2otawa.SkeletonsConverter;
import eu.parmerasa.sik.tas2otawa.XmlIo;
import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaProgram;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasProgram;

public class AbstractTasTest {
	protected void runTest(String filename) throws Exception {
		try {
			System.out.println("Starting test with " + filename);

			// Check input file
			checkStructure(new InputSource(filename));

			// Read input file
			XmlIo io = new XmlIo();
			TasProgram tasProgram = io
					.readXml(filename);

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

			StringWriter sw = new StringWriter();
			serializer.write(otawaProgram, sw);
			String content = sw.toString();

			// Check output file
			checkStructure(new InputSource(new StringReader(content)));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	protected void checkStructure(InputSource input) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		SAXParser parser = factory.newSAXParser();

		XMLReader reader = parser.getXMLReader();
		reader.setErrorHandler(new SimpleErrorHandler());
		reader.parse(input);
	}
}
