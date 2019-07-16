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

    public JDFJob getJobById(String id, String owner) {
        return iguassuController.getJobById(id, owner);
    }

    public String submitJob(String jdfFilePath, User owner) throws CompilerException, IOException {
        return iguassuController.submitJob(jdfFilePath, owner);
    }

    public ArrayList<JDFJob> getAllJobs(String owner) {
        return iguassuController.getAllJobs(owner);
    }

    public JDFJob getJobByName(String jobName, String owner) {
        return iguassuController.getJobByName(jobName, owner);
    }

    public String stopJob(String jobId, String owner) {
        return iguassuController.stopJob(jobId, owner);
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

    public User addUser(String userId, String iguassuToken) {
        return iguassuController.addUser(userId, iguassuToken);
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

    public boolean deleteOAuthToken(OAuthToken oAuthToken) {
        return this.iguassuController.deleteOAuthToken(oAuthToken);
    }
}
