/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPatterns;

import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author vefthym
 */
public class SimilarityDistributions extends DatasetStatistics {

    public SimilarityDistributions(String data1Path, String data2Path, String groundTruthPath) {
        super(data1Path, data2Path, groundTruthPath);
    }
    
    public void getMatchValueSimDistribution() {
        System.out.println("\nValue similarity of matches distribution:");
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double[] valueSims = new double[duplicates.size()];
        int i = 0; 
        for (IdDuplicates duplicate : duplicates) {            
            double valueSim = getValueSim(profiles1.get(duplicate.getEntityId1()),profiles2.get(duplicate.getEntityId2()));                        
            valueSims[i++] = valueSim;
        }
        Arrays.sort(valueSims); 
        printArray(valueSims);
        double median = getMedian(valueSims);
        System.out.println("Median:"+median);
    }
    
    
    public void getMatchNeighborSimDistribution() {
        System.out.println("\nNeighbor similarity of matches distribution:");
        Map<String,Set<String>> profilesURLs = getAllValuesFromProfileURLs();        
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double[] neighborSims = new double[duplicates.size()];
        int i = 0; 
        for (IdDuplicates duplicate : duplicates) {            
            double neighborSim = getNeighborSim(profiles1.get(duplicate.getEntityId1()),profiles2.get(duplicate.getEntityId2()), profilesURLs);            
            neighborSims[i++] = neighborSim;
        }
        Arrays.sort(neighborSims);
        printArray(neighborSims);
        double median = getMedian(neighborSims);
        System.out.println("Median:"+median);
    }
    
    
    public void getNeighborMatchesOfMatches() {
        System.out.println("\nNumber of matches in the neighborhood of matches:");        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        
        Map<String, Integer> urlToPosition = new HashMap<>(profiles1.size()+profiles2.size());
        for (int i = 0; i < profiles1.size(); i++) {
            urlToPosition.put(profiles1.get(i).getEntityUrl(), i);
        }
        for (int i = 0; i < profiles2.size(); i++) {
            urlToPosition.put(profiles2.get(i).getEntityUrl(), i+profiles1.size());
        }
        
        for (IdDuplicates duplicate : duplicates) {     
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2()); 
            System.out.println("Checking duplicate:"+e1.getEntityUrl()+"("+duplicate.getEntityId1()+"),"+e2.getEntityUrl()+"("+duplicate.getEntityId2()+")");
            
            Set<Integer> e1NeighborIds = getNeighborIds(e1, urlToPosition);
            Set<Integer> e2NeighborIds = getNeighborIds(e2, urlToPosition);
            
            for (Integer e1Neighbor : e1NeighborIds) {
                for (Integer e2Neighbor : e2NeighborIds) {
                    IdDuplicates neighborPair = new IdDuplicates(e1Neighbor, e2Neighbor);
                    System.out.println(neighborPair);
                    if (duplicates.contains(neighborPair)) {
                        //TODO: then add one to the number of neighbor matches per match
                        System.out.println("Found one pair of matching neighbors!");
                    }
                    
                    //added for debugging
                    for (IdDuplicates duplicate2 : duplicates) {
                        if (duplicate2.getEntityId1() == e1Neighbor) {
                            System.out.println(e1Neighbor+" exists in ground truth with "+duplicate2.getEntityId2());
                            return;
                        }
                        if (duplicate2.getEntityId2() == e2Neighbor) {
                            System.out.println(e2Neighbor+" exists in ground truth with "+duplicate2.getEntityId1());
                            return;
                        }
                    }                    
                }
            }
                        
        }
        
    }
    
    /**
     * Return the set of neighbors' numeric ids for a given entity e. 
     */
    private Set<Integer> getNeighborIds(EntityProfile e, Map<String, Integer> urlToPosition) {        
        Set<Integer> neighborIds = new HashSet<>();
        for (String value : e.getAllValues()) {
            Integer neighborId = urlToPosition.get(value);            
            if (neighborId != null) {//then value is a neighbor of e
                System.out.println(value+"("+neighborId+") is a neighbor of "+e.getEntityUrl());
                neighborIds.add(neighborId);
            }
        }
        return neighborIds;
    }
    
    /**
     * Get the median value of a double array, which must be sorted.
     * @param values a *sorted* array of doubles
     * @return the median value of a double array
     */
    private double getMedian(double[] values) {
        int middle = values.length/2;
        return values.length%2 == 1 ? values[middle] : (values[middle-1] + values[middle]) / 2.0;
    }
    
    private void printArray(double[] array) {
        for (double element : array) {
            System.out.println(element);
        }
    }
    
    public static void main(String[] args) {
        //set data
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\imdb-yago\\";
        String dataset1 = basePath+"rexaProfiles";
        String dataset2 = basePath+"swetodblp_april_2008Profiles";
        String datasetGroundtruth = basePath+"rexa_dblp_goldstandardIdDuplicates";
        String[] acceptableTypes = {
//                                    "http://www.okkam.org/ontology_restaurant1.owl#Restaurant",
//                                    "http://www.okkam.org/ontology_restaurant2.owl#Restaurant"
                                    };

        if (args.length == 3) { //override default values with user input
            dataset1 = args[0];
            dataset2 = args[1];
            datasetGroundtruth = args[2];
        }       
        
        SimilarityDistributions dists = new SimilarityDistributions(dataset1, dataset2, datasetGroundtruth);
//        dists.getMatchValueSimDistribution();
//        dists.getMatchNeighborSimDistribution();        
        dists.getNeighborMatchesOfMatches();
    }
    
}
