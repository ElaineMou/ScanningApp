package seniordesign.scanningapp;

/**
 * Created by Elaine on 4/27/2017.
 */

public class MarkerInfo {
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
        public static MOVE_TYPE fromNum(int i) {
            for(MOVE_TYPE type : MOVE_TYPE.values()) {
                if (type.num == i) {
                    return type;
                }
            }
            return null;
        }

    }

    private HOLD_TYPE holdType;
    private MOVE_TYPE moveType;
    private String details;

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
}
