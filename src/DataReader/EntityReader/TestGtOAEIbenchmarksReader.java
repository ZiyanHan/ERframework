package DataReader.EntityReader;

import DataReader.*;
import DataModel.Attribute;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtCSVReader;
import DataReader.GroundTruthReader.GtOAEIbenchmarksReader;

import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class TestGtOAEIbenchmarksReader {
    public static void main(String[] args) {
    	String entityFilePath1 = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2016\\UOBM_small\\Abox1Profiles";
    	String entityFilePath2 = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2016\\UOBM_small\\Abox2Profiles";
        String gtFilePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2016\\UOBM_small\\refalign.rdf";
        if (args.length == 3) {
            entityFilePath1 = args[0];
            entityFilePath2 = args[1];
            gtFilePath = args[2];
        }
        
        EntitySerializationReader esr1 = new EntitySerializationReader(entityFilePath1);
        EntitySerializationReader esr2 = new EntitySerializationReader(entityFilePath2);
        GtOAEIbenchmarksReader gtOAEIbenchmarksReader = new GtOAEIbenchmarksReader(gtFilePath);
        List<EntityProfile> profiles1 = esr1.getEntityProfiles();
        List<EntityProfile> profiles2 = esr2.getEntityProfiles();
//        for (EntityProfile profile : profiles1) {
//            System.out.println("\n\n" + profile.getEntityUrl());
//            for (Attribute attribute : profile.getAttributes()) {
//                System.out.print(attribute.toString());
//                System.out.println();
//            }
//        }
        Set<IdDuplicates> duplicates = gtOAEIbenchmarksReader.getDuplicatePairs(profiles1, profiles2);
        for (IdDuplicates duplicate : duplicates) {
        	int id1 = duplicate.getEntityId1();
            System.out.println(id1 + " " + duplicate.getEntityId2());

        }
        gtOAEIbenchmarksReader.storeSerializedObject(duplicates, gtFilePath.replaceAll("\\..*", "IdDuplicates"));
    }
}