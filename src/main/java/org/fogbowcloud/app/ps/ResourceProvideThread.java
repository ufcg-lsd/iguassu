package org.fogbowcloud.app.ps;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.dtos.ResourceNode;
import org.fogbowcloud.app.jes.arrebol.helpers.QueueRequestHelper;

public class ResourceProvideThread extends Thread {

    private static final Logger logger = Logger.getLogger(ResourceProvideThread.class);
    private String queueId;
    private ResourceNode resourceNode;
    private ProvisioningRequestHelper provisioningRequestHelper;
    private QueueRequestHelper queueRequestHelper;

    public ResourceProvideThread(String name, String queueId,
        ResourceNode resourceNode, ProvisioningRequestHelper provisioningRequestHelper,
        QueueRequestHelper queueRequestHelper) {
        super(name);
        this.queueId = queueId;
        this.resourceNode = resourceNode;
        this.provisioningRequestHelper = provisioningRequestHelper;
        this.queueRequestHelper = queueRequestHelper;
    }

    @Override
    public void run() {
        try {
            if (!provisioningRequestHelper.containsPool(queueId)) {
                logger.info("Creating pool [" + queueId + "] on provider service");
                provisioningRequestHelper.createPool(queueId);
            }
            provisioningRequestHelper.addNode(queueId, resourceNode.getResourceAddress());
            boolean isProvisioned = provisioningRequestHelper.getPool(queueId).getNode(resourceNode.getResourceAddress()).isProvisioned();
            if(isProvisioned){
                logger.info("Provide service was provider resource node [" + resourceNode.getResourceAddress() + "]");
                queueRequestHelper.addWorkerNode(queueId, resourceNode);
            } else {
                logger.error("Provide service was not provider resource node [" + resourceNode.getResourceAddress() + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
