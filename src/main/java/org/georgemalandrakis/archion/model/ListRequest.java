package org.georgemalandrakis.archion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.ArrayList;


public class ListRequest {

    //@JsonProperty("uuids")
    private ArrayList<String> uuids;

    public ListRequest() {
    }

    public ListRequest(String jsonRequest) { //DON'T DELETE!
        this.selfFromJSON(jsonRequest); //TODO: Check it
    }

    public ArrayList<String> getUuids() {
        return uuids;
    }

    public void setUuids(ArrayList<String> uuids) {
        this.uuids = uuids;
    }

    private void selfFromJSON(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        ListRequest request = ListRequest.fromJSON(json);

        //this.setUserObject(request.getUserObject());
        this.setUuids(request.getUuids());
    }


    public static ListRequest fromJSON(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        try {
            var aa = mapper.reader()
                    .forType(ListRequest.class)
                    .readValue(json);
            return mapper.readValue(json, ListRequest.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            System.out.println(e.getMessage());
            String ee = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
        }
        return null;

    }

    @JsonIgnore
    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
        }

        return jsonString;
    }

}
