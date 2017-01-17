package NewApproaches;

import BlockBuilding.IBlockBuilding;
import BlockBuilding.StandardBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class Preprocessing {
    
    private final String inputPath1;
    private final String inputPath2;
    
    public Preprocessing(String inPath1, String inPath2) {
        inputPath1 = inPath1;
        inputPath2 = inPath2;
    }
    
    public List<AbstractBlock> getBlocks() {
        IEntityReader eReader1 = new EntitySerializationReader(inputPath1);
        List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());

        IEntityReader eReader2 = new EntitySerializationReader(inputPath2);
        List<EntityProfile> profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());
        
        IBlockBuilding blockBuildingMethod = new StandardBlocking();
        List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles1, profiles2);
        System.out.println("Original blocks\t:\t" + blocks.size());

        IBlockProcessing blockPurging = new ComparisonsBasedBlockPurging();
        blocks = blockPurging.refineBlocks(blocks);
        System.out.println("Purging blocks\t:\t" + blocks.size());
        
        IBlockProcessing blockCleaningMethod = new BlockFiltering(0.8);
        blocks = blockCleaningMethod.refineBlocks(blocks);
        System.out.println("Filtered blocks\t:\t" + blocks.size());
        
        return blocks;
    }
}