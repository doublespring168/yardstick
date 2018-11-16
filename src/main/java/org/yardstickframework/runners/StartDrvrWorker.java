package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class StartDrvrWorker extends StartNodeWorker {

    private String drvrLogDirFullName;

    private String mainClass = "org.yardstickframework.BenchmarkDriverStartUp";


    public StartDrvrWorker(Properties runProps, WorkContext workContext) {
        super(runProps, workContext);
    }

    @Override public void beforeWork() {
        super.beforeWork();

        drvrLogDirFullName = String.format("%s/log_drivers", baseLogDirFullName);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        final String drvrStartTime = BenchmarkUtils.dateTime();

        StartNodeWorkContext startCtx = (StartNodeWorkContext)getWorkContext();


//        BenchmarkUtils.println(String.format("Starting driver node on the host %s with id %d", ip, cnt));

//        System.out.println(String.format("full str = %s", getCfgFullStr()));
//        System.out.println(String.format("prop path = %s", getPropPath()));

        String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s", ip, drvrLogDirFullName);

        runCmd(mkdirCmd);

        String logFileName = String.format("%s/%s-id%d-%s.log",
            drvrLogDirFullName,
            drvrStartTime,
            cnt,
            ip);

        String drvrResDir = String.format("%s/output/result-%s", getMainDir(), drvrStartTime);

        String outputFolderParam = getWorkContext().getHostList().size() > 1 ?
            String.format("--outputFolder %s/%d-%s", drvrResDir, cnt, ip) :
            String.format("--outputFolder %s", drvrResDir);

        String startCmd = String.format("%s/bin/java -Dyardstick.driver%d -cp :%s/libs/* %s -id %d %s %s --config %s " +
            "--logsFolder %s --remoteuser %s --currentFolder %s --scriptsFolder %s/bin > %s 2>& 1 &",
            getRemJava(),
            cnt,
            getMainDir(),
            mainClass,
            cnt,
            outputFolderParam,
            startCtx.getFullCfgStr(),
            startCtx.getPropPath(),
            drvrLogDirFullName,
            getRemUser(),
            getMainDir(),
            getMainDir(),
            logFileName);

        NodeStarter starter = startCtx.getStartMode() == StartMode.PLAIN ?
            new PlainNodeStarter(runProps):
            new InDockerNodeStarter(runProps, startCtx);

//        System.out.println("Start cmd:");
//        System.out.println(startCmd);

        NodeInfo nodeInfo = new NodeInfo(NodeType.DRIVER, ip, null, String.valueOf(cnt), startCmd, logFileName );

        starter.startNode(nodeInfo);

        return null;
    }
}
