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
package EntityMatching;

import DataModel.AbstractBlock;
import DataModel.Attribute;
import DataModel.Comparison;
import DataModel.EntityProfile;
import DataModel.SimilarityEdge;
import DataModel.SimilarityPairs;
import Utilities.Comparators.SimilarityEdgeComparator;
import Utilities.Enumerations.RepresentationModel;
import Utilities.TextModels.AbstractModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 *
 * @author G.A.P. II
 */
public class GroupLinkageWithGraph extends AbstractEntityMatching {

    private static final Logger LOGGER = Logger.getLogger(GroupLinkageWithGraph.class.getName());

    private boolean isCleanCleanER;

    protected List<AbstractModel[]> entityModelsD1;
    protected List<AbstractModel[]> entityModelsD2;
    protected RepresentationModel representationModel;
    protected double similarityThreshold;

    public GroupLinkageWithGraph(RepresentationModel model) {
        representationModel = model;
        similarityThreshold = 0.1;
        LOGGER.log(Level.INFO, "Initializing profile matcher with : {0}", model);
    }

    @Override
    public SimilarityPairs executeComparisons(List<AbstractBlock> blocks,
            List<EntityProfile> profilesD1, List<EntityProfile> profilesD2) {
        if (profilesD1 == null) {
            LOGGER.log(Level.SEVERE, "First list of entity profiles is null! "
                    + "The first argument should always contain entities.");
            System.exit(-1);
        }

        isCleanCleanER = false;
        entityModelsD1 = getModels(profilesD1);
        if (profilesD2 != null) {
            isCleanCleanER = true;
            entityModelsD2 = getModels(profilesD2);
        }

        SimilarityPairs simPairs = new SimilarityPairs(isCleanCleanER, blocks);
        for (AbstractBlock block : blocks) {
            final Iterator<Comparison> iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison currentComparison = iterator.next();
                PriorityQueue<SimilarityEdge> similarityQueue = getSimilarityEdges(currentComparison);
                WeightedGraph<String, DefaultWeightedEdge> similarityGraph = getSimilarityGraph(similarityQueue);
                int verticesNum = entityModelsD1.get(currentComparison.getEntityId1()).length;
                if (isCleanCleanER) {
                    verticesNum += entityModelsD2.get(currentComparison.getEntityId2()).length;
                } else {
                    verticesNum += entityModelsD1.get(currentComparison.getEntityId2()).length;
                }
                currentComparison.setUtilityMeasure(getSimilarity(similarityGraph, verticesNum));
                simPairs.addComparison(currentComparison);
            }
        }
        return simPairs;
    }

    //Every element of the getModels list is an AbstractModel[] array, corresponding to 
    //a profile. Every element of these arrays is a text-model corresponding to an attribute.
    private List<AbstractModel[]> getModels(List<EntityProfile> profiles) {
        List<AbstractModel[]> ModelsList = new ArrayList<AbstractModel[]>();
        for (EntityProfile profile : profiles) {
            int counter = 0;
            AbstractModel[] model = new AbstractModel[profile.getProfileSize()];
            for (Attribute attribute : profile.getAttributes()) {
                if (!attribute.getValue().isEmpty()) {
                    model[counter] = RepresentationModel.getModel(representationModel, attribute.getName());
                    model[counter].updateModel(attribute.getValue());
                    counter++;
                }
            }
            ModelsList.add(model);

        }
        return ModelsList;
    }

    @Override
    public String getMethodInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public PriorityQueue<SimilarityEdge> getSimilarityEdges(Comparison comparison) {
        AbstractModel[] model1 = entityModelsD1.get(comparison.getEntityId1());
        AbstractModel[] model2;
        if (isCleanCleanER) {
            model2 = entityModelsD2.get(comparison.getEntityId2());
        } else {
            model2 = entityModelsD1.get(comparison.getEntityId2());
        }

        int s1 = model1.length;
        int s2 = model2.length;
        SimilarityEdgeComparator SEcomparator = new SimilarityEdgeComparator();
        PriorityQueue<SimilarityEdge> SEqueue
                = new PriorityQueue<SimilarityEdge>(s1 * s2, SEcomparator);

        for (int i = 0; i < s1; i++) {
            for (int j = 0; j < s2; j++) {
                double sim = model1[i].getSimilarity(model2[j]);
                if (sim > similarityThreshold) {
                    SimilarityEdge se = new SimilarityEdge(i, j, sim);
                    SEqueue.add(se);
                }
            }
        }
        return SEqueue;

    }

    public WeightedGraph<String, DefaultWeightedEdge> getSimilarityGraph(PriorityQueue<SimilarityEdge> seQueue) {
        WeightedGraph<String, DefaultWeightedEdge> graph
                = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        while (seQueue.size() > 0) {
            SimilarityEdge se = seQueue.remove();
            int i = se.getModel1Pos();
            int j = se.getModel2Pos();
            if (!(graph.containsVertex("a" + i) || graph.containsVertex("b" + j)))//only if both vertices don't exist
            {
                double sim = se.getSimilarity();
                graph.addVertex("a" + i);
                graph.addVertex("b" + j);
                DefaultWeightedEdge e = graph.addEdge("a" + i, "b" + j);
                graph.setEdgeWeight(e, sim);

            }
        }

        return graph;
    }

    public double getSimilarity(WeightedGraph<String, DefaultWeightedEdge> simGraph, int verticesNum) {
        double similarity = 0;
        double nominator = 0;
        double denominator = (double) verticesNum; //m1+m2
        for (DefaultWeightedEdge e : simGraph.edgeSet()) {
            nominator += simGraph.getEdgeWeight(e);
            denominator -= 1.0;
        }
        similarity = nominator / denominator;

        return similarity;
    }

    public void setSimilarityThreshold(double p) {
        this.similarityThreshold = p;
    }
}
