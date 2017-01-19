package NewApproaches;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.ComparisonRefinement.NeighborCardinalityNodePruning;
import DataModel.AbstractBlock;
import DataReader.AbstractReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class AlternativeSlide8 {

    public static void main(String[] args) {
        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";

        String entitiesPath1 = mainDirectory + "rexaProfiles";
        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";
        String neighborIdsPath = mainDirectory + "totalNeighborIds";
        
        int[][] neighborIds = (int[][]) AbstractReader.loadSerializedObject(neighborIdsPath);
        
        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(mainDirectory + "rexa_dblp_goldstandardIdDuplicates");
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {
            List<AbstractBlock> copyOfVBlocks1 = new ArrayList<>(valueBlocks);
            CardinalityNodePruning cnpVB = new CardinalityNodePruning(wScheme);
            copyOfVBlocks1 = cnpVB.refineBlocks(copyOfVBlocks1);
            
            List<AbstractBlock> copyOfVBlocks2 = new ArrayList<>(valueBlocks);
            NeighborCardinalityNodePruning ncnpVB = new NeighborCardinalityNodePruning(neighborIds, wScheme);
            copyOfVBlocks2 = ncnpVB.refineBlocks(copyOfVBlocks2);
            
            // rank aggregation
        }
    }
}