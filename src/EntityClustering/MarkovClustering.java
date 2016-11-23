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
package EntityClustering;

import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityEdge;
import DataModel.SimilarityPairs;
import Utilities.Comparators.SimilarityEdgeComparator;
import Utilities.TextModels.AbstractModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author G.A.P. II
 */
public class MarkovClustering implements IEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(MarkovClustering.class.getName());

    private int noOfEntities;
    private int datasetLimit;
    private final SimpleGraph similarityGraph;
    private double matrixSimThreshold = 0.00001;//define similarity threshold for matrix comparison
    private int similarityChecksLimit = 2;//define check repetitions limit for the expansion-inflation process
    private double clusterThreshold = 0.001;//define similarity threshold for including in final graph

    public MarkovClustering() {
        similarityGraph = new SimpleGraph(DefaultEdge.class);
        LOGGER.log(Level.INFO, "Initializing Connected Components clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeGraph(simPairs);
        SimilarityEdgeComparator SEcomparator = new SimilarityEdgeComparator();
        PriorityQueue<SimilarityEdge> SEqueue = 
            new PriorityQueue<SimilarityEdge>(simPairs.getNoOfComparisons(), SEcomparator);
        // add an edge for every pair of entities with a weight higher than the threshold
        double threshold = getSimilarityThreshold(simPairs);
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        double[][] simMatrix = new double[noOfEntities][noOfEntities];
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                simMatrix[comparison.getEntityId1()][comparison.getEntityId2()+ datasetLimit]=comparison.getUtilityMeasure();
            }
        }
        addSelfLoop(simMatrix);
        Normalize(simMatrix);
        double[][] atStart = new double[noOfEntities][noOfEntities];
        int count = 0;
        do
        {
        	for (int i = 0; i < noOfEntities; i++)
                for (int j = 0; j < noOfEntities; j++)
                        atStart[i][j] = simMatrix[i][j];
        	expand2(simMatrix);      
	        Normalize(simMatrix);      
	        Hadamard(simMatrix, 2);      
	        Normalize(simMatrix);      
        	count++;

        }
        while ((!areSimilar(atStart, simMatrix))&&(count<similarityChecksLimit));
        
        int n1 = simMatrix.length;
        int upLimit=n1;
        int lowLimit=0;
        if (datasetLimit!=0)
        {
        	upLimit=datasetLimit;
        	lowLimit=datasetLimit;
        }
        for (int i=0; i<upLimit; i++)
        {
        	for (int j=lowLimit; j<n1; j++)
        	{
        		int v1 = i;
                int v2 = j;
                double sim = Math.max(simMatrix[i][j], simMatrix[j][i]);
                if ((sim>clusterThreshold)&&(i!=j))
                {
                    similarityGraph.addEdge(v1, v2);
                }
        	}
        }
            


        
        

        // get connected components
        ConnectivityInspector ci = new ConnectivityInspector(similarityGraph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();

        // prepare output
        List<EquivalenceCluster> equivalenceClusters = new ArrayList<>();
        for (Set<Integer> componentIds : connectedComponents) {
            EquivalenceCluster newCluster = new EquivalenceCluster();
            equivalenceClusters.add(newCluster);
            if (!simPairs.isCleanCleanER()) {
                newCluster.loadBulkEntityIdsD1(componentIds);
                continue;
            }

            for (Integer entityId : componentIds) {
                if (entityId < datasetLimit) {
                    newCluster.addEntityIdD1(entityId);
                } else {
                    newCluster.addEntityIdD2(entityId-datasetLimit);
                }
            }

        }
        return equivalenceClusters;
    }

    public int getMaxEntityId(int[] entityIds) {
        int maxId = Integer.MIN_VALUE;
        for (int i = 0; i < entityIds.length; i++) {
            if (maxId < entityIds[i]) {
                maxId = entityIds[i];
            }
        }
        return maxId;
    }

    @Override
    public String getMethodInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private double getSimilarityThreshold(SimilarityPairs simPairs) {
        double averageSimilarity = 0;
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            averageSimilarity += comparison.getUtilityMeasure();

        }
        averageSimilarity /= simPairs.getNoOfComparisons();

        double standardDeviation = 0;
        iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            standardDeviation += Math.pow(comparison.getUtilityMeasure()-averageSimilarity, 2.0);
        }
        standardDeviation = Math.sqrt(standardDeviation/simPairs.getNoOfComparisons());

        double threshold = 0.5;//+3 * standardDeviation 

        LOGGER.log(Level.INFO, "Similarity threshold : {0}", threshold);
        return threshold;
    }
    
    
    private void initializeGraph(SimilarityPairs simPairs) {
        int maxEntity1 = getMaxEntityId(simPairs.getEntityIds1());
        int maxEntity2 = getMaxEntityId(simPairs.getEntityIds2());
        if (simPairs.isCleanCleanER()) {
            datasetLimit = maxEntity1 + 1;
            noOfEntities = maxEntity1 + maxEntity2 + 2;
        } else {
            datasetLimit = 0;
            noOfEntities = Math.max(maxEntity1, maxEntity2) + 1;
        }

        for (int i = 0; i < noOfEntities; i++) {
            similarityGraph.addVertex(i);
        }
        LOGGER.log(Level.INFO, "Added {0} nodes in the graph", noOfEntities);
    }
    
    private void expand2(double[][] inputMatrix) {
    	
    	double[][] input = multiply(inputMatrix, inputMatrix);
    	for (int i = 0; i < inputMatrix.length; i++)
            for (int j = 0; j < inputMatrix[0].length; j++)
                    inputMatrix[i][j] = input[i][j];
    }

    
    private double[][] multiply(double[][] a, double[][] b) {
        int n1 = a.length;
        int upLimit=n1;
        int lowLimit=0;
        if (datasetLimit!=0)
        {
        	upLimit=datasetLimit;
        	lowLimit=datasetLimit;
        }
        
        if (n1 != a[0].length) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] c = new double[n1][n1];
        for (int i = 0; i < upLimit; i++)
        {
            for (int j = lowLimit; j < n1; j++)
            {
            	for (int k = 0; k < n1; k++)
	                {
	                    c[i][j] += a[i][k] * b[k][j];
	                }
            	
            	
            }
        }
        
        if (datasetLimit==0)
        {
        	return c;
        }
        
        for (int i = 0; i < upLimit; i++)
        {
        	c[i][i] += a[i][i] * b[i][i];
        }
        for (int j = lowLimit; j < n1; j++)
        {
        	c[j][j] += a[j][j] * b[j][j];
        }    
        return c;
    }
    
    private void Hadamard(double[][] a, int pow) {
        int m1 = a.length;
        int n1 = a[0].length;
        for (int i = 0; i < m1; i++)
        {
            for (int j = 0; j < n1; j++)
            {
                    a[i][j] = Math.pow(a[i][j], pow);
            }
        }
    }
    
    private boolean areSimilar(double[][] a, double[][] b) {
        int m1 = a.length;
        int n1 = a[0].length;
        int m2 = b.length;
        int n2 = b[0].length;
    	if (m1 != m2) return false;
    	if (n1 != n2) return false;
        for (int i = 0; i < m1; i++)
            for (int j = 0; j < n1; j++)
                    if (Math.abs(a[i][j] - b[i][j])>matrixSimThreshold) return false;
        return true;
        
    }
    
    private void Normalize(double[][] a) {
        int m1 = a.length;
        int n1 = a[0].length;
        double[][] c = new double[m1][n1];
        for (int j = 0; j < n1; j++)
        {
        	double sumCol=0.0;
            for (int i = 0; i < m1; i++) 
            {
            	sumCol+=a[i][j];
            }

            for (int i = 0; i < m1; i++) 
            {
            	a[i][j]=a[i][j]/sumCol;
            }
            	
        }
    }
    
    private void addSelfLoop(double[][] a) {
        int m1 = a.length;
        for (int i = 0; i < m1; i++)
        {
        	a[i][i]=1.0;
        }
    }
    
    public void setMatrixSimThreshold(double matrixSimThreshold) {
        this.matrixSimThreshold = matrixSimThreshold;
    }
    
    public void setclusterThreshold(double clusterThreshold) {
        this.clusterThreshold = clusterThreshold;
    }
    
    public void setsimilarityChecksLimit(int similarityChecksLimit) {
        this.similarityChecksLimit = similarityChecksLimit;
    }
    
}
