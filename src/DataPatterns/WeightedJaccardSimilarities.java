/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPatterns;

import DataModel.Attribute;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import Utilities.TextModels.AbstractModel;
import Utilities.TextModels.BagModel;
import Utilities.TextModels.TokenNGrams;
import static gr.demokritos.iit.jinsect.utils.splitToWords;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.ext.com.google.common.collect.HashMultiset;
import org.apache.jena.ext.com.google.common.collect.Multiset;
import org.apache.jena.ext.com.google.common.collect.Multisets;

/**
 *
 * @author vefthym
 */
public class WeightedJaccardSimilarities extends DatasetStatistics {
    
    public enum AGGREGATION {MAX, AVERAGE};
    
    BagModel model1;
    BagModel model2;

    public WeightedJaccardSimilarities(String data1Path, String data2Path, String groundTruthPath) {
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
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);       
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double[] neighborSims = new double[duplicates.size()];
        int i = 0; 
        for (IdDuplicates duplicate : duplicates) {            
            double neighborSim = getNeighborSimAvg(profiles1.get(duplicate.getEntityId1()),profiles2.get(duplicate.getEntityId2()), profilesURLs1, profilesURLs2);            
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
    
    
    public void getNumberOfNeighborPairsPerMatch() {
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);            
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double[] neighborPairs = new double[duplicates.size()];        
        
        int i = 0;
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            int e1Neighbors = 0;
            for (String neighbor: e1.getAllValues()) {
                Set<String> values = profilesURLs1.get(neighbor);
                if (values != null) { //then this value is an entity id
                    e1Neighbors++;
                }
            }
            
            if (e1Neighbors == 0) {
                neighborPairs[i++] = 0;
                continue;
            }
            
            int e2Neighbors = 0;
            for (String neighbor: e2.getAllValues()) {
                Set<String> values = profilesURLs2.get(neighbor);
                if (values != null) { //then this value is an entity id
                    e2Neighbors++;
                }
            }
            neighborPairs[i++] = e1Neighbors*e2Neighbors;            
        }
        Arrays.sort(neighborPairs);
        printArray(neighborPairs);
        double median = getMedian(neighborPairs);
        System.out.println("Median:"+median);
    }
    
    
    public void getRelationshipsBetweenMatchesAndNeighbors() {
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);              
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        
        Multiset<String> relationPairsCount = HashMultiset.create();
                
        int i = 0;
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            Set<Attribute> e1Relations = new HashSet<>();            
            for (Attribute att: e1.getAttributes()) {
                String value = att.getValue();
                Set<String> values = profilesURLs1.get(value);
                if (values != null) { //then this value is an entity id
                    e1Relations.add(att);
                }
            }
            
            if (e1Relations.isEmpty()) {                
                continue;
            }
            
            Set<Attribute> e2Relations = new HashSet<>();            
            for (Attribute att: e2.getAttributes()) {                
                String value = att.getValue();
                Set<String> values = profilesURLs2.get(value);
                if (values != null) { //then this value is an entity id
                    e2Relations.add(att);
                }
            }
                        
            for (Attribute att1 : e1Relations) {
                for (Attribute att2 : e2Relations) {
                    relationPairsCount.add("("+att1.getName()+", "+att2.getName()+")");
                }
            }
        }
        
        //sort and print relationPairs
        for (String relationPair : Multisets.copyHighestCountFirst(relationPairsCount).elementSet()) {
            System.out.println(relationPair+":"+relationPairsCount.count(relationPair));
        }
        
    }
    
    
    public void getValueAndNeighborSimOfMatches(AGGREGATION aggregation) {
        System.out.println("\nValuesim:NeighborSim");
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2); 
        
        createModels();
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();        
        for (IdDuplicates duplicate : duplicates) {       
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            double neighborSim;
            switch (aggregation) {
                case MAX:
                    neighborSim = getNeighborSimMax(e1,e2, profilesURLs1, profilesURLs2);      
                    break;
                case AVERAGE:
                    neighborSim = getNeighborSimAvg(e1, e2, profilesURLs1, profilesURLs2);
                    break;
                default:
                    neighborSim = getNeighborSimMax(e1,e2, profilesURLs1, profilesURLs2);
            }            
            if (neighborSim > 1) { //could be slightly above 1 due to imprecision of doubles (slgihtly below 0 is not a problem, as it gets mapped to 0)
                neighborSim = 1; 
            }
//            System.out.println("Getting the value sim of "+e1.getEntityUrl()+", "+e2.getEntityUrl());
            double valueSim = getValueSim(e1, e2);            
            System.out.println(valueSim+":"+neighborSim);
        }        
    }
    
    
    /**
     * Get the max neighbor similarity (weighted Jaccard on neighbors' tokens) of two entity profiles
     * @param e1
     * @param e2
     * @param profilesURLs
     * @return the neighbor similarity (weighted Jaccard on neighbors' tokens) of two entity profiles
     */
    @Override
    protected double getNeighborSimMax(EntityProfile e1, EntityProfile e2, Map<String, Set<String>> profilesURLs1, Map<String, Set<String>> profilesURLs2) {
        Set<Set<String>> neighbor1values = getAllNeighborValues(e1, profilesURLs1);
        Set<Set<String>> neighbor2values = getAllNeighborValues(e2, profilesURLs2);
        
        double max = 0;
        //get the similarity of each pair of neighbors and add it to the sum
        for (Set<String> neighbor1 : neighbor1values) {      
//            System.out.println("The neighbor of e1 contains the values: "+Arrays.toString(neighbor1.toArray()));
            for (Set<String> neighbor2 : neighbor2values) {
//                System.out.println("The neighbor of e2 contains the values: "+Arrays.toString(neighbor2.toArray()));
                double sim = getWeightedJaccardSim(neighbor1, neighbor2);
                if (sim > max) {
                    max = sim;
                }
            }
        }
        return max;
    }
    
    
    /**
     * Get the max neighbor similarity (weighted Jaccard on neighbors' tokens) of two entity profiles
     * @param e1
     * @param e2
     * @param profilesURLs
     * @return the neighbor similarity (weighted Jaccard on neighbors' tokens) of two entity profiles
     */
    @Override
    protected double getNeighborSimAvg(EntityProfile e1, EntityProfile e2, Map<String, Set<String>> profilesURLs1, Map<String, Set<String>> profilesURLs2) {
        Set<Set<String>> neighbor1values = getAllNeighborValues(e1, profilesURLs1);
        Set<Set<String>> neighbor2values = getAllNeighborValues(e2, profilesURLs2);
        
        double sum = 0;
        double numNeighborPairs = 0;
        //get the similarity of each pair of neighbors and add it to the sum
        for (Set<String> neighbor1 : neighbor1values) {      
//            System.out.println("The neighbor of e1 contains the values: "+Arrays.toString(neighbor1.toArray()));
            for (Set<String> neighbor2 : neighbor2values) {
//                System.out.println("The neighbor of e2 contains the values: "+Arrays.toString(neighbor2.toArray()));
                double sim = getWeightedJaccardSim(neighbor1, neighbor2);
                sum += sim;
                numNeighborPairs++;
            }
        }
        
        return numNeighborPairs == 0 ? 0 : sum / numNeighborPairs;
    }
    
    
    
    
    private Set<Set<String>> getAllNeighborValues(EntityProfile e, Map<String, Set<String>> profilesURLs) {
        Set<Set<String>> neighborValues = new HashSet<>();
        e.getAllValues().stream()
                .map((neighbor) -> profilesURLs.get(neighbor)) //for each value, check if it exists in the profileURLs
                .filter((values) -> (values != null)) //if it exists, then this value is an entity id (i.e., a neighbor)
                .forEach((values) -> {         //for each *set* of values of this neighbor  
                    Set<String> tokensOfValues = new HashSet<>(); //create a set of tokens and add it to the result
                    values.stream().forEach(value -> tokensOfValues.addAll(Arrays.asList(splitToWords(value))));
                    neighborValues.add(tokensOfValues);  
                });
        return neighborValues;
    }
    
    /////////////////////
    //UTILITY FUNCTIONS//
    /////////////////////
    
    /**
     * Get the value similarity (weighted Jaccard on tokens) of two entity profiles
     * @param e1
     * @param e2
     * @return the value similarity (weighted Jaccard on tokens) of two entity profiles
     */
    @Override
    protected double getValueSim(EntityProfile e1, EntityProfile e2) {
        return getWeightedJaccardSim(e1.getAllTokens(), e2.getAllTokens());
    }
    
    private double getWeightedJaccardSim(Set<String> strings1, Set<String> strings2) {
        double result = model1.getWeightedJaccardSimilarity(strings1, strings2, model2);
        return result != result ? 0 : result; // replace NaN with 0 (if result is NaN, then result has the property result != result)
    }
    
    protected void createModels() {
        model1 = new TokenNGrams(1, RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.WEIGHTED_JACCARD_SIMILARITY, "lala");
        model2 = new TokenNGrams(1, RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.WEIGHTED_JACCARD_SIMILARITY, "lala");
            
        profiles1.stream().map(profile -> profile.getTokensSetAsString()).forEach(entityString -> model1.updateModel(entityString));
        profiles2.stream().map(profile -> profile.getTokensSetAsString()).forEach(entityString -> model2.updateModel(entityString));        
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
        //Restaurants dataset
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
//        String dataset1 = basePath+"restaurant1Profiles";
//        String dataset2 = basePath+"restaurant2Profiles";
//        String datasetGroundtruth = basePath+"restaurantIdDuplicates";
        
        //Rexa-DBLP dataset
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
//        String dataset1 = basePath+"rexaProfiles";
//        String dataset2 = basePath+"swetodblp_april_2008Profiles";
//        String datasetGroundtruth = basePath+"rexa_dblp_goldstandardIdDuplicates";
        
        //YAGO-IMDb dataset (cannot be loaded in laptop ~60GB RAM used)
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\imdb-yago\\";
//        String dataset1 = basePath+"yagoProfiles";
//        String dataset2 = basePath+"imdbProfiles";
//        String datasetGroundtruth = basePath+"imdbgoldFinalIdDuplicates";
        
        //BBCmusic-DBpedia dataset
        final String basePath = "G:\\VASILIS\\bbcMusic\\";
        String dataset1 = basePath+"bbc-musicNewNoRdfProfiles";
        String dataset2 = basePath+"dbpedia37NewNoSameAsNoWikipediaProfiles";
        String datasetGroundtruth = basePath+"bbc-music_groundTruthUTF8IdDuplicates";
        
        if (args.length == 3) { //override default values with user input
            dataset1 = args[0];
            dataset2 = args[1];
            datasetGroundtruth = args[2];
        }       
        
        WeightedJaccardSimilarities dists = new WeightedJaccardSimilarities(dataset1, dataset2, datasetGroundtruth);
//        dists.getMatchValueSimDistribution();
//        dists.getMatchNeighborSimDistribution();        
//        dists.getNeighborMatchesOfMatches();
//        dists.getNumberOfNeighborPairsPerMatch();
//        dists.getValueAndNeighborSimCorrelation(AGGREGATION.MAX);
//        dists.getRelationshipsBetweenMatchesAndNeighbors();
        dists.getValueAndNeighborSimOfMatches(AGGREGATION.AVERAGE);
    }
    
}
