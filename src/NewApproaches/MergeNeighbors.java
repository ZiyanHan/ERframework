package NewApproaches;

import DataReader.AbstractReader;
import DataReader.EntityReader.EntityCSVReader;

/**
 *
 * @author G.A.P. II
 */

public class MergeNeighbors {
    public static void main (String[] args) {
//        String mainDirectory = "E:\\Data\\newRestaurant\\";
//        String neighborsPath1 = mainDirectory + "restaurant1Neighbors";
//        String neighborsPath2 = mainDirectory + "restaurant2Neighbors";
//        String mainDirectory = "E:\\Data\\newBibliographicalRecords\\";
//        String neighborsPath1 = mainDirectory + "rexaNeighbors";
//        String neighborsPath2 = mainDirectory + "swetodblp_april_2008Neighbors";
        String mainDirectory = "E:\\Data\\newImdb\\";
        String neighborsPath1 = mainDirectory + "imdbNeighbors";
        String neighborsPath2 = mainDirectory + "yagoNeighbors";
        String outputPath = mainDirectory + "totalNeighborIds";
        
        int[][] neighbors1 = (int[][]) AbstractReader.loadSerializedObject(neighborsPath1);
        int[][] neighbors2 = (int[][]) AbstractReader.loadSerializedObject(neighborsPath2);
        
        int datasetLimit = neighbors1.length;
        int[][] totalNeighbors = new int[neighbors1.length+neighbors2.length][];
        for (int i = 0; i < datasetLimit; i++) {
            totalNeighbors[i] = neighbors1[i];
        }
        for (int i = 0; i < neighbors2.length; i++) {
            if (neighbors2[i] == null) {
                totalNeighbors[i+datasetLimit] = null;
                continue;
            }
            
            totalNeighbors[i+datasetLimit] = new int[neighbors2[i].length];
            for (int j = 0; j < neighbors2[i].length; j++) {
                totalNeighbors[i+datasetLimit][j] = neighbors2[i][j]+datasetLimit;
            }
        }
        
        AbstractReader aReader = new EntityCSVReader(outputPath);
        aReader.storeSerializedObject(totalNeighbors, outputPath);
    }
}