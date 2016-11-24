package DataReader;

import DataStructures.IdDuplicates;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class ConvertGroundtruth {

    public static void main(String[] args) {
        String[] datasetGroundtruth = {"E:\\Data\\groundtruth\\restaurantIdDuplicates",
            "E:\\Data\\groundtruth\\censusIdDuplicates",
            "E:\\Data\\groundtruth\\coraIdDuplicates",
            "E:\\Data\\groundtruth\\cddbIdDuplicates",
            "E:\\Data\\DERdata\\abt-buy\\groundtruth",
            "E:\\Data\\DERdata\\amazon-gp\\groundtruth",
            "E:\\Data\\DERdata\\dblp-acm\\groundtruth",
            "E:\\Data\\DERdata\\dblp-scholar\\groundtruth",
            "E:\\Data\\DERdata\\movies\\groundtruth"
        };

        String[] newDatasetGroundtruth = {"E:\\Data\\csvProfiles\\restaurantIdDuplicates",
            "E:\\Data\\csvProfiles\\censusIdDuplicates",
            "E:\\Data\\csvProfiles\\coraIdDuplicates",
            "E:\\Data\\csvProfiles\\cddbIdDuplicates",
            "E:\\Data\\csvProfiles\\abtBuyIdDuplicates",
            "E:\\Data\\csvProfiles\\amazonGpIdDuplicates",
            "E:\\Data\\csvProfiles\\dblpAcmIdDuplicates",
            "E:\\Data\\csvProfiles\\dblpScholarIdDuplicates",
            "E:\\Data\\csvProfiles\\moviesIdDuplicates"
        };

        for (int datasetIndex = 0; datasetIndex < datasetGroundtruth.length; datasetIndex++) {
            System.out.println(datasetGroundtruth[datasetIndex]);

            final Set<IdDuplicates> duplicatePairs = (Set<IdDuplicates>) SerializationUtilities.loadSerializedObject(datasetGroundtruth[datasetIndex]);

            Set<DataModel.IdDuplicates> newDuplicates = new HashSet<>();
            for (IdDuplicates pair : duplicatePairs) {
                DataModel.IdDuplicates newPair = new DataModel.IdDuplicates(pair.getEntityId1(), pair.getEntityId2());
                newDuplicates.add(newPair);
            }

            SerializationUtilities.storeSerializedObject(newDuplicates, newDatasetGroundtruth[datasetIndex]);
        }
    }
}
