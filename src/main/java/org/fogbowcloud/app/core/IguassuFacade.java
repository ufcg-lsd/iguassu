package org.fogbowcloud.app.core;

import org.fogbowcloud.app.core.auth.models.User;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class IguassuFacade {

    private IguassuController iguassuController;

    public IguassuFacade(IguassuController iguassuController) {
        this.iguassuController = iguassuController;
    }

    public void init() {
        iguassuController.init();
    }

    public JDFJob getJobById(String id, String userId) {
        return iguassuController.getJobById(id, userId);
    }

    public String submitJob(String jdfFilePath, User userId)
            throws CompilerException {
        return iguassuController.submitJob(jdfFilePath, userId);
    }

    public ArrayList<JDFJob> getAllJobs(String userId) {
        return iguassuController.getAllJobs(userId);
    }

    public JDFJob getJobByLabel(String jobName, String userId) {
        return iguassuController.getJobByLabel(jobName, userId);
    }

    public String stopJob(String jobId, String userId) {
        return iguassuController.stopJob(jobId, userId);
    }

    public User authorizeUser(String credentials) throws GeneralSecurityException {
        return iguassuController.authorizeUser(credentials);
    }

    public int getNonce() {
        return iguassuController.getNonce();
    }

    public User retrieveUser(String userId) {
        return iguassuController.retrieveUser(userId);
    }

    public void storeUser(String userId, String iguassuToken) {
        iguassuController.storeUser(userId, iguassuToken);
    }

    public void updateUser(User user) {
        iguassuController.updateUser(user);
    }

    public void storeOAuthToken(OAuthToken oAuthToken) {
        iguassuController.storeOAuthToken(oAuthToken);
    }

    public OAuthToken getCurrentTokenByUserId(String userId) {
        return this.iguassuController.getCurrentTokenByUserId(userId);
    }

    public void deleteOAuthToken(OAuthToken oAuthToken) {
        this.iguassuController.deleteOAuthToken(oAuthToken);
    }
}
