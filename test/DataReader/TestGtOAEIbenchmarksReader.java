package DataReader;

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
    	String entityFilePath1 = "/home/vefthym/Desktop/DATASETS/Papadakis/Matching/rexaOAEIProfiles";
    	String entityFilePath2 = "/home/vefthym/Desktop/DATASETS/Papadakis/Matching/dblpOAEIProfilesSAMPLE";
        String gtFilePath = "/home/vefthym/Desktop/DATASETS/TBD_datasets/rexa-dblp/rexa_dblp_goldstandard.xml";
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
        gtOAEIbenchmarksReader.storeSerializedObject(duplicates, "/home/vefthym/Desktop/DATASETS/Papadakis/Matching/dblpRexaSAMPLEIdDuplicates");
    }
}