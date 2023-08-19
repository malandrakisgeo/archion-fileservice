package org.georgemalandrakis.archion.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.georgemalandrakis.archion.core.ArchionResponse;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;


@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
public class UploadResource extends AbstractResource {
    FileService fileService;

    public UploadResource(FileService fileService) {
        this.fileService = fileService;
    }

    @GET
    public Response test(@Auth ArchionUser userOpt) {
        return buildResponse(new ArchionResponse(), Response.Status.OK, "Hurray!");
    }

    @RolesAllowed("ADMIN")
    @POST
    @Timed
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@Auth ArchionUser userOpt,
                           @Valid @FormDataParam("purpose") String purpose,
                           @Valid @FormDataParam("filename") String filename,
                           @Valid @FormDataParam("file") File file
                           ) {
        ArchionResponse response = new ArchionResponse();

        if (file == null) {
            return buildResponse(response, Response.Status.BAD_REQUEST, "You need to send a file.");
        }

        try {
            FileMetadata fileMetadata = this.fileService.createNewFile(userOpt.getId(), purpose, filename, file);
            response.setLatestValidFileMetadata(fileMetadata);
        } catch (Exception e) {
            return commonExceptionResponse(e);
        }
        return buildResponse(response, Response.Status.OK, "Hurray!");

    }

}
