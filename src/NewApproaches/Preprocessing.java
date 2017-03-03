package NewApproaches;

import BlockBuilding.IBlockBuilding;
import BlockBuilding.NeighborBlocking;
import BlockBuilding.StandardBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class Preprocessing {
    
    private final String inputPath1;
    private final String inputPath2;
    private int datasetLimit;
    
    private List<EntityProfile> profiles1;
    private List<EntityProfile> profiles2;
    
    private final Set<String> acceptableTypes;
    private final Set<Integer> acceptableIds1;
    private final Set<Integer> acceptableIds2;
    
    public Preprocessing(String inPath1, String inPath2, Set<String> acceptableTypes) {
        inputPath1 = inPath1;
        inputPath2 = inPath2;
        this.acceptableTypes = acceptableTypes;
        acceptableIds1 = new HashSet<>();
        acceptableIds2 = new HashSet<>();
    }
    
    public Preprocessing(String inPath1, String inPath2) {
        inputPath1 = inPath1;
        inputPath2 = inPath2;
        acceptableTypes = new HashSet<>();
        acceptableIds1 = new HashSet<>();
        acceptableIds2 = new HashSet<>();
    }
    
    public List<AbstractBlock> getTokenBlockingBlocks() {        
        return getBlocks(new StandardBlocking());
    }
    
    public List<AbstractBlock> getNeighborBlockingBlocks() {        
        return getBlocks(new NeighborBlocking());
    }    
    
    public List<AbstractBlock> getBlocks(IBlockBuilding blockBuildingMethod) {
        IEntityReader eReader1 = new EntitySerializationReader(inputPath1);
        /*List<EntityProfile>*/ profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());
        datasetLimit = profiles1.size();
        for (int i = 0; i < profiles1.size(); i++) {
            EntityProfile profile1 = profiles1.get(i);
            if (profile1.hasOneOfTheTypes(acceptableTypes)) {
                acceptableIds1.add(i);
            }
        }

        IEntityReader eReader2 = new EntitySerializationReader(inputPath2);
        /*List<EntityProfile>*/ profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());
        for (int i = 0; i < profiles2.size(); i++) {
            EntityProfile profile2 = profiles2.get(i);
            if (profile2.hasOneOfTheTypes(acceptableTypes)) {
                acceptableIds2.add(i);
            }
        }
                 
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
    
    public List<AbstractBlock> getPurgedBlocks(IBlockBuilding blockBuildingMethod) {
        IEntityReader eReader1 = new EntitySerializationReader(inputPath1);
        /*List<EntityProfile>*/ profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());
        datasetLimit = profiles1.size();
        for (int i = 0; i < profiles1.size(); i++) {
            EntityProfile profile1 = profiles1.get(i);
            if (profile1.hasOneOfTheTypes(acceptableTypes)) {
                acceptableIds1.add(i);
            }
        }

        IEntityReader eReader2 = new EntitySerializationReader(inputPath2);
        /*List<EntityProfile>*/ profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());
        for (int i = 0; i < profiles2.size(); i++) {
            EntityProfile profile2 = profiles2.get(i);
            if (profile2.hasOneOfTheTypes(acceptableTypes)) {
                acceptableIds2.add(i);
            }
        }
                 
        List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles1, profiles2);
        System.out.println("Original blocks\t:\t" + blocks.size());

        IBlockProcessing blockPurging = new ComparisonsBasedBlockPurging();
        blocks = blockPurging.refineBlocks(blocks);
        System.out.println("Purging blocks\t:\t" + blocks.size());
        
        return blocks;
    }

    int getDatasetLimit() {
        return datasetLimit;
    }
    
    public List<EntityProfile> getProfiles1(){
        return profiles1;
    }
    
    public List<EntityProfile> getProfiles2(){
        return profiles2;
    }
    
    public Set<Integer> getAcceptableIds1() {
        return acceptableIds1;
    }
    
    public Set<Integer> getAcceptableIds2() {
        return acceptableIds2;
    }
}