package com.chli.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Image Upload Servlet",
                SLING_SERVLET_PATHS + "=/bin/chli/demoServlet"
        })
public class ChangeDescription extends SlingAllMethodsServlet {

    final Logger logger = LoggerFactory.getLogger(ChangeDescription.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        ResourceResolver resourceResolver = request.getResourceResolver();

        // Get the resource at /content/chli/in/en
        Resource rootResource = resourceResolver.getResource("/content/chli/in/en");

        if (rootResource != null) {
            // Recursively traverse through all nodes and print their paths
            try {
                traverseNodes(rootResource, response,request);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        } else {
            response.getWriter().write("Resource not found at /content/chli");
        }
    }

    private void traverseNodes(Resource resource, SlingHttpServletResponse response, SlingHttpServletRequest request) throws IOException, RepositoryException {

            ResourceResolver resourceResolver = request.getResourceResolver();
           Session session=resourceResolver.adaptTo(Session.class);
        String modifiedOutput = logPageProperties(resource, response);
        // Get the JCR node
                Node node = resource.adaptTo(Node.class);
                if (node != null) {
                    // Set the new value for description
                    node.setProperty("description", modifiedOutput);

                    // Save the session
                    session = resourceResolver.adaptTo(Session.class);
                    session.save(); // Persist changes
                }
        logger.error(modifiedOutput);
        // Recursively iterate over all child nodes
        Iterator<Resource> childResources = resource.listChildren();
        while (childResources.hasNext()) {
            Resource childResource = childResources.next();
            traverseNodes(childResource, response,request); // Recursive call for child nodes
        }
    }

    private String logPageProperties(Resource resource, SlingHttpServletResponse resp) throws IOException {
        String description=null;
        if (resource != null) {
            ValueMap properties = resource.getValueMap();
            description = properties.get("description", String.class);
        }
        String modifiedOutput=null;
        if (description != null) {
            resp.getWriter().println(resource.getPath()+" "+description);

            // Define a regular expression to match the href attribute with the specific URL and optionally ending with .HTML
            modifiedOutput = modifyAnchorTag(description);
        }
        return  modifiedOutput;
    }


    public static String modifyAnchorTag(String input) {
        // Define a regular expression to match the href attribute with the specific URL and optionally ending with .html or .HTML
        String regex = "<a\\s+[^>]*href\\s*=\\s*\"([^\"]*)\"";

        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        // Create a StringBuffer to hold the modified string
        StringBuffer result = new StringBuffer();

        // Iterate over all matches
        while (matcher.find()) {
            String originalHref = matcher.group(1);  // Capture the original href value
            String modifiedHref = originalHref;

            // Condition 1: Replace "https://www.canarahsbclife.com" with "/"
            if (modifiedHref.contains("https://www.canarahsbclife.com/")) {
                modifiedHref = modifiedHref.replace("https://www.canarahsbclife.com/", "/");
            }

            // Condition 2: Remove ".html" or ".HTML" from the end if present
            if (modifiedHref.toLowerCase().endsWith(".html")) {
                modifiedHref = modifiedHref.substring(0, modifiedHref.length() - 5);  // Remove ".html"
            }

            // Replace the matched href with the modified href
            matcher.appendReplacement(result, "<a href=\"" + modifiedHref + "\"");
        }

        // Append the rest of the string
        matcher.appendTail(result);

        return result.toString();
    }
}