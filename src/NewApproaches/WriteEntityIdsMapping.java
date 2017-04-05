/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NewApproaches;

import DataModel.EntityProfile;
import DataReader.EntityReader.EntitySerializationReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vefthym
 */
public class WriteEntityIdsMapping {
    
    public List<String> getEntityUrlsInOrder(String entityProfilesInputPath) {  
        System.out.println("\n\nReading entities from "+entityProfilesInputPath);
        List<EntityProfile> profiles = new EntitySerializationReader(entityProfilesInputPath).getEntityProfiles();        
        List<String> profilesUrls = new ArrayList<>();
        profiles.stream().forEach(profile -> profilesUrls.add(profile.getEntityUrl()));
        System.out.println("Successfully read "+profiles.size()+" entities.");
        return profilesUrls;
    }
    
    public void writeEntityMappingsInOrder(List<String> entityUrls, String entityUrlsOutputPath) {
        System.out.println("Writing entity urls to "+entityUrlsOutputPath);
        int i = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(entityUrlsOutputPath))) {            
            for (String entityUrl : entityUrls) {
                bw.write(entityUrl+"\t"+i);                
                bw.newLine();
                i++;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("Successfully wrote "+i+" entityUrls to "+entityUrlsOutputPath);
    }
    
    public static void main(String[] args) {
        
        String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String inputPath1 = mainPath+"restaurant1Profiles";
        String inputPath2 = mainPath+"restaurant2Profiles";
        
        //String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
        //String inputPath1 = mainPath+"rexaProfiles";
        //String inputPath2 = mainPath+"swetodblp_april_2008Profiles";
        
        //String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\yago-imdb\\";
        //String inputPath1 = mainPath+"yagoProfiles";
        //String inputPath2 = mainPath+"imdbProfiles";
        
        //String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\bbcMusic\\";
        //String inputPath1 = mainPath+"bbc-musicNewNoRdfProfiles";
        //String inputPath2 = mainPath+"dbpedia37NewNoSameAsNoWikipediaProfiles";
        
        if (args.length == 2) {
            inputPath1 = args[0];
            inputPath2 = args[1];
        }
        
        String outputPath1 = inputPath1+"EntityIds.txt";
        String outputPath2 = inputPath2+"EntityIds.txt";
        
        WriteEntityIdsMapping weim = new WriteEntityIdsMapping();
        List<String> entityUrls1 = weim.getEntityUrlsInOrder(inputPath1);
        weim.writeEntityMappingsInOrder(entityUrls1, outputPath1);
        
        entityUrls1.clear();
        
        List<String> entityUrls2 = weim.getEntityUrlsInOrder(inputPath2);
        weim.writeEntityMappingsInOrder(entityUrls2, outputPath2);
    }
    
}
