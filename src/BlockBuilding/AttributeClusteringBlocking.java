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

package BlockBuilding;

import DataModel.Attribute;
import DataModel.EntityProfile;
import Utilities.TextModels.AbstractModel;
import Utilities.Enumerations.RepresentationModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author gap2
 */
public class AttributeClusteringBlocking extends StandardBlocking {

    private final static double MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD = 1E-11;
    private final static String CLUSTER_PREFIX = "#$!cl";
    private final static String CLUSTER_SUFFIX = "!$#";
    private static final Logger LOGGER = Logger.getLogger(AttributeClusteringBlocking.class.getName());
    
    private final Map<String, Integer>[] attributeClusters;
    private final RepresentationModel model;

    public AttributeClusteringBlocking() {
        this(RepresentationModel.TOKEN_UNIGRAM_GRAPHS);
        LOGGER.log(Level.INFO, "Using default configuration for Attribute Clustering Blocking.");
    }
    
    public AttributeClusteringBlocking(RepresentationModel md) {
        super();
        model = md;
        LOGGER.log(Level.INFO, "Representation model\t:\t{0}", model);
        attributeClusters = new HashMap[2];
    }

    @Override
    protected void buildBlocks() {
        AbstractModel[] attributeModels1 = buildAttributeModels(entityProfilesD1);
        if (entityProfilesD2 != null) {
            AbstractModel[] attributeModels2 = buildAttributeModels(entityProfilesD2);
            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
        
        setMemoryDirectory();

        IndexWriter iWriter1 = openWriter(indexDirectoryD1);
        indexEntities(0, iWriter1, entityProfilesD1);
        closeWriter(iWriter1);

        if (indexDirectoryD2 != null) {
            IndexWriter iWriter2 = openWriter(indexDirectoryD2);
            indexEntities(1, iWriter2, entityProfilesD2);
            closeWriter(iWriter2);
        }
    }
    
    private AbstractModel[] buildAttributeModels(List<EntityProfile> profiles) {    
        final HashMap<String, List<String>> attributeProfiles = new HashMap<>();
        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            }
        }

        int index = 0;
        AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];
        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = RepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                attributeModels[index].updateModel(value);
            }
            index++;
        }
        return attributeModels;
    }

    private void clusterAttributes(AbstractModel[] attributeModels, SimpleGraph graph) {
        int noOfAttributes = attributeModels.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * noOfAttributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }
            
            for (int attributeId : cluster) {
                attributeClusters[0].put(attributeModels[attributeId].getInstanceName(), clusterId);
            }
        }
        attributeClusters[1] = null;
    }

    private void clusterAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, SimpleGraph graph) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;
        
        attributeClusters[0] = new HashMap<>(2 * d1Attributes);
        attributeClusters[1] = new HashMap<>(2 * d2Attributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }
            
            for (int attributeId : cluster) {
                if (attributeId < d1Attributes) {
                    attributeClusters[0].put(attributeModels1[attributeId].getInstanceName(), clusterId);
                } else {
                    attributeClusters[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), clusterId);
                }
            }
        }
    }

    private SimpleGraph compareAttributes(AbstractModel[] attributeModels) {
        int noOfAttributes = attributeModels.length;
        int[] mostSimilarName = new int[noOfAttributes];
        double[] maxSimillarity = new double[noOfAttributes];
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < noOfAttributes; i++) {
            for (int j = i + 1; j < noOfAttributes; j++) {
                double simValue = attributeModels[i].getSimilarity(attributeModels[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j;
                }

                if (maxSimillarity[j] < simValue) {
                    maxSimillarity[j] = simValue;
                    mostSimilarName[j] = i;
                }
            }
        }

        for (int i = 0; i < noOfAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    private SimpleGraph compareAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes + d2Attributes;
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);

        int[] mostSimilarName = new int[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
                double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j + d1Attributes;
                }

                if (maxSimillarity[j + d1Attributes] < simValue) {
                    maxSimillarity[j + d1Attributes] = simValue;
                    mostSimilarName[j + d1Attributes] = i;
                }
            }
        }

        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }
    
    protected void indexEntities(int sourceId, IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                for (Attribute attribute : profile.getAttributes()) {
                    Integer clusterId = attributeClusters[sourceId].get(attribute.getName());
                    if (clusterId == null) {
                        LOGGER.log(Level.WARNING, "No cluster id found for attribute name\t:\t{0}"
                                + ".\nCorresponding attribute value\t:\t{1}", new Object[]{attribute.getName(), attribute.getValue()});
                        continue;
                    }
                    String clusterSuffix = CLUSTER_PREFIX + clusterId + CLUSTER_SUFFIX;
                    for (String token : getTokens(attribute.getValue())) {
                        if (0 < token.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, token.trim() + clusterSuffix, Field.Store.YES));
                        }
                    }
                }

                index.addDocument(doc);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String getMethodInfo() {
        return "Attribute Clustering Blocking: it groups the attribute names into similarity clusters "
                + "and applies Standard Blocking to the values of every cluster, independently of the others.";
    }

    @Override
    public String getMethodParameters() {
        return "Attribute Clustering Blocking involves a single parameter:\n"
                + "model, the representation model that aggregates the values corresponding to every attribute name.\n"
                + "It also determines the similarity measure for comparing the representations of two attribute names.";
    }
}
