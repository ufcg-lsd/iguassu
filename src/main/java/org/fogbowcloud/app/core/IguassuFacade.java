package org.fogbowcloud.app.core;

import org.fogbowcloud.app.core.exceptions.UserNotExistException;
import org.fogbowcloud.app.core.models.user.OAuth2Identifiers;
import org.fogbowcloud.app.core.models.user.OAuthToken;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;

import java.security.GeneralSecurityException;
import java.util.Properties;

public class IguassuFacade {

    private static IguassuFacade instance;
    private IguassuController iguassuController = IguassuController.getInstance();

    private IguassuFacade() {
    }

    public static IguassuFacade getInstance() {
        synchronized (IguassuFacade.class) {
            if (instance == null) {
                instance = new IguassuFacade();
            }
            return instance;
        }
    }

    public void init(Properties properties) {
        this.iguassuController.init(properties);
    }


//    public Job getJobById(String jobId, String userId) {
//        return this.iguassuController.getJobById(jobId, userId);
//    }

    public long submitJob(String jdfFilePath, User user) throws CompilerException {
        return this.iguassuController.submitJob(jdfFilePath, user);
    }

//    public ArrayList<Job> getAllJobs(String userId) {
//        return this.iguassuController.getAllJobs(userId);
//    }
//
//    public String stopJob(String jobId, String userId) {
//        return this.iguassuController.stopJob(jobId, userId);
//    }

    public User authenticateUser(OAuth2Identifiers oAuth2Identifiers, String authorizationCode)
            throws GeneralSecurityException {
        return this.iguassuController.authenticateUser(oAuth2Identifiers, authorizationCode);
    }

    public User authorizeUser(String credentials) throws GeneralSecurityException, UserNotExistException {
        return this.iguassuController.authorizeUser(credentials);
    }

    public int getNonce() {
        return this.iguassuController.getNonce();
    }

    public void updateUser(User user) {
        this.iguassuController.updateUser(user);
    }

    public OAuthToken refreshToken(OAuthToken oAuthToken) throws GeneralSecurityException {
        return this.iguassuController.refreshToken(oAuthToken);
    }

    public OAuthToken findUserOAuthTokenByAlias(String userAlias) {
        return this.iguassuController.findUserOAuthTokenByAlias(userAlias);
    }

//    public void storeOAuthToken(OAuthToken oAuthToken) {
//        this.iguassuController.storeOAuthToken(oAuthToken);
//    }

//
//    public void deleteOAuthToken(OAuthToken oAuthToken) {
//        this.iguassuController.deleteOAuthToken(oAuthToken);
//    }
}
