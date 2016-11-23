package EntityClustering;

import DataModel.Comparison;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataModel.VertexWeight;
import Utilities.Comparators.VertexWeightComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
*
* @author Manos Thanos
*/
public class RicochetSRClustering implements IEntityClustering {

    private static final Logger LOGGER = Logger.getLogger(RicochetSRClustering.class.getName());

    private int noOfEntities;
    private int datasetLimit;
    private final SimpleGraph similarityGraph;

    public RicochetSRClustering() {
        similarityGraph = new SimpleGraph(DefaultEdge.class);
        LOGGER.log(Level.INFO, "Initializing Ricochet Sequential Rippling clustering...");
    }

    @Override
    public List<EquivalenceCluster> getDuplicates(SimilarityPairs simPairs) {
    	initializeDataProperties(simPairs);
        VertexWeightComparator VWcomparator = new VertexWeightComparator();
        PriorityQueue<VertexWeight> VWqueue = new PriorityQueue<VertexWeight>(noOfEntities, VWcomparator);
        double[] edgesWeight = new double[noOfEntities];
        int[] edgesAttached = new int[noOfEntities];
        List<HashMap<Integer, Double>> connections = new ArrayList<HashMap<Integer, Double>>();
        for (int i=0; i<noOfEntities; i++)
        {
        	HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
        	connections.add(i, hm);
        }
        double threshold = getSimilarityThreshold(simPairs);
        Iterator<Comparison> iterator = simPairs.getPairIterator();
        while (iterator.hasNext()) {	// add an edge for every pair of entities with a weight higher than the threshold
            Comparison comparison = iterator.next();
            if (threshold < comparison.getUtilityMeasure()) {
            	edgesWeight[comparison.getEntityId1()]= comparison.getUtilityMeasure() + edgesWeight[comparison.getEntityId1()];
                edgesWeight[comparison.getEntityId2()+ datasetLimit]= comparison.getUtilityMeasure() + 
                		edgesWeight[comparison.getEntityId2()+ datasetLimit];
                edgesAttached[comparison.getEntityId1()]++;
                edgesAttached[comparison.getEntityId2()+ datasetLimit]++;             
                connections.get(comparison.getEntityId1()).put(comparison.getEntityId2()+ datasetLimit, comparison.getUtilityMeasure());
                connections.get(comparison.getEntityId2()+ datasetLimit).put(comparison.getEntityId1(), comparison.getUtilityMeasure());

            }
        }
        for (int i=0; i<noOfEntities; i++)
        {
        	if (edgesAttached[i]>0)
        	{
        		VertexWeight vw = new VertexWeight (i, edgesWeight[i], edgesAttached[i], connections.get(i));
                VWqueue.add(vw);
        	}
        	
        }

        Set<Integer> Center =  new HashSet<Integer>();
        Set<Integer> NonCenter = new HashSet<Integer>();
        HashMap<Integer, Set<Integer>> Clusters = new HashMap<Integer, Set<Integer>>();
        int[] clusterCenter = new int[noOfEntities];
        double[] simWithCenter = new double[noOfEntities];
        //Deal with the heaviest vertex first
        VertexWeight vw = VWqueue.remove();
        int v1 = vw.getPos();
        Center.add(v1);


        clusterCenter[v1]=v1;
        Clusters.put(v1, new HashSet<Integer>(Arrays.asList(v1)));//initialize v1 Cluster with its own value
        
        simWithCenter[v1]=1.0;
        HashMap<Integer, Double> connect = vw.Connections();
        for (int v2 : connect.keySet())
        {
        	NonCenter.add(v2);
        	clusterCenter[v2]=v1;
            simWithCenter[v2]=connect.get(v2);//similarity between v1 and v2
            Clusters.get(v1).add(v2);
        }
        

        while (VWqueue.size() > 0)
        {
            vw = VWqueue.remove();
            v1 = vw.getPos();
            connect = vw.Connections();
            Set<Integer> toReassign =  new HashSet<Integer>();
            Set<Integer> centersToReassign =  new HashSet<Integer>();
            for (int v2 : connect.keySet())
            {
            	if (Center.contains(v2))
                {
                	continue;
                }
                double sim = connect.get(v2);
                double previousSim = simWithCenter[v2];
                if ((sim<=previousSim))
                {
                	continue;
                }
                //Since we reach this point v2 has to be put in v1's cluster
                toReassign.add(v2);
            }
            if (!toReassign.isEmpty())
            {
            	if (NonCenter.contains(v1)) //if v1 was in another cluster already then deal with that cluster
                {
            		NonCenter.remove(v1);
                    int prevClusterCenter = clusterCenter[v1];
                    Clusters.get(prevClusterCenter).remove(v1);
                    if (Clusters.get(prevClusterCenter).size()<2) //if v2's previous cluster becomes a singleton 
                    {											//delete this cluster and put 
                    	centersToReassign.add(prevClusterCenter);                   	
                    }
                }
            	toReassign.add(v1);

            	Clusters.put(v1, toReassign);
            	Center.add(v1);

            }
            
            
            Set<Integer> trCopy = new HashSet<Integer>(toReassign);
            for (int v2 : trCopy)
            {

            	if (v2==v1) continue;
                if (NonCenter.contains(v2)) //if v2 was in another cluster already then deal with that cluster
                {
                	int prevClusterCenter = clusterCenter[v2];
                    Clusters.get(prevClusterCenter).remove(v2);

                    if (Clusters.get(prevClusterCenter).size()<2) //if v2's previous cluster becomes a singleton 
                    {	
                    	centersToReassign.add(prevClusterCenter);
                    }
                }
                NonCenter.add(v2);
                clusterCenter[v2]=v1;
                simWithCenter[v2]=connect.get(v2);
            }
            for (int ctr : centersToReassign)
            {
                	Center.remove(ctr);
                	Clusters.remove(ctr);
                	
                	double max=0.0;
                	int newCenter = v1;//in case there is no close similarity
                	for (int center : Center)
                	{
                		if (connections.get(center).containsKey(ctr))
                		{
                			double newSim = connections.get(center).get(ctr);
                			if (newSim>max) 
                				{
                					max=newSim;
                					newCenter = center;
                				}
                		}
                	}
                	Clusters.get(newCenter).add(ctr);
                    Center.remove(ctr);
                    Clusters.remove(ctr);
                    NonCenter.add(ctr);
                    clusterCenter[ctr]=newCenter;
                    simWithCenter[ctr]=max;           	
                }
            
        }
        
        for (int i = 0; i < noOfEntities; i++) {
	        	if ((!NonCenter.contains(i))&&(!Center.contains(i)))
	            {
                	Center.add(i);
	                clusterCenter[i]=i;
	                Clusters.put(i, new HashSet<Integer>(Arrays.asList(i)));//initialize v1 Cluster with its own value
	                simWithCenter[i]=1.0;
	            }        
        	}
        // get connected components

        // prepare output
        List<EquivalenceCluster> equivalenceClusters = new ArrayList<>();
        for (Set<Integer> componentIds : Clusters.values()) {
                    
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
        double threshold = 0.7;

        LOGGER.log(Level.INFO, "Similarity threshold : {0}", threshold);
        return threshold;
    }
    
    
    private void initializeDataProperties(SimilarityPairs simPairs) {
        int maxEntity1 = getMaxEntityId(simPairs.getEntityIds1());
        int maxEntity2 = getMaxEntityId(simPairs.getEntityIds2());
        if (simPairs.isCleanCleanER()) {
            datasetLimit = maxEntity1 + 1;
            noOfEntities = maxEntity1 + maxEntity2 + 2;
        } else {
            datasetLimit = 0;
            noOfEntities = Math.max(maxEntity1, maxEntity2) + 1;
        }


        LOGGER.log(Level.INFO, "Added {0} entities in the dataset", noOfEntities);
    }
}
