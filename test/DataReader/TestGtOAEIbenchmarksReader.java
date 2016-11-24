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
    	String entityFilePath1 = "/home/ethanos/Downloads/JEDAIconfirmedBenchmarks/ID_REC-ID_SIMim_oaei2014_datasets/im-identity/oaei2014_identity_a.owlprof";
    	String entityFilePath2 = "/home/ethanos/Downloads/JEDAIconfirmedBenchmarks/ID_REC-ID_SIMim_oaei2014_datasets/im-identity/oaei2014_identity_b.owlprof";
        String gtFilePath = "/home/ethanos/Downloads/JEDAIconfirmedBenchmarks/ID_REC-ID_SIMim_oaei2014_datasets/im-identity/ID-RECgoldStandard.rdf";
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
        //gtOAEIbenchmarksReader.storeSerializedObject(duplicates, "C:\\Users\\G.A.P. II\\Downloads\\cddbDuplicates");
    }
}