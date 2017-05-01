package seniordesign.scanningapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Elaine on 4/27/2017.
 */

public class MarkerInfo {
    public static final String HOLD_JSON_KEY = "hold";
    public static final String MOVE_JSON_KEY = "move";
    public static final String DESC_JSON_KEY = "description";
    public static final String X_JSON_KEY = "x";
    public static final String Y_JSON_KEY = "y";
    public static final String Z_JSON_KEY = "z";


    public enum HOLD_TYPE {
        CRIMP(1, "Crimp"), EDGE(2, "Edge"), JUG(3, "Jug"), PINCH(4, "Pinch"), POCKET(5, "Pocket"),
        SLOPER(6, "Sloper"), UNDERCLING(7, "Undercling"), FINGER_CRACK(8, "Finger Crack"),
        HAND_CRACK(9, "Hand Crack"), OFFWIDTH(10, "Offwidth");

        private int num;
        private String name;

        @Override
        public String toString() {
            return name;
        }

        HOLD_TYPE(int num, String name) {
            this.num = num;
            this.name = name;
        }
        public static HOLD_TYPE fromNum(int i) {
            for(HOLD_TYPE type : HOLD_TYPE.values()) {
                if (type.num == i) {
                    return type;
                }
            }
            return null;
        }
        public static HOLD_TYPE fromString(String s) {
            for(HOLD_TYPE type : HOLD_TYPE.values()) {
                if (type.name.equals(s)) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum MOVE_TYPE {
        DEADPOINT(1, "Deadpoint"), DYNO(2, "Dyno"), FINGER_LOCK(3, "Finger Lock"),
        GASTON(4, "Gaston"), HEEL_HOOK(5, "Heel Hook"), KNEE_BAR(6,"Knee Bar"),
        LAYBACK(7, "Layback"), LOCKOFF(8, "Lock-off"), MANTLE(9, "Mantle"), SIDEPULL(10, "Sidepull"),
        STEM(11, "Stem");

        private int num;
        private String name;

        MOVE_TYPE(int num, String name) {
            this.num = num;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
        public static MOVE_TYPE fromNum(int i) {
            for(MOVE_TYPE type : MOVE_TYPE.values()) {
                if (type.num == i) {
                    return type;
                }
            }
            return null;
        }

        public static MOVE_TYPE fromString(String s) {
            for(MOVE_TYPE type : MOVE_TYPE.values()) {
                if (type.name.equals(s)) {
                    return type;
                }
            }
            return null;
        }

    }

    private HOLD_TYPE holdType;
    private MOVE_TYPE moveType;
    private String details;
    private float[] transform;

    public HOLD_TYPE getHoldType() {
        return holdType;
    }
    public void setHoldType(HOLD_TYPE type) {
        this.holdType = type;
    }
    public MOVE_TYPE getMoveType() {
        return moveType;
    }
    public void setMoveType(MOVE_TYPE moveType) {
        this.moveType = moveType;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String det){
        this.details = det;
    }
    public float[] getTransform() {
        return transform;
    }
    public void setTransform(float[] transform) {
        this.transform = transform;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(HOLD_JSON_KEY,holdType == null ? "" : holdType.toString());
        obj.put(MOVE_JSON_KEY,moveType == null ? "" : moveType.toString());
        obj.put(DESC_JSON_KEY,details);
        obj.put(X_JSON_KEY,transform[0]);
        obj.put(Y_JSON_KEY,transform[1]);
        obj.put(Z_JSON_KEY,transform[2]);

        return obj;
    }

    public static ArrayList<MarkerInfo> MarkersFromJson(String markersJson) throws JSONException {
        JSONArray jsonArray = new JSONArray(markersJson);
        ArrayList<MarkerInfo> list = new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            MarkerInfo.HOLD_TYPE hold = MarkerInfo.HOLD_TYPE.fromString(
                    obj.getString(MarkerInfo.HOLD_JSON_KEY));
            MarkerInfo.MOVE_TYPE move = MarkerInfo.MOVE_TYPE.fromString(
                    obj.getString(MarkerInfo.MOVE_JSON_KEY));
            String description = obj.getString(MarkerInfo.DESC_JSON_KEY);
            float x = (float) obj.getDouble(MarkerInfo.X_JSON_KEY);
            float y = (float) obj.getDouble(MarkerInfo.Y_JSON_KEY);
            float z = (float) obj.getDouble(MarkerInfo.Z_JSON_KEY);

            MarkerInfo marker = new MarkerInfo();
            marker.setHoldType(hold);
            marker.setMoveType(move);
            marker.setDetails(description);
            marker.setTransform(new float[]{x,y,z});
            list.add(marker);
        }
        return list;
    }

    /*public class MarkerTransform {
        public static final String POSITION_JSON_KEY = "position";
        public static final String ROTATION_JSON_KEY = "rotation";
        public static final String SCALE_JSON_KEY = "scale";
        float position[];
        float rotation[];
        float scale[];

        MarkerTransform(float[] pos, float[] rot, float[] scale) {
            this.position = pos;
            this.rotation = rot;
            this.scale = scale;
        }

        JSONObject toJson() {
            JSONObject obj = new JSONObject();

            return obj;
        }
    }*/
}
