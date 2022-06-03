
package preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author suchana
 */

public class MakeTelegraphDumpAnalyzed {
    
    String                collectionPath;
    String                dumpPath;
    Analyzer              analyzer;            //org.apache.lucene.analysis.Analyzer; we use same analyzer for searching
    String                stopWordPath;
    List<String>          stopWordList;
    static int            docCount;
    static FileWriter     fileWriter;
    
    
    public MakeTelegraphDumpAnalyzed(String collectionPath, String dumpPath) throws IOException {

        this.collectionPath = collectionPath;
        this.dumpPath = dumpPath;
        dumpPath = dumpPath + "telegraph_01_11_dump.analyzed";
        fileWriter = new FileWriter(dumpPath);
        stopWordPath = "/home/suchana/smart-stopwords";

        //for using default stopwordlist
        //analyzer = new EnglishAnalyzer();                                     //org.apache.lucene.analysis.en.EnglishAnalyzer; this uses default stopword list
        //for using external stopword list
        stopWordList = getStopwordList(stopWordPath);                         
        analyzer = new EnglishAnalyzer(StopFilter.makeStopSet(stopWordList));   // org.apache.lucene.analysis.core.StopFilter

        docCount = 0;
    }    
    
    public List<String> getStopwordList(String stopwordPath) {
        
        List<String> stopwords = new ArrayList<>();
        String line;
        try {
            System.out.println("Stopword Path: "+ stopwordPath);
            FileReader fr = new FileReader(stopwordPath);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null)
                stopwords.add(line.trim());
            br.close();
            fr.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: \n" + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n" + "Stopword file not found in: "+stopwordPath);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: \n" + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n" + "IOException occurs");
            System.exit(1);
        }
        return stopwords;
    }

    public void createDump(String collectionPath) throws FileNotFoundException, IOException, NullPointerException {

            System.out.println("Dumping started...");
            File colFile = new File(collectionPath);
            if(colFile.isDirectory())
                collectionDirectory(colFile);
            else
                getFileContent(colFile);
    }

    public void collectionDirectory(File colDir) throws FileNotFoundException, IOException, NullPointerException {
        
        File[] files = colDir.listFiles();
        for (File file : files) {
            System.out.println("Dumping file : " + file);
            if (file.isDirectory()) {
                System.out.println("It has subdirectories...\n");
                collectionDirectory(file);  // calling this function recursively to access all the subfolders in the directory
            }
            else
                getFileContent(file);
        }
    }
    
    public void getFileContent(File colFile) throws FileNotFoundException, IOException {
        
        String fileContent;
        String rawContent, analyzedContent, docid = null;
        Pattern p_docid, p_text;
        Matcher m_docid, m_text;
        BufferedReader br = new BufferedReader(new FileReader(colFile));
        String line = br.readLine();
        StringBuilder sb = new StringBuilder(); 
        while(line != null){ 
            sb.append(line).append("\n");
            line = br.readLine();
        } 
        fileContent = sb.toString();
        fileContent = fileContent.replaceAll("\"", "").replaceAll("", "").replaceAll("\n", "").replaceAll("\r", "");;
                
        p_docid = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
        m_docid = p_docid.matcher(fileContent);
        p_text = Pattern.compile("<TEXT>(.+?)</TEXT>");
        m_text = p_text.matcher(fileContent);
        
        while (m_docid.find()) {
            docid = m_docid.group(1).trim().replaceAll("\\s{2,}", " ");
            //System.out.println("doc no : " + docNo);
            fileWriter.write(docid + "\t");
            if (m_text.find()) {
                rawContent = m_text.group(1).trim().replaceAll("\\s{2,}", " ");
                analyzedContent = analyzeText(analyzer, rawContent, "content").toString();
                fileWriter.write(analyzedContent + "\n");
            }
            docCount++;
        }
        System.out.println("DOCID : " + docid + ", COUNT : " + docCount);
    }
    
    public static StringBuffer analyzeText(Analyzer analyzer, String text, String fieldName) throws IOException {

        StringBuffer tokenizedContentBuff = new StringBuffer();

        TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(text));
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        stream.reset();

        while (stream.incrementToken()) {
            String term = termAtt.toString();
            tokenizedContentBuff.append(term).append(" ");
        }

        stream.end();
        stream.close();

        return tokenizedContentBuff;
    }    
    
    public static void main(String[] args) throws IOException {

        String collectionPath, dumpPath;
        if(args.length!=2) {
            System.out.println("Usage: java preprocessing.MakeTelegraphDumpAnalyzed <collection-path> <dump-path>");
            exit(0);
        }
//        args = new String[2];
//        args[0] = "/home/suchana/Downloads/en.docs.2011/en_TheTelegraph_2001-2010/";
//        args[1] = "/store/collection/";   
        collectionPath = args[0];
        dumpPath = args[1];
        MakeTelegraphDumpAnalyzed mtd = new MakeTelegraphDumpAnalyzed(collectionPath, dumpPath);

        mtd.createDump(mtd.collectionPath);
        fileWriter.close();
        System.out.println("Complete dumping... : Total dumped documents : " + docCount);
    }
    
}