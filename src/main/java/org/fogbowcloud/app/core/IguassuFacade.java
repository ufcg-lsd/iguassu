package org.fogbowcloud.app.core;

import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class IguassuFacade {

    private IguassuController iguassuController;

    public IguassuFacade(IguassuController iguassuController){
        this.iguassuController = iguassuController;
    }

    public void init() throws Exception {
        iguassuController.init();
    }

    public JDFJob getJobById(String id, String owner){
        return iguassuController.getJobById(id, owner);
    }

    public String addJob(String jdfFilePath, User owner) throws CompilerException, IOException {
        return iguassuController.addJob(jdfFilePath, owner);
    }

    public ArrayList<JDFJob> getAllJobs(String owner) {
        return iguassuController.getAllJobs(owner);
    }

    public JDFJob getJobByName(String jobName, String owner){
        return iguassuController.getJobByName(jobName, owner);
    }

    public String stopJob(String jobId, String owner) {
        return iguassuController.stopJob(jobId, owner);
    }

    public void updateJob(JDFJob job) {
        this.iguassuController.updateJob(job);
    }

    public User authUser(String credentials) throws IOException, GeneralSecurityException {
        return iguassuController.authUser(credentials);
    }

    public int getNonce() {
        return iguassuController.getNonce();
    }

    public User getUser(String userId){
        return iguassuController.getUser(userId);
    }

    public User addUser(String userId, String iguassuToken) {
        return iguassuController.addUser(userId, iguassuToken);
    }

    public void storeOAuthToken(OAuthToken oAuthToken){
        iguassuController.storeOAuthToken(oAuthToken);
    }

    public List<OAuthToken> getAllOAuthTokens(){
        return iguassuController.getAllOAuthTokens();
    }

}
