package NewApproaches;

import DataModel.EntityProfile;
import DataReader.EntityReader.EntitySerializationReader;
import Utilities.Converter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class GetNeighbors {

    private final String inputPath;
    private final String outputPath;

    public GetNeighbors(String inPath, String outPath) {
        inputPath = inPath;
        outputPath = outPath;
    }

    public void applyProcessing() {
        EntitySerializationReader eReader1 = new EntitySerializationReader(inputPath);
        final List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());

        int noOfEntities = profiles1.size();
        final EntityProfile[] profilesArray = profiles1.toArray(new EntityProfile[noOfEntities]);

        final Map<String, Integer> urlToPosition = new HashMap<>();
        for (int i = 0; i < noOfEntities; i++) {
            urlToPosition.put(profilesArray[i].getEntityUrl(), i);
        }

        final List<Integer>[] entityToNeighborsList = new List[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            entityToNeighborsList[i] = new ArrayList<>();

            final Set<String> values = profilesArray[i].getAllValues();
            for (String value : values) {
                Integer position = urlToPosition.get(value);
                if (position != null) {
                    entityToNeighborsList[i].add(position);
                }
            }
        }

        final int[][] entityIdToNeighborIds = new int[noOfEntities][];
        for (int i = 0; i < noOfEntities; i++) {
            if (entityToNeighborsList[i].isEmpty()) {
                entityIdToNeighborIds[i] = null;
            } else {
                Collections.sort(entityToNeighborsList[i]);
                entityIdToNeighborIds[i] = Converter.convertCollectionToArray(entityToNeighborsList[i]);
            }
        }
        
        eReader1.storeSerializedObject(entityIdToNeighborIds, outputPath);
    }
    
    
    public static void main (String[] args) {
//        String basePath = "/home/gpapadakis/data/";
        String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String[] inputPaths = { 
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
            inputPaths = args;
        }
        
        String[] outputPaths = new String[inputPaths.length];
        for (int i=0; i < outputPaths.length; ++i) {
            outputPaths[i] = inputPaths[i].replaceAll("Profiles$", "Neighbors");
        }
        
        for (int i = 0; i < inputPaths.length; i++) {
            GetNeighbors gn = new GetNeighbors(inputPaths[i], outputPaths[i]);
            gn.applyProcessing();
        }
    }
}
