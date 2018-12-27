package org.yardstickframework.runners.checkers;

import java.io.IOException;
import org.yardstickframework.runners.Runner;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.RunContext;

/**
 * Node checker for docker run.
 */
public class InDockerNodeChecker extends NodeChecker {
    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public InDockerNodeChecker(RunContext runCtx) {
        super(runCtx);
    }

    /** {@inheritDoc} */
    @Override public NodeInfo checkNode(NodeInfo nodeInfo) throws InterruptedException{
        String host = nodeInfo.host();

        String nodeToCheck = String.format("Dyardstick.%s%s ",
            nodeInfo.nodeType().toString().toLowerCase(), nodeInfo.id());

        String checkCmd = String.format("exec %s pgrep -f \"%s\"", nodeInfo.dockerInfo().contName(), nodeToCheck);

        CommandExecutionResult res = CommandExecutionResult.emptyFailedResult();

        try {
            res = runCtx.handler().runDockerCmd(host, checkCmd);
        }
        catch (IOException e) {
            log().error(String.format("Failed to check node '%s' on the host '%s'.",
                nodeInfo.toShortStr(),
                nodeInfo.host()));

            nodeInfo.commandExecutionResult(res);
        }

        if(res.outputList().isEmpty())
            nodeInfo.nodeStatus(NodeStatus.NOT_RUNNING);
        else
            nodeInfo.nodeStatus(NodeStatus.RUNNING);

        return nodeInfo;
    }
}
