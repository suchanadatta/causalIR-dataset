/**
TODO: need to change the documentation.
 * SET THE ANALYZER WITH ENGLISH-ANALYZER AND SMART-STOPWORD LIST
 * 
 * @variables:
 *      Analyzer    analyzer;       // the analyzer
 *      String      stopwordPath;   // path of the smart-stopword file
 * 
 * @constructors:
 *      // Assumed that the smart-stopword file is present in: <a href=build/classes/resources/smart-stopwords>stopword-path</a>
 *      public EnglishAnalyzerWithSmartStopword() {}
 * 
 *      // The path of the stopword file is passed as argument
 *      public EnglishAnalyzerWithSmartStopword(String stopwordPath) {}
 * 
 * @methods:
 *      private void setEnglishAnalyzerWithSmartStopword() {}
 *      public Analyzer getEnglishAnalyzerWithSmartStopword() {}
 *      public Analyzer setAndGetEnglishAnalyzerWithSmartStopword() {}
 */
package common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 *
 * @author dwaipayan
 */

public class SetAnalyzerWithStopword {

    Analyzer    analyzer;
    String      stopFilePath;

    /**
     * Assumed that the smart-stopword file is present in the path:
     *      <a href=/resources/smart-stopwords>stopword-path</a>.
     */
    public SetAnalyzerWithStopword() {

        URL resourceUrl = SetAnalyzerWithStopword.class.getClassLoader().getResource("");
        if (resourceUrl != null) {
            resourceUrl.getPath();
        } else {
            System.out.println("Default stopword file error.");
            System.exit(0);
        }
        
        String filePath = resourceUrl.getPath()+"smart-stopwords";
        System.out.println("The path here:"+filePath);

        this.stopFilePath = filePath; 
    }

    /**
     * The path of the stopword file is passed as argument to the constructor
     * @param stopwordPath Path of the stopword file
     */
    public SetAnalyzerWithStopword(String stopwordPath) {
        this.stopFilePath = stopwordPath;
    }

	/**
	 *
	 * @param stopFilePath
	 * @return
	 */
	public static Analyzer setEnglishAnalyzerWithDefaultStopword(String stopFilePath) {

        List<String> stopwords = new ArrayList<>();

        String line;
        try {
            System.out.println("Stopword Path: "+stopFilePath);
            FileReader fr = new FileReader(stopFilePath);
            BufferedReader br = new BufferedReader(fr);
            while ( (line = br.readLine()) != null )
                stopwords.add(line.trim());

            br.close(); fr.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "Stopword file not found in: "+stopFilePath);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "IOException occurs");
            System.exit(1);
        }

        return new EnglishAnalyzer(StopFilter.makeStopSet(stopwords));
	}

	public static Analyzer setEnglishAnalyzerWithDefaultStopword() {
        URL resourceUrl = SetAnalyzerWithStopword.class.getClassLoader().getResource("");
        if (resourceUrl != null) {
            resourceUrl.getPath();
        } else {
            System.out.println("Default stopword file error.");
            System.exit(0);
        }
        
        String stopFilePath = resourceUrl.getPath()+"smart-stopwords";
        System.out.println("The path here:"+stopFilePath);

		return setEnglishAnalyzerWithDefaultStopword(stopFilePath);
	}

    /**
     * Set analyzer with EnglishAnalyzer with SMART-stoplist
	 * @param stopFilePath
	 * @return 
     */
    public static Analyzer setEnglishAnalyzerWithSmartStopword(String stopFilePath) {

        List<String> stopwords = new ArrayList<>();

        String line;
        try {
            System.out.println("Stopword Path: "+stopFilePath);
            FileReader fr = new FileReader(stopFilePath);
            BufferedReader br = new BufferedReader(fr);
            while ( (line = br.readLine()) != null )
                stopwords.add(line.trim());

            br.close(); fr.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "Stopword file not found in: "+stopFilePath);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "IOException occurs");
            System.exit(1);
        }

        return new EnglishAnalyzer(StopFilter.makeStopSet(stopwords));

        //analyzer = new StandardAnalyzer(StopFilter.makeStopSet(stopwords));
    }

    /**
     * Set analyzer with StandardAnalyzer with SMART-stoplist
     */
    public void setStandardAnalyzerWithSmartStopword() {

        List<String> stopwords = new ArrayList<>();

        String line;
        try {
            System.out.println("Stopword Path: "+stopFilePath);
            FileReader fr = new FileReader(stopFilePath);
            BufferedReader br = new BufferedReader(fr);
            while ( (line = br.readLine()) != null )
                stopwords.add(line.trim());

            br.close(); fr.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "Stopword file not found in: "+stopFilePath);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "IOException occurs");
            System.exit(1);
        }

        analyzer = new StandardAnalyzer(StopFilter.makeStopSet(stopwords));
    }

    /** 
     * Get the EnglishAnalyzer with Smart stopword list
     * @return analyzer
     */
    public Analyzer getEnglishAnalyzerWithSmartStopword() { return analyzer; }

    /** 
     * Set and get an EnglishAnalyzer with Smart stopword list
     * @return analyzer
     */
    public Analyzer setAndGetEnglishAnalyzerWithSmartStopword() {setEnglishAnalyzerWithDefaultStopword(); return analyzer; }

    /** 
     * Set and get an StandardAnalyzer with Smart stopword list
     * @return analyzer
     */
    public Analyzer setAndGetStandardAnalyzerWithSmartStopword() {setStandardAnalyzerWithSmartStopword(); return analyzer; }

    /**
     * For debugging purpose
     * @param args 
     */
    public static void main(String[] args) {

        SetAnalyzerWithStopword.setEnglishAnalyzerWithDefaultStopword();
    }
}
