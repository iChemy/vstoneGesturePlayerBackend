package main;

import jp.vstone.RobotLib.CCommUMotion;
import jp.vstone.RobotLib.CRobotPose;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonConverter {

    public static List<PlayPoseInfo> parsePoseList(JSONObject rootObject) {
        List<PlayPoseInfo> poseCmdList = new ArrayList<>();

        // "poses" 配列を取得
        if (!rootObject.has("poses")) {
            System.err.println("poses field cannot be found");
            return poseCmdList;
        }
        JSONArray posesArray = rootObject.getJSONArray("poses");

        if (posesArray.length() <= 0) {
            System.err.println("no poses");
            return poseCmdList;
        }

        // 配列をループ
        for (int i = 0; i < posesArray.length(); i++) {
            JSONObject poseJson = posesArray.getJSONObject(i);
            CRobotPose pose = new CRobotPose();
            HashMap<Byte, Short> angle_map = new HashMap<>();
            // 1. msec の取得
            int msec = poseJson.getInt("msec");

            // 2. ServoChange の取得とマッピング
            JSONObject servoJson = poseJson.getJSONObject("servo_change");

            // JSONにあるキーに応じてフィールドに値をセット
            // (手動で全フィールド書くか、以下のようなマッピング処理を行います)
            if (servoJson.has("L_SHOU_P"))
                angle_map.put(CCommUMotion.SV_L_SHOULDER_P, Short.valueOf((short) servoJson.getInt("L_SHOU_P")));
            if (servoJson.has("L_SHOU_R"))
                angle_map.put(CCommUMotion.SV_L_SHOULDER_R, Short.valueOf((short) servoJson.getInt("L_SHOU_R")));
            if (servoJson.has("R_SHOU_P"))
                angle_map.put(CCommUMotion.SV_R_SHOULDER_P, Short.valueOf((short) servoJson.getInt("R_SHOU_P")));
            if (servoJson.has("R_SHOU_R"))
                angle_map.put(CCommUMotion.SV_R_SHOULDER_R, Short.valueOf((short) servoJson.getInt("R_SHOU_R")));
            if (servoJson.has("BODY_P"))
                angle_map.put(CCommUMotion.SV_BODY_P, Short.valueOf((short) servoJson.getInt("BODY_P")));
            if (servoJson.has("BODY_Y"))
                angle_map.put(CCommUMotion.SV_BODY_Y, Short.valueOf((short) servoJson.getInt("BODY_Y")));
            if (servoJson.has("HEAD_P"))
                angle_map.put(CCommUMotion.SV_HEAD_P, Short.valueOf((short) servoJson.getInt("HEAD_P")));
            if (servoJson.has("HEAD_R"))
                angle_map.put(CCommUMotion.SV_HEAD_R, Short.valueOf((short) servoJson.getInt("HEAD_R")));
            if (servoJson.has("HEAD_Y"))
                angle_map.put(CCommUMotion.SV_HEAD_Y, Short.valueOf((short) servoJson.getInt("HEAD_Y")));
            if (servoJson.has("EYELID"))
                angle_map.put(CCommUMotion.SV_EYELIDs, Short.valueOf((short) servoJson.getInt("EYELID")));
            if (servoJson.has("EYES_P"))
                angle_map.put(CCommUMotion.SV_EYE_P, Short.valueOf((short) servoJson.getInt("EYES_P")));
            if (servoJson.has("L_EYE_Y"))
                angle_map.put(CCommUMotion.SV_L_EYE_Y, Short.valueOf((short) servoJson.getInt("L_EYE_Y")));
            if (servoJson.has("R_EYE_Y"))
                angle_map.put(CCommUMotion.SV_R_EYE_Y, Short.valueOf((short) servoJson.getInt("R_EYE_Y")));

            poseCmdList.add(new PlayPoseInfo(pose, msec));
        }

        return poseCmdList;
    }

}