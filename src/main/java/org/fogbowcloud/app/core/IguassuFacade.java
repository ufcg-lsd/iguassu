package org.fogbowcloud.app.core;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;

public class IguassuFacade {

    private IguassuController iguassuController;

    public IguassuFacade(IguassuController iguassuController) {
        this.iguassuController = iguassuController;
    }

    public void init() throws Exception {
        iguassuController.init();
    }

    public JDFJob getJobById(String id, String userId) {
        return iguassuController.getJobById(id, userId);
    }

    public String submitJob(String jdfFilePath, User userId)
            throws CompilerException, IOException {
        return iguassuController.submitJob(jdfFilePath, userId);
    }

    public ArrayList<JDFJob> getAllJobs(String userId) {
        return iguassuController.getAllJobs(userId);
    }

    public JDFJob getJobByName(String jobName, String userId) {
        return iguassuController.getJobByName(jobName, userId);
    }

    public String stopJob(String jobId, String userId) {
        return iguassuController.stopJob(jobId, userId);
    }

    public User authUser(String credentials) throws GeneralSecurityException {
        return iguassuController.authUser(credentials);
    }

    public int getNonce() {
        return iguassuController.getNonce();
    }

    public User getUser(String userId) {
        return iguassuController.getUser(userId);
    }

    public void addUser(String userId, String iguassuToken) {
        iguassuController.addUser(userId, iguassuToken);
    }

    public void updateUser(User user) {
        iguassuController.updateUser(user);
    }

    public void storeOAuthToken(OAuthToken oAuthToken) {
        iguassuController.storeOAuthToken(oAuthToken);
    }

    public List<OAuthToken> getAllOAuthTokens() {
        return iguassuController.getAllOAuthTokens();
    }

    public OAuthToken getCurrentTokenByUserId(String userId) {
        return this.iguassuController.getCurrentTokenByUserId(userId);
    }

    public void deleteOAuthToken(OAuthToken oAuthToken) {
        this.iguassuController.deleteOAuthToken(oAuthToken);
    }
}
