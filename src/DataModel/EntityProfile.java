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

package DataModel;

import static gr.demokritos.iit.jinsect.utils.splitToWords;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class EntityProfile implements Serializable {

    private static final long serialVersionUID = 122354534453243447L;

    private final Set<Attribute> attributes;
    private final String entityUrl;

    public EntityProfile(String url) {
        entityUrl = url;
        attributes = new HashSet();
    }

    public void addAttribute(String propertyName, String propertyValue) {
        attributes.add(new Attribute(propertyName, propertyValue));
    }

    public String getEntityUrl() {
        return entityUrl;
    }

    public int getProfileSize() {
        return attributes.size();
    }
    
    public Set<Attribute> getAttributes() {
        return attributes;
    }
    
    public Set<String> getAllValues() {
        Set<String> values = new HashSet<>();
        attributes.stream().forEach((attribute) -> {
            values.add(attribute.getValue());
        });
        return values;
    }    
    
    /**
     * Get the set of all values in a string
     * @return 
     */
    public String getValuesAsString() {
        StringBuilder valuesString = new StringBuilder();        
        attributes.stream().forEach((attribute) -> {valuesString.append(attribute.getValue()).append(" ");});
        return valuesString.toString();
    }
    
    public String getTokensSetAsString() {
        StringBuilder tokensString = new StringBuilder();
        getAllTokens().stream().forEach((value) -> {tokensString.append(value).append(" ");});
        return tokensString.toString();
    }
    
    public Set<String> getAllTokens() {
        Set<String> tokens = new HashSet<>();
        attributes.stream().forEach(attribute -> tokens.addAll(Arrays.asList(splitToWords(attribute.getValue()))));
        return tokens;
    }
    
    
    @Override
    public String toString() {
        String entityString = (entityUrl != null) ? entityUrl : "";
        for (Attribute attribute : attributes) {
            entityString += "\n"+attribute.getName() +":"+ attribute.getValue();
        }
        return entityString;
    }
}