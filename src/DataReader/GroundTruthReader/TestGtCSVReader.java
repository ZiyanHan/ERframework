package DataReader.GroundTruthReader;

import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;

import java.util.List;
import java.util.Set;

/**
 *
 * @author vefthym
 */

public class TestGtCSVReader {
    public static void main(String[] args) {
        //default input
        String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\bbcMusic\\";
//        String basePath = "/home/vassilis/datasets/imdb_yago/";        
    	String entityFilePath1 = basePath+"bbc-musicNewNoRdfProfiles";
    	String entityFilePath2 = basePath+"dbpedia37processedNewNoSameAsNoWikipediaSortedProfiles";
        String gtFilePath = basePath+"bbc-music_groundTruthUTF8.txt";
        //custom input
        if (args.length == 3) {
            entityFilePath1 = args[0];
            entityFilePath2 = args[1];
            gtFilePath = args[2];
        }
        
        EntitySerializationReader esr1 = new EntitySerializationReader(entityFilePath1);
        EntitySerializationReader esr2 = new EntitySerializationReader(entityFilePath2);
        System.out.println("Loading "+gtFilePath);
        GtCSVReader gtCSVReader = new GtCSVReader(gtFilePath);
        gtCSVReader.setSeparator(',');
        System.out.println("Loading "+entityFilePath1);
        List<EntityProfile> profiles1 = esr1.getEntityProfiles();
        System.out.println("Loading "+entityFilePath2);
        List<EntityProfile> profiles2 = esr2.getEntityProfiles();
        
        System.out.println("Loading duplicates");
        Set<IdDuplicates> duplicates = gtCSVReader.getDuplicatePairs(profiles1, profiles2);
        gtCSVReader.storeSerializedObject(duplicates, gtFilePath.replaceAll("\\..*", "IdDuplicates"));
        System.out.println(duplicates.size()+" duplicates written in "+gtFilePath.replaceAll("\\..*", "IdDuplicates"));
    }
}