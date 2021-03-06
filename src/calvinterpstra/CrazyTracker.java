package calvinterpstra;
import robocode.*;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CrazyTracker - a robot by (your name here)
 */
public class CrazyTracker extends AdvancedRobot {
    boolean movingForward;
    int driveStage;
    int turretStage;
    boolean targeted;
    double absoluteBearing;
    double bearingFromGun;
    String targetName;

    private void init() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(false);
        setAdjustRadarForRobotTurn(false);

        // Set colors
        setBodyColor(new Color(0, 255, 0));
        setGunColor(new Color(0, 0, 0));
        setRadarColor(new Color(0, 255, 0));
        setBulletColor(new Color(0, 255, 0));
        setScanColor(new Color(255, 0, 0));

        this.driveStage = 0;
        this.turretStage = 0;
        this.targeted = false;
        this.absoluteBearing = 0;
        this.bearingFromGun = 0;
        this.targetName = "";
    }

    private void runDrive(){
        switch(this.driveStage) {
            case 0:
                setAhead(40000);
                movingForward = true;
                this.driveStage++;
                break;
            case 1:
                setTurnRight(90);
                this.driveStage++;
                break;
            case 2:
                if (new TurnCompleteCondition(this).test()){
                    this.driveStage++;
                }
                break;
            case 3:
                setTurnLeft(180);
                this.driveStage++;
                break;
            case 4:
                if (new TurnCompleteCondition(this).test()){
                    this.driveStage++;
                }
                break;
            case 5:
                setTurnRight(180);
                this.driveStage++;
                break;
            case 6:
                if (new TurnCompleteCondition(this).test()){
                    this.driveStage = 0;
                }
                break;
            default:
                break;
        }
    }

    private void runTurret(){
        switch(this.turretStage) {
            case 0:
                setScanColor(new Color(255, 0, 0));
                setTurnGunRight(20);
                if(targeted){
                    this.turretStage++;
                }
                break;
            case 1:
                setScanColor(new Color(0, 255, 0));
                setTurnGunRight((bearingFromGun - 1)%360);
                if (Math.abs(bearingFromGun) <= 5) {
                    if (getGunHeat() == 0) {
                        fire(Rules.MAX_BULLET_POWER);
                    }
                }
                if(!targeted){
                    this.turretStage = 0;
                }
                break;
            default:
                break;
        }
    }

    public void run() {
        // Initialize
        this.init();
        // Loop forever
        while (true) {
            this.runDrive();
            this.runTurret();
            execute();
        }
    }

    public void onHitWall(HitWallEvent e) {
        reverseDirection();
    }

    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setAhead(40000);
            movingForward = true;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        this.targeted = true;
        this.absoluteBearing = getHeading() + e.getBearing();
        this.bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        this.targetName = e.getName();
    }

    public void onHitRobot(HitRobotEvent e) {
        if (e.isMyFault()) {
            reverseDirection();
        }
    }

    public void onRobotDeath(RobotDeathEvent e){
        if(e.getName().equals(targetName)){
            this.targeted = false;
        }
    }
}
