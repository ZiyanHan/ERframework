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
    
    public double getAverageValueSimMatch() {
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double sumSimilarity = 0;
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            sumSimilarity += getValueSim(e1, e2);
        }
        
        return sumSimilarity / duplicates.size();
    }
    
    
    public double getAverageNeighborSimMatch() {
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        double sumSimilarity = 0;
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            sumSimilarity += getNeighborSimAvg(e1, e2, profilesURLs1, profilesURLs2);
        }
        
        return sumSimilarity / duplicates.size();
    }
    
    
    
    public double getAverageNumberOfNeighborsWithCommonTokens() {
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);
        
        int neighborsWithCommonTokens = 0;
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            
            Set<String> neighbors1 = new HashSet<>();
            
            for (String neighbor: e1.getAllValues()) {
                Set<String> values = profilesURLs1.get(neighbor);                
                if (values != null) { //then this value is an entity id
                    neighbors1.add(neighbor);                    
                }
            }
            
            for (String neighbor: e2.getAllValues()) {
                Set<String> values = profilesURLs2.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String neighbor1 : neighbors1) {
                        Set<String> neighbor1Values = new HashSet<>(profilesURLs1.get(neighbor1));
                        Set<String> neighbor1Tokens = getAllTokensFromStrings(neighbor1Values);
                        Set<String> neighbor2Tokens = getAllTokensFromStrings(values);
                        if (neighbor1Tokens.removeAll(neighbor2Tokens)) {
                            neighborsWithCommonTokens++;
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
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);
        
        int numPairsWithHigherNeighborSim = 0;
        
        double minValueSim = Double.MAX_VALUE, maxValueSim = -1, minNeighborSim = Double.MAX_VALUE, maxNeighborSim = -1;
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());            
            
            //get the value similarity
            double valueSim = getValueSim(e1, e2);
            
            if (valueSim < minValueSim) {
                minValueSim = valueSim;
            }
            if (valueSim > maxValueSim) {
                maxValueSim = valueSim;
            }
                        
            //get the neighbor similarity
            double neighborSim = getNeighborSimAvg(e1, e2, profilesURLs1, profilesURLs2);
            
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
    
    
    public int getMatchVsNonMatchNeighborSim() {
        Map<String,Set<String>> profilesURLs1 = getAllValuesFromProfileURLs(profiles1);
        Map<String,Set<String>> profilesURLs2 = getAllValuesFromProfileURLs(profiles2);
        
        int numMatchesWithHigherNeighborSim = 0;
        
        double avgDifference = 0;        
        
        Set<IdDuplicates> duplicates = groundTruth.getDuplicates();
        EntityProfile e3 = null; //a non-matching entity profile for e1
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());         
                       
            double neighborSim = getNeighborSimAvg(e1, e2, profilesURLs1, profilesURLs2);    
            
            if (e3 == null) { //only for the first iteration
                e3 = e2;
                continue;
            }
            
            double nonMatchNeighborSim = getNeighborSimAvg(e1, e3, profilesURLs1, profilesURLs2);
            if (neighborSim > nonMatchNeighborSim) {
                numMatchesWithHigherNeighborSim++;
            }
            avgDifference += (neighborSim - nonMatchNeighborSim);
        }       
        avgDifference /= (duplicates.size() - 1);
        System.out.println(numMatchesWithHigherNeighborSim+"/"+(duplicates.size()-1)+" times a random match had higher neighbor sim than a random non-match.");
        System.out.println("The average difference in neighbor sim between a match and a non-match is: "+avgDifference);
        
        return numMatchesWithHigherNeighborSim;
    }
        
    /**
     * Keeps for each entity profile URL (key of map) its set of values (value of map).
     * The resulting keys are stored in random order (HashMap implementation).
     * @return 
     */
    protected Map<String,Set<String>> getAllValuesFromProfileURLs(List<EntityProfile> profiles) {
        Map<String,Set<String>> profilesURLs = new HashMap<>(profiles.size()); //key: entityURL, value: entity values
        profiles.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        return profilesURLs;
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

    /**
     * Get the average neighbor similarity (Jaccard on neighbors' tokens) of two entity profiles
     * @param e1
     * @param e2
     * @param profilesURLs1
     * @param profilesURLs2
     * @return the neighbor similarity (Jaccard on neighbors' tokens) of two entity profiles
     */
    protected double getNeighborSimAvg(EntityProfile e1, EntityProfile e2, Map<String, Set<String>> profilesURLs1, Map<String, Set<String>> profilesURLs2) {
        Set<Set<String>> neighbor1values = getAllNeighborValues(e1, profilesURLs1);
        Set<Set<String>> neighbor2values = getAllNeighborValues(e2, profilesURLs2);
        
        double sum = 0;
        //get the similarity of each pair of neighbors and add it to the sum
        for (Set<String> neighbor1 : neighbor1values) {
            for (Set<String> neighbor2 : neighbor2values) {
                sum += getJaccardSim(neighbor1, neighbor2);
            }
        }
        return sum / (neighbor1values.size() * neighbor2values.size());
    }
    
    /**
     * Get the max neighbor similarity (Jaccard on neighbors' tokens) of two entity profiles
     * @param e1
     * @param e2
     * @param profilesURLs1
     * @param profilesURLs2
     * @return the neighbor similarity (Jaccard on neighbors' tokens) of two entity profiles
     */
    protected double getNeighborSimMax(EntityProfile e1, EntityProfile e2, Map<String, Set<String>> profilesURLs1, Map<String, Set<String>> profilesURLs2) {
        Set<Set<String>> neighbor1values = getAllNeighborValues(e1, profilesURLs1);
        Set<Set<String>> neighbor2values = getAllNeighborValues(e2, profilesURLs2);
        
        double max = 0;
        //get the similarity of each pair of neighbors and add it to the sum
        for (Set<String> neighbor1 : neighbor1values) {
            for (Set<String> neighbor2 : neighbor2values) {
                double sim = getJaccardSim(neighbor1, neighbor2);
                if (sim > max) {
                    max = sim;
                }
            }
        }
        return max;
    }
    
    private Set<Set<String>> getAllNeighborValues(EntityProfile e, Map<String, Set<String>> profilesURLs) {
        Set<Set<String>> neighborValues = new HashSet<>();
        e.getAllValues().stream()
                .map((neighbor) -> profilesURLs.get(neighbor)) //for each value, check if it exists in the profileURLs
                .filter((values) -> (values != null)) //if it exists, then this value is an entity id (i.e., a neighbor)
                .forEach((values) -> {         //for each set of values of this neighbor           
                    neighborValues.add(values);  //add this value to the values of the neighbor
                });
        return neighborValues;
    }

    /**
     * Get the value similarity (Jaccard on tokens) of two entity profiles
     * @param e1
     * @param e2
     * @return the value similarity (Jaccard on tokens) of two entity profiles
     */
    protected double getValueSim(EntityProfile e1, EntityProfile e2) {
        return getJaccardSim(e1.getAllValues(), e2.getAllValues(), e1.getEntityUrl(), e2.getEntityUrl());
    }
    
    private double getJaccardSim(Set<String> strings1, Set<String> strings2, String instanceName1, String instanceName2) {
        AbstractModel model1 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, instanceName1);
        AbstractModel model2 = RepresentationModel.getModel(RepresentationModel.TOKEN_UNIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, instanceName2);
        strings1.stream().forEach(value -> model1.updateModel(value));
        strings2.stream().forEach(value -> model2.updateModel(value));
        double result = model1.getSimilarity(model2);
        return result != result ? 0 : result; // replace NaN with 0 (if result is NaN, then result has the property result != result)
    }
    
    private double getJaccardSim(Set<String> strings1, Set<String> strings2) {
        return getJaccardSim(strings1, strings2, "noname1", "noname2");
    }
    
    public boolean haveSameLabels(EntityProfile e1, EntityProfile e2) {
        //restaurants
//        String[] labelAtts1 = new String[]{"http://www.okkam.org/ontology_restaurant1.owl#name"};
//        String[] labelAtts2 = new String[]{"http://www.okkam.org/ontology_restaurant2.owl#name"};
        
        //Rexa-DBLP
//        String[] labelAtts1 = new String[]{"http://www.w3.org/2000/01/rdf-schema#label", "<http://xmlns.com/foaf/0.1/name>"};
//        String[] labelAtts2 = labelAtts1;
        
        //BBCmusic-DBpedia
        String[] labelAtts1 = new String[]{"<http://purl.org/dc/elements/1.1/title>", "<http://open.vocab.org/terms/sortLabel>", "<http://xmlns.com/foaf/0.1/name>"};
        String[] labelAtts2 = new String[]{"<http://www.w3.org/2000/01/rdf-schema#label>", "<http://dbpedia.org/property/name>", "<http://xmlns.com/foaf/0.1/name>"};
        
        //Yago-IMDb
//        String[] labelAtts1 = new String[]{"rdfs:label", "label", "skos:prefLabel"};
//        String[] labelAtts2 = labelAtts1;

        Set<String> labels1 = getLabelValuesOfEntity(e1, labelAtts1);        
        Set<String> labels2 = getLabelValuesOfEntity(e2, labelAtts2);                
        
        return haveIntersectingLabels(labels1, labels2);
    }
    
    private Set<String> getLabelValuesOfEntity (EntityProfile e, String[] labelAtts) {
        Set<String> labelResults = new HashSet<>();
        for (String labelAtt : labelAtts) {
            String label = e.getValueOf(labelAtt);
            if (label != null) {
                labelResults.add(label.toLowerCase().replaceAll("[^a-z0-9 ]", ""));
            }
        }
        return labelResults;
    }
    
    private boolean haveIntersectingLabels(Set<String> labels1, Set<String> labels2) {     
        return labels1.stream().anyMatch(label1 -> labels2.contains(label1));      
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
//        double avNeighbs1 = stats.getAverageNeighbors(stats.getProfiles1(), new HashSet<>(Arrays.asList(acceptableTypes)));
//        double avNeighbs2 = stats.getAverageNeighbors(stats.getProfiles2(), new HashSet<>(Arrays.asList(acceptableTypes)));
//        System.out.println("Average #neighbors for profiles1: "+avNeighbs1);
//        System.out.println("Average #neighbors for profiles2: "+avNeighbs2);
        System.out.println("Average value similarity of matches: "+stats.getAverageValueSimMatch());
        System.out.println("Average neighbor similarity of matches: "+stats.getAverageNeighborSimMatch());
        System.out.println("Neighbor sim is greater than value sim "+stats.getMatchValueVsNeighborSim()+"/"
                    +stats.getGroundTruth().getDuplicates().size()+" times.");
        System.out.println("Average #neighbor pairs with common tokens per match: "+stats.getAverageNumberOfNeighborsWithCommonTokens());
        stats.getMatchVsNonMatchNeighborSim();
    }
    
}
