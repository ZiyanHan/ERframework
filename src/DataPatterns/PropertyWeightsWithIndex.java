package DataPatterns;

import DataModel.Attribute;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.apache.jena.ext.com.google.common.collect.HashBasedTable;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.Multiset;
import org.apache.jena.ext.com.google.common.collect.Table;

/**
 *
 * @author vefthym
 */
public class PropertyWeightsWithIndex extends PropertyWeights {

    public PropertyWeightsWithIndex(String data1Path, String data2Path, String groundTruthPath) {
        super(data1Path, data2Path, groundTruthPath);
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
    
    /**
     * Returns an index of the spo profiles by object o. The output index is a table
     * which is a short for Map<String,Map<String,String>>, where row: object, column: subject (entityId), value: predicate
     * @param profiles
     * @param urlToEntityIds
     * @return 
     */
    private Table<String,Integer,String> indexByObject(List<EntityProfile> profiles, Map<String,Integer> urlToEntityIds) {
        System.out.println("Creating the first osp index...");
        Table<String,Integer,String> osp = HashBasedTable.create();        
        for (EntityProfile profile: profiles) {
            int entityId = urlToEntityIds.get(profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                osp.put(attribute.getValue(), entityId, attribute.getName());
            }            
        }
        return osp;
    }
    
    /**
     * Returns an index of the spo profiles by object o. The output index is a table
     * which is a short for Map<String,Map<String,String>>, where row: object, column: subject, value: predicate. 
     * It only adds a value to the index, if this value also appears in the input otherIndex (to save some memory space). 
     * @param profiles
     * @param urlToEntityIds
     * @param otherIndex
     * @return 
     */
    private Table<String,Integer,String> indexByObjectGivenAnotherIndex(List<EntityProfile> profiles, Map<String,Integer> urlToEntityIds, Table<String,Integer,String> otherIndex) {
        System.out.println("Creating the second osp index...");
        Table<String,Integer,String> osp = HashBasedTable.create();
        for (EntityProfile profile: profiles) {
            int entityId = urlToEntityIds.get(profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                String value = attribute.getValue();
                if (otherIndex.containsRow(value)) { //to save some space, we don't need values from only one collection
                    osp.put(value, entityId, attribute.getName());
                }
            }            
        }
        return osp;
    }
    
    /**
     * Returns an index of the spo profiles by object o. The output index is a table
     * which is a short for Map<String,Map<String,String>>, where row: object (entityId of object), column: predicate, value: subject (entityId). 
     * The predicates are only object properties, i.e., entity relations. 
     * @param profiles
     * @param urlToEntityIds
     * @return 
     */
    private Table<Integer,String,Integer> indexByObjectRelation(List<EntityProfile> profiles, Map<String,Integer> urlToEntityIds) {
        System.out.println("Creating the first osp index...");
        Table<Integer,String,Integer> ops = HashBasedTable.create();        
        for (EntityProfile profile: profiles) {
            int entityId = urlToEntityIds.get(profile.getEntityUrl());            
            for (Attribute attribute : profile.getAttributes()) {
                Integer objectId = urlToEntityIds.get(attribute.getValue());
                if (objectId != null) {
                    ops.put(objectId, attribute.getName(), entityId);
                }
            }            
        }
        return ops;
    }
    
    
    public Map<IdDuplicates,Collection<PropertyPair>> getPropertyAgreement(Table<String,Integer,String> ospIndex1, Table<String,Integer,String> ospIndex2) {
        System.out.println("Getting the property agreement...");
        Multimap<IdDuplicates,PropertyPair> pa = HashMultimap.create();
        for (String value : ospIndex1.rowKeySet()) {
            Map<Integer,String> sp2 = ospIndex2.row(value);
            if (sp2 != null) { //then value is a common value
                
                Map<Integer,String> sp1 = ospIndex1.row(value);
                
                for (Map.Entry<Integer, String> s1p1 : sp1.entrySet()) {
                   int entityId1 = s1p1.getKey();
                   String property1 = s1p1.getValue();
                   for (Map.Entry<Integer, String> s2p2 : sp2.entrySet()) {                       
                       IdDuplicates entityPair = new IdDuplicates(entityId1, s2p2.getKey());
                       PropertyPair propertyPair = new PropertyPair(property1, s2p2.getValue(), 0);
                       
                       pa.put(entityPair, propertyPair);
                   } 
                }
                
            }
        }
        
        return pa.asMap();
    }
    
    public Map<PropertyPair,Collection<IdDuplicates>> getInversePropertyAgreement(Table<String,Integer,String> ospIndex1, Table<String,Integer,String> ospIndex2) {
        System.out.println("Getting the inverse property agreement...");
        Multimap<PropertyPair,IdDuplicates> pa = HashMultimap.create();
        for (String value : ospIndex1.rowKeySet()) {
            Map<Integer,String> sp2 = ospIndex2.row(value);
            if (sp2 != null) { //then value is a common value
                
                Map<Integer,String> sp1 = ospIndex1.row(value);
                
                for (Map.Entry<Integer, String> s1p1 : sp1.entrySet()) {
                   int entityId1 = s1p1.getKey();
                   String property1 = s1p1.getValue();
                   for (Map.Entry<Integer, String> s2p2 : sp2.entrySet()) {                       
                       IdDuplicates entityPair = new IdDuplicates(entityId1, s2p2.getKey());
                       PropertyPair propertyPair = new PropertyPair(property1, s2p2.getValue(), 0);
                                              
                       pa.put(propertyPair, entityPair);
                   } 
                }
                
            }
        }
        
        return pa.asMap();
    }
    
    
    public double getCoverageOfPair(PropertyPair propertyPair, Map<PropertyPair, Collection<IdDuplicates>> inversePropertyAgreement) {        
        Collection<IdDuplicates> entityPairs = inversePropertyAgreement.get(propertyPair);
        if (entityPairs == null) {
            return 0;
        }
        double noOfEntities = profiles1.size()+ profiles2.size();
        Set<Integer> coveredEntities1 = new HashSet<>();
        Set<Integer> coveredEntities2 = new HashSet<>();
        for (IdDuplicates entityPair : entityPairs) {
            coveredEntities1.add(entityPair.getEntityId1());
            coveredEntities2.add(entityPair.getEntityId2());
        }
        return (coveredEntities1.size() + coveredEntities2.size()) / noOfEntities;
    }
    
    
    /**
     * Returns an index of the spo profiles by predicate p. The output index is a table
     * which is a short for Map<String,Map<String,String>>, where row: predicate, column: subject (entityId), value: object
     * @param profiles
     * @param urlToEntityIds
     * @return 
     */
    private Table<String,Integer,String> indexByPredicate(List<EntityProfile> profiles, Map<String,Integer> urlToEntityIds) {
        System.out.println("Creating the first pso index...");
        Table<String,Integer,String> osp = HashBasedTable.create();        
        for (EntityProfile profile: profiles) {
            int entityId = urlToEntityIds.get(profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                osp.put(attribute.getName(), entityId, attribute.getValue());
            }            
        }
        return osp;
    }
    
    public Multiset<PropertyPair> getCandidateLabelPairs() {                
        //get osp indices
        Table<String,Integer,String> ospIndex1 = indexByObject(profiles1, urlToEntityIds1);
        Table<String,Integer,String> ospIndex2 = indexByObjectGivenAnotherIndex(profiles2, urlToEntityIds2, ospIndex1);
                
        Map<PropertyPair,Collection<IdDuplicates>> ipa = getInversePropertyAgreement(ospIndex1, ospIndex2);
                
        ipa.entrySet().stream().forEach((entry) -> {
            double coverage = getCoverageOfPair(entry.getKey(), ipa);
            System.out.println("Coverage of "+entry.getKey()+" = "+coverage);
        });
        
        
        return null;
    }
        
    public static void main (String[] args) {
        //Restaurants dataset
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String dataset1 = basePath+"restaurant1Profiles";
        String dataset2 = basePath+"restaurant2Profiles";
        String datasetGroundtruth = basePath+"restaurantIdDuplicates";  
        
        //Rexa-DBLP
//        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\rexa-dblp\\";
//        String dataset1 = basePath+"rexaProfiles";
//        String dataset2 = basePath+"swetodblp_april_2008Profiles";
//        String datasetGroundtruth = basePath+"rexa_dblp_goldstandardIdDuplicates";
//        
        //BBCmusic-DBpedia dataset
//        final String basePath = "G:\\VASILIS\\bbcMusic\\";
//        String dataset1 = basePath+"bbc-musicNewProfiles";
//        String dataset2 = basePath+"dbpedia37NewProfiles";
//        String datasetGroundtruth = basePath+"bbc-music_groundTruthUTF8IdDuplicates";
        
        PropertyWeightsWithIndex pw = new PropertyWeightsWithIndex(dataset1, dataset2, datasetGroundtruth);
        List<EntityProfile> profiles1 = pw.getProfiles1();
        List<EntityProfile> profiles2 = pw.getProfiles2();
        
        Multiset<PropertyPair> candidateLabels = pw.getCandidateLabelPairs();
        double noOfEntities = profiles1.size()+ profiles2.size();
//        for (PropertyPair candidateLabel : candidateLabels.elementSet()) {
//            System.out.println("Coverage of "+candidateLabel+" = "+(candidateLabels.count(candidateLabel) /noOfEntities));
//        }
        
        
        System.out.println("\n\nGetting the support and discriminability of attribute pairs (later used as labels):\n");        
        PriorityQueue<PropertyPair> labelPairs = new PriorityQueue<>(3, new PropertyPairComparator());
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
                    if (labelPairs.size() > 3) {
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
        PriorityQueue<PropertyPair> relationPairs = new PriorityQueue<>(3, new PropertyPairComparator());        
        for (String relation1 : pw.getAllEntityRelationsFromCollection(profiles1)) {
            for (String relation2 : pw.getAllEntityRelationsFromCollection(profiles2)) {
                PropertyPair relationPair = new PropertyPair(relation1, relation2, 0);
                double pairSupport = pw.getPropertyPairSupport(relationPair);
                double pairDiscrim = pw.getRelationPairDiscriminability(relationPair, labelPairs.toArray(new PropertyPair[labelPairs.size()]));
                double fMeasure = 2 * pairSupport * pairDiscrim / (pairSupport + pairDiscrim);
                
                relationPair.setScore(fMeasure);
                if (fMeasure > 0) {
                    relationPairs.add(relationPair);
                    if (relationPairs.size() > 3) {
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
                    relationPairs.toArray(new PropertyPair[relationPairs.size()]), 
                    labelPairs.toArray(new PropertyPair[labelPairs.size()])
            );
            double valueSimilarity = pw.getValueSim(e1, e2);
            System.out.println(valueSimilarity+":"+neighborSimilarity);
        }
        
    }

}
