package NewApproaches;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import DataModel.AbstractBlock;
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
public class GetValueBlockingPerformance {

    public static void main(String[] args) {
        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";

        String neighborProfilesPath1 = mainDirectory + "rexaNeighborProfile";
        String neighborProfilesPath2 = mainDirectory + "swetodblp_april_2008NeighborProfile";

        Preprocessing neighborBlocking = new Preprocessing(neighborProfilesPath1, neighborProfilesPath2);
        final List<AbstractBlock> neighborBlocks = neighborBlocking.getBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(mainDirectory + "rexa_dblp_goldstandardIdDuplicates");
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        BlocksPerformance blpe = new BlocksPerformance(neighborBlocks, duplicatePropagation);
        blpe.setStatistics();
        blpe.printStatistics();

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> copyOfBlocks = new ArrayList<>(neighborBlocks);

            CardinalityNodePruning cnp = new CardinalityNodePruning(wScheme);
            copyOfBlocks = cnp.refineBlocks(copyOfBlocks);

            BlocksPerformance blp = new BlocksPerformance(copyOfBlocks, duplicatePropagation);
            blp.setStatistics();
            blp.printStatistics();
        }
    }
}
