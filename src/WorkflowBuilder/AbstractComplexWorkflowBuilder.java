/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockBuilding.IBlockBuilding;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import EntityClustering.IEntityClustering;
import EntityMatching.IEntityMatching;
import Utilities.BlocksPerformance;
import Utilities.Enumerations.BlockBuildingMethod;
import java.util.List;

/**
 *
 * @author VASILIS
 */
public abstract class AbstractComplexWorkflowBuilder extends AbstractWorkflowBuilder {
    //parameters to set
    BlockBuildingMethod blockingMethod1, blockingMethod2;
    IBlockProcessing metaBlockingMethod1, metaBlockingMethod2;
    
    List<AbstractBlock> blocks1, blocks2;

    public AbstractComplexWorkflowBuilder(String dataset1Path, String dataset2Path, String groundTruthPath, 
            BlockBuildingMethod blockingMethod1, BlockBuildingMethod blockingMethod2, 
            IBlockProcessing metaBlockingMethod1, IBlockProcessing metaBlockingMethod2, 
            IEntityMatching similarityMethod, IEntityClustering clusteringMethod) {
        super(dataset1Path,dataset2Path, groundTruthPath, blockingMethod1, metaBlockingMethod1, similarityMethod, clusteringMethod);
        this.blockingMethod1 = blockingMethod1;
        this.blockingMethod2 = blockingMethod2;
        this.metaBlockingMethod1 = metaBlockingMethod1;
        this.metaBlockingMethod2 = metaBlockingMethod2;
    }
    
    /**
     * Runs both content and neighbor blocking.
     */
    @Override
    protected void runBlocking() {   
        super.runBlocking();
        blocks1 = blocks;        
                
        System.out.println("\n\nStarting blocking2...");
        IBlockBuilding blockBuildingMethod2 = BlockBuildingMethod.getDefaultConfiguration(blockingMethod2);
        blocks2 = blockBuildingMethod2.getBlocks(profiles1, profiles2);
        System.out.println("Original blocks2\t:\t" + blocks2.size());        

        //block filtering
        IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(blockingMethod2);
        if (blockCleaningMethod != null) {
            blocks2 = blockCleaningMethod.refineBlocks(blocks2);
        }
        System.out.println("Filtered blocks2\t:\t"+blocks2.size());
        
    }
    
    public void setBlockingMethods(BlockBuildingMethod blockingMethod1, BlockBuildingMethod blockingMethod2) {
        this.blockingMethod1 = blockingMethod1;
        this.blockingMethod2 = blockingMethod2;
    }

    public void setMetaBlockingMethods(IBlockProcessing metaBlockingMethod1, IBlockProcessing metaBlockingMethod2) {
        this.metaBlockingMethod1 = metaBlockingMethod1;
        this.metaBlockingMethod2 = metaBlockingMethod2;
    }
    
    @Override
    protected void runMetaBlocking() {
        super.runMetaBlocking();
        blocks1 = blocks;
        
        if (blocks2 == null) {
            throw new IllegalStateException("Cannot run meta-blocking on a null block collection. Run blocking2 first!");
        }
        blocks2 = metaBlockingMethod2.refineBlocks(blocks2);
    }
    
    @Override
    public void getBlockPerformance() {
        super.getBlockPerformance();
        
        BlocksPerformance blp = new BlocksPerformance(blocks2, groundTruth);
        blp.setStatistics();
        blp.printStatistics();
    }
    
    public BlockBuildingMethod getBlockingMethod1() {
        return blockingMethod1;
    }
    
    public BlockBuildingMethod getBlockingMethod2() {
        return blockingMethod2;
    }

    public IBlockProcessing getMetaBlockingMethod1() {
        return metaBlockingMethod1;
    }
    
    public IBlockProcessing getMetaBlockingMethod2() {
        return metaBlockingMethod2;
    }

    public List<AbstractBlock> getBlocks1() {
        return blocks1;
    }
    
    public List<AbstractBlock> getBlocks2() {
        return blocks2;
    }
        
    @Override
    public String toString() {
        String result = "";
        result += "Dataset:"+dataset1Path+"\n";
        result += "Blocking method (for content):"+blockingMethod1+"\n";
        result += "Blocking method (for neighbors):"+blockingMethod2+"\n";
        result += "Meta-blocking method (for content):"+ (metaBlockingMethod1 != null ? metaBlockingMethod1.getMethodInfo() : "NONE") +"\n";
        result += "Meta-blocking method (for neighbors):"+ (metaBlockingMethod2 != null ? metaBlockingMethod2.getMethodInfo() : "NONE") +"\n";
        result += "Similarity method:"+similarityMethod.getMethodInfo()+"\n";
        result += "Similarity threshold:"+similarity_threshold+"\n";
        result += "Clustering method:"+ (clusteringMethod != null ? clusteringMethod.getMethodInfo() : "NONE")+"\n";
        return result;
    }
}
