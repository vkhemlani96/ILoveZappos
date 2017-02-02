package com.vinaykhemlani.ilovezappos.API;

import java.util.List;

/**
 * Created by Vinay on 2/1/17.
 *
 *  This is used to map the JSON keys from the entire Zappos API Response
 *  to a Java class.
 *
 *  All functions are getters for class variables.
 *  toString function prints class variables
 */

public class APIResponse {

    String originalTerm;
    String currentResultCount;
    String totalResultCount;
    String term;
    List<SearchResult> results;
    String statusCode;

    public String getOriginalTerm() {
        return originalTerm;
    }

    public String getCurrentResultCount() {
        return currentResultCount;
    }

    public String getTotalResultCount() {
        return totalResultCount;
    }

    public String getTerm() {
        return term;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "APIResponse{" +
                "originalTerm='" + originalTerm + '\'' +
                ", currentResultCount='" + currentResultCount + '\'' +
                ", totalResultCount='" + totalResultCount + '\'' +
                ", term='" + term + '\'' +
                ", results='" + results + '\'' +
                ", statusCode='" + statusCode + '\'' +
                '}';
    }
}
