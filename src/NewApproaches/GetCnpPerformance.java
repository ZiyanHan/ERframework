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

public class GetCnpPerformance {
    public static void main(String[] args) {
        String mainDirectory = "/home/gpapadakis/data/newBibliographicalRecords/";

        String entitiesPath1 = mainDirectory + "rexaProfiles";
        String entitiesPath2 = mainDirectory + "swetodblp_april_2008Profiles";
        String gtPath = mainDirectory + "rexa_dblp_goldstandardIdDuplicates";
        
        if (args.length == 3) {
            entitiesPath1 = args[0];
            entitiesPath2 = args[1];
            gtPath = args[2];
        }
        
        Preprocessing valueBlocking = new Preprocessing(entitiesPath1, entitiesPath2);
        final List<AbstractBlock> valueBlocks = valueBlocking.getBlocks();

        IGroundTruthReader gtReader = new GtSerializationReader(gtPath);
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(null));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        for (WeightingScheme wScheme : WeightingScheme.values()) {            
            List<AbstractBlock> copyOfVBlocks1 = new ArrayList<>(valueBlocks);
            CardinalityNodePruning cnpVB = new CardinalityNodePruning(wScheme);
            copyOfVBlocks1 = cnpVB.refineBlocks(copyOfVBlocks1);
            
            BlocksPerformance blpe = new BlocksPerformance(copyOfVBlocks1, duplicatePropagation);
            blpe.setStatistics();
            blpe.printStatistics();
            System.out.println(wScheme);
        }
    }
}