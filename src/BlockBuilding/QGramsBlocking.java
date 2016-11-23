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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */

public class QGramsBlocking extends StandardBlocking {
    
    private static final Logger LOGGER = Logger.getLogger(QGramsBlocking.class.getName());

    protected final int nGramSize;

    public QGramsBlocking() {
        this(6);
        LOGGER.log(Level.INFO, "Using default configuration for Q-Grams Blocking.");
    }
    
    public QGramsBlocking(int n) {
        super();
        nGramSize = n;
        LOGGER.log(Level.INFO, "N-gram size\t:\t{0}", nGramSize);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> nGrams = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            nGrams.addAll(getNGrams(nGramSize, token));
        }

        return nGrams;
    }
    
    @Override
    public String getMethodInfo() {
        return "Q-Grams Blocking: it creates one block for every q-gram that is extracted from any token in the attribute values of any entity.\n"
                + "The q-gram must be shared by at least two entities.";
    }

    @Override
    public String getMethodParameters() {
        return "Due to its unsupervised, schema-agnostic blocking keys, Q-Grams Blocking involves a single parameter:\n"
                + "n, the number of characters comprising every q-gram.\n"
                + "Default value: 6.";
    }
    
    protected List<String> getNGrams(int n, String blockingKey) {
        final List<String> nGrams = new ArrayList<>();
        if (blockingKey.length() < n) {
            nGrams.add(blockingKey);
        } else {
            int currentPosition = 0;
            final int length = blockingKey.length() - (n - 1);
            while (currentPosition < length) {
                nGrams.add(blockingKey.substring(currentPosition, currentPosition + n));
                currentPosition++;
            }
        }
        return nGrams;
    }
}