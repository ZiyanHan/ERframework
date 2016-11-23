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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */
public class ExtendedSuffixArraysBlocking extends SuffixArraysBlocking {

    private static final Logger LOGGER = Logger.getLogger(ExtendedSuffixArraysBlocking.class.getName());
    
    public ExtendedSuffixArraysBlocking() {
        this(39, 6);
        LOGGER.log(Level.INFO, "Using default configuration for Extended Suffix Arrays Blocking.");
    }
    
    public ExtendedSuffixArraysBlocking(int maxSize, int minLength) {
        super(maxSize, minLength);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(getExtendedSuffixes(minimumSuffixLength, token));
        }
        return suffixes;
    }

    @Override
    public String getMethodInfo() {
        return "Extended Suffix Arrays Blocking: it creates one block for every substring (not just suffix) that appears in the tokens of at least two entities.";
    }

    @Override
    public String getMethodParameters() {
        return "Extended Suffix Arrays Blocking involves two parameters:\n"
                + "1) minLength, the minimum size of substrings that are used as blocking keys.\n"
                + "Default value: 6.\n"
                + "2) maxSize, the maximum frequency of every suffix, i.e., the maximum block size.\n"
                + "Default value: 39.";
    }

    public Set<String> getExtendedSuffixes(int minimumLength, String blockingKey) {
        final Set<String> suffixes = new HashSet<>();
        suffixes.add(blockingKey);
        if (minimumLength <= blockingKey.length()) {
            for (int nGramSize = blockingKey.length() - 1; minimumLength <= nGramSize; nGramSize--) {
                int currentPosition = 0;
                final int length = blockingKey.length() - (nGramSize - 1);
                while (currentPosition < length) {
                    String newSuffix = blockingKey.substring(currentPosition, currentPosition + nGramSize);
                    suffixes.add(newSuffix);
                    currentPosition++;
                }
            }
        }
        return suffixes;
    }
}
