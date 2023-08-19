package org.georgemalandrakis.archion;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jobs.JobConfiguration;
import io.dropwizard.jobs.JobsBundle;
import org.georgemalandrakis.archion.auth.BasicAuthenticator;
import org.georgemalandrakis.archion.auth.BasicAuthorizer;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.dao.FileDAO;
import org.georgemalandrakis.archion.dao.UserDAO;
import org.georgemalandrakis.archion.filter.CrossDomainFilter;
import org.georgemalandrakis.archion.handlers.LocalMachineHandler;
import org.georgemalandrakis.archion.resource.DeleteResource;
import org.georgemalandrakis.archion.resource.DownloadResource;
import org.georgemalandrakis.archion.resource.ListResource;
import org.georgemalandrakis.archion.resource.UploadResource;
import org.georgemalandrakis.archion.scheduledtasks.*;
import org.georgemalandrakis.archion.handlers.CloudHandler;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.georgemalandrakis.archion.service.FileService;
import org.georgemalandrakis.archion.service.ScheduledTaskService;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class ArchionApplication extends Application<Config> implements JobConfiguration {

    private DeleteOldFiles deleteOldFiles;
    private DeleteOldTestFiles deleteOldTestFiles;
    private DeleteOldTempFiles deleteOldTempFiles;
    private DeleteDuplicates deleteDuplicates;
    private CleanLocalMachine cleanLocalMachine;

    public static void main(String[] args) throws Exception {
        new ArchionApplication().run(args);
    }


    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        deleteOldFiles = new DeleteOldFiles();
        deleteOldTestFiles = new DeleteOldTestFiles();
        deleteOldTempFiles = new DeleteOldTempFiles();
        deleteDuplicates = new DeleteDuplicates();
        cleanLocalMachine = new CleanLocalMachine();

        bootstrap.addBundle(new MultiPartBundle()); //Necessary for forms.
        bootstrap.addBundle(new JobsBundle(deleteOldFiles, deleteOldTestFiles, deleteOldTempFiles, cleanLocalMachine, deleteDuplicates)); //Necessary for scheduled tasks
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
        // Redis
        final String jedisServer = config.getJedis().get(Config.JEDIS_SERVER);
        final JedisPool jedisPool = new JedisPool(jedisServer, 6379);

        // Connection
        final ConnectionManager connectionObject = new ConnectionManager();
        connectionObject.setJedisPool(jedisPool);
        connectionObject.setBaseServer(config.getBase().get(Config.BASE_SERVER));
        connectionObject.setBaseName(config.getBase().get(Config.BASE_NAME));
        connectionObject.setBaseUsername(config.getBase().get(Config.BASE_USERNAME));
        connectionObject.setBasePassword(config.getBase().get(Config.BASE_PASSWORD));
        connectionObject.setAmazonAccesskey(config.getAmazon().get(Config.AMAZON_ACCESS_KEY));
        connectionObject.setAmazonSecretkey(config.getAmazon().get(Config.AMAZON_SECRET_KEY));
        connectionObject.setLocalMachineFolder(config.getLocalMachineFolder().get(Config.LOCAL_FILE_LOCATION));

        // Filters
        environment.jersey().register(CrossDomainFilter.class);

        // Logging
        final Integer logFileMaxSize = Integer.parseInt(config.getLogsettings().get(Config.LOGSETTINGS_MAXFILESIZE));
        final Map<String, Map<String, String>> logTypes = config.getLogs();

        // DAOs
        final FileDAO fileDao = new FileDAO(connectionObject);
        final UserDAO userDAO = new UserDAO(connectionObject);

        // Handlers
        final CloudHandler cloudHandler = new CloudHandler(connectionObject);
        final LocalMachineHandler localMachineHandler = new LocalMachineHandler(connectionObject);

        // Services
        final FileService fileService = new FileService(connectionObject, fileDao, cloudHandler, localMachineHandler);

        // Auth
        environment.jersey().register(new AuthDynamicFeature(
                new BasicCredentialAuthFilter.Builder<ArchionUser>()
                        .setAuthenticator(new BasicAuthenticator(userDAO))
                        .setAuthorizer(new BasicAuthorizer())
                        .setRealm("SUPER SECRET STUFF")
                        .buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        // For using @Auth in Resources:
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(ArchionUser.class));

        // Support for Multipart forms
        //environment.jersey().register(MultiPartFeature.class);

        //Jobs
        ScheduledTaskService scheduledTaskService = new ScheduledTaskService(fileDao, localMachineHandler, cloudHandler);
        deleteOldFiles.setNecessaryClasses(scheduledTaskService); //every 24h, for normal files
        deleteOldTestFiles.setNecessaryClasses(scheduledTaskService);
        deleteOldTempFiles.setNecessaryClasses(scheduledTaskService);
        deleteDuplicates.setNecessaryClasses(scheduledTaskService);
        cleanLocalMachine.setNecessaryClasses(scheduledTaskService);

        //Resources
        environment.jersey().register(new DownloadResource(fileService));
        environment.jersey().register(new UploadResource(fileService));
        environment.jersey().register(new ListResource(fileService));
        environment.jersey().register(new DeleteResource(fileService));

    }
}
