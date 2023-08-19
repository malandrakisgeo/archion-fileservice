package org.georgemalandrakis.archion.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.georgemalandrakis.archion.core.ArchionRequest;
import org.georgemalandrakis.archion.core.ArchionResponse;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.MULTIPART_FORM_DATA})
public class DeleteResource extends AbstractResource {
    FileService fileService;

    public DeleteResource(FileService fileService) {
        this.fileService = fileService;
    }

    @DELETE
    @Timed
    @Path("/{fileId}")
    public Response deletebyId(@Auth ArchionUser user, @PathParam("fileId") String fileId) {
        ArchionResponse response = new ArchionResponse();

        try {
            fileService.remove(fileId, user);
            return buildResponse(response, Response.Status.OK);
        } catch (Exception e) {
            return commonExceptionResponse(e);
        }

    }

    @POST
    @Timed
    @Path("/")
    public Response deletebyIds(@FormDataParam("req") ArchionRequest archionRequest) {

       //TODO: Implement

        return buildResponse(archionRequest.getResponseObject(), Response.Status.NO_CONTENT);

    }

}
