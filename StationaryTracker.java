package TriStateRobots;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationaryTracker extends AdvancedRobot {

	private final Map<String, EnemyData> enemies = new HashMap<>();

	private static final int MAX_TRACKING_TIME = 40;
	private static final int MAX_THREAT_COUNT = 5;

	private static final int DANGER_SAMPLES = 16;
	private static final int DODGE_SAMPLES = 24;

	private static final double MOVEMENT_DISTANCE = 120.0;
	private static final double DANGER_SCAN_DISTANCE = 700.0;
	private static final double WALL_MARGIN = 70.0;

	private static final double MIN_BULLET_POWER = 0.1;
	private static final double MAX_BULLET_POWER = 3.0;

	// Stage 8
	private static final double ROBOT_RADIUS = 18.0;
	private static final double DODGE_LOOKAHEAD = 120.0;
	private static final double BULLET_DANGER_RADIUS = 38.0;
	private static final double MOVEMENT_PATTERN_PENALTY = 20.0;

	// Stage 9 - Radar Lock
	private static final long RADAR_LOCK_TIMEOUT = 8;
	private static final double RADAR_LOCK_OVERSHOOT = 1.15;

	// Stage 10 - Statistical Targeting
	private static final int MAX_STATISTICAL_BINS = 31;
	private static final int STATISTICAL_SEGMENTS = 9;
	private static final int MIN_STATISTICAL_SAMPLES = 8;
	private static final double STATISTICAL_BIN_WIDTH = 0.10;
	private static final double STATISTICAL_LEARNING_RATE = 1.0;
	private static final double STATISTICAL_ESCAPE_THRESHOLD = 0.55;

	private int movementDirection = 1;

	private double lastMovementAngle;
	private double previousMovementAngle;

	private long lastDirectionChange;
	private long lastBulletDetection;

	// Stage 9
	private String radarTarget;
	private long lastRadarScan;

	// Stage 10
	private final Map<String, StatisticalData> statisticalData =
		new HashMap<>();

	public void run() {

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		lastRadarScan = getTime();

		while (true) {

			updateRadar();

			updateThreatAssessment();

			updateBulletDetection();

			updateMovement();

			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		EnemyData enemy = enemies.get(e.getName());

		if (enemy == null) {

			enemy = new EnemyData(e.getName());
			enemies.put(e.getName(), enemy);

			statisticalData.put(
				e.getName(),
				new StatisticalData()
			);
		}

		/*
		 * Stage 10:
		 * Before replacing the previous enemy state,
		 * register where the enemy actually moved.
		 */
		learnMovementPattern(enemy);

		enemy.update(
			e,
			getX(),
			getY(),
			getHeadingRadians(),
			getTime()
		);

		lastRadarScan = getTime();

		updateRadarTarget(enemy);

		detectEnemyFire(enemy);

		updateThreat(enemy);

		// =====================================================
		// Stage 10 - Statistical Targeting
		// =====================================================

		double bulletPower =
			BulletPower.calculate(
				enemy.distance,
				getEnergy(),
				enemy.energy
			);

		double bulletSpeed =
			20.0 -
			(3.0 * bulletPower);

		Point2D.Double predictedPosition =
			getStatisticalPrediction(
				enemy,
				bulletSpeed
			);

		double predictedX =
			predictedPosition.x;

		double predictedY =
			predictedPosition.y;

		/*
		 * Keep the prediction inside the battlefield.
		 */
		predictedX =
			Math.max(
				18,
				Math.min(
					getBattleFieldWidth() - 18,
					predictedX
				)
			);

		predictedY =
			Math.max(
				18,
				Math.min(
					getBattleFieldHeight() - 18,
					predictedY
				)
			);

		double aimBearing =
			Math.atan2(
				predictedX - getX(),
				predictedY - getY()
			);

		setTurnGunRightRadians(
			Utils.normalRelativeAngle(
				aimBearing -
				getGunHeadingRadians()
			)
		);

		if (
			getGunHeat() <= 0 &&
			Math.abs(getGunTurnRemaining()) < 1
		) {

			setFire(bulletPower);
		}
	}

	// =========================================================
	// Stage 10 - Statistical Targeting
	// =========================================================

	private void learnMovementPattern(
		EnemyData enemy
	) {

		if (!enemy.hasPreviousData()) {
			return;
		}

		StatisticalData data =
			statisticalData.get(enemy.name);

		if (data == null) {

			data = new StatisticalData();
			statisticalData.put(
				enemy.name,
				data
			);
		}

		double movementAngle =
			Math.atan2(
				enemy.x - enemy.previousX,
				enemy.y - enemy.previousY
			);

		double actualMovement =
			enemy.getMovementDistance();

		if (actualMovement < 0.1) {

			data.stationarySamples +=
				STATISTICAL_LEARNING_RATE;

			return;
		}

		double movementRelativeToEnemy =
			Utils.normalRelativeAngle(
				movementAngle -
				enemy.previousHeading
			);

		/*
		 * Normalize lateral movement.
		 *
		 * -1 = strongly moving left
		 *  0 = moving directly toward/away
		 * +1 = strongly moving right
		 */
		double lateralFactor =
			Math.sin(
				movementRelativeToEnemy
			);

		double acceleration =
			enemy.velocity -
			enemy.previousVelocity;

		double velocityFactor =
			Math.min(
				Math.abs(enemy.velocity) / 8.0,
				1.0
			);

		int directionBin =
			getDirectionBin(
				lateralFactor
			);

		int velocityBin =
			getVelocityBin(
				velocityFactor
			);

		int accelerationBin =
			getAccelerationBin(
				acceleration
			);

		data.lateralBins[directionBin] +=
			STATISTICAL_LEARNING_RATE;

		data.velocityBins[velocityBin] +=
			STATISTICAL_LEARNING_RATE;

		data.accelerationBins[accelerationBin] +=
			STATISTICAL_LEARNING_RATE;

		data.totalSamples +=
			STATISTICAL_LEARNING_RATE;

		if (lateralFactor > 0.35) {

			data.rightSamples +=
				STATISTICAL_LEARNING_RATE;

		} else if (lateralFactor < -0.35) {

			data.leftSamples +=
				STATISTICAL_LEARNING_RATE;

		} else {

			data.forwardSamples +=
				STATISTICAL_LEARNING_RATE;
		}

		/*
		 * Track whether the enemy frequently changes
		 * movement side.
		 */
		if (data.hasPreviousLateral) {

			if (
				Math.signum(
					lateralFactor
				) !=
				Math.signum(
					data.previousLateral
				)
			) {

				data.directionChanges++;
			}
		}

		data.previousLateral =
			lateralFactor;

		data.hasPreviousLateral = true;
	}

	private Point2D.Double getStatisticalPrediction(
		EnemyData enemy,
		double bulletSpeed
	) {

		/*
		 * Start with the normal wall-aware linear prediction.
		 */
		double predictedX =
			enemy.x;

		double predictedY =
			enemy.y;

		double deltaTime =
			Point2D.distance(
				getX(),
				getY(),
				enemy.x,
				enemy.y
			) / bulletSpeed;

		for (int i = 0; i < 10; i++) {

			Point2D.Double prediction =
				WallPrediction.predict(
					enemy.x,
					enemy.y,
					enemy.heading,
					enemy.velocity,
					deltaTime,
					getBattleFieldWidth(),
					getBattleFieldHeight()
				);

			predictedX =
				prediction.x;

			predictedY =
				prediction.y;

			double distance =
				Point2D.distance(
					getX(),
					getY(),
					predictedX,
					predictedY
				);

			deltaTime =
				distance /
				bulletSpeed;
		}

		StatisticalData data =
			statisticalData.get(
				enemy.name
			);

		if (
			data == null ||
			data.totalSamples < MIN_STATISTICAL_SAMPLES
		) {

			return new Point2D.Double(
				predictedX,
				predictedY
			);
		}

		/*
		 * Determine the most likely movement direction
		 * from previous observations.
		 */
		double leftProbability =
			data.leftSamples /
			Math.max(
				data.totalSamples,
				1.0
			);

		double rightProbability =
			data.rightSamples /
			Math.max(
				data.totalSamples,
				1.0
			);

		double forwardProbability =
			data.forwardSamples /
			Math.max(
				data.totalSamples,
				1.0
			);

		double lateralOffset = 0;

		if (
			leftProbability >
			rightProbability &&
			leftProbability >
			forwardProbability
		) {

			lateralOffset =
				-enemy.velocity *
				0.75;

		} else if (
			rightProbability >
			leftProbability &&
			rightProbability >
			forwardProbability
		) {

			lateralOffset =
				enemy.velocity *
				0.75;
		}

		/*
		 * If the enemy has a strong tendency to switch
		 * directions, reduce the statistical correction.
		 */
		double changeProbability =
			data.directionChanges /
			Math.max(
				data.totalSamples,
				1.0
			);

		if (
			changeProbability >
			STATISTICAL_ESCAPE_THRESHOLD
		) {

			lateralOffset *= 0.35;
		}

		/*
		 * Convert the statistical lateral movement into
		 * a position offset perpendicular to the enemy's
		 * current heading.
		 */
		double perpendicularX =
			Math.cos(enemy.heading);

		double perpendicularY =
			-Math.sin(enemy.heading);

		predictedX +=
			perpendicularX *
			lateralOffset *
			Math.min(deltaTime, 20.0);

		predictedY +=
			perpendicularY *
			lateralOffset *
			Math.min(deltaTime, 20.0);

		/*
		 * Statistical velocity prediction.
		 */
		double averageVelocity =
			calculateStatisticalVelocity(
				data,
				enemy.velocity
			);

		double velocityDifference =
			averageVelocity -
			enemy.velocity;

		predictedX +=
			Math.sin(enemy.heading) *
			velocityDifference *
			Math.min(deltaTime, 20.0);

		predictedY +=
			Math.cos(enemy.heading) *
			velocityDifference *
			Math.min(deltaTime, 20.0);

		return new Point2D.Double(
			predictedX,
			predictedY
		);
	}

	private double calculateStatisticalVelocity(
		StatisticalData data,
		double currentVelocity
	) {

		double weightedVelocity = 0;
		double totalWeight = 0;

		for (
			int i = 0;
			i < data.velocityBins.length;
			i++
		) {

			double weight =
				data.velocityBins[i];

			if (weight <= 0) {
				continue;
			}

			double normalized =
				(
					i /
					(double)
					(data.velocityBins.length - 1)
				);

			double velocity =
				normalized * 8.0;

			weightedVelocity +=
				velocity * weight;

			totalWeight += weight;
		}

		if (totalWeight <= 0) {
			return currentVelocity;
		}

		double average =
			weightedVelocity /
			totalWeight;

		/*
		 * Preserve the current direction of movement.
		 */
		if (currentVelocity < 0) {
			average = -average;
		}

		return average;
	}

	private int getDirectionBin(
		double lateralFactor
	) {

		double normalized =
			Math.max(
				-1.0,
				Math.min(
					1.0,
					lateralFactor
				)
			);

		int bin =
			(int)
			Math.floor(
				(
					normalized + 1.0
				) /
				2.0 *
				MAX_STATISTICAL_BINS
			);

		return Math.max(
			0,
			Math.min(
				MAX_STATISTICAL_BINS - 1,
				bin
			)
		);
	}

	private int getVelocityBin(
		double velocityFactor
	) {

		int bin =
			(int)
			Math.floor(
				velocityFactor *
				MAX_STATISTICAL_BINS
			);

		return Math.max(
			0,
			Math.min(
				MAX_STATISTICAL_BINS - 1,
				bin
			)
		);
	}

	private int getAccelerationBin(
		double acceleration
	) {

		double normalized =
			Math.max(
				-1.0,
				Math.min(
					1.0,
					acceleration / 2.0
				)
			);

		int bin =
			(int)
			Math.floor(
				(
					normalized + 1.0
				) /
				2.0 *
				MAX_STATISTICAL_BINS
			);

		return Math.max(
			0,
			Math.min(
				MAX_STATISTICAL_BINS - 1,
				bin
			)
		);
	}

	// =========================================================
	// Stage 9 - Radar Lock
	// =========================================================

	private void updateRadarTarget(
		EnemyData enemy
	) {

		if (radarTarget == null) {

			radarTarget =
				enemy.name;

			return;
		}

		EnemyData currentTarget =
			enemies.get(radarTarget);

		if (currentTarget == null) {

			radarTarget =
				enemy.name;

			return;
		}

		long currentAge =
			getTime() -
			currentTarget.lastScanTime;

		if (
			currentAge >
			RADAR_LOCK_TIMEOUT
		) {

			radarTarget =
				enemy.name;

			return;
		}

		if (
			enemy.threatScore >
			currentTarget.threatScore +
			15.0
		) {

			radarTarget =
				enemy.name;
		}
	}

	private void updateRadar() {

		if (
			radarTarget == null ||
			getTime() -
			lastRadarScan >
			RADAR_LOCK_TIMEOUT
		) {

			setTurnRadarRightRadians(
				2.0 * Math.PI
			);

			return;
		}

		EnemyData target =
			enemies.get(
				radarTarget
			);

		if (target == null) {

			setTurnRadarRightRadians(
				2.0 * Math.PI
			);

			return;
		}

		double absoluteBearing =
			Math.atan2(
				target.x - getX(),
				target.y - getY()
			);

		double radarTurn =
			Utils.normalRelativeAngle(
				absoluteBearing -
				getRadarHeadingRadians()
			);

		double overshoot =
			Math.copySign(
				Math.max(
					Math.toRadians(3),
					Math.abs(radarTurn) *
					(RADAR_LOCK_OVERSHOOT - 1.0)
				),
				radarTurn
			);

		radarTurn +=
			overshoot;

		setTurnRadarRightRadians(
			radarTurn
		);
	}

	// =========================================================
	// Stage 5 - Multi-Enemy Threat Assessment
	// =========================================================

	private void updateThreatAssessment() {

		for (EnemyData enemy : enemies.values()) {

			if (enemy == null) {
				continue;
			}

			long age =
				getTime() -
				enemy.lastScanTime;

			if (
				age >
				MAX_TRACKING_TIME
			) {

				enemy.threatScore = 0;
				enemy.isThreat = false;

				continue;
			}

			updateThreat(enemy);
		}
	}

	private void updateThreat(
		EnemyData enemy
	) {

		long age =
			getTime() -
			enemy.lastScanTime;

		if (
			age >
			MAX_TRACKING_TIME
		) {

			enemy.threatScore = 0;
			enemy.isThreat = false;

			return;
		}

		double distanceScore =
			1.0 -
			Math.min(
				enemy.distance / 800.0,
				1.0
			);

		distanceScore *= 45.0;

		double energyScore =
			Math.min(
				enemy.energy / 100.0,
				1.0
			);

		energyScore *= 10.0;

		double movementScore =
			Math.min(
				Math.abs(enemy.velocity) / 8.0,
				1.0
			);

		movementScore *= 5.0;

		double aimScore = 0;

		if (isEnemyAimingAtUs(enemy)) {
			aimScore = 25.0;
		}

		double fireScore = 0;

		if (enemy.fireDetected) {
			fireScore = 30.0;
		}

		double informationConfidence =
			1.0 -
			Math.min(
				age /
				(double)
				MAX_TRACKING_TIME,
				1.0
			);

		double rawThreat =
			distanceScore +
			energyScore +
			movementScore +
			aimScore +
			fireScore;

		enemy.threatScore =
			rawThreat *
			informationConfidence;

		enemy.isThreat =
			enemy.threatScore >= 25.0;
	}

	private boolean isEnemyAimingAtUs(
		EnemyData enemy
	) {

		double angleToUs =
			Math.atan2(
				getX() - enemy.x,
				getY() - enemy.y
			);

		double difference =
			Math.abs(
				Utils.normalRelativeAngle(
					angleToUs -
					enemy.heading
				)
			);

		return difference <
			Math.toRadians(15);
	}

	private List<EnemyData> getThreats() {

		List<EnemyData> threats =
			new ArrayList<>();

		for (
			EnemyData enemy :
			enemies.values()
		) {

			if (
				enemy.isThreat &&
				enemy.threatScore > 0
			) {

				threats.add(enemy);
			}
		}

		threats.sort(
			Comparator.comparingDouble(
				EnemyData::getThreatScore
			).reversed()
		);

		if (
			threats.size() >
			MAX_THREAT_COUNT
		) {

			return new ArrayList<>(
				threats.subList(
					0,
					MAX_THREAT_COUNT
				)
			);
		}

		return threats;
	}

	// =========================================================
	// Stage 6 - Danger Map + Escape Movement
	// =========================================================

	private void updateMovement() {

		List<EnemyData> threats =
			getThreats();

		if (threats.isEmpty()) {

			moveForwardSafely();

			return;
		}

		boolean escapeMode =
			threats.size() >= 3;

		if (hasRecentIncomingFire()) {

			double dodgeAngle =
				findAdaptiveDodgeDirection(
					threats
				);

			moveToAngle(dodgeAngle);

			return;
		}

		double safestAngle =
			findSafestDirection(
				threats,
				escapeMode
			);

		moveToAngle(safestAngle);
	}

	private double findSafestDirection(
		List<EnemyData> threats,
		boolean escapeMode
	) {

		double bestAngle =
			getHeadingRadians();

		double lowestDanger =
			Double.POSITIVE_INFINITY;

		for (
			int i = 0;
			i < DANGER_SAMPLES;
			i++
		) {

			double angle =
				getHeadingRadians() +
				(
					2.0 *
					Math.PI /
					DANGER_SAMPLES
				) *
				i;

			double danger =
				calculateDirectionDanger(
					angle,
					threats,
					escapeMode
				);

			if (
				danger <
				lowestDanger
			) {

				lowestDanger =
					danger;

				bestAngle =
					angle;
			}
		}

		return bestAngle;
	}

	private double calculateDirectionDanger(
		double angle,
		List<EnemyData> threats,
		boolean escapeMode
	) {

		double danger = 0;

		double futureX =
			getX() +
			Math.sin(angle) *
			MOVEMENT_DISTANCE;

		double futureY =
			getY() +
			Math.cos(angle) *
			MOVEMENT_DISTANCE;

		danger +=
			calculateWallDanger(
				futureX,
				futureY
			);

		for (
			EnemyData enemy :
			threats
		) {

			danger +=
				calculateEnemyDanger(
					enemy,
					futureX,
					futureY
				);
		}

		if (escapeMode) {

			double clusterDistance =
				calculateThreatClusterDistance(
					futureX,
					futureY,
					threats
				);

			danger -=
				Math.min(
					clusterDistance / 100.0,
					20.0
				);
		}

		return danger;
	}

	private double calculateEnemyDanger(
		EnemyData enemy,
		double futureX,
		double futureY
	) {

		double distance =
			Point2D.distance(
				futureX,
				futureY,
				enemy.x,
				enemy.y
			);

		if (
			distance >
			DANGER_SCAN_DISTANCE
		) {

			return 0;
		}

		double distanceDanger =
			Math.max(
				0,
				1.0 -
				distance /
				DANGER_SCAN_DISTANCE
			);

		distanceDanger *= 40.0;

		double angleToFuture =
			Math.atan2(
				futureX - enemy.x,
				futureY - enemy.y
			);

		double angleDifference =
			Math.abs(
				Utils.normalRelativeAngle(
					angleToFuture -
					enemy.heading
				)
			);

		double aimDanger = 0;

		double aimCone =
			Math.toRadians(35);

		if (
			angleDifference <
			aimCone
		) {

			double aimFactor =
				1.0 -
				angleDifference /
				aimCone;

			aimDanger =
				aimFactor *
				50.0;
		}

		double fireDanger = 0;

		if (enemy.fireDetected) {
			fireDanger = 40.0;
		}

		double energyDanger =
			Math.min(
				enemy.energy / 100.0,
				1.0
			) *
			10.0;

		return
			distanceDanger +
			aimDanger +
			fireDanger +
			energyDanger;
	}

	private double calculateWallDanger(
		double x,
		double y
	) {

		double width =
			getBattleFieldWidth();

		double height =
			getBattleFieldHeight();

		double danger = 0;

		if (x < WALL_MARGIN) {

			danger +=
				(WALL_MARGIN - x) *
				2.0;
		}

		if (
			x >
			width - WALL_MARGIN
		) {

			danger +=
				(
					x -
					(width - WALL_MARGIN)
				) *
				2.0;
		}

		if (y < WALL_MARGIN) {

			danger +=
				(WALL_MARGIN - y) *
				2.0;
		}

		if (
			y >
			height - WALL_MARGIN
		) {

			danger +=
				(
					y -
					(height - WALL_MARGIN)
				) *
				2.0;
		}

		return danger;
	}

	private double calculateThreatClusterDistance(
		double x,
		double y,
		List<EnemyData> threats
	) {

		if (threats.isEmpty()) {
			return 0;
		}

		double centerX = 0;
		double centerY = 0;

		for (
			EnemyData enemy :
			threats
		) {

			centerX += enemy.x;
			centerY += enemy.y;
		}

		centerX /=
			threats.size();

		centerY /=
			threats.size();

		return Point2D.distance(
			x,
			y,
			centerX,
			centerY
		);
	}

	// =========================================================
	// Stage 7 - Bullet / Fire Detection
	// =========================================================

	private void updateBulletDetection() {

		for (
			EnemyData enemy :
			enemies.values()
		) {

			if (enemy == null) {
				continue;
			}

			if (
				getTime() -
				enemy.fireDetectionTime >
				12
			) {

				enemy.fireDetected =
					false;

				enemy.detectedBulletPower =
					0;

				enemy.detectedBulletSpeed =
					0;
			}
		}
	}

	private void detectEnemyFire(
		EnemyData enemy
	) {

		if (!enemy.hasPreviousData()) {
			return;
		}

		double energyDrop =
			enemy.previousEnergy -
			enemy.energy;

		if (
			energyDrop >=
			MIN_BULLET_POWER &&
			energyDrop <=
			MAX_BULLET_POWER
		) {

			enemy.detectedBulletPower =
				energyDrop;

			enemy.fireDetected =
				true;

			enemy.fireDetectionTime =
				getTime();

			enemy.lastKnownFireX =
				enemy.x;

			enemy.lastKnownFireY =
				enemy.y;

			enemy.lastKnownFireHeading =
				enemy.heading;

			enemy.lastKnownFireDistance =
				enemy.distance;

			enemy.detectedBulletSpeed =
				20.0 -
				(
					3.0 *
					energyDrop
				);

			lastBulletDetection =
				getTime();
		}
	}

	private boolean hasRecentIncomingFire() {

		for (
			EnemyData enemy :
			enemies.values()
		) {

			if (
				enemy.fireDetected &&
				getTime() -
				enemy.fireDetectionTime <=
				12
			) {

				return true;
			}
		}

		return false;
	}

	// =========================================================
	// Stage 8 - Adaptive Dodging
	// =========================================================

	private double findAdaptiveDodgeDirection(
		List<EnemyData> threats
	) {

		double bestAngle =
			getHeadingRadians();

		double lowestDanger =
			Double.POSITIVE_INFINITY;

		for (
			int i = 0;
			i < DODGE_SAMPLES;
			i++
		) {

			double angle =
				getHeadingRadians() +
				(
					2.0 *
					Math.PI /
					DODGE_SAMPLES
				) *
				i;

			double danger =
				calculateDodgeDanger(
					angle,
					threats
				);

			if (
				danger <
				lowestDanger
			) {

				lowestDanger =
					danger;

				bestAngle =
					angle;
			}
		}

		if (
			Math.abs(
				Utils.normalRelativeAngle(
					bestAngle -
					previousMovementAngle
				)
			) <
			Math.toRadians(10)
		) {

			bestAngle =
				Utils.normalRelativeAngle(
					bestAngle +
					movementDirection *
					Math.toRadians(25)
				) +
				getHeadingRadians();
		}

		return bestAngle;
	}

	private double calculateDodgeDanger(
		double angle,
		List<EnemyData> threats
	) {

		double danger = 0;

		for (
			int step = 1;
			step <= 4;
			step++
		) {

			double distance =
				(
					DODGE_LOOKAHEAD /
					4.0
				) *
				step;

			double futureX =
				getX() +
				Math.sin(angle) *
				distance;

			double futureY =
				getY() +
				Math.cos(angle) *
				distance;

			danger +=
				calculateWallDanger(
					futureX,
					futureY
				) *
				2.0;

			for (
				EnemyData enemy :
				threats
			) {

				danger +=
					calculateBulletDanger(
						enemy,
						futureX,
						futureY,
						getTime() + step
					);

				danger +=
					calculateEnemyDanger(
						enemy,
						futureX,
						futureY
					) *
					0.35;
			}
		}

		danger +=
			calculateMovementPatternPenalty(
				angle
			);

		double turn =
			Math.abs(
				Utils.normalRelativeAngle(
					angle -
					getHeadingRadians()
				)
			);

		danger +=
			turn *
			4.0;

		return danger;
	}

	private double calculateBulletDanger(
		EnemyData enemy,
		double futureX,
		double futureY,
		long futureTime
	) {

		if (!enemy.fireDetected) {
			return 0;
		}

		long ticksSinceFire =
			futureTime -
			enemy.fireDetectionTime;

		if (ticksSinceFire < 0) {
			return 0;
		}

		double bulletSpeed =
			enemy.detectedBulletSpeed;

		if (bulletSpeed <= 0) {
			return 0;
		}

		double bulletDistance =
			bulletSpeed *
			ticksSinceFire;

		double bulletX =
			enemy.lastKnownFireX +
			Math.sin(
				enemy.lastKnownFireHeading
			) *
			bulletDistance;

		double bulletY =
			enemy.lastKnownFireY +
			Math.cos(
				enemy.lastKnownFireHeading
			) *
			bulletDistance;

		double distance =
			Point2D.distance(
				futureX,
				futureY,
				bulletX,
				bulletY
			);

		if (
			distance >
			BULLET_DANGER_RADIUS *
			3.0
		) {

			return 0;
		}

		double danger =
			1.0 -
			distance /
			(
				BULLET_DANGER_RADIUS *
				3.0
			);

		if (
			distance <=
			BULLET_DANGER_RADIUS
		) {

			danger *= 250.0;

		} else {

			danger *= 80.0;
		}

		return danger;
	}

	private double calculateMovementPatternPenalty(
		double angle
	) {

		double penalty = 0;

		double currentTurn =
			Math.abs(
				Utils.normalRelativeAngle(
					angle -
					getHeadingRadians()
				)
			);

		double previousTurn =
			Math.abs(
				Utils.normalRelativeAngle(
					previousMovementAngle -
					getHeadingRadians()
				)
			);

		if (
			Math.abs(
				currentTurn -
				previousTurn
			) <
			Math.toRadians(5)
		) {

			penalty +=
				MOVEMENT_PATTERN_PENALTY;
		}

		if (
			getTime() -
			lastDirectionChange <
			18
		) {

			if (
				Math.abs(
					Utils.normalRelativeAngle(
						angle -
						previousMovementAngle
					)
				) <
				Math.toRadians(20)
			) {

				penalty +=
					15.0;
			}
		}

		return penalty;
	}

	// =========================================================
	// Movement
	// =========================================================

	private void moveToAngle(
		double desiredAngle
	) {

		previousMovementAngle =
			lastMovementAngle;

		lastMovementAngle =
			desiredAngle;

		double turn =
			Utils.normalRelativeAngle(
				desiredAngle -
				getHeadingRadians()
			);

		if (
			Math.abs(turn) >
			Math.toRadians(90)
		) {

			movementDirection *= -1;

			lastDirectionChange =
				getTime();
		}

		setTurnRightRadians(turn);

		setAhead(100);
	}

	private void moveForwardSafely() {

		double futureX =
			getX() +
			Math.sin(
				getHeadingRadians()
			) *
			MOVEMENT_DISTANCE;

		double futureY =
			getY() +
			Math.cos(
				getHeadingRadians()
			) *
			MOVEMENT_DISTANCE;

		double danger =
			calculateWallDanger(
				futureX,
				futureY
			);

		if (danger > 20) {

			double centerAngle =
				Math.atan2(
					getBattleFieldWidth() /
						2.0 -
						getX(),

					getBattleFieldHeight() /
						2.0 -
						getY()
				);

			moveToAngle(
				centerAngle
			);

			return;
		}

		setAhead(100);
	}

	// =========================================================
	// Movement Reactions
	// =========================================================

	public void onHitByBullet(
		HitByBulletEvent e
	) {

		movementDirection *= -1;

		lastDirectionChange =
			getTime();

		updateThreatAssessment();

		List<EnemyData> threats =
			getThreats();

		if (!threats.isEmpty()) {

			double dodgeAngle =
				findAdaptiveDodgeDirection(
					threats
				);

			moveToAngle(
				dodgeAngle
			);

		} else {

			setAhead(-150);
		}
	}

	public void onHitWall(
		HitWallEvent e
	) {

		movementDirection *= -1;

		lastDirectionChange =
			getTime();

		updateThreatAssessment();

		List<EnemyData> threats =
			getThreats();

		if (!threats.isEmpty()) {

			double dodgeAngle =
				findAdaptiveDodgeDirection(
					threats
				);

			moveToAngle(
				dodgeAngle
			);

		} else {

			moveForwardSafely();
		}
	}

	// =========================================================
	// Enemy Tracking
	// =========================================================

	private static class EnemyData {

		String name;

		double x;
		double y;

		double previousX;
		double previousY;

		double heading;
		double previousHeading;

		double velocity;
		double previousVelocity;

		double energy;
		double previousEnergy;

		double distance;
		double absoluteBearing;

		long lastScanTime;

		int scanCount;

		double threatScore;
		boolean isThreat;

		boolean fireDetected;
		long fireDetectionTime;

		double detectedBulletPower;
		double detectedBulletSpeed;

		double lastKnownFireX;
		double lastKnownFireY;

		double lastKnownFireHeading;
		double lastKnownFireDistance;

		EnemyData(String name) {
			this.name = name;
		}

		void update(
			ScannedRobotEvent e,
			double ourX,
			double ourY,
			double ourHeading,
			long currentTime
		) {

			previousX = x;
			previousY = y;

			previousHeading = heading;
			previousVelocity = velocity;

			previousEnergy = energy;

			absoluteBearing =
				ourHeading +
				e.getBearingRadians();

			x =
				ourX +
				e.getDistance() *
				Math.sin(
					absoluteBearing
				);

			y =
				ourY +
				e.getDistance() *
				Math.cos(
					absoluteBearing
				);

			heading =
				e.getHeadingRadians();

			velocity =
				e.getVelocity();

			energy =
				e.getEnergy();

			distance =
				e.getDistance();

			lastScanTime =
				currentTime;

			scanCount++;
		}

		boolean hasPreviousData() {
			return scanCount > 1;
		}

		double getVelocityChange() {

			if (!hasPreviousData()) {
				return 0;
			}

			return velocity -
				previousVelocity;
		}

		double getHeadingChange() {

			if (!hasPreviousData()) {
				return 0;
			}

			return Utils.normalRelativeAngle(
				heading -
				previousHeading
			);
		}

		double getMovementDistance() {

			if (!hasPreviousData()) {
				return 0;
			}

			return Point2D.distance(
				previousX,
				previousY,
				x,
				y
			);
		}

		double getEnergyDrop() {

			if (!hasPreviousData()) {
				return 0;
			}

			return Math.max(
				0,
				previousEnergy -
				energy
			);
		}

		double getThreatScore() {
			return threatScore;
		}
	}

	// =========================================================
	// Stage 10 - Statistical Data
	// =========================================================

	private static class StatisticalData {

		double[] lateralBins =
			new double[MAX_STATISTICAL_BINS];

		double[] velocityBins =
			new double[MAX_STATISTICAL_BINS];

		double[] accelerationBins =
			new double[MAX_STATISTICAL_BINS];

		double totalSamples;

		double leftSamples;
		double rightSamples;
		double forwardSamples;

		double stationarySamples;

		double previousLateral;
		boolean hasPreviousLateral;

		int directionChanges;
	}

	// =========================================================
	// Adaptive Bullet Power
	// =========================================================

	private static class BulletPower {

		static double calculate(
			double distance,
			double ourEnergy,
			double enemyEnergy
		) {

			double power;

			if (distance < 100) {
				power = 3.0;
			}
			else if (distance < 200) {
				power = 2.5;
			}
			else if (distance < 350) {
				power = 2.0;
			}
			else if (distance < 500) {
				power = 1.5;
			}
			else {
				power = 1.0;
			}

			if (ourEnergy < 20) {
				power =
					Math.min(
						power,
						1.5
					);
			}

			if (ourEnergy < 10) {
				power =
					Math.min(
						power,
						1.0
					);
			}

			if (
				enemyEnergy > 0 &&
				enemyEnergy < power
			) {

				power =
					enemyEnergy;
			}

			return Math.max(
				0.5,
				Math.min(
					3.0,
					power
				)
			);
		}
	}

	// =========================================================
	// Wall-aware Prediction
	// =========================================================

	private static class WallPrediction {

		private static final double WALL_MARGIN = 18.0;
		private static final int MAX_STEPS = 1000;

		public static Point2D.Double predict(
			double startX,
			double startY,
			double heading,
			double velocity,
			double time,
			double battlefieldWidth,
			double battlefieldHeight
		) {

			double predictedX =
				startX;

			double predictedY =
				startY;

			double predictedHeading =
				heading;

			for (
				int step = 0;
				step < MAX_STEPS &&
				step < time;
				step++
			) {

				predictedX +=
					velocity *
					Math.sin(
						predictedHeading
					);

				predictedY +=
					velocity *
					Math.cos(
						predictedHeading
					);

				if (
					predictedX <
						WALL_MARGIN ||
					predictedX >
						battlefieldWidth -
						WALL_MARGIN
				) {

					predictedHeading =
						-predictedHeading;
				}

				if (
					predictedY <
						WALL_MARGIN ||
					predictedY >
						battlefieldHeight -
						WALL_MARGIN
				) {

					predictedHeading =
						Math.PI -
						predictedHeading;
				}

				predictedX =
					Math.max(
						WALL_MARGIN,
						Math.min(
							battlefieldWidth -
								WALL_MARGIN,
							predictedX
						)
					);

				predictedY =
					Math.max(
						WALL_MARGIN,
						Math.min(
							battlefieldHeight -
								WALL_MARGIN,
							predictedY
						)
					);
			}

			return new Point2D.Double(
				predictedX,
				predictedY
			);
		}
	}

	// =========================================================
	// Cleanup
	// =========================================================

	public void onRobotDeath(
		RobotDeathEvent e
	) {

		enemies.remove(
			e.getName()
		);

		statisticalData.remove(
			e.getName()
		);

		if (
			radarTarget != null &&
			radarTarget.equals(
				e.getName()
			)
		) {

			radarTarget = null;
			lastRadarScan = 0;
		}
	}
}