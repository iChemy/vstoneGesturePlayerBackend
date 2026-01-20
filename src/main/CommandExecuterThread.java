package main;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import jp.vstone.RobotLib.CCommUMotion;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotPose;

public class CommandExecuterThread implements Runnable {
    private BlockingQueue<JSONObject> commandQueue;
    private Logger logger;
    private final Byte[] HOME_POSITION_IDS = new Byte[]{
        CCommUMotion.SV_BODY_P,
        CCommUMotion.SV_BODY_Y,
        CCommUMotion.SV_L_SHOULDER_P,
        CCommUMotion.SV_L_SHOULDER_R,
        CCommUMotion.SV_R_SHOULDER_P,
        CCommUMotion.SV_R_SHOULDER_R,
        CCommUMotion.SV_HEAD_P,
        CCommUMotion.SV_HEAD_R,
        CCommUMotion.SV_HEAD_Y,
        CCommUMotion.SV_EYE_P,
        CCommUMotion.SV_L_EYE_Y,
        CCommUMotion.SV_R_EYE_Y,
        CCommUMotion.SV_EYELIDs,
        CCommUMotion.SV_MOUTH

    };
    private final Short[] HOME_POSITION_VALUES = new Short[]{
        -2, 0, 58, 2, -58, -2, 0, 0, 0, 0, 0, 0, 0, 0
    };
    private final CRobotPose HOME_POSITION;

    public CommandExecuterThread(BlockingQueue<JSONObject> commandQueue, Logger logger) {
        this.commandQueue = commandQueue;
        this.logger = logger;
        CRobotPose home_position = new CRobotPose();
        home_position.SetPose(HOME_POSITION_IDS, HOME_POSITION_VALUES);
        this.HOME_POSITION = home_position;
    }

    @Override
    public void run() {
        // VSMDと通信ソケット・メモリアクセス用クラス
        CRobotMem mem = new CRobotMem();
        CCommUMotion motion = new CCommUMotion(mem);
        if (mem.Connect()) {
            if (!motion.InitRobot_CommU()) {
                logger.log(Level.SEVERE, "error detected during CommU initialization");
                return;
            }
        } else {
            logger.log(Level.SEVERE, "cannot connect to CommU");
            return;
        }

        motion.ServoOn();
        motion.play(this.HOME_POSITION, 1000);

        while (true) {
            try {
                // キューからコマンドを取り出し (空なら待機)
                JSONObject cmd = commandQueue.take();

                List<PlayPoseInfo> poses = JsonConverter.parsePoseList(cmd);
                logger.log(Level.INFO, "poses", poses);

                for (PlayPoseInfo pose: poses) {
                    motion.play(pose.pose, pose.msec);
                    motion.waitEndinterpAll();
                }

                // 一旦 debug 用にプリントするだけ
                logger.log(Level.INFO, cmd.toString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "", e);
            }
        }

    }
}
