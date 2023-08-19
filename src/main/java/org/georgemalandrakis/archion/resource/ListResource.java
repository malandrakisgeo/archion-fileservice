package org.georgemalandrakis.archion.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.georgemalandrakis.archion.core.ArchionResponse;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.model.ListRequest;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.service.FileService;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/filelist")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class ListResource extends AbstractResource {
    final FileService fileService;


    public ListResource(FileService fileService) {
        this.fileService = fileService;
    }
    @GET
    @Timed
    @Produces({"application/json"})
    @RolesAllowed("USER")
    public Response fileList(@Auth ArchionUser userOpt) {
        List<FileMetadata> fileList;

        fileList = fileService.retrieveList(userOpt.getId());
        ArchionResponse r = new ArchionResponse();
        if (!fileList.isEmpty()) {
            return buildResponse(r , Response.Status.OK, fileList);
        }
        r.setData(fileList);

        return buildResponse( new ArchionResponse(), Response.Status.NO_CONTENT);
    }

    @POST
    @Path("/exportfiles")
    @Produces({"application/json", "application/octet-stream"})
    @Timed
    public Response exportFiles(@Valid @FormDataParam("ids") ListRequest request, @Auth ArchionUser userOpt) {
        FileMetadata fileMetadata;
        int count = 0;
        ZipOutputStream zipFile = new ZipOutputStream(new ByteArrayOutputStream());
        ArchionResponse resp = new ArchionResponse();
        try {

            for (String id : request.getUuids()) {
                //fileMetadata = fileService.getUpdatedMetadata(id);
               // count++;
                zipFile.putNextEntry(new ZipEntry(id));
                var ee = fileService.getFileById(id, userOpt.getId());
                if(ee == null){
                    continue; //TODO: Throw exception or handle otherwise.
                }
                zipFile.write(ee);
                count++;

            }
            //zipFile.close();
        } catch (Exception exc) {
            return commonExceptionResponse(exc);

        }

        if (count != 0) {
            return buildResponse(resp, Response.Status.OK, zipFile);
        }

        return buildResponse(resp, Response.Status.NO_CONTENT);
    }

}


