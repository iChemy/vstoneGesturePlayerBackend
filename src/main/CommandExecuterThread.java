package main;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import jp.vstone.RobotLib.CCommUMotion;
import jp.vstone.RobotLib.CRobotMem;

public class CommandExecuterThread implements Runnable {
    private BlockingQueue<JSONObject> commandQueue;
    private Logger logger;

    public CommandExecuterThread(BlockingQueue<JSONObject> commandQueue, Logger logger) {
        this.commandQueue = commandQueue;
        this.logger = logger;
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
