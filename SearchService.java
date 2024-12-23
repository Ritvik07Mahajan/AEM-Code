package com.aem.geeks.core.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.json.JSONObject;

public interface SearchService {
    public JSONObject searchResult(String searchText, int startResult, int resultPerPage, SlingHttpServletRequest req);
}
