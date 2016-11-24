package DataReader;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class ConvertEntities {

    public static void main(String[] args) {
        String[] datasetProfiles = {"E:\\Data\\profiles\\restaurantProfiles",
            "E:\\Data\\profiles\\censusProfiles",
            "E:\\Data\\profiles\\coraProfiles",
            "E:\\Data\\profiles\\cddbProfiles",
            "E:\\Data\\DERdata\\abt-buy\\dataset",
            "E:\\Data\\DERdata\\amazon-gp\\dataset",
            "E:\\Data\\DERdata\\dblp-acm\\dataset",
            "E:\\Data\\DERdata\\dblp-scholar\\dataset",
            "E:\\Data\\DERdata\\movies\\dataset"
        };

        String[] newDatasetProfiles = {"E:\\Data\\csvProfiles\\restaurantProfiles",
            "E:\\Data\\csvProfiles\\censusProfiles",
            "E:\\Data\\csvProfiles\\coraProfiles",
            "E:\\Data\\csvProfiles\\cddbProfiles",
            "E:\\Data\\csvProfiles\\abtBuyProfiles",
            "E:\\Data\\csvProfiles\\amazonGpProfiles",
            "E:\\Data\\csvProfiles\\dblpAcmProfiles",
            "E:\\Data\\csvProfiles\\dblpScholarProfiles",
            "E:\\Data\\csvProfiles\\moviesProfiles"
        };

        for (int datasetIndex = 0; datasetIndex < datasetProfiles.length; datasetIndex++) {
            System.out.println(datasetProfiles[datasetIndex]);
            
            final List<EntityProfile> profiles = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(datasetProfiles[datasetIndex]);

            List<DataModel.EntityProfile> newProfiles = new ArrayList<>();
            for (EntityProfile profile : profiles) {
                DataModel.EntityProfile newProfile = new DataModel.EntityProfile(profile.getEntityUrl());
                for (Attribute attribute : profile.getAttributes()) {
                    newProfile.addAttribute(attribute.getName(), attribute.getValue());
                }
                newProfiles.add(newProfile);
            }

            SerializationUtilities.storeSerializedObject(newProfiles, newDatasetProfiles[datasetIndex]);
        }
    }
}
