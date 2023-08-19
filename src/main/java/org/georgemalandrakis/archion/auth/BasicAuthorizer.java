package org.georgemalandrakis.archion.auth;

import io.dropwizard.auth.Authorizer;
import org.georgemalandrakis.archion.core.ArchionUser;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;

public class BasicAuthorizer implements Authorizer<ArchionUser> {
/*
        NOTE: This version of Archion includes only sample Authorization&Authentication!
 */
    @Override
    public boolean authorize(ArchionUser archionUser, String role) {
        return archionUser.getRoles().contains(role);
    }

}
