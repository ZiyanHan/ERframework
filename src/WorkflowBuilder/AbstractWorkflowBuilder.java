/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockBuilding.IBlockBuilding;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import EntityClustering.IEntityClustering;
import EntityMatching.IEntityMatching;
import EntityMatching.ProfileWithNeighborMatcher;
import Utilities.BlocksPerformance;
import Utilities.ClustersPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.BlockBuildingMethod;
import java.util.List;

/**
 *
 * @author VASILIS
 */
public abstract class AbstractWorkflowBuilder {
    //parameters to set
    String dataset1Path, dataset2Path, groundTruthPath;
    BlockBuildingMethod blockingMethod;
    IEntityClustering clusteringMethod;
    IEntityMatching similarityMethod;
    double similarity_threshold;
    IBlockProcessing metaBlockingMethod;
    
    //variables needed and created afterwards
    List<EntityProfile> profiles1;
    List<EntityProfile> profiles2;
    AbstractDuplicatePropagation groundTruth;
    
    List<AbstractBlock> blocks;
    
    double precision, recall, fmeasure;

    public AbstractWorkflowBuilder(String dataset1Path, String dataset2Path, String groundTruthPath, BlockBuildingMethod blockingMethod, IBlockProcessing metaBlockingMethod, IEntityMatching similarityMethod, IEntityClustering clusteringMethod) {
        this.dataset1Path = dataset1Path;
        this.dataset2Path = dataset2Path;
        this.groundTruthPath = groundTruthPath;
        this.blockingMethod = blockingMethod;
        this.clusteringMethod = clusteringMethod;
        this.similarityMethod = similarityMethod;
        this.metaBlockingMethod = metaBlockingMethod;
    }
    
    public void loadData() {
        IEntityReader eReader1 = new EntitySerializationReader(dataset1Path);
        profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());            
        
        IEntityReader eReader2 = new EntitySerializationReader(dataset2Path);
        profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());            

        IGroundTruthReader gtReader = new GtSerializationReader(groundTruthPath);
        groundTruth = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(profiles1, profiles2));
        System.out.println("Existing Duplicates\t:\t" + groundTruth.getDuplicates().size());
    }
    
    protected void runBlocking() {   
        if (profiles1 == null || profiles2 == null || groundTruth == null) {
            throw new IllegalStateException("Cannot run blocking before successfully loading data and ground truth.");
        }
        IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(blockingMethod);
        blocks = blockBuildingMethod.getBlocks(profiles1, profiles2);
        System.out.println("Original blocks\t:\t" + blocks.size());

        //block filtering
        IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(blockingMethod);
        if (blockCleaningMethod != null) {
            blocks = blockCleaningMethod.refineBlocks(blocks);
        }
    }

    public void setBlockingMethod(BlockBuildingMethod blockingMethod) {
        this.blockingMethod = blockingMethod;
    }

    public void setClusteringMethod(IEntityClustering clusteringMethod) {
        this.clusteringMethod = clusteringMethod;
    }

    public void setSimilarityMethod(IEntityMatching similarityMethod) {
        this.similarityMethod = similarityMethod;
    }

    public void setSimilarity_threshold(double similarity_threshold) {
        this.similarity_threshold = similarity_threshold;
    }

    public void setMetaBlockingMethod(IBlockProcessing metaBlockingMethod) {
        this.metaBlockingMethod = metaBlockingMethod;
    }
    
    protected void runMetaBlocking() {
        if (blocks == null) {
            throw new IllegalStateException("Cannot run meta-blocking on a null block collection. Run blocking first!");
        }
        blocks = metaBlockingMethod.refineBlocks(blocks);
    }
    
    public void getBlockPerformance() {
        BlocksPerformance blp = new BlocksPerformance(blocks, groundTruth);
        blp.setStatistics();
        blp.printStatistics();
    }
    
    //TODO: return precision, recall, fmeasure for ProfileWithNeighborMatcher (meta-blocking)
    protected SimilarityPairs runSimilarityComputations() {
        if (blocks == null) {
            throw new IllegalStateException("Cannot compute similarity on a null block collection. Run blocking first!");
        }
        if (similarityMethod instanceof ProfileWithNeighborMatcher) {
            ((ProfileWithNeighborMatcher) similarityMethod).setGroundTruth(groundTruth);
        }
        return similarityMethod.executeComparisons(blocks, profiles1, profiles2);      
    }
    
    protected void runClustering(SimilarityPairs simPairs) {
        if (simPairs == null || clusteringMethod == null) {
            throw new IllegalStateException("Cannot run clustering at this state, since either simPairs or clustering method are null.");
        }
        List<EquivalenceCluster> entityClusters = clusteringMethod.getDuplicates(simPairs);

        ClustersPerformance clp = new ClustersPerformance(entityClusters, groundTruth);
        clp.setStatistics();
        clp.printStatistics();
        
        precision = clp.getPrecision();
        recall = clp.getRecall();
        fmeasure = clp.getFMeasure();
    }
    
    
    
    public abstract void runWorkflow();
    
    public double getPrecision() {
        return precision;
    }
    
    public double getRecall() {
        return recall;
    }
    
    public double getFMeasure() {
        return fmeasure;
    }
    
    public String getDataset1Path() {
        return dataset1Path;
    }

    public String getDataset2Path() {
        return dataset2Path;
    }

    public String getGroundTruthPath() {
        return groundTruthPath;
    }

    public BlockBuildingMethod getBlockingMethod() {
        return blockingMethod;
    }

    public IEntityClustering getClusteringMethod() {
        return clusteringMethod;
    }

    public IEntityMatching getEntityMatchingMethod() {
        return similarityMethod;
    }

    public IBlockProcessing getMetaBlockingMethod() {
        return metaBlockingMethod;
    }

    public List<EntityProfile> getProfiles1() {
        return profiles1;
    }

    public List<EntityProfile> getProfiles2() {
        return profiles2;
    }

    public AbstractDuplicatePropagation getGroundTruth() {
        return groundTruth;
    }

    public List<AbstractBlock> getBlocks() {
        return blocks;
    }
    
    public double getSimilarityThreshold() {
        return similarity_threshold;
    }
    
    
    @Override
    public String toString() {
        String result = "";
        result += "Dataset:"+dataset1Path+"\n";
        result += "Blocking method:"+blockingMethod+"\n";
        result += "Meta-blocking method:"+ (metaBlockingMethod != null ? metaBlockingMethod.getMethodInfo() : "NONE") +"\n";
        result += "Similarity method:"+similarityMethod.getMethodInfo()+"\n";
        result += "Similarity threshold:"+similarity_threshold+"\n";
        result += "Clustering method:"+ (clusteringMethod != null ? clusteringMethod.getMethodInfo() : "NONE")+"\n";
        return result;
    }
}
