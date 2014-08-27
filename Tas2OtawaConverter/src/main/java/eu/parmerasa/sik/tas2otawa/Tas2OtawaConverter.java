package eu.parmerasa.sik.tas2otawa;

import eu.parmerasa.sik.tas2otawa.xml.otawa.OtawaProgram;
import eu.parmerasa.sik.tas2otawa.xml.tas.TasProgram;

/**
 * This class holds the main method to call the converter
 * 
 * @author Rolf Kiefhaber
 */
public class Tas2OtawaConverter {

    /**
     * Converts a TAS XML file to a OTAWA XML file
     * 
     * @param args
     *            2 arguments are expected:
     *            <ol>
     *            <li>The input TAS XML file</li>
     *            <li>The output OTAWA XML file</li>
     *            </ol>
     */
    public static void main(String[] args) {
        if (!(args.length == 2)) {
            System.out.println("Usage: " + Tas2OtawaConverter.class.getName()
                    + " [input] [output]");
            return;
        }

        String inputFilename = args[0];
        String outputFilename = args[1];

        XmlIo xmlIo = new XmlIo();

        TasProgram tasProgram = xmlIo.readXml(inputFilename);
        if (tasProgram == null) {
            System.out
                    .println("Unable to read xml, see exception for more information, aborting");
            return;
        }
        SkeletonsConverter factory = new SkeletonsConverter();
        OtawaProgram otawaProgram = factory.transformParallelism(tasProgram);
        xmlIo.writeXml(outputFilename, otawaProgram);
    }

}
