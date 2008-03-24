/*
Copyright 2008 Flaptor (flaptor.com) 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

    http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/
package com.flaptor.hounder.classifier.bayes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.flaptor.hounder.classifier.util.DocumentParser;

/**
 * @author Flaptor Development Team
 */
public class MultiClassifier {

    public static final String OVERALL_SCORE = "OVERALL_SCORE";
    private final int numCategories;
    private final String[] categories;
    private final BayesClassifier[] classifiers;

    public MultiClassifier(String[] categories) {
        this(categories, -1);    
    }

    public MultiClassifier(String[] categories, double unknownTermsProbability) {
        numCategories = categories.length;
        classifiers = new BayesClassifier[numCategories];
        this.categories = new String[numCategories];
        for (int i=0; i<numCategories; i++) {
            this.categories[i] = categories[i];
            if (unknownTermsProbability != -1) {
                classifiers[i] = new BayesClassifier(".",this.categories[i], unknownTermsProbability);
            } else {    
                classifiers[i] = new BayesClassifier(this.categories[i]);
            } 
        }
    }

    /**
     * Calculates the score that the bayesian filter assigns to the matching of the document to each category.
     * The results correspond to the categories passed as argument when creating the MultiClassifier, they are returned in the same order. 
     * @param doc the document represented as a string
     * @return an array of doubles containing the score of the document for each category
     */
    public double[] getScores(String doc) {
        // Each category might have been parsed with a different maxTuple number
        // To avoid parsing the document again, for each such number we will check
        // the maxTuple values and store the parsed docs in a hash.
        // Using old parsed when available,
        Map<Integer, Map<String,int[]>> map= new HashMap<Integer, Map<String,int[]>>();
        double[] results = new double[numCategories];
        for (int i=0; i<numCategories; i++) {
            Integer imt=classifiers[i].getMaxTuple();
            if (!map.containsKey(imt)){
                map.put(imt, DocumentParser.parse(doc, classifiers[i].getMaxTuple()));
            }
            Map<String,int[]> tokens = map.get(imt);
            results[i] = classifiers[i].classify(tokens);
        }
        return results;
    }

    /**
     * Calculates the score that the bayesian filter assigns to the matching of the document to each category (see getScores()).
     * The returned map also contains an entry ("OVERALL_SCORE") mapped to the global score of the page (as returned by calculateGlobalScore()).
     * @param doc the document represented as a string
     * @return a map that binds each category name to its score
     */
    public Map<String,Double> getNamedScores(String doc) {
        double[] results = getScores(doc);
        Map<String,Double> resultMap = new HashMap<String,Double>(results.length);
        for (int i=0; i<numCategories; i++) {
            resultMap.put(categories[i],Double.valueOf(results[i]));
        }
        resultMap.put(OVERALL_SCORE, Double.valueOf(calculateGlobalScore(results)));
        return resultMap;
    }

    /**
     * Calculates the global score of the page, based on the results generated by the filter.
     * This implementation returns the max score.
     * FIXME: Implememt it!!!!!
     */
    public static double calculateGlobalScore(double[] results) {
        double max = Double.MIN_VALUE;
        for (double val : results) {
            max = Math.max(max, val);
        }
        return max;
    }

    public static void main(String[] args) throws IOException {
        if ((args.length != 2) && (args.length != 3)) {
            System.err.println("Usage: MultiClassifier <category list> < filename > [unknownTermsProb]");
            return;
        }
        File f = new File(".", args[1]);
        String document = readFromFile(f);
        String categoryList = args[0];
        String[] categories = categoryList.split(",");
        MultiClassifier multi = null;
        if (args.length == 3) {
            multi = new MultiClassifier(categories, Double.parseDouble(args[2]));
        } else {
            multi = new MultiClassifier(categories);
        }
        Map<String, Double> results = multi.getNamedScores(document);
        System.out.println("Results: " +results);
    }

    private static String readFromFile(File f) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(f));
            byte[] data = new byte[(int)f.length()];
            int numRead = is.read(data);
            try {
                return new String(data,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("close: " +e);
                }
            }
        }

    }

}

