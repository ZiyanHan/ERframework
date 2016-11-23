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
public class ExtendedQGramsBlocking extends QGramsBlocking {

    private final static int MAX_Q_GRAMS = 15;
    private static final Logger LOGGER = Logger.getLogger(ExtendedQGramsBlocking.class.getName());
    
    private final double threshold;

    public ExtendedQGramsBlocking() {
        this(0.95, 6);
        LOGGER.log(Level.INFO, "Using default configuration for Extended Q-Grams Blocking.");
    }
    
    public ExtendedQGramsBlocking(double t, int n) {
        super(n);
        threshold = t;
        LOGGER.log(Level.INFO, "Threshold\t:\t{0}", threshold);
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> keys = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            List<String> nGrams = getNGrams(nGramSize, token);
            if (nGrams.size() == 1) {
                keys.add(nGrams.get(0));
            } else {
                if (MAX_Q_GRAMS < nGrams.size()) {
                    nGrams = nGrams.subList(0, MAX_Q_GRAMS);
                }

                int minimumLength = (int) Math.max(1, Math.floor(nGrams.size() * threshold));
                for (int i = minimumLength; i <= nGrams.size(); i++) {
                    keys.addAll(getCombinationsFor(nGrams, i));
                }
            }
        }
        return keys;
    }
    
    protected Set<String> getCombinationsFor(List<String> sublists, int sublistLength) {
        if (sublistLength == 0 || sublists.size() < sublistLength) {
            return new HashSet<>();
        }

        List<String> remainingElements = new ArrayList<>(sublists);
        String lastSublist = remainingElements.remove(sublists.size() - 1);

        final Set<String> combinationsExclusiveX = getCombinationsFor(remainingElements, sublistLength);
        final Set<String> combinationsInclusiveX = getCombinationsFor(remainingElements, sublistLength - 1);

        final Set<String> resultingCombinations = new HashSet<>();
        resultingCombinations.addAll(combinationsExclusiveX);
        if (combinationsInclusiveX.isEmpty()) {
            resultingCombinations.add(lastSublist);
        } else {
            combinationsInclusiveX.stream().forEach((combination) -> {
                resultingCombinations.add(combination + lastSublist);
            });
        }
        return resultingCombinations;
    }
    
    @Override
    public String getMethodInfo() {
        return "Extended Q-Grams Blocking: it creates one block for every combination of q-grams that represents at least two entities.\n"
                + "The q-grams aer extracted from any token in the attribute values of any entity.";
    }

    @Override
    public String getMethodParameters() {
        return "Extended Q-Grams Blocking involves two parameters:\n"
                + "1) n, which defines the size of q-grams, i.e., the characters that comprise them.\n"
                + "Default value: 6.\n"
                + "2) t \\in [0,1], the threshold that defines the number N of q-grams that are combined to form an individual blocking key.\n"
                + "In more detail, the minimum number l_{min} of q-grams per blocking key is defined as l_{min} = max (1, \\floor{k \\cdot t}),\n"
                + "where k is the number of q-grams from the original blocking key (token)."
                + "Default value: 0.95.";
    }
}
