package org.georgemalandrakis.archion.resource;

import org.georgemalandrakis.archion.core.*;
import org.georgemalandrakis.archion.exception.AuthException;
import org.georgemalandrakis.archion.exception.FileException;
import org.georgemalandrakis.archion.exception.FileExistsException;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractResource {

    public Response buildResponse(ArchionResponse archionResponse, Response.Status status) {
        return buildResponse(archionResponse, status, false, null, null, null);
    }

    public Response buildResponse(ArchionResponse archionResponse, Response.Status status, Object entity) {
        return buildResponse(archionResponse, status, false, entity, null, null);
    }

    public Response buildResponse(ArchionResponse archionResponse, Response.Status status, Boolean data, Object entity, HashMap<String, Object> header, String mediaType) {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        responseBuilder.type(mediaType);


        //Data
        if (entity != null && !data) {
            archionResponse.setData(entity);
        }

        if (data) {
            responseBuilder = responseBuilder.entity(entity);
        } else {
            responseBuilder = responseBuilder.entity(archionResponse);
        }

        //Check header
        if (header != null) {
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                responseBuilder.header(key, value);
            }
        }

        return responseBuilder.build();
    }


    Response commonExceptionResponse(Exception e) {
        ArchionResponse response = new ArchionResponse();
        response.setErrorMessage(e.getMessage());
        if (e instanceof FileException) {
            response.setLatestValidFileMetadata(((FileException) e).getLatestMetadata()); //TODO: Kolla om vi vill ha det så.
        }

        if (e instanceof FileExistsException) {
            response.setLatestValidFileMetadata(((FileException) e).getLatestMetadata());
            return buildResponse(response, Response.Status.CONFLICT);
        }

        if (e instanceof AuthException) {
            return buildResponse(response, Response.Status.UNAUTHORIZED);
        }

        return buildResponse(response, Response.Status.INTERNAL_SERVER_ERROR);
    }

    protected HashMap<String, Object> createProperHeaders(String fileExtension, String fileLength) {
        HashMap<String, Object> header = new HashMap<>();

        String contentType;
        switch (fileExtension.toLowerCase()) {
			/*case "pdf":
				contentType = "application/pdf";
				break;

			case "jpeg":
			case "jpg":
				contentType = "image/jpeg";
				break;*/

            default:
                contentType = "application/octet-stream";
        }

        header.put("Content-Length", fileLength);
        header.put("Content-Type", contentType);

        return header;
    }
}
