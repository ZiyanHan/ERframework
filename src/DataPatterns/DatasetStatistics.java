/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPatterns;

import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import Utilities.TextModels.AbstractModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author vefthym
 */
public class DatasetStatistics {
    
    List<EntityProfile> profiles1;
    List<EntityProfile> profiles2;
    AbstractDuplicatePropagation groundTruth;   

    public DatasetStatistics (String data1Path, String data2Path, String groundTruthPath) {
        profiles1 = new ArrayList<>();
        profiles2 = new ArrayList<>();
        loadData(data1Path, data2Path, groundTruthPath);
    }
    
    private void loadData(String dataset1Path, String dataset2Path, String groundTruthPath) {
        IEntityReader eReader1 = new EntitySerializationReader(dataset1Path);
        profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());            
        
        IEntityReader eReader2 = new EntitySerializationReader(dataset2Path);
        profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());            

        IGroundTruthReader gtReader = new GtSerializationReader(groundTruthPath);
        groundTruth = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(profiles1, profiles2));
        System.out.println("Existing Duplicates\t:\t" + groundTruth.getDuplicates().size());
    }
    
    
    public double getAverageNeighbors(List<EntityProfile> entities, Set<String> acceptableTypes) {
        Map<String,Set<String>> profilesURLs = new HashMap<>(entities.size()); //key: entityURL, value: entity values
        entities.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        int sumNeighbors = 0;
        int acceptableEntities = 0;
        for (EntityProfile entity : entities) {
            Set<String> acceptablesCopy = new HashSet<>(acceptableTypes);
            Set<String> values = entity.getAllValues();
            if (acceptableTypes != null && !acceptableTypes.isEmpty() && !acceptablesCopy.removeAll(values)) { //if one of the entity values is an acceptable type
                continue;
            }
            acceptableEntities++;
            long localNeighbors = values.stream() //for each value of the current profile (possible neighbor)
            .filter((neighbor) -> profilesURLs.containsKey(neighbor)) //keep only entity neighbors and skip other literals & URIs
            .count();
            //System.out.println("Found "+localNeighbors+" neighbors for entity "+ entity.getEntityUrl());
            sumNeighbors += localNeighbors;            
        }
        return (double) sumNeighbors / acceptableEntities;
    }
    
    public double getAverageMatchSim() {
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double sumSimilarity = 0;
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            AbstractModel model1 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e1.getEntityUrl());
            e1.getAllValues().stream().forEach((value) -> {
                model1.updateModel(value);
            });
            
            AbstractModel model2 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e2.getEntityUrl());
            e2.getAllValues().stream().forEach((value) -> {
                model2.updateModel(value);
            });
            
            sumSimilarity += model1.getSimilarity(model2);            
        }
        
        return sumSimilarity / duplicates.size();
    }
    
    
    public double getAverageMatchNeighborSim() {
        Map<String,Set<String>> profilesURLs = new HashMap<>(profiles1.size()+profiles2.size()); //key: entityURL, value: entity values
        profiles1.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        profiles2.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double sumSimilarity = 0;
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            AbstractModel model1 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e1.getEntityUrl());
            
            for (String neighbor: e1.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String value : values) { //update the model with its values
                        model1.updateModel(value);
                    }
                }
            }
            
            AbstractModel model2 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e2.getEntityUrl());
            for (String neighbor: e2.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String value : values) { //update the model with its values
                        model2.updateModel(value);
                    }
                }
            }
            
            sumSimilarity += model1.getSimilarity(model2);            
        }
        
        return sumSimilarity / duplicates.size();
    }
    
    
    
    public double getAverageNumberOfNeighborsWithCommonTokens() {
        Map<String,Set<String>> profilesURLs = new HashMap<>(profiles1.size()+profiles2.size()); //key: entityURL, value: entity values
        profiles1.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        profiles2.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        
        int neighborsWithCommonTokens = 0;
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            Set<String> neighbors1 = new HashSet<>();
            
            for (String neighbor: e1.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);                
                if (values != null) { //then this value is an entity id
                    neighbors1.add(neighbor);                    
                }
            }
            
            for (String neighbor: e2.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String neighbor1 : neighbors1) {
                        Set<String> neighbor1Values = new HashSet<>(profilesURLs.get(neighbor1));
                        Set<String> neighbor1Tokens = getAllTokensFromStrings(neighbor1Values);
                        Set<String> neighbor2Tokens = getAllTokensFromStrings(values);
                        if (neighbor1Tokens.removeAll(neighbor2Tokens)) {
                            neighborsWithCommonTokens++;
                        } else {
                            System.out.println("The pair ("+neighbor1+", "+neighbor+") have 0 common tokens");
                        }
                    }
                }
            }
            
            
        }
        System.out.println(neighborsWithCommonTokens+" pairs of neighbors of matches have common tokens.");
        
        return (double)neighborsWithCommonTokens / duplicates.size();
    }
    
    
    private Set<String> getAllTokensFromStrings(Set<String> stringValues) {
        Set<String> tokens = new HashSet<>();
        stringValues.stream()
                .forEach((value) -> {
                    tokens.addAll(Arrays.asList(value.split("\\W")));
                });
        return tokens;
    }
    
    public int getMatchValueVsNeighborSim() {
        Map<String,Set<String>> profilesURLs = new HashMap<>(profiles1.size()+profiles2.size()); //key: entityURL, value: entity values
        profiles1.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        profiles2.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        
        int numPairsWithHigherNeighborSim = 0;
        
        double minValueSim = Double.MAX_VALUE, maxValueSim = -1, minNeighborSim = Double.MAX_VALUE, maxNeighborSim = -1;
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());            
            
            //get the value similarity
            AbstractModel model1 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e1.getEntityUrl());
            e1.getAllValues().stream().forEach((value) -> {
                model1.updateModel(value);
            });
            
            AbstractModel model2 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e2.getEntityUrl());
            e2.getAllValues().stream().forEach((value) -> {
                model2.updateModel(value);
            });            
            
            double valueSim = model1.getSimilarity(model2);
            if (valueSim < minValueSim) {
                minValueSim = valueSim;
            }
            if (valueSim > maxValueSim) {
                maxValueSim = valueSim;
            }
            
            
            //get the neighbor similarity
            AbstractModel neighborModel1 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e1.getEntityUrl());
            
            for (String neighbor: e1.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String value : values) { //update the model with its values
                        neighborModel1.updateModel(value);
                    }
                }
            }
            
            AbstractModel neighborModel2 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, e2.getEntityUrl());
            for (String neighbor: e2.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String value : values) { //update the model with its values
                        neighborModel2.updateModel(value);
                    }
                }
            }
            
            double neighborSim = neighborModel1.getSimilarity(neighborModel2);
            
            if (neighborSim < minNeighborSim) {
                minNeighborSim = neighborSim;
            }
            if (neighborSim > maxNeighborSim) {
                maxNeighborSim = neighborSim;
            }
            
           // System.out.println("Value sim: "+ valueSim+", neighbor sim: "+neighborSim);
            if (valueSim < neighborSim) {
                numPairsWithHigherNeighborSim++;
            }
        }
        
        System.out.println("Min value sim: "+minValueSim);
        System.out.println("Max value sim: "+maxValueSim);
        System.out.println("Min neighbor sim: "+minNeighborSim);
        System.out.println("Max neighbor sim: "+maxNeighborSim);
        return numPairsWithHigherNeighborSim;
    }
    
    
    
    

    public List<EntityProfile> getProfiles1() {
        return profiles1;
    }

    public void setProfiles1(List<EntityProfile> profiles1) {
        this.profiles1 = profiles1;
    }

    public List<EntityProfile> getProfiles2() {
        return profiles2;
    }

    public void setProfiles2(List<EntityProfile> profiles2) {
        this.profiles2 = profiles2;
    }

    public AbstractDuplicatePropagation getGroundTruth() {
        return groundTruth;
    }

    public void setGroundTruth(AbstractDuplicatePropagation groundTruth) {
        this.groundTruth = groundTruth;
    }
    
    
    public static void main(String[] args) {
        //set data
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String dataset1 = basePath+"restaurant1Profiles";
        String dataset2 = basePath+"restaurant2Profiles";
        String datasetGroundtruth = basePath+"restaurantIdDuplicates";
        String[] acceptableTypes = {
//                                    "http://www.okkam.org/ontology_person1.owl#Person",
//                                    "http://www.okkam.org/ontology_person2.owl#Person", 
            
//                                    "http://www.okkam.org/ontology_restaurant1.owl#Restaurant",
//                                    "http://www.okkam.org/ontology_restaurant2.owl#Restaurant",
            
//                                      "http://www.bbc.co.uk/ontologies/creativework/NewsItem",
//                                      "http://www.bbc.co.uk/ontologies/creativework/BlogPost",
//                                      "http://www.bbc.co.uk/ontologies/creativework/Programme",
//                                      "http://www.bbc.co.uk/ontologies/creativework/CreativeWork"
                                    };

        if (args.length == 3) {
            dataset1 = args[0];
            dataset2 = args[1];
            datasetGroundtruth = args[2];
        }
        
        
        DatasetStatistics stats = new DatasetStatistics(dataset1, dataset2, datasetGroundtruth);
        double avNeighbs1 = stats.getAverageNeighbors(stats.getProfiles1(), new HashSet<>(Arrays.asList(acceptableTypes)));
        double avNeighbs2 = stats.getAverageNeighbors(stats.getProfiles2(), new HashSet<>(Arrays.asList(acceptableTypes)));
        System.out.println("Average #neighbors for profiles1: "+avNeighbs1);
        System.out.println("Average #neighbors for profiles2: "+avNeighbs2);
        System.out.println("Average value similarity of matches: "+stats.getAverageMatchSim());
        System.out.println("Average neighbor similarity of matches: "+stats.getAverageMatchNeighborSim());
        System.out.println("Neighbor sim is greater than value sim "+stats.getMatchValueVsNeighborSim()+"/"
                    +stats.getGroundTruth().getDuplicates().size()+" times.");
        System.out.println("Average #neighbor pairs with common tokens per match: "+stats.getAverageNumberOfNeighborsWithCommonTokens());
    }
    
}
