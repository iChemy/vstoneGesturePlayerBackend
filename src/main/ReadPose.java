package main;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import jp.vstone.RobotLib.CCommUMotion;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotPose;

public class ReadPose {
    CRobotMem mem;
    CCommUMotion motion;

    public ReadPose() {
        mem = new CRobotMem();
        motion = new CCommUMotion(mem);
        if (mem.Connect()) {
            if (!motion.InitRobot_CommU()) {
                System.err.println("err");
            }
        }
    }

    void getPose() {
        CRobotPose pose = motion.getReadPose();

        Map<Byte, Short> map = pose.getPose();

        // 1. 逆引き用のMapを作成する（static初期化子などで一度だけ作ると効率的です）
        Map<Byte, String> byteToNameMap = new HashMap<>();

        try {
            // CCommUMotionクラスの全フィールドを取得
            for (Field field : CCommUMotion.class.getDeclaredFields()) {
                // static finalなByteフィールドのみ対象にするなどのフィルタリングも可能
                if (field.getType() == byte.class || field.getType() == Byte.class) {
                    field.setAccessible(true);
                    Byte value = (Byte) field.get(null); // staticフィールドの値を取得
                    byteToNameMap.put(value, field.getName()); // 値と名前(例: "SV_BODY_P")をマッピング
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 2. デバッグ出力時にそのMapを使う
        map.forEach((key, value) -> {
            // マップに名前があればそれを、なければ16進数を表示
            String keyName = byteToNameMap.getOrDefault(key, String.format("UNKNOWN(0x%02X)", key));

            System.out.printf("%-20s : %d%n", keyName, value);
        });
    }

    public static void main(String[] args) {
        ReadPose reader = new ReadPose();
        reader.getPose();
    }
}
