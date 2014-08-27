package eu.parmerasa.sik.tas2otawa;

import java.io.File;
import java.io.IOException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.Format;

import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaProgram;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasProgram;

/**
 * This class handles all IO operations to read and write XML files.
 * 
 * @author Rolf Kiefhaber
 */
public class XmlIo {

	/** The Serialized to read TAS XML files */
	private Serializer tasXmlReader;
	/** The Serialized to write OTAWA XML files */
	private Serializer otawaXmlWriter;
	/** prolog for each TAS XML */
	public static final String TAS_PROLOG = "<?xml version=\"1.0\"?>\n<!DOCTYPE program SYSTEM \"tas_annotations.dtd\">";
	/** prolog for each OTAWA XML */
	public static final String OTAWA_PROLOG = "<?xml version=\"1.0\"?>\n<!DOCTYPE program SYSTEM \"otawa_annotations.dtd\">";

	/**
	 * Initializing the reader
	 */
	public XmlIo() {
		Format tasFormatter = new Format(TAS_PROLOG);
		tasXmlReader = new Persister(tasFormatter);

		VisitorStrategy strategy = new CommentVisitorStrategy(null);
		Format otawaFormatter = new Format(OTAWA_PROLOG);
		otawaXmlWriter = new Persister(strategy, otawaFormatter);
	}

	/**
	 * Reads a XML file with TAS skeleton descriptions and returns the created object representation of the XML document
	 * 
	 * @param filename
	 *            the filename with full path information of the XML file to read
	 */
	public TasProgram readXml(String filename) {
		TasProgram result = null;
		File input = new File(filename);

		try {
			result = tasXmlReader.read(TasProgram.class, input);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Writes an OTAWA XML file from the given object representation of the OTAWA XML
	 * 
	 * @param filename
	 *            the filename with path information of the destination file
	 * @param otawaProgram
	 *            the object representation of the OTAWA XML
	 */
	public void writeXml(String filename, OtawaProgram otawaProgram) {
		File output = new File(filename);
		File parent = output.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		if (!output.exists()) {
			try {
				output.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			otawaXmlWriter.write(otawaProgram, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}