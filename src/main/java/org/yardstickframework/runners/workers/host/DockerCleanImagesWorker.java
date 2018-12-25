package org.yardstickframework.runners.workers.host;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.WorkResult;

/**
 * Cleans docker containers.
 */
public class DockerCleanImagesWorker extends DockerHostWorker {
    /** {@inheritDoc} */
    public DockerCleanImagesWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host);

        return null;
    }

    /**
     * @param host Host.
     */
    private void removeImages(String host) {
        Collection<Map<String, String>> imageMaps = getImages(host);

        Map<String, String> toRem = new HashMap<>();

        for (Map<String, String> imageMap : imageMaps) {
            String imageName = imageMap.get("REPOSITORY");

            if (nameToDelete(imageName))
                toRem.put(imageMap.get("IMAGE ID"), imageName);
        }

        int tryes = 2;

        // Removing images twice because some of the images can have child images and therefore cannot be removed
        // right away.
        while (tryes-- > 0) {
            for (String id : toRem.keySet()) {
                if (checkIfImageIdExists(host, id))
                    removeImage(host, id, toRem.get(id));
            }
        }
    }

    /**
     * @param host Host.
     * @param imageId Image id.
     * @param imageName Image name.
     * @return Command execution result.
     */
    private CommandExecutionResult removeImage(String host, String imageId, String imageName) {
        log().info(String.format("Removing the image '%s' (id=%s) from the host '%s'",
            imageName, imageId, host));

        CommandExecutionResult cmdRes = null;

        try {
            cmdRes = runCtx.handler().runDockerCmd(host, String.format("rmi -f %s", imageId));
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if(!checkIfImageIdExists(host, imageId))
            log().info(String.format("Image '%s' on the host '%s' is successfully removed.", imageName, host));
        else
            log().info(String.format("Image '%s' on the host '%s' is not removed.", imageName, host));

        return cmdRes;
    }
}
