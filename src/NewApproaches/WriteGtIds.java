/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NewApproaches;

import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author vefthym
 */
public class WriteGtIds {
    
    public void writeDuplicatesIdsInOrder(String gtPath, String outputPath) {
        System.out.println("Writing ids of matches to "+outputPath);
        
        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        Set<IdDuplicates> duplicates = gtReader.getDuplicatePairs(null);
        
        int i = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {            
            for (IdDuplicates match : duplicates) {
                bw.write(match.getEntityId1()+"\t"+match.getEntityId2());                
                bw.newLine();
                i++;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("Successfully wrote "+i+" entityUrls to "+outputPath);
    }
    
    public static void main(String[] args) {
        
        //String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        //String gtPath = mainPath+"restaurantIdDuplicates";        
        
        String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";        
        String gtPath = mainPath+"rexa_dblp_goldstandardIdDuplicates";
        
        //String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\yago-imdb\\";
                
        //String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\bbcMusic\\";        
        
        if (args.length == 1) {
            gtPath = args[0];
        }        
        
        new WriteGtIds().writeDuplicatesIdsInOrder(gtPath, gtPath+"EntityIds.txt");                
    }
    
}
