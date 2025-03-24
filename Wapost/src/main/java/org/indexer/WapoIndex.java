package org.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author suchana
 */
public class WapoIndex {
    String               collectionPath;
    String               indexPath;
    String               stopWordPath;
    EnglishAnalyzer      analyzer;
    static IndexWriter   writer;
    static int           docCount;
    List<String>         stopWordList;
    JSONParser           jsonParser;
    
    
    public WapoIndex(String collectionPath, String indexPath) throws IOException {

        this.collectionPath = collectionPath;
        this.indexPath = indexPath;
        stopWordPath = "/home/suchana/smart-stopwords";
        
        //for using default stopwordlist
        analyzer = new EnglishAnalyzer(); //org.apache.lucene.analysis.en.EnglishAnalyzer; this uses default stopword list
        //for using external stopword list
//        stopWordList = getStopwordList(stopWordPath);                         
//        analyzer = new EnglishAnalyzer(StopFilter.makeStopSet(stopWordList)); // org.apache.lucene.analysis.core.StopFilter
        
        Directory dir; // org.apache.lucene.store.Directory

        // FSDirectory.open(file-path-of-the-directory)
        dir = FSDirectory.open((new File(this.indexPath)).toPath()); // org.apache.lucene.store.FSDirectory

        IndexWriterConfig iwc; // org.apache.lucene.index.IndexWriterConfig
        iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // other options: APPEND, CREATE_OR_APPEND
        
        iwc.setRAMBufferSizeMB(2048); // default is 16 (MB)
        iwc.setUseCompoundFile(false);
        iwc.setMergeScheduler(new ConcurrentMergeScheduler());

        writer = new IndexWriter(dir, iwc);
        docCount = 0;
        jsonParser = new JSONParser();
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
    
    FieldType constructStrField(boolean toStore, boolean toStore_tokenized, boolean toStore_TermVector) {

        FieldType ft = new FieldType();
        ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        ft.setStored(toStore);
        // ft.setTokenized(toStore_tokenized);
         ft.setStoreTermVectors(toStore_TermVector);
        // ft.setStoreTermVectorPositions(true);

        return ft;
    }
    
    private void createIndex(String collectionPath) throws FileNotFoundException, IOException, NullPointerException, ParseException {
        
        System.out.println("Indexing started...");
        File colFile = new File(collectionPath);
        if(colFile.isDirectory())
            collectionDirectory(colFile);
        else
            indexFile(colFile);
    }

    private void collectionDirectory(File colDir) throws FileNotFoundException, IOException, NullPointerException, ParseException {
        
        File[] files = colDir.listFiles();
        for (File file : files) {
            System.out.println("Indexing file : " + file);
            if (file.isDirectory()) {
                System.out.println("It has subdirectories...\n");
                collectionDirectory(file);  // calling this function recursively to access all the subfolders in the directory
            }
            else
                indexFile(file);
        }
    }
    
    private void indexFile(File colFile) throws FileNotFoundException, IOException, ParseException {
        
        Object eachDoc;
        BufferedReader reader;
        
        try{
            reader = new BufferedReader(new FileReader(colFile));
            String line = reader.readLine();
            while (line != null) {
		//System.out.println(line);
                eachDoc = jsonParser.parse(line);
                parseJson((JSONObject) eachDoc);
                line = reader.readLine();
            }
            reader.close();
        }catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    private void parseJson(JSONObject eachDoc) throws IOException {
        int i = 1;
        Document doc;
        String rawArticle = "", analyzedText;
        JSONArray contentObj;
        JSONObject subContentValue;
        
        doc = new Document();
        
        FieldType ft_store_tv = constructStrField(true, true, true);
        FieldType ft_store = constructStrField(true, false, false);
        
        if(eachDoc.get("title") != null && eachDoc.get("author") != null && eachDoc.get("article_url") != null){
                
            doc.add(new Field("docId", eachDoc.get("id").toString(), ft_store));
            System.out.println("ID : " + eachDoc.get("id"));
            doc.add(new Field("url", eachDoc.get("article_url").toString(), ft_store));  
            System.out.println("URL : " + eachDoc.get("article_url"));
            doc.add(new Field("title", eachDoc.get("title").toString(), ft_store));
            System.out.println("TITLE : " + eachDoc.get("title"));
//            doc.add(new Field("author", eachDoc.get("author").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));   
//            doc.add(new Field("publishedDate", String.valueOf(eachDoc.get("published_date")), Field.Store.YES, Field.Index.NOT_ANALYZED));   
//            doc.add(new Field("type", eachDoc.get("type").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));   
//            doc.add(new Field("source", eachDoc.get("source").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));   

            contentObj = (JSONArray) eachDoc.get("contents"); 
            for(Object subContent : contentObj){
                
                if((JSONObject) subContent != null) {
                    subContentValue = (JSONObject) subContent;

                    if(subContentValue.get("content") != null && subContentValue.get("type").equals("kicker"))
                        doc.add(new Field("category", (String) subContentValue.get("content"), ft_store_tv));   
                    else if (subContentValue.get("content") != null && subContentValue.get("type").equals("sanitized_html"))
                        rawArticle = rawArticle + (String) subContentValue.get("content");       
                } 
            } 
        }
//        System.out.println("RAW CONTENT : " + rawArticle);
        doc.add(new Field("rawcontent", rawArticle, ft_store_tv));
        analyzedText = analyzeText(analyzer, cleanText(rawArticle), "content").toString();
//        System.out.println("CONTENT : " + analyzedText);
        doc.add(new Field("content", analyzedText, ft_store_tv));
        writer.addDocument(doc);
        
        System.out.println("Indexed doc no. : " + ++docCount + "\n");
        
    }
    
    private String cleanText(String rawArticle){
        
        String pattern = "\\<(.*?)\\>";
        //rawArticle = rawArticle.replaceAll(pattern, "").replaceAll("[^a-zA-Z0-9\\.\\,]", " ").trim().replaceAll(" +", " ");
        rawArticle = rawArticle.replaceAll(pattern, "").trim().replaceAll(" +", " ");
//        System.out.println("++++++++ : " + rawArticle);
        
        return rawArticle;
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
    
    public static void main(String[] args) throws IOException, FileNotFoundException, NullPointerException, ParseException {
        
//        ----for static path
//        String INDEX_PATH = "./data/test_index_foo.index";
//        String INPUT_PATH = "./data/test_col.json";
//        
//        WapoIndex indexer = new WapoIndex(INPUT_PATH, INDEX_PATH);
//        indexer.createIndex(INPUT_PATH);
//        writer.close();
        
//        ----via command line arguments
        
        String collectionPath, indexPath;
        collectionPath = args[0];
        indexPath = args[1];

        WapoIndex indexer = new WapoIndex(collectionPath, indexPath);
        indexer.createIndex(collectionPath);
        writer.close();
        
        System.out.println("Complete indexing... : Total indexed documents : " + docCount);
        // indexer.delete();
    }
    
    public void delete() throws IOException {
        writer.deleteDocuments(new TermQuery(new Term("asdplot", "murder")));
//        writer.commit();
        writer.close();
    }
 
}
