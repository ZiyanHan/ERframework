/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package Utilities;

import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import DataModel.Comparison;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.IdDuplicates;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public class ClustersPerformance {

    private static final Logger LOGGER = Logger.getLogger(ClustersPerformance.class.getName());

    private double fMeasure;
    private double precision;
    private double recall;
    private double totalMatches;

    private final AbstractDuplicatePropagation abstractDP;
    private final List<EquivalenceCluster> entityClusters;
    
    private Set<IdDuplicates> falseMatches;
    private Set<IdDuplicates> missedMatches;

    public ClustersPerformance(List<EquivalenceCluster> clusters, AbstractDuplicatePropagation adp) {
        abstractDP = adp;
        abstractDP.resetDuplicates();
        entityClusters = clusters;
        falseMatches = new HashSet<>();
        missedMatches = new HashSet<>();
    }

    public int getDetectedDuplicates() {
        return abstractDP.getNoOfDuplicates();
    }
    
    public int getEntityClusters() {
        return entityClusters.size();
    }

    public int getExistingDuplicates() {
        return abstractDP.getExistingDuplicates();
    }

    public double getFMeasure() {
        return fMeasure;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getTotalMatches() {
        return totalMatches;
    }

    public Set<IdDuplicates> getFalseMatches() {
        return falseMatches;
    }
    
    public Set<IdDuplicates> getMissedMatches() {
        return missedMatches;
    }

    public void printStatistics() {
        System.out.println("\n\n\n**************************************************");
        System.out.println("************** Clusters Performance **************");
        System.out.println("**************************************************");
        System.out.println("No of clusters\t:\t" + entityClusters.size());
        System.out.println("Detected duplicates\t:\t" + abstractDP.getNoOfDuplicates());
        System.out.println("Existing duplicates\t:\t" + abstractDP.getExistingDuplicates());
        System.out.println("Total matches\t:\t" + totalMatches);
        System.out.println("Precision\t:\t" + precision);
        System.out.println("Recall\t:\t" + recall);
        System.out.println("F-Measure\t:\t" + fMeasure);
    }
    
    public void printStatisticsShort() {
        System.out.println("Precision\t:\t" + precision);
        System.out.println("Recall\t:\t" + recall);
        System.out.println("F-Measure\t:\t" + fMeasure);
    }

    public void setStatistics() {
        if (entityClusters.isEmpty()) {
            LOGGER.log(Level.WARNING, "Empty set of equivalence clusters given as input!");
            return;
        }
        abstractDP.resetDuplicates(); //why?

        totalMatches = 0;
        if (abstractDP instanceof BilateralDuplicatePropagation) { // Clean-Clean ER
            for (EquivalenceCluster cluster : entityClusters) {
                for (int entityId1 : cluster.getEntityIdsD1()) {
                    for (int entityId2 : cluster.getEntityIdsD2()) {
                        totalMatches++;
                        Comparison comparison = new Comparison(true, entityId1, entityId2);
                        abstractDP.isSuperfluous(comparison); //isSuperfluous is a transformer, not an accessor!
                    }
                }
            }
            falseMatches = ((BilateralDuplicatePropagation)abstractDP).getFalseMatches();
            missedMatches = ((BilateralDuplicatePropagation)abstractDP).getMissedMatches();
        } else { // Dirty ER
            for (EquivalenceCluster cluster : entityClusters) {
                List<Integer> duplicates = cluster.getEntityIdsD1();
                Integer[] duplicatesArray = duplicates.toArray(new Integer[duplicates.size()]);

                for (int i = 0; i < duplicatesArray.length; i++) {
                    for (int j = i + 1; j < duplicatesArray.length; j++) {
                        totalMatches++;
                        Comparison comparison = new Comparison(false, duplicatesArray[i], duplicatesArray[j]);
                        abstractDP.isSuperfluous(comparison);
                    }
                }
            }
        }

        if (0 < totalMatches) {
            precision = abstractDP.getNoOfDuplicates() / totalMatches;
        } else {
            precision = 0;
        }
        recall = ((double) abstractDP.getNoOfDuplicates()) / abstractDP.getExistingDuplicates();
        if (0 < precision && 0 < recall) {
            fMeasure = 2 * precision * recall / (precision + recall);
        } else {
            fMeasure = 0;
        }
    }
    public void printStatisticsLong(List<EntityProfile> profiles1, List<EntityProfile> profiles2) {
        printStatistics();
        System.out.println(falseMatches.size()+" False Matches:\n");
        for (IdDuplicates falseMatch : falseMatches) {
            System.out.println("("+profiles1.get(falseMatch.getEntityId1()).getEntityUrl()+"(id:"+ falseMatch.getEntityId1()+"), "
                    +profiles2.get(falseMatch.getEntityId2()).getEntityUrl()+"(id:"+(falseMatch.getEntityId2()+profiles1.size()-2)+")) is a false match!");
        }
        System.out.println("\n\n"+missedMatches.size()+" Missed Matches:\n");
        for (IdDuplicates missedMatch : missedMatches) {
            System.out.println("("+profiles1.get(missedMatch.getEntityId1()).getEntityUrl()+"(id:"+ missedMatch.getEntityId1()+"), "
                    +profiles2.get(missedMatch.getEntityId2()).getEntityUrl()+"(id:"+(missedMatch.getEntityId2()+profiles1.size()-2)+")) is a missed match!");
        }
    }
}
