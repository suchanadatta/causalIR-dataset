package wapo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author suchana
 */

public class makeWapoTopics {

    String             indexPath;
    String             queryFilePath;
    String             parsedQueryPath;
    static FileWriter  fileWriter;
        
    
    public makeWapoTopics(String indexPath, String queryFilePath, String paersedQueryPath) throws IOException {

        this.indexPath = indexPath;
        this.queryFilePath = queryFilePath;
        this.parsedQueryPath = paersedQueryPath;
        fileWriter = new FileWriter(paersedQueryPath + "2019_TREC_topics_parsed.xml");
        fileWriter.write("<topics>");
    }
    
    
    public String parseTrecTopicFile(String indexPath, String queryFilePath, String parsedQueryPath) throws IOException, ParseException {
        
        BufferedReader reader;
        Pattern qNum, docID;
        Matcher qNumMat, docIDMat;
        String parseQID = "", parseDocID = "";
        
        qNum = Pattern.compile("<num> Number:(.+?)</num>");
        docID = Pattern.compile("<docid>(.+?)</docid>");
                
        try{
            reader = new BufferedReader(new FileReader(queryFilePath));
            String line = reader.readLine();
            while (line != null) {
		//System.out.println("LINE : " + line);
                qNumMat = qNum.matcher(line);
                docIDMat = docID.matcher(line);
                if (qNumMat.find()){ 
                    parseQID = qNumMat.group(1).replaceAll(" ", "");
                    System.out.println("+++++ : " + parseQID);
                    fileWriter.write("\n\n<top>" + "\n<num>" + parseQID + "</num>");
                }
                else if(docIDMat.find()){
                    parseDocID = docIDMat.group(1).replaceAll(" ", "");
                    System.out.println("%%%%% : " + parseDocID);
                    createTopic(parseDocID);
                }
                line = reader.readLine();
            }
            reader.close();
        }catch (FileNotFoundException ex) {
        }        
        return parseDocID; 
    }
    
    
    public void createTopic(String parseDocID) throws IOException, ParseException {
        
        IndexSearcher searcher = createSearcher();        
        TopDocs foundDocs = searchIndex(parseDocID, searcher);
        //System.out.println("Total Results :: " + foundDocs.totalHits); //total no. of hits
         
        for (ScoreDoc sd : foundDocs.scoreDocs) 
        {
            Document d = searcher.doc(sd.doc);
            System.out.println("docID : " + d.get("docId") + "\nArticle : " + d.get("content") + "\nScore : " + sd.score + "\nTitle : " + d.get("title") + "\n");
            fileWriter.write("\n<title>" + d.get("title") + "</title>");
            fileWriter.write("\n<docid>" + d.get("docId") + "</docid>");
            fileWriter.write("\n<pubDate>" + d.get("publishedDate") + "</pubDate>");
            fileWriter.write("\n</top>");
            //fetch whatever field we want to fetch from the index
        }        
    }    
    
    
    public IndexSearcher createSearcher() throws IOException {
        
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
    
    
    private TopDocs searchIndex(String searchByText, IndexSearcher searcher) throws ParseException, IOException {
        
        QueryParser qp = new QueryParser("docId", new StandardAnalyzer());
        Query query = qp.parse(searchByText);
        TopDocs hits = searcher.search(query, 1); //no. of top docs to be retrieved
        return hits;        
    }
    
    public static void main(String[] args) throws IOException, ParseException{

        String indexPath, queryFilePath, parsedQueryPath;
//        if(args.length!=3) {
//            System.out.println("Usage: java lucenelearn.makeWapoTopics <index-path> <query-file-path> <parsed-query-path>");
//            exit(0);
//        }
        args = new String[3];
        args[0] = "/home/suchana/store/TREC_newsIR/WashingtonPost.v2/WaPo_index/";
        args[1] = "/home/suchana/store/TREC_newsIR/2019_TREC_topics.xml";
        args[2] = "/home/suchana/store/TREC_newsIR/";
        indexPath = args[0];
        queryFilePath = args[1];
        parsedQueryPath = args[2];
        
        makeWapoTopics makeTopics = new makeWapoTopics(indexPath, queryFilePath, parsedQueryPath);
        makeTopics.parseTrecTopicFile(makeTopics.indexPath, makeTopics.queryFilePath, makeTopics.parsedQueryPath);
        fileWriter.write("\n\n</topics>");
        fileWriter.close();
        //System.out.println("Complete indexing... : Total indexed documents : " + docCount);
    }
}