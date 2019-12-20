package org.fogbowcloud.app.ps;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.dtos.ResourceNode;
import org.fogbowcloud.app.jes.arrebol.helpers.QueueRequestHelper;

public class ResourceProvideThread extends Thread {

    private static final Logger logger = Logger.getLogger(ResourceProvideThread.class);
    private String queueId;
    private String poolId;
    private ResourceNode resourceNode;
    private ProvisioningRequestHelper provisioningRequestHelper;
    private QueueRequestHelper queueRequestHelper;

    public ResourceProvideThread(String threadName, String queueId, String poolId,
        ResourceNode resourceNode, ProvisioningRequestHelper provisioningRequestHelper,
        QueueRequestHelper queueRequestHelper) {
        super(threadName);
        this.queueId = queueId;
        this.poolId = poolId;
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
            String nodeId = provisioningRequestHelper.addNode(poolId, resourceNode);
            while (true) {
                boolean isReady = provisioningRequestHelper.getNode(queueId, nodeId).isReady();
                boolean isFailed = provisioningRequestHelper.getNode(queueId, nodeId).isFailed();
                if (isReady) {
                    logger.info("Provide service was provider resource node [" + resourceNode
                        .getResourceAddress() + "]");
                    queueRequestHelper.addWorkerNode(queueId, resourceNode);
                    break;
                }
                else if (isFailed) {
                    break;
                } else {
                    sleep(5000);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
