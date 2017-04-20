/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NewApproaches;

import DataModel.Attribute;
import DataModel.EntityProfile;
import DataReader.EntityReader.EntitySerializationReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author vefthym
 */
public class WriteTsvFromSerializedProfiles {
    
    public void writeEntityTriplesInTsv(String entityProfilesInputPath, String tsvOutputPath) {
        System.out.println("\n\nReading entities from "+entityProfilesInputPath);
        List<EntityProfile> profiles = new EntitySerializationReader(entityProfilesInputPath).getEntityProfiles();        
        
        System.out.println("Writing entity triples to "+tsvOutputPath);
        int numEntities = 0;
        int numLines = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tsvOutputPath))) {            
            for (EntityProfile entity : profiles) {
                String entityUrl = entity.getEntityUrl();
                for (Attribute att : entity.getAttributes()) {
                    bw.write(entityUrl+"\t"+att.getName()+"\t"+att.getValue());                
                    bw.newLine();      
                    numLines++;
                }
                numEntities++;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("Successfully wrote "+numEntities+" entities ("+numLines+" lines) to "+tsvOutputPath);
    }
    
    public static void main(String[] args) {        
        String mainPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
        String inputPath1 = mainPath+"rexaProfiles";
        String inputPath2 = mainPath+"swetodblp_april_2008Profiles";
        
        if (args.length == 2) {
            inputPath1 = args[0];
            inputPath2 = args[1];
        }
        
        String outputPath1 = inputPath1+"Deserialized.tsv";
        String outputPath2 = inputPath2+"Deserialized.tsv";
        
        WriteTsvFromSerializedProfiles wtsv = new WriteTsvFromSerializedProfiles();
        wtsv.writeEntityTriplesInTsv(inputPath1, outputPath1);        
        wtsv.writeEntityTriplesInTsv(inputPath2, outputPath2);        
    }
    
}
