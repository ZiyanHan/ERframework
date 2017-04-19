package NewApproaches;

import BlockBuilding.StandardBlocking;
import DataModel.AbstractBlock;
import DataModel.IdDuplicates;
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
public class MeasureBlocksPerformance {
    
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String mainDirectory = "/home/gpapadakis/newData/";
        String[] datasetsPaths = { mainDirectory + "restaurant/",
                                   mainDirectory + "rexa_dblp/",
                                   mainDirectory + "yago_imdb/",
                                   mainDirectory + "bbcMusic/"
        };
        
        String[] d1Datasets = { "restaurant1Profiles",
            "rexaProfiles",
            "yagoProfiles",
            "bbc-musicNewNoRdfProfiles"
        };

        String[] d2Datasets = { "restaurant2Profiles",
            "swetodblp_april_2008Profiles",
            "imdbProfiles",
            "dbpedia37NewNoSameAsNoWikipediaProfiles"
        };
        
        String[] duplicates = {
            "restaurantIdDuplicates",
            "rexa_dblp_goldstandardIdDuplicates",
            "imdbgoldFinalIdDuplicates",
            "bbc-music_groundTruthUTF8IdDuplicates"
        };

        for (int datasetIndex = 3; datasetIndex < d1Datasets.length; datasetIndex++) {
            Preprocessing preprocessing = new Preprocessing(datasetsPaths[datasetIndex]+d1Datasets[datasetIndex], 
                    datasetsPaths[datasetIndex]+d2Datasets[datasetIndex]);
            
//            final List<AbstractBlock> valueBlocks = preprocessing.getBlocks(new StandardBlocking());
            final List<AbstractBlock> valueBlocks = preprocessing.getRelaxedBlocks(new StandardBlocking());
            
            IGroundTruthReader gtReader = new GtSerializationReader(datasetsPaths[datasetIndex]+duplicates[datasetIndex]);
            Set<IdDuplicates> duplicatePairs = gtReader.getDuplicatePairs(null);
            System.out.println("Pairs of duplicates\t:\t" + duplicatePairs.size());
            
            BlocksPerformance blpe = new BlocksPerformance(valueBlocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
        }
    }
}
