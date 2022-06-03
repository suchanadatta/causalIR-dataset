
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

public class Trec678DumpFreeFlow {
    
    String                collectionPath;
    String                dumpPath;
    Analyzer              analyzer;            //org.apache.lucene.analysis.Analyzer; we use same analyzer for searching
    String                stopWordPath;
    List<String>          stopWordList;
    static int            docPerFile, fileCount, totalDocs;
    static FileWriter     writer;
    
    
    public Trec678DumpFreeFlow(String collectionPath, String dumpPath) throws IOException {

        this.collectionPath = collectionPath;
        this.dumpPath = dumpPath;
        dumpPath = dumpPath + "trec678_free_flow.dump";
        writer = new FileWriter(dumpPath);
        stopWordPath = "/home/suchana/smart-stopwords";

        //for using default stopwordlist
        //analyzer = new EnglishAnalyzer();                                     //org.apache.lucene.analysis.en.EnglishAnalyzer; this uses default stopword list
        //for using external stopword list
        stopWordList = getStopwordList(stopWordPath);                         
        analyzer = new EnglishAnalyzer(StopFilter.makeStopSet(stopWordList));   // org.apache.lucene.analysis.core.StopFilter
    }    
    
    private List<String> getStopwordList(String stopwordPath) {
        
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

    void createDump(String collectionPath) throws FileNotFoundException, IOException, NullPointerException {

            System.out.println("Dumping started...");
            File colFile = new File(collectionPath);
            if(colFile.isDirectory())
                collectionDirectory(colFile);
            else
                makeDump(colFile);
    }

    private void collectionDirectory(File colDir) throws FileNotFoundException, IOException, NullPointerException {
        
        File[] files = colDir.listFiles();
        for (File file : files) {
            System.out.println("Dumping file : " + file);
            fileCount++;
            docPerFile = 0;
            if (file.isDirectory()) {
                System.out.println("It has subdirectories...\n");
                collectionDirectory(file);  // calling this function recursively to access all the subfolders in the directory
            }
            else
                makeDump(file);
        }
    }
    
    private void makeDump(File colFile) throws FileNotFoundException, IOException {
        
        String fileContent, parseContent, analyzed_content, docNo = null;
        Pattern p_docno, p_text;
        Matcher m_docno, m_text;
        BufferedReader br = new BufferedReader(new FileReader(colFile));
        String line = br.readLine();
        StringBuilder sb = new StringBuilder(); 
        while(line != null){ 
            sb.append(line).append("\n");
            line = br.readLine();
        } 
        fileContent = sb.toString();
//        System.out.println("file content : " + fileContent);
        fileContent = fileContent.replaceAll("\"", "").replaceAll("", "").replaceAll("\n", "").replaceAll("\r", "");
//        System.out.println("file content : " + fileContent);
        
        p_docno = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
        m_docno = p_docno.matcher(fileContent);
        p_text = Pattern.compile("<TEXT>(.+?)</TEXT>");
        m_text = p_text.matcher(fileContent);
        while (m_docno.find()) {
            docNo = m_docno.group(1).trim().replaceAll("\\s{2,}", " ");
            System.out.println("doc no : " + docNo);
//            fileWriter.write(docNo + "\t");
            if (m_text.find()){
                parseContent = m_text.group(1).trim().replaceAll("\\s{2,}", " ");
                docPerFile++;
                //System.out.println("parsed content : " + parseContent);
                analyzed_content = analyzeText(analyzer, parseContent, "content").toString();
                writer.write(analyzed_content + " ");
            }
        }
        System.out.println("Total no. of articles in the file : " + docPerFile);
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
            System.out.println("Usage: java lucenelearn.Trec678DumpFreeFlow <collection-path> <dump-path>");
            exit(0);
        }
//        args = new String[2];
//        args[0] = "/home/suchana/Downloads/en.docs.2011/en_TheTelegraph_2001-2010/";
//        args[1] = "/store/collection/";
        collectionPath = args[0];
        dumpPath = args[1];
        Trec678DumpFreeFlow mtd = new Trec678DumpFreeFlow(collectionPath, dumpPath);

        mtd.createDump(mtd.collectionPath);
        writer.close();
        System.out.println("Complete dumping... : Total no. of files parsed : " + fileCount);
    }    
}