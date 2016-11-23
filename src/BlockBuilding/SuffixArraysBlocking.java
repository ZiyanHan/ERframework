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

import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public class SuffixArraysBlocking extends StandardBlocking {

    private static final Logger LOGGER = Logger.getLogger(SuffixArraysBlocking.class.getName());
    
    protected final int maximumBlockSize;
    protected final int minimumSuffixLength;
    
    public SuffixArraysBlocking() {
        this(53, 6);
        LOGGER.log(Level.INFO, "Using default configuration for Suffix Arrays Blocking.");
    }
    
    public SuffixArraysBlocking(int maxSize, int minLength) {
        super();
        maximumBlockSize = maxSize;
        minimumSuffixLength = minLength;
        LOGGER.log(Level.INFO, "Maximum block size\t:\t{0}", maximumBlockSize);
        LOGGER.log(Level.INFO, "Minimum suffix length\t:\t{0}", minimumSuffixLength);
    }

     @Override
    public List<AbstractBlock> getBlocks(List<EntityProfile> profilesD1,
            List<EntityProfile> profilesD2) {
        List<AbstractBlock> purgedBlocks = super.getBlocks(profilesD1, profilesD2);
        Iterator<AbstractBlock> blocksIterator = purgedBlocks.iterator();
        while (blocksIterator.hasNext()) {
            AbstractBlock block = (AbstractBlock) blocksIterator.next();
            if (maximumBlockSize < block.getTotalBlockAssignments()) {
                blocksIterator.remove();
            }
        }
        return purgedBlocks;
    }
    
    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(getSuffixes(minimumSuffixLength, token));
        }
        return suffixes;
    }
    
    @Override
    public String getMethodInfo() {
        return "Suffix Arrays Blocking: it creates one block for every suffix that appears in the tokens of at least two entities.";
    }

    @Override
    public String getMethodParameters() {
        return "Suffix Arrays Blocking involves two parameters:\n"
                + "1) minLength, the minimum size of suffixes that are used as blocking keys.\n"
                + "Default value: 6.\n"
                + "2) maxSize, the maximum frequency of every suffix, i.e., the maximum block size.\n"
                + "Defaule value: 53.";
    }
    
    public Set<String> getSuffixes(int minimumLength, String blockingKey) {
        final Set<String> suffixes = new HashSet<>();
        if (blockingKey.length() < minimumLength) {
            suffixes.add(blockingKey);
        } else {
            int limit = blockingKey.length() - minimumLength + 1;
            for (int i = 0; i < limit; i++) {
                suffixes.add(blockingKey.substring(i));
            }
        }
        return suffixes;
    }
}