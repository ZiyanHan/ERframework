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
import DataModel.SimilarityPairs;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author G.A.P. II
 */
public class MarkovClustering extends AbstractEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(MarkovClustering.class.getName());

    protected double clusterThreshold;//define similarity threshold for including in final graph
    protected double matrixSimThreshold;//define similarity threshold for matrix comparison
    protected int similarityChecksLimit;//define check repetitions limit for the expansion-inflation process

    public MarkovClustering() {
        super();

        clusterThreshold = 0.001;
        matrixSimThreshold = 0.00001;
        similarityChecksLimit = 2;
        
        LOGGER.log(Level.INFO, "Initializing Markov Clustering...");
    }

    private void addSelfLoop(double[][] a) {
        int m1 = a.length;
        for (int i = 0; i < m1; i++) {
            a[i][i] = 1.0;
        }
    }

    private boolean areSimilar(double[][] a, double[][] b) {
        int m1 = a.length;
        int m2 = b.length;
        if (m1 != m2) {
            return false;
        }

        int n1 = a[0].length;
        int n2 = b[0].length;
        if (n1 != n2) {
            return false;
        }

        for (int i = 0; i < m1; i++) {
            for (int j = 0; j < n1; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > matrixSimThreshold) {
                    return false;
                }
            }
        }

        return true;
    }
    
    private void expand2(double[][] inputMatrix) {
        double[][] input = multiply(inputMatrix, inputMatrix);
        for (int i = 0; i < inputMatrix.length; i++) {
            for (int j = 0; j < inputMatrix[0].length; j++) {
                inputMatrix[i][j] = input[i][j];
            }
        }
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
        initializeData(simPairs);
        initializeGraph();
        
        // add an edge for every pair of entities with a weight higher than the threshold
        final Iterator<Comparison> iterator = simPairs.getPairIterator();
        double[][] simMatrix = new double[noOfEntities][noOfEntities];
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
                simMatrix[comparison.getEntityId1()][comparison.getEntityId2() + datasetLimit] = comparison.getUtilityMeasure();
            }
        }

        addSelfLoop(simMatrix);
        normalizeColumns(simMatrix);
        double[][] atStart = new double[noOfEntities][noOfEntities];
        int count = 0;
        do {
            for (int i = 0; i < noOfEntities; i++) {
                for (int j = 0; j < noOfEntities; j++) {
                    atStart[i][j] = simMatrix[i][j];
                }
            }
            expand2(simMatrix);
            normalizeColumns(simMatrix);
            hadamard(simMatrix, 2);
            normalizeColumns(simMatrix);
            count++;
        } while ((!areSimilar(atStart, simMatrix)) && (count < similarityChecksLimit));

        int n1 = simMatrix.length;
        int upLimit = n1;
        int lowLimit = 0;
        if (datasetLimit != 0) {
            upLimit = datasetLimit;
            lowLimit = datasetLimit;
        }
        
        for (int i = 0; i < upLimit; i++) {
            for (int j = lowLimit; j < n1; j++) {
                double sim = Math.max(simMatrix[i][j], simMatrix[j][i]);
                if ((sim > clusterThreshold) && (i != j)) {
                    similarityGraph.addEdge(i, j);
                }
            }
        }

       return getConnectedComponents();
    }

    @Override
    public String getMethodInfo() {
        return "Markov Clustering: implements the Markov Cluster Algorithm.";
    }

    @Override
    public String getMethodParameters() {
        return "The Markov Cluster algorithm involves 4 parameters:\n" 
             + explainThresholdParameter()
             + "2) cluster threshold : double, default value : 0.001.\n"
             + "It determines the similarity threshold for including an edge in the similarity graph.\n"
             + "3) matrix similarity threshold : double, default value : 0.00001.\n"
             + "It determines the similarity threshold for compariing all cells of two matrices and considering them similar.\n"
             + "4) similarity checks limit : integer, default value : 2.\n"
             + "It determines the maximum number of repetitions we apply the expansion-inflation process.\n";
    }

    private void hadamard(double[][] a, int pow) {
        int m1 = a.length;
        int n1 = a[0].length;
        for (int i = 0; i < m1; i++) {
            for (int j = 0; j < n1; j++) {
                a[i][j] = Math.pow(a[i][j], pow);
            }
        }
    }

    private double[][] multiply(double[][] a, double[][] b) {
        int n1 = a.length;
        if (n1 != a[0].length) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        
        int upLimit = n1;
        int lowLimit = 0;
        if (datasetLimit != 0) {
            upLimit = datasetLimit;
            lowLimit = datasetLimit;
        }
        
        double[][] c = new double[n1][n1];
        for (int i = 0; i < upLimit; i++) {
            for (int j = lowLimit; j < n1; j++) {
                for (int k = 0; k < n1; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        if (datasetLimit == 0) {
            return c;
        }

        for (int i = 0; i < upLimit; i++) {
            c[i][i] += a[i][i] * b[i][i];
        }
        for (int j = lowLimit; j < n1; j++) {
            c[j][j] += a[j][j] * b[j][j];
        }
        return c;
    }

    private void normalizeColumns(double[][] a) {
        int m1 = a.length;
        int n1 = a[0].length;
        for (int j = 0; j < n1; j++) {
            double sumCol = 0.0;
            for (int i = 0; i < m1; i++) {
                sumCol += a[i][j];
            }

            for (int i = 0; i < m1; i++) {
                a[i][j] /= sumCol;
            }
        }
    }

    public void setClusterThreshold(double clusterThreshold) {
        this.clusterThreshold = clusterThreshold;
    }
    
    public void setMatrixSimThreshold(double matrixSimThreshold) {
        this.matrixSimThreshold = matrixSimThreshold;
    }

    public void setSimilarityChecksLimit(int similarityChecksLimit) {
        this.similarityChecksLimit = similarityChecksLimit;
    }
}
