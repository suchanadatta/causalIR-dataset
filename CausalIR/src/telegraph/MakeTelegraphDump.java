/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author suchana
 */

public class MakeTelegraphDump {
    
    String                collectionPath;
    String                dumpPath;
    static int            docCount;
    static FileWriter     fileWriter;
    
    
    public MakeTelegraphDump(String collectionPath, String dumpPath) throws IOException {

        this.collectionPath = collectionPath;
        this.dumpPath = dumpPath; 
        docCount = 0;
        fileWriter = new FileWriter(dumpPath + "telegraph_01_11.dump");
    }
    
    public void createDump(String collectionPath) throws FileNotFoundException, IOException {
        
        System.out.println("Dumping started...");
        File colFile = new File(collectionPath);
        if(colFile.isDirectory())
            collectionDirectory(colFile);
        else
            getFileContent(colFile);
            
    }
    
    private void collectionDirectory(File colDir) throws FileNotFoundException, IOException, NullPointerException {
        
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
    
    // for storing raw content
    private void getFileContent(File colFile) throws FileNotFoundException, IOException {
        
        String fileContent;
        String parseContent, docid = null;
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
                parseContent = m_text.group(1).trim().replaceAll("\\s{2,}", " ");
                fileWriter.write(parseContent + "\n");
            }
            docCount++;
        }
        //System.out.println("parsed content : " + parseContent);
        System.out.println("DOCID : " + docid + ", COUNT : " + docCount);
    }        
    
    public static void main(String[] args) throws IOException {

        String collectionPath, dumpPath;
        if(args.length!=2) {
            System.out.println("Usage: java preprocessing.MakeTelegraphDump <collection-path> <dump-path>");
            exit(0);
        }
//        args = new String[2];
//        args[0] = "/home/suchana/Downloads/en.docs.2011/en_TheTelegraph_2001-2010/";
//        args[1] = "/home/suchana/Downloads/en.docs.2011/telegraph_index_NC_TV/";
        collectionPath = args[0];
        dumpPath = args[1];
        MakeTelegraphDump mtd = new MakeTelegraphDump(collectionPath, dumpPath);

        mtd.createDump(mtd.collectionPath);
        fileWriter.close();
        System.out.println("Complete dumping... : Total dumped documents : " + docCount);
    }
    
}