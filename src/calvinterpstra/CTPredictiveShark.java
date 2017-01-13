package calvinterpstra;
import robocode.*;
import java.awt.*;
import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * CrazyTracker - a robot by (your name here)
 */
public class CTPredictiveShark extends AdvancedRobot {
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
    private int moveBack;

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
        this.moveBack = 0;
    }

    private void runDrive(){
        switch(this.driveStage) {
            case 0:
                if (target != null){
                    manageMove();
                }
                break;
            default:
                break;
        }
    }
    ////////////////////////////////////////////////////////////// Thanks in part to David Cincotta:
    public void manageMove(){

        double angle = Math.toRadians((getHeading() + target.getBearing() % 360));
        double x = (getX() + Math.sin(angle) * target.getDistance());
        double y = (getY() + Math.cos(angle) * target.getDistance());

        if (target.getDistance()<=250){
            double angleT = (robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing(getX(),getY(),x,y)) - getHeading() + 90)%360;
            if (Math.abs(angleT) > 90.0) {
                if (angleT > 0.0) {
                    angleT -= 180.0;
                }
                else {
                    angleT += 180.0;
                }
            }
            if (moveBack % 100 > 50){
                setTurnRight(angleT);
                setAhead(1000);
            }
            else {
                setTurnRight(-angleT);
                setBack(1000);
            }
        }
        else {
            Point2D.Double enemyCenter = new Point2D.Double(x,y);
            moveTo(enemyCenter);
        }

    }
    double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) { // both pos: lower-Left
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }

        return bearing;
    }
    public void moveTo(Point2D point){
        double distance = Point2D.distance(getX(),getY(),point.getX(),point.getY());
        double angle = robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing(getX(),getY(),point.getX(),point.getY()) - getHeading());

        if (Math.abs(angle) > 90.0) {
            distance *= -1.0;
            if (angle > 0.0) {
                angle -= 180.0;
            }
            else {
                angle += 180.0;
            }
        }
        setTurnRight(angle);
        setAhead(distance);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
            moveBack++;
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
