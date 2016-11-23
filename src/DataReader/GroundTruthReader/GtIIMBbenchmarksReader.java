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

package DataReader.GroundTruthReader;

import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import com.opencsv.CSVReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jgrapht.alg.ConnectivityInspector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author G.A.P. II
 */

public class GtIIMBbenchmarksReader extends AbstractGtReader {

    private static final Logger LOGGER = Logger.getLogger(GtIIMBbenchmarksReader.class.getName());

    private String baseGTfile;

    public GtIIMBbenchmarksReader(String filePath) {
        super(filePath);
        baseGTfile = "/home/ethanos/Downloads/JEDAIconfirmedBenchmarks/iimb2009/001/rdfalign.rdf";
    }

    @Override
    public String getMethodInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected void getBilateralConnectedComponents(List<Set<Integer>> connectedComponents) {
        for (Set<Integer> cluster : connectedComponents) {
            if (cluster.size() != 2) {
                //LOGGER.log(Level.WARNING, "Connected component that does not involve just a couple of entities!\t{0}", cluster.toString());
                continue;
            }

            // add a new pair of IdDuplicates for every pair of entities in the cluster
            Iterator<Integer> idIterator = cluster.iterator();
            int id1 = idIterator.next();
            int id2 = idIterator.next();
            if (id1 < id2) {
                id2 = id2 - datasetLimit; // normalize id to [0, profilesD2.size()]
                if (id2 < 0) {
                    LOGGER.log(Level.WARNING, "Entity id not corresponding to dataset 2!\t{0}", id2);
                    continue;
                }
                idDuplicates.add(new IdDuplicates(id1, id2));
            } else {
                id1 = id1 - datasetLimit; // normalize id to [0, profilesD2.size()]
                if (id1 < 0) {
                    LOGGER.log(Level.WARNING, "Entity id not corresponding to dataset 2!\t{0}", id1);
                    continue;
                }
                idDuplicates.add(new IdDuplicates(id2, id1));
            }
        }
    }

    @Override
    public Set<IdDuplicates> getDuplicatePairs(List<EntityProfile> profilesD1,
            List<EntityProfile> profilesD2) {
        if (!idDuplicates.isEmpty()) {
            return idDuplicates;
        }

        if (profilesD1 == null) {
            LOGGER.log(Level.SEVERE, "First list of entity profiles is null! "
                    + "The first argument should always contain entities.");
            return null;
        }

        initializeDataStructures(profilesD1, profilesD2);
        try {
//        	we parse both gt files and keep as duplicates the entity2-instances
//        	being at the same corresponding order, since they are linked to the 
//        	same entity1-instance in both files
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        	Document doc1 = dBuilder.parse(baseGTfile);
        	doc1.getDocumentElement().normalize();
        	Document doc2 = dBuilder.parse(inputFilePath);
            NodeList nList1 = doc1.getElementsByTagName("Cell");
            NodeList nList2 = doc2.getElementsByTagName("Cell");
            for (int temp = 0; temp < nList1.getLength(); temp++) {
            	Node nNode1 = nList1.item(temp);
            	Node nNode2 = nList2.item(temp);
            	int entityId1;
            	int entityId2;
                if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
                	Element eElement = (Element) nNode1;
                	Element eElement1 = (Element) eElement.getElementsByTagName("entity2").item(0);
	                entityId1 = urlToEntityId1.get(eElement1.getAttribute("rdf:resource"));
	                if (nNode1.getNodeType() == Node.ELEMENT_NODE) {
	                	eElement = (Element) nNode2;
	                	Element eElement2 = (Element) eElement.getElementsByTagName("entity2").item(0);
		                entityId2 = urlToEntityId2.get(eElement2.getAttribute("rdf:resource"));
		                duplicatesGraph.addEdge(entityId1, entityId2); 
	                  }
	                  
                }
            }
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.log(Level.INFO, "Total edges in duplicates graph\t:\t{0}", duplicatesGraph.edgeSet().size());

        // get connected components
        ConnectivityInspector ci = new ConnectivityInspector(duplicatesGraph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        LOGGER.log(Level.INFO, "Total connected components in duplicate graph\t:\t{0}", connectedComponents.size());

        // transform connected components into pairs of duplicates
        if (profilesD2 != null) { // Clean-Clean ER
            getBilateralConnectedComponents(connectedComponents);
        } else { // Dirty ER
            getUnilateralConnectedComponents(connectedComponents);
        }
        LOGGER.log(Level.INFO, "Total pair of duplicats\t:\t{0}", idDuplicates.size());

        return idDuplicates;
    }

    protected void getUnilateralConnectedComponents(List<Set<Integer>> connectedComponents) {
        for (Set<Integer> cluster : connectedComponents) {
            if (cluster.size() < 2) {
                LOGGER.log(Level.WARNING, "Connected component with a single element!{0}", cluster.toString());
                continue;
            }

            // add a new pair of IdDuplicates for every pair of entities in the cluster
            int clusterSize = cluster.size();
            Integer[] clusterEntities = cluster.toArray(new Integer[clusterSize]);
            for (int i = 0; i < clusterSize; i++) {
                for (int j = i + 1; j < clusterSize; j++) {
                    idDuplicates.add(new IdDuplicates(clusterEntities[i], clusterEntities[j]));
                }
            }
        }
    }

    protected void initializeDataStructures(List<EntityProfile> profilesD1,
            List<EntityProfile> profilesD2) {
        // count total entities
        noOfEntities = profilesD1.size();
        datasetLimit = 0;
        if (profilesD2 != null) {
            noOfEntities += profilesD2.size();
            datasetLimit = profilesD1.size(); //specifies where the first dataset ends and the second one starts
        }

        // build inverted index from URL to entity id
        int counter = 0;
        for (EntityProfile profile : profilesD1) {
            urlToEntityId1.put(profile.getEntityUrl(), counter++);
        }
        if (profilesD2 != null) {
            for (EntityProfile profile : profilesD2) {
                urlToEntityId2.put(profile.getEntityUrl(), counter++);
            }
        }

        // add a node for every input entity 
        for (int i = 0; i < noOfEntities; i++) {
            duplicatesGraph.addVertex(i);
        }
        LOGGER.log(Level.INFO, "Total nodes in duplicate graph\t:\t{0}", duplicatesGraph.vertexSet().size());
    }

    public void setBaseGTfile(String baseGTfile) {
        this.baseGTfile = baseGTfile;
    }

}
