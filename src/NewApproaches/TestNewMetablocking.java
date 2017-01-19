package NewApproaches;

import BlockProcessing.ComparisonRefinement.NeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataReader.AbstractReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class TestNewMetablocking {

    public static void main(String[] args) {
        String mainDirectory = "E:\\Data\\newRestaurant\\";

        String entitiesPath1 = mainDirectory + "restaurant1Profiles";
        String entitiesPath2 = mainDirectory + "restaurant2Profiles";
        String neighborIdsPath = mainDirectory + "totalNeighborIds";

        int[][] neighborIds = (int[][]) AbstractReader.loadSerializedObject(neighborIdsPath);

        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(mainDirectory + "restaurantIdDuplicates");
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> copyOfVBlocks = new ArrayList<>(valueBlocks);
            NeighborCardinalityNodePruning ncnpVB = new NeighborCardinalityNodePruning(neighborIds, wScheme);
            copyOfVBlocks = ncnpVB.refineBlocks(copyOfVBlocks);

            BlocksPerformance blp = new BlocksPerformance(copyOfVBlocks, duplicatePropagation);
            blp.setStatistics();
            blp.printStatistics();
        }

        List<AbstractBlock> copyOfVBlocks = new ArrayList<>(valueBlocks);
        NeighborCardinalityNodePruning ncnpVB = new NeighborCardinalityNodePruning(neighborIds, null);
        copyOfVBlocks = ncnpVB.refineBlocks(copyOfVBlocks);

        BlocksPerformance blp = new BlocksPerformance(copyOfVBlocks, duplicatePropagation);
        blp.setStatistics();
        blp.printStatistics();
    }
}
