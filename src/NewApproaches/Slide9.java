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
public class Slide9 {

    public static void main(String[] args) {
        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";

        String entitiesPath1 = mainDirectory + "rexaProfiles";
        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";

        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getBlocks();

        String neighborProfilesPath1 = mainDirectory + "rexaNeighborProfile";
        String neighborProfilesPath2 = mainDirectory + "swetodblp_april_2008NeighborProfile";

        Preprocessing neighborBlocking = new Preprocessing(neighborProfilesPath1, neighborProfilesPath2);
        final List<AbstractBlock> neighborBlocks = neighborBlocking.getBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(mainDirectory + "rexa_dblp_goldstandardIdDuplicates");
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> unionBlocks = new ArrayList<>(valueBlocks);
            unionBlocks.addAll(neighborBlocks);

            CardinalityNodePruning cnp = new CardinalityNodePruning(wScheme);
            unionBlocks = cnp.refineBlocks(unionBlocks);

            BlocksPerformance blp = new BlocksPerformance(unionBlocks, duplicatePropagation);
            blp.setStatistics();
            blp.printStatistics();
            
            // entity matching??
            
            // clustering
        }
    }
}
