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
                // Tell the game we will want to move ahead 40000 -- some large number
                setAhead(40000);
                movingForward = true;
                if (new TurnCompleteCondition(this).test()){
                    this.driveStage++;
                }
                break;
            case 1:
                // Tell the game we will want to turn right 90
                setTurnRight(90);
                if (new TurnCompleteCondition(this).test()){
                    this.driveStage++;
                }
                break;
            case 2:
                // Now we'll turn the other way...
                setTurnLeft(180);
                if (new TurnCompleteCondition(this).test()){
                    this.driveStage++;
                }
                break;
            case 3:
                // ... then the other way ...
                setTurnRight(180);
                // .. and wait for that turn to finish.
//                waitFor(new TurnCompleteCondition(this));
                // then back to the top to do it all again
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
                // scanning
                setScanColor(new Color(255, 0, 0));
                setTurnGunRight(20);
                if(targeted){
                    this.turretStage++;
                }
                break;
            case 1:
                // targeted
                setScanColor(new Color(0, 255, 0));

                setTurnGunRight((bearingFromGun - 1)%360);
                // If it's close enough, fire!
                if (Math.abs(bearingFromGun) <= 5) {
                    if (getGunHeat() == 0) {
                        fire(Rules.MAX_BULLET_POWER);
                    }
                }
//                if (bearingFromGun == 0) {
//                    scan();
//                }
                if(!targeted){
                    this.turretStage = 0;
                }
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
//            this.targeted = false;
//            scan();
            this.runDrive();
            this.runTurret();
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
        this.targeted = true;
        // Calculate exact location of the robot
        this.absoluteBearing = getHeading() + e.getBearing();
        this.bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        this.targetName = e.getName();
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

    public void onRobotDeath(RobotDeathEvent e){
        if(e.getName().equals(targetName)){
            this.targeted = false;
        }
    }
}
