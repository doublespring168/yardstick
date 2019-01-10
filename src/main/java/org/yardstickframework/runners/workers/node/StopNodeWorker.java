package org.yardstickframework.runners.workers.node;

import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.context.RunContext;

/**
 * Terminates nodes.
 */
public class StopNodeWorker extends NodeWorker {
    /** {@inheritDoc} */
    public StopNodeWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        log().info(String.format("Stopping node '%s' on the host '%s'.",
            nodeInfo.toShortStr(),
            nodeInfo.host()));

        try {
            return stopNode(nodeInfo);
        }
        catch (IOException e) {
            log().error(String.format("Failed to stop node '%s' on the host '%s'",
                nodeInfo.toShortStr(),
                nodeInfo.host()), e);
        }

        return nodeInfo;
    }

    /**
     * @param nodeInfo Node info.
     * @return Node  info.
     * @throws IOException if failed.
     * @throws InterruptedException if interrupted.
     */
    public NodeInfo stopNode(NodeInfo nodeInfo) throws IOException, InterruptedException {

        nodeInfo = runCtx.handler().killNode(nodeInfo);

        return nodeInfo;
    }
}
