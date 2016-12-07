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
import Utilities.Enumerations.SimilarityMetric;
import Utilities.TextModels.AbstractModel;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 *
 * @author G.A.P. II
 */
public class GroupLinkage extends AbstractEntityMatching {

    private static final Logger LOGGER = Logger.getLogger(GroupLinkage.class.getName());

    protected double similarityThreshold;
    protected AbstractModel[][] entityModelsD1;
    protected AbstractModel[][] entityModelsD2;

    public GroupLinkage(RepresentationModel model, SimilarityMetric simMetric) {
        super(model, simMetric);
        similarityThreshold = 0.1;

        LOGGER.log(Level.INFO, "Initializing Group Linkage with : {0}", model);
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

        final SimilarityPairs simPairs = new SimilarityPairs(isCleanCleanER, blocks);
        for (AbstractBlock block : blocks) {
            final Iterator<Comparison> iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison currentComparison = iterator.next();
                final Queue<SimilarityEdge> similarityQueue = getSimilarityEdges(currentComparison);
                WeightedGraph<String, DefaultWeightedEdge> similarityGraph = getSimilarityGraph(similarityQueue);
                int verticesNum = entityModelsD1[currentComparison.getEntityId1()].length;
                if (isCleanCleanER) {
                    verticesNum += entityModelsD2[currentComparison.getEntityId2()].length;
                } else {
                    verticesNum += entityModelsD1[currentComparison.getEntityId2()].length;
                }
                currentComparison.setUtilityMeasure(getSimilarity(similarityGraph, verticesNum));
                simPairs.addComparison(currentComparison);
            }
        }

        return simPairs;
    }

    @Override
    public String getMethodInfo() {
        return "Group Linkage : it implements the group linkage algorithm for schema-agnostic comparison of the attribute values of two entity profiles.";
    }

    @Override
    public String getMethodParameters() {
        return "The Group Linkage involves 3 parameters:\n"
             + "1) representation model : character- or token-based bag or graph model.\n"
             + "It determines the building modules that form the model of individual attribute values.\n"
             + "2) similarity metric : bag or graph similarity metric.\n"
             + "It determines the measure that estimates the similarity of two attribute values.\n"
             + "3) similarity threshold : double, default value : 0.1\n"
             + "It determines the similarity value over which two compared attribute values are connected with an edge on the bipartite graph.";
    }

    //Every element of the getModels list is an AbstractModel[] array, corresponding to 
    //a profile. Every element of these arrays is a text-model corresponding to an attribute.
    private AbstractModel[][] getModels(List<EntityProfile> profiles) {
        int entityCounter = 0;
        final AbstractModel[][] ModelsList = new AbstractModel[profiles.size()][];
        for (EntityProfile profile : profiles) {
            int counter = 0;
            ModelsList[entityCounter] = new AbstractModel[profile.getProfileSize()];
            for (Attribute attribute : profile.getAttributes()) {
                if (!attribute.getValue().isEmpty()) {
                    ModelsList[entityCounter][counter] = RepresentationModel.getModel(representationModel, simMetric, attribute.getName());
                    ModelsList[entityCounter][counter].updateModel(attribute.getValue());
                    counter++;
                }
            }
            entityCounter++;
        }

        return ModelsList;
    }

    private double getSimilarity(WeightedGraph<String, DefaultWeightedEdge> simGraph, int verticesNum) {
        double nominator = 0;
        double denominator = (double) verticesNum; //m1+m2
        for (DefaultWeightedEdge e : simGraph.edgeSet()) {
            nominator += simGraph.getEdgeWeight(e);
            denominator -= 1.0;
        }
        return nominator / denominator;
    }

    private Queue<SimilarityEdge> getSimilarityEdges(Comparison comparison) {
        AbstractModel[] model1 = entityModelsD1[comparison.getEntityId1()];
        AbstractModel[] model2;
        if (isCleanCleanER) {
            model2 = entityModelsD2[comparison.getEntityId2()];
        } else {
            model2 = entityModelsD1[comparison.getEntityId2()];
        }

        int s1 = model1.length;
        int s2 = model2.length;
        final Queue<SimilarityEdge> SEqueue = new PriorityQueue<>(s1 * s2, new SimilarityEdgeComparator());
        for (int i = 0; i < s1; i++) {
            for (int j = 0; j < s2; j++) {
                double sim = model1[i].getSimilarity(model2[j]);
                if (similarityThreshold < sim) {
                    SEqueue.add(new SimilarityEdge(i, j, sim));
                }
            }
        }

        return SEqueue;
    }

    private WeightedGraph<String, DefaultWeightedEdge> getSimilarityGraph(Queue<SimilarityEdge> seQueue) {
        WeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        while (seQueue.size() > 0) {
            SimilarityEdge se = seQueue.remove();
            int i = se.getModel1Pos();
            int j = se.getModel2Pos();
            String label1 = "a" + i;
            String label2 = "b" + j;
            if (!(graph.containsVertex(label1) || graph.containsVertex(label2))) {//only if both vertices don't exist
                graph.addVertex(label1);
                graph.addVertex(label2);
                DefaultWeightedEdge e = graph.addEdge(label1, label2);
                graph.setEdgeWeight(e, se.getSimilarity());
            }
        }

        return graph;
    }

    public void setSimilarityThreshold(double p) {
        this.similarityThreshold = p;
    }
}
