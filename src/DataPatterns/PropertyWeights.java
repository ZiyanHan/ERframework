package DataPatterns;

import DataModel.Attribute;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import Utilities.LevenshteinSimilarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.HashMultiset;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Multiset;

/**
 *
 * @author vefthym
 */
public class PropertyWeights extends WeightedJaccardSimilarities {
    
    protected Map<String, Integer> urlToEntityIds1;
    protected Map<String, Integer> urlToEntityIds2;
    

    public PropertyWeights(String data1Path, String data2Path, String groundTruthPath) {
        super(data1Path, data2Path, groundTruthPath);
        urlToEntityIds1 = getEntityURLtoEntityID(profiles1);
        urlToEntityIds2 = getEntityURLtoEntityID(profiles2);
    }
    
    public double getPropertySupport(String property, List<EntityProfile> profiles) {        
        double frequency = 0;
        for (EntityProfile profile : profiles) {
            if (profile.getAllAttributeNames().contains(property)) {
                frequency++;
            }
        }
        return frequency / profiles.size();
    }
    
    /**
     * Precondition: relation is an object property
     * @param relation a property used to link entities
     * @param profiles
     * @return 
     */
    public double getRelationSupport(String relation, List<EntityProfile> profiles) {        
        double frequency = 0;
        for (EntityProfile profile : profiles) {
            for (Attribute att : profile.getAttributes()) {
                if (att.getName().equals(relation)) {
                    frequency++;
                }
            }
        }
        long profilesSize = profiles.size();             
        return frequency / (profilesSize * profilesSize);
    }
    
    public double getPropertyDiscriminability(String property, List<EntityProfile> profiles) {
        Set<String> distinctValues = new HashSet<>();
        int frequencyOfProperty = 0;
        for (EntityProfile profile : profiles) {
            for (Attribute attribute : profile.getAttributes()) {
                if (attribute.getName().equals(property)) {
                    distinctValues.add(attribute.getValue());
                    frequencyOfProperty++;
                }                    
            }            
        }
        return (double) distinctValues.size() / frequencyOfProperty;
    }
    
    
    public double getPropertyPairSupport(PropertyPair propertyPair) {                
        Set<String> profiles1Covered = new HashSet<>();
        Set<String> profiles2Covered = new HashSet<>();
        for (EntityProfile profile1 : profiles1) {
            if (!profile1.getAllAttributeNames().contains(propertyPair.getProperty1())) {
                continue;
            }
            for (EntityProfile profile2 : profiles2) {
                if (profile2.getAllAttributeNames().contains(propertyPair.getProperty2())) {
                    profiles1Covered.add(profile1.getEntityUrl());
                    profiles2Covered.add(profile2.getEntityUrl());
                }
            }
        }
        return (double)(profiles1Covered.size()+profiles2Covered.size()) / (profiles1.size()+profiles2.size());
    }
    
    /*
    public Map<String,Double> getPropertyPairSupportPerType(String property1, String property2) {                
        Set<String> profiles1Covered = new HashSet<>();
        Set<String> profiles2Covered = new HashSet<>();
        
        Multiset<String> type1Counter = HashMultiset.create();
        Multiset<String> type2Counter = HashMultiset.create();
        
        Map<String,Double> results = new HashMap<>();
        
        for (EntityProfile profile1 : profiles1) {
            Set<String> typesOfEntity1 = profile1.getTypes();
            if (!profile1.getAllAttributeNames().contains(property1)) {
                continue;
            }
            
            
            for (EntityProfile profile2 : profiles2) {
                if (profile2.getAllAttributeNames().contains(property2)) {
                    
                    
                    
                    
                    profiles1Covered.add(profile1.getEntityUrl());
                    profiles2Covered.add(profile2.getEntityUrl());
                }
            }
        }
        return (double)(profiles1Covered.size()+profiles2Covered.size()) / (profiles1.size()+profiles2.size());
    }
    */
    
    
    
    /**
     * Returns the support of this property per type, as a Map with key: type, value: support of this property 
     * @param property
     * @param profiles
     * @return a Map with key: type, value: support of this property 
     */
    public Map<String, Double> getPropertySupportPerType(String property, List<EntityProfile> profiles) {
        Multiset<String> supportPerType = HashMultiset.create();
        Multiset<String> typeCounter = HashMultiset.create();
        for (EntityProfile profile : profiles) {            
            Set<String> entityTypes = profile.getTypes();
            for (String type : entityTypes) {
                typeCounter.add(type);                
                if (profile.getAllAttributeNames().contains(property)) {                    
                    supportPerType.add(type);
                }                
            }
        }
        
        //normalization phase
        Map<String, Double> finalResults = new HashMap<>();
        for (String type : supportPerType.elementSet()) {
            finalResults.put(type, (double) supportPerType.count(type) / typeCounter.count(type));
        }
        
        return finalResults;
    }
    
    
    
    public double getPropertyPairDiscriminability(String property1, String property2) {
        Multimap<String,String> links1 = HashMultimap.create(); //key: common value, value: entityURLs with this value
        Multimap<String,String> links2 = HashMultimap.create(); //key: common value, value: entityURLs with this value
        
        for (EntityProfile profile1 : profiles1) {
            for (Attribute attribute1 : profile1.getAttributes()) {
                if (attribute1.getName().equals(property1)) {
                    links1.put(attribute1.getValue(), profile1.getEntityUrl());                    
                }                    
            }            
        }
        
        for (EntityProfile profile2 : profiles2) {
            for (Attribute attribute2 : profile2.getAttributes()) {
                if (attribute2.getName().equals(property2)) {                    
                    links2.put(attribute2.getValue(), profile2.getEntityUrl());
                }
            }
        }
        
        double candidatePairs = 0;
        Set<String> entities1Total = new HashSet<>();
        Set<String> entities2Total = new HashSet<>();
        for (String value : links1.keySet()) {
            Set<String> entities1 = (Set)links1.get(value);            
            if (links2.get(value) != null) {                
                Set<String> entities2 = (Set)links2.get(value);
                entities1Total.addAll(entities1);
                entities2Total.addAll(entities2);
                candidatePairs += entities1.size() * entities2.size();
            }
        }
        
        //System.out.println("e1Size:"+entities1Total.size()+", e2Size:"+entities2Total.size()+", candidate pairs:"+candidatePairs);
        if (candidatePairs == 0) {
            return 0;
        }
        return Math.min(entities1Total.size(), entities2Total.size()) / candidatePairs;        
    }
    
    /**
     * Finds the discriminability of a relation pair, based on the similarity (exact match) of the value of their neighbors on a pair of label properties.
     * @param relation1
     * @param relation2
     * @param labelPropertyPerType1
     * @param labelPropertyPerType2
     * @return 
     */
    public double getRelationPairDiscriminability(PropertyPair relationPair, Map<String,String> labelPropertyPerType1, Map<String,String> labelPropertyPerType2) {
        Multimap<String,String> links1 = HashMultimap.create(); //key: common value, value: entityURLs with this value
        Multimap<String,String> links2 = HashMultimap.create(); //key: common value, value: entityURLs with this value
        
        for (EntityProfile profile1 : profiles1) {
            for (Attribute attribute1 : profile1.getAttributes()) {
                if (attribute1.getName().equals(relationPair.getProperty1())) {
                    int neighborId = urlToEntityIds1.get((attribute1.getValue()));
                    EntityProfile neighbor = profiles1.get(neighborId);                    
                    Set<String> neighborTypes = neighbor.getTypes(); //set, in case it's a multi-type entity
                    for (String type : neighborTypes) {
                        String neighborsLabel = neighbor.getValueOf(labelPropertyPerType1.get(type));
                        if (neighborsLabel != null) {
                            links1.put(neighborsLabel, profile1.getEntityUrl());                    
                        }
                    }
                }                    
            }            
        }
        
        for (EntityProfile profile2 : profiles2) {
            for (Attribute attribute2 : profile2.getAttributes()) {
                if (attribute2.getName().equals(relationPair.getProperty2())) {
                    int neighborId = urlToEntityIds2.get((attribute2.getValue()));
                    EntityProfile neighbor = profiles2.get(neighborId);
                    Set<String> neighborTypes = neighbor.getTypes(); //set, in case it's a multi-type entity
                    for (String type : neighborTypes) {
                        String neighborsLabel = neighbor.getValueOf(labelPropertyPerType2.get(type));
                        if (neighborsLabel != null) {
                            links2.put(neighborsLabel, profile2.getEntityUrl());                    
                        }
                    }
                }                    
            }            
        }
        
        double candidatePairs = 0;
        Set<String> entities1Total = new HashSet<>();
        Set<String> entities2Total = new HashSet<>();
        for (String value : links1.keySet()) {
            Set<String> entities1 = (Set)links1.get(value);
            entities1Total.addAll(entities1);
            if (links2.get(value) != null) {
                Set<String> entities2 = (Set)links2.get(value);
                entities2Total.addAll(entities2);
                candidatePairs += entities1.size() * entities2.size();
            }
        }
        
        //System.out.println("e1Size:"+entities1Total.size()+", e2Size:"+entities2Total.size()+", candidate pairs:"+candidatePairs);
        if (candidatePairs == 0) {
            return 0;
        }
        return Math.min(entities1Total.size(), entities2Total.size()) / candidatePairs;        
    }    
    
    
    /**
     * Finds the discriminability of a relation pair, based on the similarity (exact match) of the value of their neighbors on a pair of label properties.
     * @param relationPair
     * @param labelPairs
     * @return 
     */
    public double getRelationPairDiscriminability(PropertyPair relationPair, PropertyPair[] labelPairs) {
        Multimap<String,String> links1 = HashMultimap.create(); //key: common value, value: entityURLs with this value
        Multimap<String,String> links2 = HashMultimap.create(); //key: common value, value: entityURLs with this value
         
        for (EntityProfile profile1 : profiles1) {
            for (Attribute attribute1 : profile1.getAttributes()) {
                if (attribute1.getName().equals(relationPair.getProperty1())) {
                    int neighborId = urlToEntityIds1.get((attribute1.getValue()));
                    EntityProfile neighbor = profiles1.get(neighborId);                    
                    
                    for (int i = 0; i < labelPairs.length; ++i) {
                        String neighborsLabel = neighbor.getValueOf(labelPairs[i].getProperty1());
                        if (neighborsLabel != null) {
                            links1.put(neighborsLabel, profile1.getEntityUrl());                    
                        }
                    }
                }                    
            }            
        }
        
        for (EntityProfile profile2 : profiles2) {
            for (Attribute attribute2 : profile2.getAttributes()) {
                if (attribute2.getName().equals(relationPair.getProperty2())) {
                    int neighborId = urlToEntityIds2.get((attribute2.getValue()));
                    EntityProfile neighbor = profiles2.get(neighborId);
                    for (int i = 0; i < labelPairs.length; ++i) {
                        String neighborsLabel = neighbor.getValueOf(labelPairs[i].getProperty2());
                        if (neighborsLabel != null) {
                            links2.put(neighborsLabel, profile2.getEntityUrl());                    
                        }
                    }
                }                    
            }            
        }
                
        double candidatePairs = 0;
        Set<String> entities1Total = new HashSet<>();
        Set<String> entities2Total = new HashSet<>();
        for (String value : links1.keySet()) {
            Set<String> entities1 = (Set)links1.get(value);
            entities1Total.addAll(entities1);
            if (links2.get(value) != null) {
                Set<String> entities2 = (Set)links2.get(value);
                entities2Total.addAll(entities2);
                candidatePairs += entities1.size() * entities2.size();
            }
        }
        
        //System.out.println("e1Size:"+entities1Total.size()+", e2Size:"+entities2Total.size()+", candidate pairs:"+candidatePairs);
        if (candidatePairs == 0) {
            return 0;
        }
        return Math.min(entities1Total.size(), entities2Total.size()) / candidatePairs;        
    }
    
    
    public double getNeighborSimilarityForRelation(EntityProfile e1, EntityProfile e2, PropertyPair[] relationPairs, PropertyPair[] labelPairs) {        
        double result = 0;
        for (PropertyPair relationPair : relationPairs) {
            String neighbor1 = e1.getValueOf(relationPair.getProperty1());
            String neighbor2 = e2.getValueOf(relationPair.getProperty2());
            
            for (PropertyPair labelPair : labelPairs) {
                String value1 = profiles1.get(urlToEntityIds1.get(neighbor1)).getValueOf(labelPair.getProperty1());
                String value2 = profiles2.get(urlToEntityIds2.get(neighbor2)).getValueOf(labelPair.getProperty2());
                
                double similarity = new LevenshteinSimilarity(value1, value2).getLevenshteinSimilarity();
                if (similarity > result) {
                    result = similarity;
                }
            }
        }
        return result;
    }
    
    public double getNeighborSimilarityForRelation(EntityProfile e1, EntityProfile e2, PropertyPair[] relationPairs) {        
        double similarity = 0;
        double neighborPairs = 0;
        for (PropertyPair relationPair : relationPairs) {
            String neighbor1URL = e1.getValueOf(relationPair.getProperty1());
            String neighbor2URL = e2.getValueOf(relationPair.getProperty2());
            
            if (neighbor1URL != null && neighbor2URL != null) {
                neighborPairs ++;
                EntityProfile neighbor1 = profiles1.get(urlToEntityIds1.get(neighbor1URL));
                EntityProfile neighbor2 = profiles2.get(urlToEntityIds2.get(neighbor2URL));
            
            
                similarity += getValueSim(neighbor1, neighbor2);                        
            }
        }
        
        return (neighborPairs > 0) ? (similarity/neighborPairs) : 0;
    }
    
    
    public double getNeighborSimilarityForRelations(EntityProfile e1, EntityProfile e2, String[] relations1, String[] relations2) {        
        double similarity = 0;
        double neighborPairs = 0;
        for (String relation1 : relations1) {
            String neighbor1URL = e1.getValueOf(relation1);
            if (neighbor1URL == null) {
                continue;
            }
            for (String relation2 : relations2) {
                String neighbor2URL = e2.getValueOf(relation2);
                if (neighbor2URL == null) {
                    continue;
                }
                
                Integer neighbor1Id = urlToEntityIds1.get(neighbor1URL);
                if (neighbor1Id == null) {
                    continue;
                }
                EntityProfile neighbor1 = profiles1.get(neighbor1Id);
                Integer neighbor2Id = urlToEntityIds2.get(neighbor2URL);
                if (neighbor2Id == null) {
                    continue;
                }
                EntityProfile neighbor2 = profiles2.get(neighbor2Id);
            
                neighborPairs ++;
                similarity += getValueSim(neighbor1, neighbor2);                        
            }
        }
        
        return (neighborPairs > 0) ? (similarity/neighborPairs) : 0;
    }
    
    
    
    private double getAvgNeighborSimilarityForRelations(EntityProfile e1, EntityProfile e2, String[] relations1, String[] relations2) {        
        double sumSimilarity = 0;        
        int numPairs = 0;
        for (String relation1 : relations1) {
            String neighbor1URL = e1.getValueOf(relation1);
            if (neighbor1URL == null) {
                continue;
            }
            for (String relation2 : relations2) {
                String neighbor2URL = e2.getValueOf(relation2);
                if (neighbor2URL == null) {
                    continue;
                }
                
                Integer neighbor1Id = urlToEntityIds1.get(neighbor1URL);
                if (neighbor1Id == null) {
                    continue;
                }
                EntityProfile neighbor1 = profiles1.get(neighbor1Id);
                Integer neighbor2Id = urlToEntityIds2.get(neighbor2URL);
                if (neighbor2Id == null) {
                    continue;
                }
                EntityProfile neighbor2 = profiles2.get(neighbor2Id);
                double similarity= getValueSim(neighbor1, neighbor2);       
                sumSimilarity += similarity;
                numPairs++;                
            }
        }
        
        return (numPairs == 0) ? 0 : sumSimilarity/numPairs;
    }
    
    
    private double getMaxNeighborSimilarityForRelations(EntityProfile e1, EntityProfile e2, String[] relations1, String[] relations2) {        
        double maxSimilarity = 0;        
        for (String relation1 : relations1) {
            String neighbor1URL = e1.getValueOf(relation1);
            if (neighbor1URL == null) {
                continue;
            }
            for (String relation2 : relations2) {
                String neighbor2URL = e2.getValueOf(relation2);
                if (neighbor2URL == null) {
                    continue;
                }
                
                Integer neighbor1Id = urlToEntityIds1.get(neighbor1URL);
                if (neighbor1Id == null) {
                    continue;
                }
                EntityProfile neighbor1 = profiles1.get(neighbor1Id);
                Integer neighbor2Id = urlToEntityIds2.get(neighbor2URL);
                if (neighbor2Id == null) {
                    continue;
                }
                EntityProfile neighbor2 = profiles2.get(neighbor2Id);
                double similarity= getValueSim(neighbor1, neighbor2);                        
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                }
            }
        }
        
        return maxSimilarity;
    }
    
    
    
    
    private String[] getTopKRelationsPerEntity(EntityProfile e, List<String> relations, int K) {
        PriorityQueue<CustomRelation> topK1 = new PriorityQueue<>(K);
        Set<String> allAttributes = e.getAllAttributeNames();
        for (String relation : allAttributes) {            
            int relationRank = relations.indexOf(relation);
            if (relationRank == -1) { //then this is not a relation
                continue;
            }
            /*if (allAttributes.contains(relation.replace("/ontology/", "/property/"))) { //dbpedia contains duplicate properties, keep one
                continue;
            }*/
            CustomRelation curr = new CustomRelation(relation, relationRank);
            topK1.add(curr);
            if (topK1.size() > K) {
                topK1.poll();
            }
        }
        
        String[] result = new String[topK1.size()];
        int i = 0;
        while (!topK1.isEmpty()) {
            result[i++] = topK1.poll().getString();
        }
        return result;
    }
    
    /**
     * Keep topK e1's relations and topK e2's relations and call getNeighborSimilarityForRelations without K
     * @param e1
     * @param e2
     * @param relations1
     * @param relations2
     * @param K
     * @return 
     */
    private double getNeighborSimilarityForRelations(EntityProfile e1, EntityProfile e2, List<String> relations1, List<String> relations2, int K) {                
        String[] topKRelations1 = getTopKRelationsPerEntity(e1, relations1, K);
        String[] topKRelations2 = getTopKRelationsPerEntity(e2, relations2, K);
        
        //System.out.println("The top relations of "+e1.getEntityUrl()+" are: "+Arrays.toString(topKRelations1));
        //System.out.println("The top relations of "+e2.getEntityUrl()+" are: "+Arrays.toString(topKRelations2));
        return getNeighborSimilarityForRelations(e1, e2, topKRelations1, topKRelations2);        
    }
    
    /**
     * Keep topK e1's relations and topK e2's relations and call getNeighborSimilarityForRelations without K
     * @param e1
     * @param e2
     * @param relations1
     * @param relations2
     * @param K
     * @return 
     */
    private double getMaxNeighborSimilarityForRelations(EntityProfile e1, EntityProfile e2, List<String> relations1, List<String> relations2, int K) {                
        String[] topKRelations1 = getTopKRelationsPerEntity(e1, relations1, K);
        String[] topKRelations2 = getTopKRelationsPerEntity(e2, relations2, K);
        
        //System.out.println("The top relations of "+e1.getEntityUrl()+" are: "+Arrays.toString(topKRelations1));
        //System.out.println("The top relations of "+e2.getEntityUrl()+" are: "+Arrays.toString(topKRelations2));
        return getMaxNeighborSimilarityForRelations(e1, e2, topKRelations1, topKRelations2);        
    }
    
    /**
     * Keep topK e1's relations and topK e2's relations and call getNeighborSimilarityForRelations without K
     * @param e1
     * @param e2
     * @param relations1
     * @param relations2
     * @param K
     * @return 
     */
    private double getAvgNeighborSimilarityForRelations(EntityProfile e1, EntityProfile e2, List<String> relations1, List<String> relations2, int K) {                
        String[] topKRelations1 = getTopKRelationsPerEntity(e1, relations1, K);
        String[] topKRelations2 = getTopKRelationsPerEntity(e2, relations2, K);
        
        //System.out.println("The top relations of "+e1.getEntityUrl()+" are: "+Arrays.toString(topKRelations1));
        //System.out.println("The top relations of "+e2.getEntityUrl()+" are: "+Arrays.toString(topKRelations2));
        return getAvgNeighborSimilarityForRelations(e1, e2, topKRelations1, topKRelations2);        
    }
    
    
    
    //utility methods
    
    
    protected Set<String> getAllPropertiesFromCollection(List<EntityProfile> profiles) {
        Set<String> attributeNames = new HashSet<>();
        profiles.stream().forEach((profile) -> attributeNames.addAll(profile.getAllAttributeNames()));
        return attributeNames;
    }
    
    
    protected Set<String> getAllDatatypePropertiesFromCollection(List<EntityProfile> profiles) {
        Set<String> datatypeProperties = getAllPropertiesFromCollection(profiles);
        datatypeProperties.removeAll(getAllEntityRelationsFromCollection(profiles));
        return datatypeProperties;
    }
    
    /** 
     * Returns the set of all object properties (not datatype properties) in an entity collection. 
     * An object property can be found if at least one of its values is an entityURL in this collection 
     * (i.e., it appears as a subject of some triples).
     * @param profiles
     * @return 
     */
    protected Set<String> getAllEntityRelationsFromCollection(List<EntityProfile> profiles) {
        Set<String> relations = new HashSet<>();
        Set<String> entityURLs = getEntityURLs(profiles);    
        
        Multiset<String> nonRelationCount = HashMultiset.create();
        Multiset<String> relationCount = HashMultiset.create();
        
        for (EntityProfile profile : profiles) {
            for (Attribute att: profile.getAttributes()) {                
                if (entityURLs.contains(att.getValue())) {                     
                    /*
                    if (relations.add(att.getName())) {
                        System.out.println("Adding relation "+att.getName()+" because it links to "+att.getValue()+" for entity "+profile.getEntityUrl());
                    }
                    */
//                    relations.add(att.getName());
                    relationCount.add(att.getName());
                } else {
                    nonRelationCount.add(att.getName());
                }
            }
        }        
        
        //majority voting
        for (String relation : relationCount) {
            if (relationCount.count(relation) > nonRelationCount.count(relation)) {
                relations.add(relation);
            }
        }
        return relations;
    }
    
    /**
     * Get all the relations of the given collection, sorted by the f-measure of support and discriminability
     * @param profiles1
     * @param MIN_SUPPORT
     * @return 
     */
    private List<String> getAllRelationsSorted(List<EntityProfile> profiles, double MIN_SUPPORT) {
        double max_support = 0;        
        Map<String, Double> supportOfRelation = new HashMap<>();
        Set<String> relations = getAllEntityRelationsFromCollection(profiles);
        int numRelations = relations.size();
        int cnt = 0;
        for (String relation : relations) {
            if (++cnt % 10 == 0) {
                System.out.println("Checking relation "+cnt+"/"+numRelations+": "+relation);                
            }
            double support = getRelationSupport(relation, profiles);   
            if (support > max_support) {
                max_support = support;
            }
            supportOfRelation.put(relation, support);
        }
        
        Map<String, Double> scoredRelations = new HashMap<>();
        for (String relation : relations) {
            double support = supportOfRelation.get(relation) / max_support;
            if (support > MIN_SUPPORT) {                
                double discrim = getPropertyDiscriminability(relation, profiles);            
                double fMeasure = 2 * support * discrim / (support + discrim);                
                //System.out.print(relation+": "+fMeasure);
                //System.out.println(". support: "+support+", discriminability: "+discrim);                
                scoredRelations.put(relation, fMeasure);
            }
        }
        
        Map<String,Double> sortedRelations = sortByValue(scoredRelations);
        return new ArrayList<>(sortedRelations.keySet()); //sorted in descending score
    }
    
    private Map<String,Integer> getEntityURLtoEntityID(List<EntityProfile> profiles) {
        Map<String, Integer> urlToEntityIds = new HashMap<>();
        for (int i = 0; i < profiles.size(); ++i) {
            urlToEntityIds.put(profiles.get(i).getEntityUrl(), i);
        }
        return urlToEntityIds;
    }
    
    private Set<String> getEntityURLs(List<EntityProfile> profiles) {
        Set<String> entityURLs = new HashSet<>();
        for (EntityProfile profile : profiles) {
            entityURLs.add(profile.getEntityUrl());
        }
        return entityURLs;
    }
    
    
    private String[] getTopKRelations(List<EntityProfile> profiles, int K, double MIN_SUPPORT){         
        double max_support = 0;        
        Map<String, Double> supportOfRelation = new HashMap<>();
        Set<String> relations = getAllEntityRelationsFromCollection(profiles);
        int numRelations = relations.size();
        int cnt = 0;
        for (String relation : relations) {
            if (++cnt % 10 == 0) {
                System.out.println("Checking relation "+cnt+"/"+numRelations+": "+relation);                
            }
            double support = getRelationSupport(relation, profiles);   
            if (support > max_support) {
                max_support = support;
            }
            supportOfRelation.put(relation, support);
        }
                
        PriorityQueue<CustomRelation> topKRelations = new PriorityQueue<>(K);        

        for (String relation : relations) {
            double support = supportOfRelation.get(relation) / max_support;
            if (support > MIN_SUPPORT) {                
                double discrim = getPropertyDiscriminability(relation, profiles);            
                double fMeasure = 2 * support * discrim / (support + discrim);                
                System.out.print(relation+": "+fMeasure);
                System.out.println(". support: "+support+", discriminability: "+discrim);
                CustomRelation curr = new CustomRelation(relation, fMeasure);
                topKRelations.add(curr);
                if (topKRelations.size() > K) {
                    topKRelations.poll();
                }
            }
        }
        
        //System.out.println(Arrays.toString(top3Relations1.toArray(new CustomRelation[top3Relations1.size()])));
        String[] topRelations = new String[topKRelations.size()];
        for (int i = 0; i < topRelations.length; ++i) {
            topRelations[i] = topKRelations.poll().getString();            
        }
        System.out.println(Arrays.toString(topRelations));
        return topRelations;
    }
    
    private String[] getLabels(List<EntityProfile> profiles, int K, double MIN_SUPPORT){ 
        double max_support = 0;
        Map<String, Double> supportOfProperty = new HashMap<>();
        Set<String> properties = getAllDatatypePropertiesFromCollection(profiles);
        int numProperties = properties.size();
        int cnt = 0;
        for (String relation : properties) {
            if (++cnt % 10 == 0) {
                System.out.println("Checking relation "+cnt+"/"+numProperties+": "+relation);                
            }
            double support = getRelationSupport(relation, profiles);   
            if (support > max_support) {
                max_support = support;
            }
            supportOfProperty.put(relation, support);
        }
                
        PriorityQueue<CustomRelation> topKproperties = new PriorityQueue<>(K);        

        for (String property : properties) {
            double support = supportOfProperty.get(property) / max_support;
            if (support > MIN_SUPPORT) {                
                double discrim = getPropertyDiscriminability(property, profiles);            
                double fMeasure = 2 * support * discrim / (support + discrim);                
                System.out.print(property+": "+fMeasure);
                System.out.println(". support: "+support+", discriminability: "+discrim);
                CustomRelation curr = new CustomRelation(property, fMeasure);
                topKproperties.add(curr);
                if (topKproperties.size() > K) {
                    topKproperties.poll();
                }
            }
        }
        
        //System.out.println(Arrays.toString(top3Relations1.toArray(new CustomRelation[top3Relations1.size()])));
        String[] topProperties = new String[topKproperties.size()];
        for (int i = 0; i < topProperties.length; ++i) {
            topProperties[i] = topKproperties.poll().getString();            
        }
        Collections.reverse(Arrays.asList(topProperties));
        System.out.println(Arrays.toString(topProperties));
        return topProperties;
        
    }
    
    /**
     * Copied from http://stackoverflow.com/a/2581754/2516301
     * @param <K>
     * @param <V>
     * @param map
     * @return 
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())) //Collections.reverseOrder() for descending, comment out for ascending
            .collect(Collectors.toMap(
              Map.Entry::getKey, 
              Map.Entry::getValue, 
              (e1, e2) -> e1, 
              LinkedHashMap::new
            ));
    }
    
    
    
    //tests start here
    public static void main (String[] args) {
        //Restaurants dataset
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
//        String dataset1 = basePath+"restaurant1Profiles";
//        String dataset2 = basePath+"restaurant2Profiles";
//        String datasetGroundtruth = basePath+"restaurantIdDuplicates";  
        
        //Rexa-DBLP
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
//        String dataset1 = basePath+"rexaProfiles";
//        String dataset2 = basePath+"swetodblp_april_2008Profiles";
//        String datasetGroundtruth = basePath+"rexa_dblp_goldstandardIdDuplicates";
        
        //BBCmusic-DBpedia dataset
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\bbcMusic\\";
        String dataset1 = basePath+"bbc-musicNewNoRdfProfiles";
        String dataset2 = basePath+"dbpedia37processedNewNoSameAsNoWikipediaSortedProfiles";
        String datasetGroundtruth = basePath+"bbc-music_groundTruthUTF8IdDuplicates";
        
        double MIN_SUPPORT = 0.01;   //TODO: tune those parameters        
        int topK =3; //TODO: test those parameters
        
        //override input parameters if run in console
        if (args.length >= 3) {
            dataset1 = args[0];
            dataset2 = args[1];
            datasetGroundtruth = args[2];
            if (args.length == 5) {
                MIN_SUPPORT = Double.parseDouble(args[3]);
                topK = Integer.parseInt(args[4]);
            }
        }
        
//        testTopKLocalRelations(dataset1, dataset2, datasetGroundtruth, topK, MIN_SUPPORT);
//        testTopKGlobal(dataset1, dataset2, datasetGroundtruth, topK, MIN_SUPPORT);
        testLabelDetection(dataset1, dataset2, topK, MIN_SUPPORT);
    }
    
    
    public static void testLabelDetection (String dataset1, String dataset2, int K, double MIN_SUPPORT) {
        PropertyWeights pw = new PropertyWeights(dataset1, dataset2, null);
        List<EntityProfile> profiles1 = pw.getProfiles1();
        List<EntityProfile> profiles2 = pw.getProfiles2();
        
        System.out.println("D1");
        String[] labels1 = pw.getLabels(profiles1, K, MIN_SUPPORT);
        System.out.println("D2");
        String[] labels2 = pw.getLabels(profiles2, K, MIN_SUPPORT);
        
        System.out.println("D1");
        System.out.println(Arrays.toString(labels1));
        
        System.out.println("D2");
        System.out.println(Arrays.toString(labels2));        
       
    }
    
    
    
    public static void testTopKLocalRelations (String dataset1, String dataset2, String datasetGroundtruth, int K, double MIN_SUPPORT) {
        PropertyWeights pw = new PropertyWeights(dataset1, dataset2, datasetGroundtruth);
        List<EntityProfile> profiles1 = pw.getProfiles1();
        List<EntityProfile> profiles2 = pw.getProfiles2();
                        
        //System.out.println("\n\nDataset 1 relations:\n");        
        List<String> relations1Sorted = pw.getAllRelationsSorted(profiles1, MIN_SUPPORT);
        //System.out.println(Arrays.toString(relations1Sorted.toArray()));
        
        //System.out.println("\n\nDataset 2 relations:\n");
        List <String> relations2Sorted = pw.getAllRelationsSorted(profiles2, MIN_SUPPORT);
        //System.out.println(Arrays.toString(relations2Sorted.toArray()));
                
        //now, get the value and neighbor sim of the matches 
        System.out.println("Creating the models for weighted Jaccard sim...");
        pw.createModels(); //used for weighted jaccard value sim
        System.out.println("valueSim:neighborSim:matchingLabels");
        Set<IdDuplicates> duplicates = pw.groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            double valueSimilarity = pw.getValueSim(e1, e2);
            double neighborSimilarity = pw.getMaxNeighborSimilarityForRelations(e1, e2, relations1Sorted, relations2Sorted, K);
            int matchingLabels = pw.haveSameLabels(e1,e2) ? 1 : 0;
            
            System.out.println(valueSimilarity+":"+neighborSimilarity+":"+matchingLabels);
        }
        
        
    }
        
    public static void testTopKGlobal (String dataset1, String dataset2, String datasetGroundtruth, int K, double MIN_SUPPORT) {
        PropertyWeights pw = new PropertyWeights(dataset1, dataset2, datasetGroundtruth);
        List<EntityProfile> profiles1 = pw.getProfiles1();
        List<EntityProfile> profiles2 = pw.getProfiles2();
        
        System.out.println("\n\nDataset 1 relations:\n");
        String[] topRelations1 = pw.getTopKRelations(profiles1, K, MIN_SUPPORT);
        
        System.out.println("\n\nDataset 2 relations:\n");
        String[] topRelations2 = pw.getTopKRelations(profiles2, K, MIN_SUPPORT);
        
        
        //now, get the value and neighbor sim of the matches 
        System.out.println("Creating the models for weighted Jaccard sim...");
        pw.createModels(); //used for weighted jaccard value sim
        System.out.println("valueSim:neighborSim");
        Set<IdDuplicates> duplicates = pw.groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            double valueSimilarity = pw.getValueSim(e1, e2);
            double neighborSimilarity = pw.getNeighborSimilarityForRelations(e1, e2, topRelations1, topRelations2);
            
            System.out.println(valueSimilarity+":"+neighborSimilarity);
        }
        
        
    }
    
    
    
    
    public static void testLinkKeys (String dataset1, String dataset2, String datasetGroundtruth, int K) {        
        PropertyWeights pw = new PropertyWeights(dataset1, dataset2, datasetGroundtruth);
        List<EntityProfile> profiles1 = pw.getProfiles1();
        List<EntityProfile> profiles2 = pw.getProfiles2();
        
        System.out.println("\n\nGetting the support and discriminability of attribute pairs (later used as labels):\n");        
        PriorityQueue<PropertyPair> labelPairs = new PriorityQueue<>(K, new PropertyPairComparator());
        for (String att1 : pw.getAllPropertiesFromCollection(profiles1)) {
            System.out.println("Checking "+att1+" with all attributes of D2...");
            for (String att2 : pw.getAllPropertiesFromCollection(profiles2)) {
                PropertyPair labelPair = new PropertyPair(att1, att2, 0);
                double pairSupport = pw.getPropertyPairSupport(labelPair);
                double pairDiscrim = pw.getPropertyPairDiscriminability(att1, att2);
                double fMeasure = 2 * pairSupport * pairDiscrim / (pairSupport + pairDiscrim);
                labelPair.setScore(fMeasure);
                
                if (fMeasure > 0) {
                    labelPairs.add(labelPair);
                    if (labelPairs.size() > K) {
                        labelPairs.poll();
                    }
                    
                    System.out.println(labelPair);
                    System.out.println("support: "+pairSupport);
                    System.out.println("discriminability: "+pairDiscrim);
                    System.out.println();
                }
                
            }
        }
        System.out.println(Arrays.toString(labelPairs.toArray(new PropertyPair[labelPairs.size()])));
        
        
        System.out.println("\n\nGetting the support and discriminability of relation pairs (later used as key relations):\n");        
        PriorityQueue<PropertyPair> relationPairs = new PriorityQueue<>(K, new PropertyPairComparator());        
        for (String relation1 : pw.getAllEntityRelationsFromCollection(profiles1)) {
            for (String relation2 : pw.getAllEntityRelationsFromCollection(profiles2)) {
                PropertyPair relationPair = new PropertyPair(relation1, relation2, 0);
                double pairSupport = pw.getPropertyPairSupport(relationPair);
                double pairDiscrim = pw.getRelationPairDiscriminability(relationPair, labelPairs.toArray(new PropertyPair[labelPairs.size()]));
                double fMeasure = 2 * pairSupport * pairDiscrim / (pairSupport + pairDiscrim);
                
                relationPair.setScore(fMeasure);
                if (fMeasure > 0) {
                    relationPairs.add(relationPair);
                    if (relationPairs.size() > K) {
                        relationPairs.poll();
                    }
                    System.out.println(relationPair);
                    System.out.println("support: "+pairSupport);
                    System.out.println("discriminability: "+pairDiscrim);
                    System.out.println();
                }
                
            }
        }
        
        System.out.println(Arrays.toString(relationPairs.toArray(new PropertyPair[relationPairs.size()])));   
        
        pw.createModels(); //used for weighted jaccard value sim
        Set<IdDuplicates> duplicates = pw.groundTruth.getDuplicates();
        for (IdDuplicates duplicate : duplicates) {
            EntityProfile e1 = profiles1.get(duplicate.getEntityId1());
            EntityProfile e2 = profiles2.get(duplicate.getEntityId2());
            double neighborSimilarity = pw.getNeighborSimilarityForRelation(
                    e1, 
                    e2, 
                    relationPairs.toArray(new PropertyPair[relationPairs.size()])
                    //, labelPairs.toArray(new PropertyPair[labelPairs.size()])
            );
            double valueSimilarity = pw.getValueSim(e1, e2);
            System.out.println(valueSimilarity+":"+neighborSimilarity);
        }
        
    }

    
    /**
     * Copied (and altered) from http://stackoverflow.com/a/16297127/2516301
     */
    private static class CustomRelation implements Comparable<CustomRelation> {
    // public final fields ok for this small example
    public final String string;
    public double value;

    public CustomRelation(String string, double value) {
        this.string = string;
        this.value = value;
    }

    @Override
    public int compareTo(CustomRelation other) {
        // define sorting according to double fields
        return Double.compare(value, other.value); 
    }
    
    public String getString(){
        return string;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return string+":"+value;
    }
}

}
