/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.ComparisonRefinement.CardinalityNodePruningReWeighting;
import BlockProcessing.IBlockProcessing;
import DataModel.Comparison;
import DataModel.PairIterator;
import DataModel.SimilarityPairs;
import EntityClustering.CenterClustering;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import EntityMatching.AbstractEntityMatching;
import EntityMatching.IEntityMatching;
import EntityMatching.ProfileMatcher;
import EntityMatching.ProfileWithNeighborMatcher;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import Utilities.Enumerations.WeightingScheme;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author VASILIS
 */
public class TestFullWorfklowComplexSimWithClustering {
    
    public static void main (String[] args) {
    
        //set data
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2016\\UOBM_small\\";
        String dataset1 = basePath+"Abox1Profiles";
        String dataset2 = basePath+"Abox2Profiles";
        String datasetGroundtruth = basePath+"UOBM_smallIdDuplicates";
        String[] acceptableTypes = {
//                                    "http://www.okkam.org/ontology_person1.owl#Person",
//                                    "http://www.okkam.org/ontology_person2.owl#Person", 
            
//                                    "http://www.okkam.org/ontology_restaurant1.owl#Restaurant",
//                                    "http://www.okkam.org/ontology_restaurant2.owl#Restaurant",
            
//                                      "http://www.bbc.co.uk/ontologies/creativework/NewsItem",
//                                      "http://www.bbc.co.uk/ontologies/creativework/BlogPost",
//                                      "http://www.bbc.co.uk/ontologies/creativework/Programme",
//                                      "http://www.bbc.co.uk/ontologies/creativework/CreativeWork"
                                    };

        //set parameters
        BlockBuildingMethod blockingWorkflow = BlockBuildingMethod.STANDARD_BLOCKING;
        IBlockProcessing metaBlocking = new CardinalityNodePruning(WeightingScheme.CBS);
        AbstractEntityMatching similarity = new ProfileWithNeighborMatcher(
                RepresentationModel.CHARACTER_TRIGRAM_GRAPHS, 
                SimilarityMetric.getModelDefaultSimMetric(RepresentationModel.CHARACTER_TRIGRAM_GRAPHS), 
                RepresentationModel.CHARACTER_BIGRAMS, 
                SimilarityMetric.getModelDefaultSimMetric(RepresentationModel.CHARACTER_BIGRAMS), 
                0.8); //weight of values sim over neighbors sim //set to 1 for no-neighbors, 0 for neighbors-only
        if (acceptableTypes.length > 0) {
            similarity.setAcceptableTypes(new HashSet<>(Arrays.asList(acceptableTypes)));
        }
        
        IEntityClustering clustering =  new UniqueMappingClustering();
                
        AbstractWorkflowBuilder full = new FullWithClustering(
                dataset1, dataset2, datasetGroundtruth, 
                blockingWorkflow, 
                metaBlocking, 
                similarity, 
                clustering);
        
        full.loadData();
        full.runBlocking();
//        full.runMetaBlocking(); //meta-blocking may be executed within ProfileWithNeighborMatcher
        SimilarityPairs simPairs = full.runSimilarityComputations();
        full.setSimilarity_threshold(0.4);
        full.runClustering(simPairs);
                
        if (1>0) {
            return;
        }
        
        RepresentationModel[] repModels = {
                            RepresentationModel.TOKEN_UNIGRAMS,
                            RepresentationModel.CHARACTER_BIGRAMS,
                            RepresentationModel.CHARACTER_TRIGRAMS,
                            RepresentationModel.TOKEN_UNIGRAM_GRAPHS,
                            RepresentationModel.CHARACTER_BIGRAM_GRAPHS, 
                            RepresentationModel.CHARACTER_TRIGRAM_GRAPHS};
        
        double bestPrecision=0, bestRecall=0, bestFmeasure=0, bestThreshold=0, bestA=0;
        RepresentationModel bestRepresentation= null, bestNeighborRepresentation=null;
        SimilarityMetric bestSimilarityMetric = null, bestNeighborSimilarityMetric=null;
        
        for (RepresentationModel repModel : repModels) {                       
            
            for (SimilarityMetric simMetric : SimilarityMetric.getModelCompatibleSimMetrics(repModel)) {
                for (RepresentationModel repModel2 : repModels) { 
                    for (SimilarityMetric simMetric2 : SimilarityMetric.getModelCompatibleSimMetrics(repModel2)) {
//                        for (double a = 0.3; a < 0.8; a+= 0.2) {
                        for (double a = 0.66; a < 0.8; a+= 0.2) { //just check a=0.66
                            similarity = new ProfileWithNeighborMatcher(repModel, simMetric, repModel2, simMetric2, a);
                            if (acceptableTypes.length > 0) {
                                similarity.setAcceptableTypes(new HashSet<>(Arrays.asList(acceptableTypes)));
                            }

                            full.setSimilarityMethod(similarity);

                            simPairs = full.runSimilarityComputations();

                            for (double sim_threshold = 0.2; sim_threshold < 0.9; sim_threshold += 0.2) {  
                                full.setClusteringMethod(new UniqueMappingClustering());
                                full.setSimilarity_threshold(sim_threshold);                    

                                //System.out.println(full);
                                full.runClustering(simPairs);

                                double fMeasure = full.getFMeasure();
                                if (fMeasure > bestFmeasure) {
                                    bestPrecision = full.getPrecision();
                                    bestRecall = full.getRecall();
                                    bestThreshold = sim_threshold;
                                    bestFmeasure = fMeasure;
                                    bestRepresentation = repModel;
                                    bestSimilarityMetric = simMetric;                        
                                    bestNeighborRepresentation = repModel2;
                                    bestNeighborSimilarityMetric = simMetric2;
                                    bestA = a;
                                }
                            }

                        }
                    }
                }
                
                
                
            }
            
        }
        
        System.out.println("\n\nBest results were for the settings:");
        System.out.println("Representation model: "+bestRepresentation);
        System.out.println("Similarity metric: "+bestSimilarityMetric);
        System.out.println("Representation model (neighbor): "+bestNeighborRepresentation);
        System.out.println("Similarity metric (neighbor): "+bestNeighborSimilarityMetric);
        System.out.println("similarity threshold: "+bestThreshold);
        System.out.println("relative weight of values over neighbor values: "+bestA);
        System.out.println("Precision: "+bestPrecision);
        System.out.println("Recall: "+bestRecall);
        System.out.println("F-measure: "+bestFmeasure);
                
        
//        full.runWorkflow();
    }
    
}
