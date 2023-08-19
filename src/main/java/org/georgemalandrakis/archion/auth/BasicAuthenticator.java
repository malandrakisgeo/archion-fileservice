package org.georgemalandrakis.archion.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.dao.UserDAO;

import java.util.Optional;

public class BasicAuthenticator implements Authenticator<BasicCredentials, ArchionUser> {
    /*
        NOTE: This version of Archion includes no real Authorization or Authentication!
    */

    private final UserDAO userDAO;

    public BasicAuthenticator(UserDAO userDAO) {
        this.userDAO = userDAO;

    }

    @Override
    public Optional<ArchionUser> authenticate(BasicCredentials basicCredentials) throws AuthenticationException {
        if(basicCredentials.getUsername().toLowerCase().contains("george")){ //Sample use.
            ArchionUser archionUser = new ArchionUser();
            archionUser.setEmail("malandrakisgeo@gmail.com");
            archionUser.setId("36f632c4-c4e7-4522-a39b-1e9d14e8a2b9");
            archionUser.setName("George Malandrakis");
            archionUser.getRoles().add("ADMIN");
            archionUser.getRoles().add("USER");

            return Optional.of(archionUser);
        }
        else if(basicCredentials.getUsername().toLowerCase().contains("test")){ //
            ArchionUser archionUser = new ArchionUser();
            archionUser.setEmail("test@testmail.com");
            archionUser.setId("36f632c4-c4e7-4522-a38c-1e9d14e8a2b9");
            archionUser.setName("Test testsson");
            archionUser.getRoles().add("USER");
            return Optional.of(archionUser);
        }

        return Optional.empty();
    }

}
