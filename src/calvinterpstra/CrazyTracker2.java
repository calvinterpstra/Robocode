package calvinterpstra;
import robocode.*;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CrazyTracker - a robot by (your name here)
 */
public class CrazyTracker2 extends AdvancedRobot {
    boolean movingForward;
    int driveStage;
    int turretStage;
    boolean targeted;
    double absoluteBearing;
    double bearingFromGun;
    double bearingFromRadar;
    boolean oscillator;
    String targetName;

    private ScannedRobotEvent target;

    private void init() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        // Set colors
        setBodyColor(new Color(0, 200, 0));
        setGunColor(new Color(0, 150, 0));
        setRadarColor(new Color(0, 0, 0));
        setBulletColor(new Color(0, 200, 0));
        setScanColor(new Color(255, 0, 0));

        this.driveStage = 0;
        this.turretStage = 0;
        this.targeted = false;
        this.absoluteBearing = 0;
        this.bearingFromGun = 0;
        this.bearingFromRadar = 0;
        this.targetName = "";
        this.oscillator = true;
        this.target = null;
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
                //setScanColor(new Color(255, 0, 0));
                setTurnRadarRight(20);
                setTurnGunRight(20);
                if(targeted){
                    this.turretStage++;
                }
                break;
            case 1:
                if (this.oscillator){
                    setTurnRadarRight(this.bearingFromRadar + 20);
                    this.oscillator = false;
                    setScanColor(new Color(0, 0, 0));
                }
                else {
                    setTurnRadarRight(this.bearingFromRadar - 20);
                    this.oscillator = true;
                    setScanColor(new Color(0, 255, 0));
                }

                setTurnGunRight((this.bearingFromGun - 1)%360);
                if (Math.abs(this.bearingFromGun) <= 5) {
                    if (getGunHeat() == 0) {
                        fire(Rules.MAX_BULLET_POWER);
                    }
                }
//                if (target != null) {
//                    attackTargetPredictive();
//                }

                if(!targeted){
                    this.turretStage = 0;
                }
                break;
            default:
                break;
        }
    }

    private void attackTargetPredictive(){
        double absoluteTargetBearing = (getHeading() + target.getBearing())%360;
        double targetRelativeHeading = (target.getHeading() + 360 - absoluteTargetBearing)%360;
        double targetVelocityX = target.getVelocity() * Math.sin(Math.toRadians(targetRelativeHeading));
        double targetDistance = target.getDistance();
        double travelTime = targetDistance/Rules.getBulletSpeed(Rules.MAX_BULLET_POWER);

        double predictedAngleAddition = Math.toDegrees(Math.atan2(travelTime*targetVelocityX, targetDistance));
        double predictiveTargetBearing = (absoluteTargetBearing + predictedAngleAddition)%360;
        double bearingFromGun = normalRelativeAngleDegrees(predictiveTargetBearing - getGunHeading());

        System.out.println("absoluteTargetBearing: " + absoluteTargetBearing + ", targetRelativeHeading: " + targetRelativeHeading + ", targetVelocityX: " + targetVelocityX + ", predictedAngleAddition: " + predictedAngleAddition);

        setTurnGunRight(bearingFromGun);

        if (getGunHeat() == 0) {
            setFire(Rules.MAX_BULLET_POWER);
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
        this.bearingFromRadar = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());
        this.targetName = e.getName();
        this.target = e;
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
