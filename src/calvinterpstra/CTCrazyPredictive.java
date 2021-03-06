package calvinterpstra;
import robocode.*;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CrazyTracker - a robot by (your name here)
 */
public class CTCrazyPredictive extends AdvancedRobot {
    private boolean movingForward;
    private int driveStage;
    private int turretStage;
    private int scannerStage;
    private boolean targeted;
    private double absoluteBearing;
    private double bearingFromGun;
    private double bearingFromRadar;
    private boolean oscillator;
    private String targetName;

    private ScannedRobotEvent target;
    private ScannedRobotEvent targetLast;

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
        this.scannerStage = 0;
        this.targeted = false;
        this.absoluteBearing = 0;
        this.bearingFromGun = 0;
        this.bearingFromRadar = 0;
        this.targetName = "";
        this.oscillator = true;
        this.target = null;
        this.targetLast = null;
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
//                setTurnRadarRight(20);
                //setTurnGunRight(20);
                if(targeted){
                    this.turretStage++;
                }
                break;
            case 1:
//                if (this.oscillator){
//                    setTurnRadarRight(this.bearingFromRadar + 20);
//                    this.oscillator = false;
//                    setScanColor(new Color(0, 0, 0));
//                }
//                else {
//                    setTurnRadarRight(this.bearingFromRadar - 20);
//                    this.oscillator = true;
//                    setScanColor(new Color(0, 255, 0));
//                }
                if (target != null && targetLast != null) {
                    attackTargetPredictive();
                }

                if(!targeted){
                    this.turretStage = 0;
                }
                break;
            default:
                break;
        }
    }

    private void runScanner(){
        switch(this.turretStage) {
            case 0:
                //setScanColor(new Color(255, 0, 0));
                setTurnRadarRight(20);
                //setTurnGunRight(20);
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

        double targetDistance = target.getDistance();
        double travelTime = targetDistance/Rules.getBulletSpeed(Rules.MAX_BULLET_POWER);
        double targetAngularVelocity = (Math.abs(target.getHeading() - targetLast.getHeading()))/10;
        double targetVelocity = target.getVelocity();
        double targetVelocityX = targetVelocity * Math.sin(Math.toRadians(targetRelativeHeading));
        if (targetAngularVelocity > 0.5){
            targetVelocityX = 0.1;
        }

        double targetNewX = travelTime*targetVelocityX;

        double predictedAngleAddition = Math.toDegrees(Math.atan2(targetNewX, targetDistance));
        double predictiveTargetBearing = (absoluteTargetBearing + predictedAngleAddition)%360;
        double bearingFromGun = normalRelativeAngleDegrees(predictiveTargetBearing - getGunHeading());

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
            this.runScanner();
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
        if (target != null) {
            if (e.getName().equals(target.getName())) {
                this.targetLast = target;
            }
        }
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
