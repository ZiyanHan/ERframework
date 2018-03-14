/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPatterns;

import DataModel.EntityProfile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.ext.com.google.common.collect.HashMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;

/**
 *
 * @author vefthym
 */
public class DatasetStatisticsYAGO extends DatasetStatistics {

    public DatasetStatisticsYAGO(String yagoPath) {
        super(yagoPath, null, null);
    }
    
    public Multimap<String,String> getTypesMap(String rdfTypesPath) {
        Multimap<String,String> typesMap = HashMultimap.create();
        try (BufferedReader br = new BufferedReader(new FileReader(rdfTypesPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                typesMap.put(parts[1], parts[3]);
            }            
        } catch (IOException ex) {
            System.err.println(ex);
        }
        System.out.println("Loaded "+typesMap.size()+" types");
        return typesMap;
    }
    
    public Set<String> getAllTypes(List<EntityProfile> yagoProfiles, Multimap<String,String> typesMap) {
        Set<String> existingTypes = new HashSet<>();
        yagoProfiles.stream().map(profile -> profile.getEntityUrl()).forEach(entityUrl -> existingTypes.addAll(typesMap.get(entityUrl)));
        return existingTypes;
    }
    
    public static void main(String[] args) {
        String yagoProfilesPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\yago-imdb\\yagoProfiles";
        String yagoTypesPath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\yago-imdb\\yagoTypes.tsv";
        
        if (args.length == 2) {
            yagoProfilesPath = args[0];
            yagoTypesPath = args[1];
        }
        
        DatasetStatisticsYAGO yagoStats = new DatasetStatisticsYAGO(yagoProfilesPath);
                
        Multimap typesMap = yagoStats.getTypesMap(yagoTypesPath);
        
        List<EntityProfile> yagoProfiles = yagoStats.getProfiles1();
        
        System.out.println("Types: "+yagoStats.getAllTypes(yagoProfiles, typesMap).size());
    }
}
