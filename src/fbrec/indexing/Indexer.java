/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.indexing;

import fbrec.database.Products;
import fbrec.database.Products.Product;
import fbrec.indexing.IndexCreator.DocValue;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.lucene.store.Directory;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 *
 * @author Daniel
 */
public class Indexer {
    
    private IndexCreator    creator;
    private int             stepping    = 100000;
    
    
    //main method for creating database index
    public static void main(String args[]){
        //args[0] = "/Users/Daniel/Schreibtisch/index";
        
        if(args.length < 1 || args[0] == null){
            System.out.println("You need to specify a path!");
            return;
        }
        
        Indexer indexer = new Indexer(args[0]);
        indexer.createIndex();
    }
    

    public Indexer(String indexDir) {
        try{
            //load directory
            Directory dir = new SimpleFSDirectory(new File(indexDir)); 
            
            //init creator
            creator = new IndexCreator();
            creator.init(dir);
        }
        catch(Exception e){
	    System.out.println("Could not access indexing directory!");
            e.printStackTrace();
	}
    }
    
    /**
     * @param args the command line arguments
     */
    public void createIndex(){
        try{
            Boolean             dbCompleted = false;
            int                 offset      = 0;
            int                 count;
            List<Product>       result;
            List<DocValue>      docValues;        
            
	    //retrieve data from db step by step
	    while(dbCompleted == false){              
		//Select next products
                System.out.println("Get entries "+offset+" to "+(offset+stepping)+"...");
                result = Products.getAll(offset, stepping);
                
                
                count = 0;
                System.out.println("Indexing entries...");
                for(Product prod : result){
                    docValues = new ArrayList<DocValue>();
                    docValues.add(new DocValue(Products.ID_FIELD, String.valueOf(prod.ID), IndexCreator.STORED_NOT_TOKENIZED));
                    if(prod.title       != null) docValues.add(new DocValue(Products.TITLE_FIELD, prod.title, IndexCreator.STORED));
                    if(prod.description != null) docValues.add(new DocValue(Products.TEXT_FIELD, prod.description, IndexCreator.NOT_STORED));
                    if(prod.brand       != null) docValues.add(new DocValue(Products.BRAND_FIELD, prod.brand, IndexCreator.NOT_STORED));
                    if(prod.genderAge   != null) docValues.add(new DocValue(Products.GENDER_AGE_FIELD, prod.genderAge, IndexCreator.NOT_STORED));
                    
                    for(String cat : prod.categories){
                        docValues.add(new DocValue(Products.CATEGORY_FIELD, cat, IndexCreator.NOT_STORED));
                    }
                                       
		    creator.addDocument(docValues);

                    count++;
                }                
                offset += stepping;                
        
                //log
                System.out.println("Indexing completed.");
                System.out.println(count+" entries were indexed.");
                
                
                //check if db was loaded completely
                if(count == 0){
                    dbCompleted = true;
                }
	    }
            creator.close();
	}
	catch(Exception e){
            e.printStackTrace();
	}
        
    }
}
