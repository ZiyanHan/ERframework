/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.SimilarityPairs;
import EntityClustering.CenterClustering;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import EntityMatching.AbstractEntityMatching;
import EntityMatching.IEntityMatching;
import EntityMatching.ProfileMatcher;
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
public class TestComplexWorfklow {
    
    public static void main (String[] args) {
    
        //set data
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String dataset1 = basePath+"restaurant1Profiles";
        String dataset2 = basePath+"restaurant2Profiles";
        String datasetGroundtruth = basePath+"restaurantIdDuplicates";
        
        if (args.length == 3) {
            dataset1 = args[0];
            dataset2 = args[1];
            datasetGroundtruth = args[2];
        }
        
        String[] acceptableTypes = {
//                                    "http://www.okkam.org/ontology_person1.owl#Person",
//                                    "http://www.okkam.org/ontology_person2.owl#Person", 
            
                                    "http://www.okkam.org/ontology_restaurant1.owl#Restaurant",
                                    "http://www.okkam.org/ontology_restaurant2.owl#Restaurant",
            
//                                      "http://www.bbc.co.uk/ontologies/creativework/NewsItem",
//                                      "http://www.bbc.co.uk/ontologies/creativework/BlogPost",
//                                      "http://www.bbc.co.uk/ontologies/creativework/Programme",
//                                      "http://www.bbc.co.uk/ontologies/creativework/CreativeWork"
                                    };

        //set parameters
        BlockBuildingMethod blockingWorkflow1 = BlockBuildingMethod.STANDARD_BLOCKING;
        BlockBuildingMethod blockingWorkflow2 = BlockBuildingMethod.NEIGHBOR_BLOCKING;
        IBlockProcessing metaBlocking1 = new CardinalityNodePruning(WeightingScheme.CBS);
        IBlockProcessing metaBlocking2 = new CardinalityNodePruning(WeightingScheme.CBS);
        AbstractEntityMatching similarity = new ProfileMatcher(RepresentationModel.CHARACTER_BIGRAM_GRAPHS);
        IEntityClustering clustering =  new UniqueMappingClustering();
                
        AbstractComplexWorkflowBuilder complex = new ComplexWorkflow(
                dataset1, dataset2, datasetGroundtruth, 
                blockingWorkflow1, blockingWorkflow2,
                metaBlocking1, metaBlocking2,
                similarity, 
                clustering);
        
        complex.loadData();
        complex.runBlocking();
        complex.runMetaBlocking();
        /*
        RepresentationModel[] repModels = {
                            RepresentationModel.TOKEN_UNIGRAMS,
                            RepresentationModel.CHARACTER_BIGRAMS,
                            RepresentationModel.CHARACTER_TRIGRAMS,
                            RepresentationModel.TOKEN_UNIGRAM_GRAPHS,
                            RepresentationModel.CHARACTER_BIGRAM_GRAPHS, 
                            RepresentationModel.CHARACTER_TRIGRAM_GRAPHS};
        
        double bestPrecision=0, bestRecall=0, bestFmeasure=0, bestThreshold=0;
        RepresentationModel bestRepresentation= null;
        SimilarityMetric bestSimilarityMetric = null;
        
        for (RepresentationModel repModel : repModels) {                       
            
            for (SimilarityMetric simMetric : SimilarityMetric.getModelCompatibleSimMetrics(repModel)) {
                similarity = new ProfileMatcher(repModel, simMetric);
                if (acceptableTypes.length > 0) {
                    similarity.setAcceptableTypes(new HashSet<>(Arrays.asList(acceptableTypes)));
                }
                
                complex.setSimilarityMethod(similarity);
                
                SimilarityPairs simPairs = complex.runSimilarityComputations();
                
                for (double sim_threshold = 0.2; sim_threshold < 0.9; sim_threshold += 0.2) {  
                    complex.setClusteringMethod(new UniqueMappingClustering());
                    complex.setSimilarity_threshold(sim_threshold);                    

                    //System.out.println(full);
                    complex.runClustering(simPairs);

                    double fMeasure = complex.getFMeasure();
                    if (fMeasure > bestFmeasure) {
                        bestPrecision = complex.getPrecision();
                        bestRecall = complex.getRecall();
                        bestThreshold = sim_threshold;
                        bestFmeasure = fMeasure;
                        bestRepresentation = repModel;
                        bestSimilarityMetric = simMetric;                        
                    }
                }
                
            }
            
            
        }
        
        System.out.println("\n\nBest results were for the settings:");
        System.out.println("Representation model: "+bestRepresentation);
        System.out.println("Similarity metric: "+bestSimilarityMetric);
        System.out.println("similarity threshold: "+bestThreshold);
        System.out.println("Precision: "+bestPrecision);
        System.out.println("Recall: "+bestRecall);
        System.out.println("F-measure: "+bestFmeasure);
        */        
        
//        full.runWorkflow();
    }
    
}
