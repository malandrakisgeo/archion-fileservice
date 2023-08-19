package org.georgemalandrakis.archion.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.georgemalandrakis.archion.core.ArchionResponse;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.service.FileService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Path("/download")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@RolesAllowed("ADMIN")

public class DownloadResource extends AbstractResource {
    final FileService fileService;

    public DownloadResource(FileService fileService) {
        this.fileService = fileService;
    }


    @GET
    @Path("/{fileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getfileInfo(@Auth ArchionUser user, @PathParam("fileId") String fileId) {
        ArchionResponse response = new ArchionResponse();

        try {
            FileMetadata fileMetadata = fileService.getUpdatedMetadata(fileId, user);
            response.setLatestValidFileMetadata(fileMetadata);
        } catch (Exception e) {
            return commonExceptionResponse(e);
        }
        return buildResponse(response, Response.Status.OK);

    }


    @GET
    @Path("/{fileId}/download")
    //@Produces({"application/pdf", "image/jpeg", "image/gif", "image/png", "application/octet-stream"})
    @Timed
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public Response downloadFile(@Auth ArchionUser user, @PathParam("fileId") String fileId) {
        ArchionResponse response = new ArchionResponse();

        try {
            byte[] file;
            file = fileService.getFileById(fileId, user.getId());
            if (file != null) {
                HashMap<String, Object> header = this.createProperHeaders(" ", String.valueOf(file.length));
                return buildResponse(null, Response.Status.OK, true, file, header, null);
            } else {
                return buildResponse(response, Response.Status.NOT_FOUND);
            }

        } catch (Exception e) {
            return commonExceptionResponse(e);
        }

    }


}