package main;

import jp.vstone.RobotLib.CRobotPose;

public class PlayPoseInfo {
    public CRobotPose pose;
    public int msec;

    public PlayPoseInfo(CRobotPose pose, int msec) {
        this.pose = pose;
        this.msec = msec;
    }
}
