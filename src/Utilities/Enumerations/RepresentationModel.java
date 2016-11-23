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

package Utilities.Enumerations;

import Utilities.TextModels.AbstractModel;
import Utilities.TextModels.CharacterNGramGraphs;
import Utilities.TextModels.CharacterNGrams;
import Utilities.TextModels.TokenNGramGraphs;
import Utilities.TextModels.TokenNGrams;

/**
 *
 * @author G.A.P. II
 */

public enum RepresentationModel {
    CHARACTER_BIGRAMS,
    CHARACTER_BIGRAM_GRAPHS,
    CHARACTER_TRIGRAMS,
    CHARACTER_TRIGRAM_GRAPHS,
    CHARACTER_FOURGRAMS,
    CHARACTER_FOURGRAM_GRAPHS,
    TOKEN_UNIGRAMS, 
    TOKEN_UNIGRAM_GRAPHS, 
    TOKEN_BIGRAMS,
    TOKEN_BIGRAM_GRAPHS, 
    TOKEN_TRIGRAMS,
    TOKEN_TRIGRAM_GRAPHS;
    
    public static AbstractModel getModel (RepresentationModel model, String instanceName) {
        switch (model) {
            case CHARACTER_BIGRAMS:
                return new CharacterNGrams(2, model, instanceName);
            case CHARACTER_BIGRAM_GRAPHS:
                return new CharacterNGramGraphs(2, model, instanceName);
            case CHARACTER_FOURGRAMS:
                return new CharacterNGrams(4, model, instanceName);
            case CHARACTER_FOURGRAM_GRAPHS:
                return new CharacterNGramGraphs(4, model, instanceName);
            case CHARACTER_TRIGRAMS:
                return new CharacterNGrams(3, model, instanceName);
            case CHARACTER_TRIGRAM_GRAPHS:
                return new CharacterNGramGraphs(3, model, instanceName);
            case TOKEN_BIGRAMS:
                return new TokenNGrams(2, model, instanceName);
            case TOKEN_BIGRAM_GRAPHS:
                return new TokenNGramGraphs(2, model, instanceName);
            case TOKEN_TRIGRAMS:
                return new TokenNGrams(3, model, instanceName);
            case TOKEN_TRIGRAM_GRAPHS:
                return new TokenNGramGraphs(3, model, instanceName);
            case TOKEN_UNIGRAMS:
                return new TokenNGrams(1, model, instanceName);
            case TOKEN_UNIGRAM_GRAPHS:
                return new TokenNGramGraphs(1, model, instanceName);
            default:
                return null;    
        }
    }
}