package DataReader;

import DataModel.EntityProfile;
import DataReader.EntityReader.EntityCSVReader;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class TestEntityCSVReader {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\G.A.P. II\\Downloads\\cd.csv";
        EntityCSVReader csvReader = new EntityCSVReader(filePath);
        csvReader.setAttributeNamesInFirstRow(true);
        csvReader.setSeparator(';');
        csvReader.setAttributesToExclude(new int[]{1});
        csvReader.setIdIndex(0);
        List<EntityProfile> profiles = csvReader.getEntityProfiles();
//        for (EntityProfile profile : profiles) {
//            System.out.println("\n\n" + profile.getEntityUrl());
//            for (Attribute attribute : profile.getAttributes()) {
//                System.out.println(attribute.toString());
//            }
//        }
        csvReader.storeSerializedObject(profiles, "C:\\Users\\G.A.P. II\\Downloads\\cddbProfiles");
    }
}