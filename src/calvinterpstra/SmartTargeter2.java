package calvinterpstra;
import robocode.*;
import java.awt.*;
import java.util.HashMap;

import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class SmartTargeter2 extends AdvancedRobot {
    private HashMap<String, ScannedRobotEvent> targets = new HashMap<String, ScannedRobotEvent>();
    private int driveStage;
    private int turretStage;
    private int scannerStage;

    private ScannedRobotEvent target;

    private boolean movingForward;

    private void init() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        // Set colors
        setBodyColor(new Color(0, 200, 255));
        setGunColor(new Color(0, 200, 255));
        setRadarColor(new Color(0, 0, 0));
        setBulletColor(new Color(0, 200, 255));
        setScanColor(new Color(0, 0, 0));

        this.driveStage = 0;
        this.turretStage = 0;
        this.scannerStage = 0;
        this.target = null;
    }

    private void runDrive(){
        switch(this.driveStage) {
            case 0:
                break;
            default:
                break;
        }
    }

    private void selectTarget(){
        double closestDist = 1000000000;
        for (ScannedRobotEvent robot : targets.values()) {
            if (robot.getEnergy() > 0){
                if (robot.getDistance() < closestDist){
                    target = robot;
                    closestDist = robot.getDistance();
                }
            }
        }
    }

//    private void attackTarget(){
//            double absoluteBearing = getHeading() + target.getBearing();
//            double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
//
//            System.out.println("targetName: "+ target.getName() + ", energy: " + target.getEnergy());
//
//            if (Math.abs(bearingFromGun) <= 3) {
//                setTurnGunRight(bearingFromGun);
//                if (getGunHeat() == 0) {
//                    fire(Rules.MAX_BULLET_POWER);
//                }
//            }
//            else {
//                setTurnGunRight(bearingFromGun);
//            }
//    }

    private void attackTargetPredictive(){
        double absoluteTargetBearing = (getHeading() + target.getBearing())%360;
        double targetRelativeHeading = (target.getHeading() + 360 - absoluteTargetBearing)%360;
        double targetVelocityX = target.getVelocity() * Math.sin(Math.toRadians(targetRelativeHeading));
        double velocityCoeffocient = 40;
        double targetDistance = target.getDistance();

        double predictedAngleAddition = Math.toDegrees(Math.atan2(velocityCoeffocient*targetVelocityX, targetDistance));
        double predictiveTargetBearing = absoluteTargetBearing + predictedAngleAddition;
        double bearingFromGun = normalRelativeAngleDegrees(predictiveTargetBearing - getGunHeading());

        System.out.println("absoluteTargetBearing: " + absoluteTargetBearing + ", targetRelativeHeading: " + targetRelativeHeading + ", targetVelocityX: " + targetVelocityX + ", predictedAngleAddition: " + predictedAngleAddition);

        if (Math.abs(bearingFromGun) <= 3) {
            setTurnGunRight(bearingFromGun);
            if (getGunHeat() == 0) {
                fire(Math.min(1000/targetDistance, Rules.MAX_BULLET_POWER));
            }
        }
        else {
            setTurnGunRight(bearingFromGun);
        }
    }


    private void runTurret(){
        switch(this.turretStage) {
            case 0:
                if (target != null) {
                    attackTargetPredictive();
                }
                break;
            default:
                break;
        }
    }

    private void runScanner(){
        switch(this.scannerStage) {
            case 0:
                // scanning
                setTurnRadarRight(360);
                selectTarget();
                break;
            default:
                break;
        }
    }

    /**
     * run: Crazy's main run function
     */

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

    /**
     * onHitWall:  Handle collision with wall.
     */
    public void onHitWall(HitWallEvent e) {
        // Bounce off!
        reverseDirection();
    }

    /**
     * reverseDirection:  Switch from ahead to back & vice versa
     */
    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setAhead(40000);
            movingForward = true;
        }
    }

    /**
     * onScannedRobot:  Fire!
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        this.targets.put(e.getName() , e);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        this.targets.remove(e.getName());
    }


    /**
     * onHitRobot:  Back up!
     */
    public void onHitRobot(HitRobotEvent e) {
        // If we're moving the other robot, reverse!
        if (e.isMyFault()) {
            reverseDirection();
        }
    }
}