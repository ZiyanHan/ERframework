package NewApproaches;

import DataModel.Attribute;
import DataModel.EntityProfile;
import DataReader.AbstractReader;
import DataReader.EntityReader.EntitySerializationReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class GetNeighborProfiles {

    private final String entitiesPath;
    private final String neighborsPath;
    private final String outputPath;

    public GetNeighborProfiles(String inPath1, String inPath2, String outPath) {
        entitiesPath = inPath1;
        neighborsPath = inPath2;
        outputPath = outPath;
    }

    public void applyProcessing() {
        EntitySerializationReader eReader1 = new EntitySerializationReader(entitiesPath);
        final List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());

        int noOfEntities = profiles1.size();
        final EntityProfile[] profilesArray = profiles1.toArray(new EntityProfile[noOfEntities]);
        int[][] entityIdToNeighborIds = (int[][]) AbstractReader.loadSerializedObject(neighborsPath);
        
        final List<EntityProfile> newProfiles = new ArrayList<>();
        for (int i = 0; i < noOfEntities; i++) {
            EntityProfile newProfile = new EntityProfile(profilesArray[i].getEntityUrl());
            if (entityIdToNeighborIds[i] != null) {
                for (int neighborId : entityIdToNeighborIds[i]) {
                    EntityProfile neighbor = profilesArray[neighborId];
                    Set<Attribute> attributes = neighbor.getAttributes();
                    for (Attribute attr : attributes) {
                        newProfile.addAttribute(attr.getName(), attr.getValue());
                    }
                }
            }
            newProfiles.add(newProfile);
        }
             
        eReader1.storeSerializedObject(newProfiles, outputPath);
    }
    
    
    public static void main (String[] args) {
        
//        String basePath = "/home/gpapadakis/data/";
        String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        
        String[] entityPaths = { 
            basePath+"restaurant1Profiles",
            basePath+"restaurant2Profiles",
//            basePath+"newRestaurant/restaurant1Profiles",
//            basePath+"newRestaurant/restaurant2Profiles",
//            basePath+"newBibliographicalRecords/rexaProfiles",
//            basePath+"newBibliographicalRecords/swetodblp_april_2008Profiles",
//            basePath+"newImdb/yagoProfiles",
//            basePath+"newImdb/imdbProfiles"
        };
        
        if (args.length != 0) {
            entityPaths = args;
        }
        
        
        String[] neighborPaths = new String[entityPaths.length];
        for (int i=0; i < neighborPaths.length; ++i) {
            neighborPaths[i] = entityPaths[i].replaceAll("Profiles$", "Neighbors");
        }
        
        String[] outputPaths = new String[entityPaths.length];
        for (int i=0; i < outputPaths.length; ++i) {
            outputPaths[i] = entityPaths[i].replaceAll("Profiles$", "NeighborProfiles");
        }
        
        for (int i = 0; i < entityPaths.length; i++) {
            GetNeighborProfiles gnp = new GetNeighborProfiles(entityPaths[i], neighborPaths[i], outputPaths[i]);
            gnp.applyProcessing();
        }
    }
}
