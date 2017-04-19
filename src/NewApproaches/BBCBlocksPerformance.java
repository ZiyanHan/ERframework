package NewApproaches;

import BlockBuilding.IBlockBuilding;
import BlockBuilding.StandardBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */
public class BBCBlocksPerformance {

    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String mainDirectory = "/home/gpapadakis/newData/";
        String[] datasetsPaths = {mainDirectory + "restaurant/",
            mainDirectory + "rexa_dblp/",
            mainDirectory + "yago_imdb/",
            mainDirectory + "bbcMusic/"
        };

        String[] d1Datasets = {"restaurant1Profiles",
            "rexaProfiles",
            "yagoProfiles",
            "bbc-musicNewNoRdfProfiles"
        };

        String[] d2Datasets = {"restaurant2Profiles",
            "swetodblp_april_2008Profiles",
            "imdbProfiles",
            "dbpedia37processedNewNoSameAsNoWikipediaSortedProfiles"
        };

        String[] duplicates = {
            "restaurantIdDuplicates",
            "rexa_dblp_goldstandardIdDuplicates",
            "imdbgoldFinalIdDuplicates",
            "bbc-music_groundTruthUTF8IdDuplicates"
        };

        int datasetIndex = 3;
//        for (int datasetIndex = 3; datasetIndex < d1Datasets.length; datasetIndex++) {
        IEntityReader eReader1 = new EntitySerializationReader(datasetsPaths[datasetIndex] + d1Datasets[datasetIndex]);
        List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());

        IEntityReader eReader2 = new EntitySerializationReader(datasetsPaths[datasetIndex] + d2Datasets[datasetIndex]);
        List<EntityProfile> profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());

        IBlockBuilding blockBuildingMethod = new BBCMusicTokenBlocking();
        List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles1, profiles2);
        System.out.println("Original blocks\t:\t" + blocks.size());

        IBlockProcessing blockPurging = new ComparisonsBasedBlockPurging();
        blocks = blockPurging.refineBlocks(blocks);
        System.out.println("Purging blocks\t:\t" + blocks.size());

        IBlockProcessing blockCleaningMethod = new BlockFiltering(0.8);
        blocks = blockCleaningMethod.refineBlocks(blocks);
        System.out.println("Filtered blocks\t:\t" + blocks.size());

        IGroundTruthReader gtReader = new GtSerializationReader(datasetsPaths[datasetIndex] + duplicates[datasetIndex]);
        Set<IdDuplicates> duplicatePairs = gtReader.getDuplicatePairs(null);
        System.out.println("Pairs of duplicates\t:\t" + duplicatePairs.size());

        BlocksPerformance blpe = new BlocksPerformance(blocks, new BilateralDuplicatePropagation(duplicatePairs));
        blpe.setStatistics();
        blpe.printStatistics();
    }
}
