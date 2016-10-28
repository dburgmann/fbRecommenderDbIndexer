/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.indexing;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Daniel
 */
public class IndexCreator {
    public static final FieldType STORED;
    public static final FieldType NOT_STORED;
    public static final FieldType STORED_NOT_TOKENIZED;
    public static final FieldType NOT_STORED_NOT_TOKENIZED;
    
    public static final String   STOPWORD_FILE = "";
    
    
    private Analyzer            analyzer;                                       //Analyzer used for indexing
    private IndexWriterConfig   config;                                         //Indexwriter config used
    private IndexWriter         writer;                                         //Indexwriter
    
    
    static{
        STORED = new FieldType();
        STORED.setStoreTermVectors(false);
        STORED.setTokenized(true);
        STORED.setIndexed(true);
        STORED.setStored(true);
        STORED.freeze();
            
        NOT_STORED = new FieldType();
        NOT_STORED.setStoreTermVectors(false);
        NOT_STORED.setTokenized(true);
        NOT_STORED.setIndexed(true);
        NOT_STORED.setStored(false);
        NOT_STORED.freeze();
       
        STORED_NOT_TOKENIZED = new FieldType();
        STORED_NOT_TOKENIZED.setStoreTermVectors(false);
        STORED_NOT_TOKENIZED.setTokenized(false);
        STORED_NOT_TOKENIZED.setIndexed(true);
        STORED_NOT_TOKENIZED.setStored(true);
        STORED_NOT_TOKENIZED.freeze();
        
        NOT_STORED_NOT_TOKENIZED = new FieldType();
        NOT_STORED_NOT_TOKENIZED.setStoreTermVectors(false);
        NOT_STORED_NOT_TOKENIZED.setTokenized(false);
        NOT_STORED_NOT_TOKENIZED.setIndexed(true);
        NOT_STORED_NOT_TOKENIZED.setStored(false);
        NOT_STORED_NOT_TOKENIZED.freeze();
    }
    
    
   
    
    /**
     * Sets up a new index creator for creating a index in given directory
     * @param dir 
     */
    public IndexCreator() {
        //init default analyzer
        CharArraySet stopWords;     //list of stopword
        try {
            FileReader fr = new FileReader(STOPWORD_FILE);//Config.loadFile(Config.GERMAN_STOPWORD_FILE);
            stopWords  = WordlistLoader.getSnowballWordSet(fr, Version.LUCENE_40);
            fr.close();
        } catch (IOException ex) {
            stopWords = GermanAnalyzer.getDefaultStopSet();
        }
        
        analyzer = new GermanAnalyzer(Version.LUCENE_40);               //set default analyzer  
    }
    
    
    /**
     * Initialises the index creator for writing the index.
     * This freezes the analyzer setting, changing the analyzer will have
     * no effect after calling this method.
     * @throws IOException 
     */
    public void init(Directory dir) throws IOException{
        config  = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        writer  = new IndexWriter(dir, config);
    }
   
    
    /**
     * Closes the writer. Needs to be called after finishing creation
     * @throws IOException 
     */
    public void close() throws IOException{
        writer.close();
    }
    
    
    /**
     * Adds a new document to the index
     * @throws IOException 
     */
    public void addDocument(List<DocValue> docValues) throws IOException{
        Document    doc     = new Document();
        for(DocValue value : docValues){
            doc.add(new Field(value.field, value.value, value.type));
        }
             
        writer.addDocument(doc);
    }

    
    /**
     * Changes the default Analyzer. 
     * Default Analyzer is GermanAnalyzer.
     * Needs to be called before init-method is called.
     * @param analyzer 
     */
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }
    
    
    
    public static class DocValue{
        public String    field = "";
        public String    value = "";
        public FieldType type  = null;

        public DocValue(String field, String value, FieldType type) {
            this.field = field;
            this.value = value;
            this.type = type;
        }
        
        
    }
}
