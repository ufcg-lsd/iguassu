package org.fogbowcloud.app.ps;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.dtos.ResourceNode;
import org.fogbowcloud.app.jes.arrebol.helpers.QueueRequestHelper;
import org.fogbowcloud.app.ps.models.Node;

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
            String nodeId = provisioningRequestHelper.addNode(poolId, resourceNode);
            while (true) {
                Node node = provisioningRequestHelper.getNode(poolId, nodeId);
                boolean isReady = node.isReady();
                boolean isFailed = node.isFailed();
                if (isReady) {
                    logger.info("Provide service was provider resource node [" + nodeId + "]");
                    queueRequestHelper.addWorkerNode(queueId, node);
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
