/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataReader.GroundTruthReader;

import com.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author vefthym
 */
public class FixImdbGoldStandard {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String imdbPersonGoldPath = "/home/vassilis/datasets/imdb_yago/imdbpersongold.txt";
        String imdbMovieGoldPath = "/home/vassilis/datasets/imdb_yago/imdbmoviegold.txt";
        String imdbIdentifierPath = "/home/vassilis/datasets/imdb_yago/IMDBIdentifier.tsv";
        String outputPath = "/home/vassilis/datasets/imdb_yago/imdbgoldFinal.csv";
        
        if (args.length == 4) {
            imdbPersonGoldPath = args[0];
            imdbMovieGoldPath = args[1];
            imdbIdentifierPath = args[2];
            outputPath = args[3];
        }
        
        final char SEPARATOR = '\t';
        
        //read the identifiers and load them in memory
        Map<String,String> imdbIdentifier = new HashMap<>();     //key: imdbIdentifier(0696378), value: datasetURL(p980269)   
        CSVReader reader = new CSVReader(new FileReader(imdbIdentifierPath), SEPARATOR);
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            imdbIdentifier.put(nextLine[1].substring(2), nextLine[0]);
        }        
        reader.close();
        
        int counter = 0;
        //read the gold standard and replace the imdb identifiers with the dataset identifiers        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), "utf-8"))) {
            reader = new CSVReader(new FileReader(imdbPersonGoldPath), SEPARATOR);
            while ((nextLine = reader.readNext()) != null) {
                //first part is yago id
                String yagoId = nextLine[0];
                //yagoId = StringEscapeUtils.unescapeJava(yagoId); //persons' URLs are already unescaped :)
                yagoId = '<'+yagoId+'>';                
                
                String imdbId = imdbIdentifier.get(nextLine[1]);
                
                if (imdbId != null) {                
                    writer.write(yagoId+','+imdbId+"\n");
                    counter++;
                }
            }
            reader.close();
            imdbIdentifier.clear(); //not needed any more
            
            //now write the movie matches after the person matches
            
            try (BufferedReader br = new BufferedReader(new FileReader(imdbMovieGoldPath))) { //CSVReader destroys the encoding 
                String line;
                while ((line = br.readLine()) != null) {
                    nextLine = line.split("\t");
                    //first part is yago url. transform to yago infix, as in yago.tsv file: <yagoId>
                    String yagoURL = nextLine[0];
                    yagoURL = StringEscapeUtils.unescapeJava(yagoURL);
                    String yagoId = yagoURL;
                    if (yagoURL.contains("/")) {
                        yagoId = yagoURL.substring(yagoURL.lastIndexOf("/")+1);
                    }
                    yagoId = "<"+yagoId+">";
                    
                    
                    //second part is imdb url. transform to a movie identifier, as in imdb.tsv: ttxxxxxxx, where x are digits
                    String imdbURL = nextLine[1];
                    String imdbId = "tt"+imdbURL.substring(imdbURL.lastIndexOf("/")+1);
                    
                    writer.write(yagoId+','+imdbId+"\n");
                    counter++;
                }
            }
        }             
        
        System.out.println(counter +" matches were written in "+outputPath);
        
    }    
}
