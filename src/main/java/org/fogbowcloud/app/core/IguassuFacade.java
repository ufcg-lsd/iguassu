package org.fogbowcloud.app.core;

import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.task.TaskState;

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

    public String addJob(String jdfFilePath, User owner) throws CompilerException, IOException, BlowoutException {
        return iguassuController.addJob(jdfFilePath, owner);
    }

    public ArrayList<JDFJob> getAllJobs(String owner) {
        return iguassuController.getAllJobs(owner);
    }

    public JDFJob getJobByName(String jobName, String owner){
        return iguassuController.getJobByName(jobName, owner);
    }

    public String stopJob(String jobReference, String owner) {
        return iguassuController.stopJob(jobReference, owner);
    }

    public TaskState getTaskState(String taskId){
        return iguassuController.getTaskState(taskId);
    }

    public User authUser(String credentials) throws IOException, GeneralSecurityException {
        return iguassuController.authUser(credentials);
    }

    public User authenticateUser(String credentials) throws IOException, GeneralSecurityException {
        return iguassuController.authUser(credentials);
    }

    public int getNonce() {
        return iguassuController.getNonce();
    }

    public User getUser(String username){
        return iguassuController.getUser(username);
    }

    public User addUser(String username, String publicKey) {
        return iguassuController.addUser(username, publicKey);
    }

    public boolean storeOAuthToken(OAuthToken oAuthToken){
        return iguassuController.storeOAuthToken(oAuthToken);
    }

    public List<OAuthToken> getAllOAuthTokens(){
        return iguassuController.getAllOAuthTokens();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername){
        return iguassuController.getAccessTokenByOwnerUsername(ownerUsername);
    }

    public void deleteAllExternalOAuthTokens(){
        iguassuController.deleteAllExternalOAuthTokens();
    }

}
