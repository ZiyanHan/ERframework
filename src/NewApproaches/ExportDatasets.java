package NewApproaches;

import BlockBuilding.StandardBlocking;
import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class ExportDatasets {

    private final static String SEGMENT_DELIMITER = ";";
    private final static String VALUE_DELIMITER = "#";
    
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

        for (int datasetIndex = 0; datasetIndex < d1Datasets.length; datasetIndex++) {
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(datasetsPaths[datasetIndex]+"blocks.txt"), "UTF-8"));
            
            Preprocessing preprocessing = new Preprocessing(datasetsPaths[datasetIndex]+d1Datasets[datasetIndex], 
                    datasetsPaths[datasetIndex]+d2Datasets[datasetIndex]);
            
            final List<AbstractBlock> valueBlocks = preprocessing.getPurgedBlocks(new StandardBlocking());
            int index = 0;
            for (AbstractBlock block : valueBlocks) {
                index++;
                out.write(index + "\t");
                
                BilateralBlock bBlock = (BilateralBlock) block;
                for (int entityId : bBlock.getIndex1Entities()) {
                    out.write(entityId + VALUE_DELIMITER);
                }
                
                out.write(SEGMENT_DELIMITER);
                for (int entityId : bBlock.getIndex2Entities()) {
                    int newId = -entityId;
                    out.write(newId + VALUE_DELIMITER);
                }
                out.write("\n");
            }
            
            out.close();
        }
    }
}
