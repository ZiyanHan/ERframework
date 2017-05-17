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
    private final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    private final Set<Attribute> attributes;
    private final String entityUrl;
    
    private Set<String> types;

    public EntityProfile(String url) {
        entityUrl = url;
        attributes = new HashSet();
        types = new HashSet<>();
    }

    public void addAttribute(String propertyName, String propertyValue) {
        attributes.add(new Attribute(propertyName, propertyValue));        
        if (propertyName.replaceAll("<", "").replaceAll(">", "").equals(RDF_TYPE)) {
            types.add(propertyValue);
        }
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
    
    public Set<String> getAllAttributeNames() {
        Set<String> attributeNames = new HashSet<>();
        attributes.stream().forEach((attribute) -> attributeNames.add(attribute.getName()));
        return attributeNames;
    }
    
    public Set<String> getAllValues() {
        Set<String> values = new HashSet<>();
        attributes.stream().forEach((attribute) -> {
            values.add(attribute.getValue());
        });
        return values;
    }    
    
    public Set<String> getTypes() {                        
        if (types == null || types.isEmpty()) {            
            types = new HashSet<>();
            for (Attribute attribute : attributes) {                
                if (attribute.getName().replaceAll("<", "").replaceAll(">", "").equals(RDF_TYPE)) {
                    String type = attribute.getValue();                    
                    if (type != null && !type.isEmpty()) {
                        types.add(type);
                    }
                }
            }
        }
        return types;
    }
    
    /**
     * Returns the *first* value found for the given attribute, or null, if the 
     * attribute does not exist for this entity.
     * @param attributeName the attribute whose value is asked
     * @return the *first* value found for the given attribute, or null, if the 
     * attribute does not exist for this entity
     */
    public String getValueOf(String attributeName) {
        for (Attribute attribute : attributes) {
            if (attributeName.equals(attribute.getName())) {
                return attribute.getValue();
            }
        }
        return null;
    }
    
    /**
     * Returns *all* the values found for the given attribute, or null, if the 
     * attribute does not exist for this entity.
     * @param attributeName the attribute whose value is asked
     * @return the *first* value found for the given attribute, or null, if the 
     * attribute does not exist for this entity
     */
    public Set<String> getValuesOf(String attributeName) {
        Set<String> values = new HashSet<>();
        for (Attribute attribute : attributes) {
            if (attributeName.equals(attribute.getName())) {
                values.add(attribute.getValue());
            }
        }
        return values;
    }
        
    public boolean isOfType(String type) {
        if (types ==  null) {
            getTypes();
        }
        return types.contains(type);
    }
    
    public boolean hasOneOfTheTypes(Set<String> acceptableTypes) {
        if (types == null)  {
            getTypes();
        }
        for (String acceptableType : acceptableTypes) {
            if (types.contains(acceptableType)) {                
                return true;
            }
        }
        return false;
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