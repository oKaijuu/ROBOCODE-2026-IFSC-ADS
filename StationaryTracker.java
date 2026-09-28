package TriStateRobots;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class StationaryTracker extends AdvancedRobot {
	public void run() {

		// Weapon Movement != Body Movement
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true) {
			// Commands

			// Turn until found another robot
			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			execute();

		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		double robotHeading = e.getHeadingRadians();
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

		// Config bullet attributes
		double bulletPower = 3; // -- Set bullet power
		double bulletSpeed = 20 - (3 * bulletPower); // -- Set bullet speed formula

		// Get Vel \ X \ Y
		double enemyVel = e.getVelocity();
		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);

		// Set variables to Linear Prediction for shooting
		double deltaTime = 0;
		double predX = enemyX;
		double predY = enemyY;

		// Location prediction based on bullet travel time
		for (int i = 0; i < 10; i++) {
			// Distance from robot to prediction location
			double predLocation = Point2D.distance(getX(), getY(), predX, predY);

			// Time that the bullet will have to reach the prediction location
			deltaTime = predLocation / bulletSpeed;

			// Update prediction location based on time
			predX = enemyX + enemyVel * deltaTime * Math.sin(robotHeading);
			predX = enemyY + enemyVel * deltaTime * Math.cos(robotHeading);

		}

		// Prevent from aiming outside bounderies
		predX = Math.max(18, Math.min(getBattleFieldWidth() - 18, predX));
		predY = Math.max(18, Math.min(getBattleFieldHeight() - 18, predY));

		// Locate robot and fire
		double aimBearing = Math.atan2(predX - getX(), predY - getY());

		// Turn gun to the shortest angle toward the target pos
		setTurnGunRightRadians(Utils.normalRelativeAngle(aimBearing - getGunHeadingRadians()));
		
		// Check gun heat before shooting, preventing energy loss
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 1) {
            setFire(bulletPower);

		}
	}
}