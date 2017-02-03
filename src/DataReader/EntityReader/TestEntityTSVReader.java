package DataReader.EntityReader;

import DataModel.EntityProfile;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class TestEntityTSVReader {
    public static void main(String[] args) {
        String filePath = "G:\\VASILIS\\bbcMusic\\dbpedia37New.nt";
        if (args.length == 1) {
            filePath = args[0];
        }
        EntityTSVReader tsvReader = new EntityTSVReader(filePath);
        List<EntityProfile> profiles = tsvReader.getEntityProfiles();
//        for (EntityProfile profile : profiles) {
//            System.out.println("\n\n" + profile.getEntityUrl());
//            for (Attribute attribute : profile.getAttributes()) {
//                System.out.println(attribute.toString());
//            }
//        }
        tsvReader.storeSerializedObject(profiles, filePath.replaceAll("\\..*", "Profiles"));
        System.out.println(profiles.size()+" profiles written in "+filePath.replaceAll("\\..*", "Profiles"));
    }
}