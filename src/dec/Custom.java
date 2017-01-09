package dec;
import robocode.*;
import robocode.robotinterfaces.IAdvancedRobot;
import robocode.robotinterfaces.IPaintRobot;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class Custom extends AdvancedRobot implements IAdvancedRobot {
    HashMap<String,Enemy> enemyList = new HashMap<String, Enemy>();
    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        setColors(Color.black,Color.red,Color.white,Color.white,Color.white);
        //turnRadarRight(Double.POSITIVE_INFINITY);
        while(true){
            manageMove();
            manageRadar();
            manageShoot();
            execute();
        }

    }

    public void manageMove(){
        double x = getBattleFieldWidth()/2;
        double y = getBattleFieldHeight()/2;
        double distance = 1000000;

        for (Enemy a : enemyList.values()){
            if (a.distance<distance){
                distance = a.distance;
                x = a.x;
                y = a.y;
            }
        }
        /*
        if (distance<=200){
            setTurnRight(1000);
            setAhead(1000);
            return;
        }
        */
        if (distance<= 200){

        }
        Point2D.Double enemyCenter = new Point2D.Double(x,y);
        //Point2D.Double nextPoint = new Point2D.Double(Math.cos(20*getTime())*200+x,Math.sin(20*getTime())*200+y);
        moveTo(enemyCenter);

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
    public void manageRadar(){
        setTurnRadarRight(360);
    }
    public void manageShoot(){
        Enemy closestRobot = new Enemy();
        enemyList.put(new Enemy().name,new Enemy());
        for (Enemy a : enemyList.values()) {
            if (closestRobot.distance>a.distance){
                closestRobot = a;
            }
        }

        double abs = closestRobot.absBearing;
        double x = 0;
        double y = 0;

        x = closestRobot.x;
        y = closestRobot.y;

        //x = closestRobot.x + Math.cos(Math.toRadians(closestRobot.heading))*closestRobot.velocity*10;
        //y = closestRobot.y + Math.sin(Math.toRadians(closestRobot.heading))*closestRobot.velocity*10;

        double theta = abs-getGunHeading();
        //setTurnGunRight(robocode.util.Utils.normalRelativeAngleDegrees(theta));
        setTurnGunRight(robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing(getX(),getY(),x,y)-getGunHeading()));
        if (closestRobot.distance<400) {
            setFire(3);
        }


    }
    public Enemy getClosestRobot(){
        Enemy closestRobot = new Enemy();
        enemyList.put(new Enemy().name,new Enemy());
        if (enemyList.isEmpty()){
            return new Enemy();
        }
        for (Enemy a : enemyList.values()) {
            if (closestRobot.distance>a.distance){
                closestRobot = a;
            }
        }
        return closestRobot;
    }
    @Override
    public void onScannedRobot(ScannedRobotEvent enemy) {

        if (enemyList.containsKey(enemy.getName())){
            //Enemy tempEnemy = enemyList.get(enemy.getName());
            //tempEnemy.update(enemy);
            enemyList.remove(enemy.getName());
            enemyList.put(enemy.getName(), new Enemy(enemy));
            //enemyList.putIfAbsent(enemy.getName(),tempEnemy);
        } else{
            enemyList.putIfAbsent(enemy.getName(),new Enemy(enemy));
        }
    }


    public class Enemy {
        double x,y,velocity,distance,bearing,heading,energy,timeSinceScanned,absBearing;
        String name;
        //ArrayList<Point2D.Double> previousPoints = new ArrayList<Point2D.Double>();
        Enemy(){this.distance = 80000;this.absBearing = 0;this.name = "StarterRobot";}
        Enemy(ScannedRobotEvent enemy){
            this.timeSinceScanned = enemy.getTime();
            this.name = enemy.getName();
            this.velocity = enemy.getVelocity();
            this.distance = enemy.getDistance();
            this.bearing = enemy.getBearing();
            this.heading = enemy.getHeading();
            this.energy = enemy.getEnergy();
            this.absBearing = bearing+getHeading();


            double angle = Math.toRadians((getHeading() + bearing % 360));
            this.x = (getX() + Math.sin(angle) * distance);
            this.y = (getY() + Math.cos(angle) * distance);
            System.out.println("X = "+x+" Y = "+y);

            //previousPoints.add(new Point2D.Double(this.x,this.y));
            //System.out.println(name+previousPoints);

        }
        void update(ScannedRobotEvent enemy){
            this.timeSinceScanned = enemy.getTime();
            this.name = enemy.getName();
            this.velocity = enemy.getVelocity();
            this.distance = enemy.getDistance();
            this.bearing = enemy.getBearing();
            this.heading = enemy.getHeading();
            this.energy = enemy.getEnergy();
            this.absBearing = bearing+getHeading();
            double relTheta = 0;
            if (absBearing>=0 && absBearing<90){
                relTheta = 90-absBearing;
            } else if (absBearing>=90 && absBearing<180){
                relTheta = 180-absBearing;
            } else if (absBearing>=180 && absBearing<270){
                relTheta = 270-absBearing;
            } else if (absBearing>=270 && absBearing<360){
                relTheta = 360-absBearing;
            }
            this.x = getX()+distance*Math.cos(Math.toRadians(absBearing));
            this.y = getY()+distance*Math.sin(Math.toRadians(absBearing));
            //previousPoints.add(new Point2D.Double(this.x,this.y));
        }

    }



    /*
        @Override
        public void onBulletHit(BulletHitEvent event) {
        }
        @Override
        public void onBulletHitBullet(BulletHitBulletEvent event) {
        }
        @Override
        public void onBulletMissed(BulletMissedEvent event) {
        }
        @Override
        public void onHitByBullet(HitByBulletEvent event) {
        }
        @Override
        public void onHitRobot(HitRobotEvent event) {
        }
        @Override
        public void onHitWall(HitWallEvent event) {
        }
        */
    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        enemyList.remove(event.getName());
    }
    /*
    @Override
    public void onWin(WinEvent event) {
    }
    @Override
    public void onRoundEnded(RoundEndedEvent event) {
    }
    @Override
    public void onBattleEnded(BattleEndedEvent event) {
    }
    */
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
}