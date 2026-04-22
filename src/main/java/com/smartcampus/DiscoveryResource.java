package com.smartcampus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Discovery Endpoint Resource.
 * Mapped to the root of the ApplicationPath (/api/v1/)
 */
@Path("/")
public class DiscoveryResource {

    /**
     * Method handling HTTP GET requests for the root discovery endpoint.
     *
     * @return JSON representing the discovery metadata and HATEOAS links.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DiscoveryResponse getDiscoveryInfo() {
        DiscoveryResponse response = new DiscoveryResponse();
        response.setVersion("1.0");
        response.setContact("admin@smartcampus.uni.edu");
        
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1/");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        
        response.setLinks(links);
        return response;
    }

    /**
     * POJO for consistent JSON representation of the Discovery Response.
     */
    public static class DiscoveryResponse {
        private String version;
        private String contact;
        private Map<String, String> links;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public Map<String, String> getLinks() {
            return links;
        }

        public void setLinks(Map<String, String> links) {
            this.links = links;
        }
    }
}
