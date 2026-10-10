package TriStateRobots;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.io.*;

/**
 * MadaraV3 (ex-MadaraV1) - DangerBasedBot.
 *
 * Actual STATUS: MegaBot 
 * 
 * Actual systems:
 *  - Melee radar: narrow lock on the gun target + periodic full sweeps for stale enemies.
 *  - Targeting: segmented GuessFactor (distance x lateral speed) learned from real AND virtual
 *    waves, persisted across rounds (static), with a wall-aware linear fallback.
 *  - Movement: danger map (walls, enemy proximity, collisions) + dodging of tracked enemy
 *    bullets projected on their real path (towards our position at fire time).
 *  - Shot detection: energy drop compensated for known sources (our hits, collisions, hits on us).
 *
 **/

public class MadaraV3 extends AdvancedRobot {

	// =====================================================================
	// Constants
	// =====================================================================

	private static final double TWO_PI = Math.PI * 2.0;
	private static final double HALF_PI = Math.PI / 2.0;
	private static final double ROBOT_RADIUS = 18.0;
	private static final double MAX_SPEED = 8.0;

	//Colors
    private static final Color VERMELHO_ARMADURA = new Color(139, 0, 0);
	private static final Color PRETO_UCHIHA = new Color(25, 25, 25);
	private static final Color AZUL_SUSANOO = new Color(30, 60, 255);
	
	private static final Color ROXO_RINNEGAN_DARK = new Color(45, 0, 70);
	private static final Color ROXO_RINNEGAN = new Color(100, 0, 150);
	private static final Color ROXO_RINNEGAN_LIGHT = new Color(155, 40, 220);
	private static final double DODGE_COLOR_ANGLE = Math.toRadians(3.0);
	private static final long DODGE_COLOR_TICKS = 8;

	// Tracking / threat
	private static final int MAX_TRACKING_TIME = 40;
	private static final int MAX_THREATS = 5;
	private static final int TARGET_FRESH_TICKS = 12;
	private static final int RECENT_FIRE_TICKS = 20;

	// Radar
	private static final int RADAR_LOST_TICKS = 3;
	private static final int RADAR_STALE_TICKS = 16;
	private static final int RADAR_SWEEP_TICKS = 9;
	private static final int RADAR_SWEEP_COOLDOWN = 20;
	private static final double RADAR_MIN_OVERSHOOT = Math.toRadians(3.0);
	private static final double RADAR_OVERSHOOT_RATIO = 0.15;

	// Movement
	private static final int DANGER_SAMPLES = 24;
	private static final double MOVE_DISTANCE = 120.0;
	private static final double DANGER_SCAN_DISTANCE = 700.0;
	private static final double COLLISION_RADIUS = 80.0;
	private static final double WALL_MARGIN = 70.0;
	private static final double WALL_HARD_MARGIN = 28.0;
	private static final double TURN_COST = 6.0;
	private static final double HYSTERESIS_COST = 4.0;
	private static final double EVADE_RADIUS = 120.0;
	private static final long EVADE_TICKS = 10;
	private static final double RAM_FINISH_ENERGY = 15.0;

	// Enemy shots
	private static final int MAX_SHOTS = 32;
	private static final int DODGE_TICKS = 24;
	private static final int MAX_SHOT_SCAN_GAP = 3;
	private static final double SHOT_HIT_RADIUS = 26.0;
	private static final double SHOT_NEAR_RADIUS = 70.0;
	private static final double SHOT_DODGE_DEBUG_THRESHOLD = 20.0;
	private static final int MAX_ENEMY_WAVES = 64;
	private static final int SURF_BINS = 47;
	private static final int SURF_PREDICT_TICKS = 80;
	private static final double SURF_WAVE_MARGIN = 45.0;
	private static final double SURF_BASE_DANGER = 35.0;
	private static final double SURF_HIT_WEIGHT = 3.0;
	private static final double SHOT_EXPIRE_MARGIN = 40.0;
	private static final double MIN_BULLET_POWER = 0.1;
	private static final double MAX_BULLET_POWER = 3.0;
	private static final double ENERGY_EPS = 1e-6;

	// GuessFactor targeting
	private static final int GF_BINS = 31;
	private static final int GF_CENTER = GF_BINS / 2;
	private static final int SEGMENTS = 9;
	private static final double MIN_SEGMENT_SAMPLES = 10.0;
	private static final double MIN_TOTAL_SAMPLES = 6.0;
	private static final double[] KERNEL = {1.0, 0.6, 0.25};
	private static final double REAL_WAVE_WEIGHT = 2.0;
	private static final double VIRTUAL_WAVE_WEIGHT = 1.0;
	private static final long VIRTUAL_WAVE_INTERVAL = 2;
	private static final int MAX_WAVES = 128;
	private static final long WAVE_MAX_AGE = 130;
	private static final double WAVE_FRESH_TICKS = 2.5;
	private static final int LINEAR_MAX_TICKS = 150;

	// =====================================================================
	// State
	// =====================================================================

	/** Survives across rounds (robot instances are recreated every round). */
	private static final Map<String, GuessFactorStats> STATS = new HashMap<>();
	private static boolean learningLoaded;
	private static long roundsPlayed, roundsWon, bulletsFired, bulletsHit;
	private static double damageReceived;
	private static double adaptiveMoveDistance = MOVE_DISTANCE;
	private static double adaptiveSurfWeight = 1.0;
	private static final String LEARNING_FILE = "madara-v3-learning.dat";
	private long lastLearningSaveTime = -100;

	private final Map<String, EnemyData> enemies = new HashMap<>();
	private final ArrayList<EnemyData> enemyList = new ArrayList<>();
	private final EnemyData[] threatBuf = new EnemyData[MAX_THREATS];
	private int threatCount;

	private final EnemyShot[] shots = new EnemyShot[MAX_SHOTS];
	private final EnemyWave[] enemyWaves = new EnemyWave[MAX_ENEMY_WAVES];
	private int enemyWaveCount;
	private int shotCursor;
	private int activeShots;

	private final Wave[] waves = new Wave[MAX_WAVES];
	private int waveCount;

	private final double[] travelDist = new double[DODGE_TICKS + 1];

	private EnemyData gunTarget;

	private long sweepEnd;
	private long nextSweepAllowed;
	private double radarSpinDir = 1.0;

	private double fieldW;
	private double fieldH;

	private long now;
	private double myX;
	private double myY;
	private double myHeading;
	private double myVelocity;

	private double lastMoveAngle;
	private double clusterX;
	private double clusterY;

	private double evadeX;
	private double evadeY;
	private long evadeUntil;
	private long dodgeColorUntil;

	{
		for (int i = 0; i < MAX_SHOTS; i++) shots[i] = new EnemyShot();
		for (int i = 0; i < MAX_ENEMY_WAVES; i++) enemyWaves[i] = new EnemyWave();
		for (int i = 0; i < MAX_WAVES; i++) {
			waves[i] = new Wave();
		}
	}

	// =====================================================================
	// Main loop
	// =====================================================================

	@Override
	public void run() {

		// Set body, gun, radar, bullet, and scan arc colors

		setColors(
        	VERMELHO_ARMADURA,
        	PRETO_UCHIHA,
        	PRETO_UCHIHA,
        	AZUL_SUSANOO,
        	VERMELHO_ARMADURA
		);


		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		fieldW = getBattleFieldWidth();
		fieldH = getBattleFieldHeight();
		loadLearningData();

		// Experiment: disable adaptive movement recalibration to isolate movement quality.
		roundsPlayed++;
		adaptiveMoveDistance = MOVE_DISTANCE;
		adaptiveSurfWeight = 1.0;
		saveLearningData(false);

		syncState();
		lastMoveAngle = myHeading;

		while (true) {

			syncState();
			refreshState();
			updateRadar();
			updateMovement();

			execute();

			// Persist learning periodically in case the robot is destroyed before round end.
			if (now - lastLearningSaveTime >= 100) {
				saveLearningData(false);
				lastLearningSaveTime = now;
			}
		}
	}

	private void syncState() {

		now = getTime();
		myX = getX();
		myY = getY();
		myHeading = getHeadingRadians();
		myVelocity = getVelocity();
	}

	/** Once per tick: threats, expiry of shots/waves, target selection. */
	private void refreshState() {

		int n = enemyList.size();

		threatCount = 0;

		for (int i = 0; i < n; i++) {

			EnemyData en = enemyList.get(i);

			updateThreat(en);

			if (en.threat <= 0) {
				continue;
			}

			int pos = threatCount;

			if (threatCount < MAX_THREATS) {
				threatCount++;
			} else if (en.threat <= threatBuf[MAX_THREATS - 1].threat) {
				continue;
			} else {
				pos = MAX_THREATS - 1;
			}

			while (pos > 0 && threatBuf[pos - 1].threat < en.threat) {
				threatBuf[pos] = threatBuf[pos - 1];
				pos--;
			}

			threatBuf[pos] = en;
		}

		if (threatCount > 0) {

			double sx = 0;
			double sy = 0;

			for (int i = 0; i < threatCount; i++) {
				sx += threatBuf[i].x;
				sy += threatBuf[i].y;
			}

			clusterX = sx / threatCount;
			clusterY = sy / threatCount;
		}

		expireShots();
		expireEnemyWaves();
		expireWaves();
		selectGunTarget();

		if (activeShots > 0) {
			buildTravelTable();
		}
	}

	// =====================================================================
	// Scanning
	// =====================================================================

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {

		syncState();

		String name = e.getName();

		EnemyData en = enemies.get(name);

		if (en == null) {

			en = new EnemyData(name);

			enemies.put(name, en);
			enemyList.add(en);

			if (!STATS.containsKey(name)) {
				STATS.put(name, new GuessFactorStats());
			}
		}

		double absBearing = myHeading + e.getBearingRadians();

		en.update(
			e.getDistance(),
			absBearing,
			myX,
			myY,
			e.getHeadingRadians(),
			e.getVelocity(),
			e.getEnergy(),
			now
		);

		detectEnemyShot(en);
		processWaves(en);
		updateThreat(en);
		selectGunTarget();

		if (en == gunTarget) {
			aimAndFire(en);
		}
	}

	private double distTo(EnemyData en) {

		double dx = en.x - myX;
		double dy = en.y - myY;

		return Math.sqrt(dx * dx + dy * dy);
	}

	private void selectGunTarget() {

		EnemyData best = null;
		double bestDist = Double.POSITIVE_INFINITY;

		for (int i = 0, n = enemyList.size(); i < n; i++) {

			EnemyData en = enemyList.get(i);

			if (now - en.lastScanTime > TARGET_FRESH_TICKS) {
				continue;
			}

			double d = distTo(en);

			if (d < bestDist) {
				bestDist = d;
				best = en;
			}
		}

		if (best == null) {
			gunTarget = null;
			return;
		}

		if (
			gunTarget == null ||
			now - gunTarget.lastScanTime > TARGET_FRESH_TICKS ||
			bestDist < distTo(gunTarget) * 0.75
		) {
			gunTarget = best;
		}
	}

	private void updateThreat(EnemyData en) {

		long age = now - en.lastScanTime;

		if (age > MAX_TRACKING_TIME) {
			en.threat = 0;
			return;
		}

		double distanceScore =
			(1.0 - Math.min(distTo(en) / 800.0, 1.0)) * 45.0;

		double energyScore =
			Math.min(en.energy / 100.0, 1.0) * 10.0;

		double movementScore =
			Math.min(Math.abs(en.velocity) / MAX_SPEED, 1.0) * 5.0;

		double fireScore =
			(now - en.lastFireTime <= RECENT_FIRE_TICKS) ? 30.0 : 0.0;

		double confidence =
			1.0 - Math.min(age / (double) MAX_TRACKING_TIME, 1.0);

		en.threat =
			(distanceScore + energyScore + movementScore + fireScore) *
			confidence;
	}

	// =====================================================================
	// Radar
	// =====================================================================

	private void updateRadar() {

		int others = getOthers();

		if (others > 1 && now >= nextSweepAllowed) {

			if (enemyList.size() < others) {

				startSweep();

			} else {

				EnemyData oldest = null;
				long oldestAge = -1;

				for (int i = 0, n = enemyList.size(); i < n; i++) {

					EnemyData en = enemyList.get(i);

					if (en == gunTarget) {
						continue;
					}

					long age = now - en.lastScanTime;

					if (age > oldestAge) {
						oldestAge = age;
						oldest = en;
					}
				}

				if (oldest != null && oldestAge > RADAR_STALE_TICKS) {

					double toOldest =
						Utils.normalRelativeAngle(
							Math.atan2(oldest.x - myX, oldest.y - myY) -
							getRadarHeadingRadians()
						);

					radarSpinDir = toOldest >= 0 ? 1.0 : -1.0;

					startSweep();
				}
			}
		}

		boolean lost =
			gunTarget == null ||
			now - gunTarget.lastScanTime > RADAR_LOST_TICKS;

		if (lost || now < sweepEnd) {
			setTurnRadarRightRadians(radarSpinDir * TWO_PI);
			return;
		}

		double absBearing =
			Math.atan2(gunTarget.x - myX, gunTarget.y - myY);

		double turn =
			Utils.normalRelativeAngle(
				absBearing - getRadarHeadingRadians()
			);

		double overshoot =
			Math.copySign(
				Math.max(
					RADAR_MIN_OVERSHOOT,
					Math.abs(turn) * RADAR_OVERSHOOT_RATIO
				),
				turn
			);

		setTurnRadarRightRadians(turn + overshoot);
	}

	private void startSweep() {

		sweepEnd = now + RADAR_SWEEP_TICKS;
		nextSweepAllowed = sweepEnd + RADAR_SWEEP_COOLDOWN;
	}

	// =====================================================================
	// Targeting (GuessFactor)
	// =====================================================================

	private void aimAndFire(EnemyData en) {

		double dx = en.x - myX;
		double dy = en.y - myY;
		double dist = Math.sqrt(dx * dx + dy * dy);

		double power = chooseBulletPower(dist, en.energy);
		double speed = 20.0 - 3.0 * power;

		double absBearing = Math.atan2(dx, dy);

		double lateralVelocity =
			en.velocity * Math.sin(en.heading - absBearing);

		int direction = lateralVelocity >= 0 ? 1 : -1;
		int segment = segmentOf(dist, lateralVelocity);

		GuessFactorStats stats = STATS.get(en.name);
		double[] bins = stats == null ? null : stats.select(segment);

		double aimAngle;

		if (bins != null) {

			double guessFactor = binToGuessFactor(bestBin(bins));

			aimAngle =
				absBearing +
				guessFactor * direction * maxEscapeAngle(speed);

		} else {

			aimAngle = linearAimAngle(en, speed);
		}

		double gunTurn =
			Utils.normalRelativeAngle(aimAngle - getGunHeadingRadians());

		setTurnGunRightRadians(gunTurn);

		boolean fired = false;

		if (
			getGunHeat() <= 0.0 &&
			getEnergy() > power &&
			Math.abs(gunTurn) <= aimTolerance(dist)
		) {

			if (setFireBullet(power) != null) {

				bulletsFired++;
				addWave(en, power, absBearing, segment, direction, true);

				fired = true;
			}
		}

		if (!fired && now - en.lastWaveTime >= VIRTUAL_WAVE_INTERVAL) {

			addWave(en, power, absBearing, segment, direction, false);
		}
	}

	private double aimTolerance(double dist) {

		return Math.min(
			Math.toRadians(4.0),
			Math.max(
				Math.toRadians(0.4),
				Math.atan2(ROBOT_RADIUS * 0.8, dist)
			)
		);
	}

	private double chooseBulletPower(double dist, double enemyEnergy) {

		double power;

		if (dist < 100) {
			power = 3.0;
		} else if (dist < 200) {
			power = 2.5;
		} else if (dist < 350) {
			power = 2.0;
		} else if (dist < 500) {
			power = 1.5;
		} else {
			power = 1.0;
		}

		double energy = getEnergy();

		if (energy < 20) {
			power = Math.min(power, 1.5);
		}

		if (energy < 10) {
			power = Math.min(power, 1.0);
		}

		if (energy < 4) {
			power = Math.min(power, 0.5);
		}

		/*
		 * Kill shot: exact power needed to finish the enemy.
		 * damage = 4p (p <= 1) or 6p - 2 (p > 1).
		 */
		double needed =
			enemyEnergy <= 4.0
				? enemyEnergy / 4.0
				: (enemyEnergy + 2.0) / 6.0;

		if (needed < power) {
			power = needed;
		}

		power = Math.min(power, energy);

		return Math.max(MIN_BULLET_POWER, Math.min(MAX_BULLET_POWER, power));
	}

	/** Iterative-free linear prediction; the enemy stops at the walls. */
	private double linearAimAngle(EnemyData en, double bulletSpeed) {
		double ex = en.x;
		double ey = en.y;
		double vx = Math.sin(en.heading) * en.velocity;
		double vy = Math.cos(en.heading) * en.velocity;
		double time = Math.hypot(ex - myX, ey - myY) / bulletSpeed;
		for (int i = 0; i < 10; i++) {
			ex = en.x + vx * time;
			ey = en.y + vy * time;
			ex = Math.max(ROBOT_RADIUS, Math.min(fieldW - ROBOT_RADIUS, ex));
			ey = Math.max(ROBOT_RADIUS, Math.min(fieldH - ROBOT_RADIUS, ey));
			time = Math.hypot(ex - myX, ey - myY) / bulletSpeed;
		}
		return Math.atan2(ex - myX, ey - myY);
	}

	private static double maxEscapeAngle(double bulletSpeed) {

		return Math.asin(Math.min(1.0, MAX_SPEED / bulletSpeed));
	}

	private static int segmentOf(double dist, double lateralVelocity) {

		int d = dist < 250 ? 0 : (dist < 500 ? 1 : 2);

		double lv = Math.abs(lateralVelocity);
		int v = lv < 1.0 ? 0 : (lv < 5.0 ? 1 : 2);

		return d * 3 + v;
	}

	private static int bestBin(double[] bins) {

		int best = GF_CENTER;
		double bestValue = bins[GF_CENTER];

		for (int i = 0; i < GF_BINS; i++) {

			if (bins[i] > bestValue + 1e-9) {
				bestValue = bins[i];
				best = i;
			}
		}

		return best;
	}

	private static int guessFactorToBin(double guessFactor) {

		int bin =
			(int) Math.round(
				(guessFactor + 1.0) * 0.5 * (GF_BINS - 1)
			);

		return Math.max(0, Math.min(GF_BINS - 1, bin));
	}

	private static double binToGuessFactor(int bin) {

		return bin / (double) (GF_BINS - 1) * 2.0 - 1.0;
	}

	// ---------------------------------------------------------------------
	// Waves (our bullets, real and virtual)
	// ---------------------------------------------------------------------

	private void addWave(
		EnemyData en,
		double power,
		double absBearing,
		int segment,
		int direction,
		boolean real
	) {

		int idx;

		if (waveCount < MAX_WAVES) {

			idx = waveCount++;

		} else if (real) {

			idx = -1;

			for (int i = 0; i < waveCount; i++) {
				if (!waves[i].real) {
					idx = i;
					break;
				}
			}

			if (idx < 0) {
				return;
			}

		} else {

			return;
		}

		double speed = 20.0 - 3.0 * power;

		Wave w = waves[idx];

		w.ox = myX;
		w.oy = myY;
		w.fireTime = now;
		w.speed = speed;
		w.directAngle = absBearing;
		w.maxEscape = maxEscapeAngle(speed);
		w.target = en;
		w.direction = direction;
		w.segment = segment;
		w.real = real;
		w.weight = real ? REAL_WAVE_WEIGHT : VIRTUAL_WAVE_WEIGHT;

		en.lastWaveTime = now;
	}

	private void removeWave(int index) {

		Wave removed = waves[index];

		waves[index] = waves[--waveCount];
		waves[waveCount] = removed;

		removed.target = null;
	}

	/** Learn from every wave that reached the freshly scanned enemy. */
	private void processWaves(EnemyData en) {

		int i = 0;

		while (i < waveCount) {

			Wave w = waves[i];

			if (w.target != en) {
				i++;
				continue;
			}

			double traveled = (now - w.fireTime) * w.speed;

			double dx = en.x - w.ox;
			double dy = en.y - w.oy;

			double dist = Math.sqrt(dx * dx + dy * dy);

			if (traveled >= dist) {

				/*
				 * Only learn when the crossing just happened;
				 * otherwise the enemy position is too old.
				 */
				if (traveled - dist <= w.speed * WAVE_FRESH_TICKS) {
					learn(w, en, Math.atan2(dx, dy));
				}

				removeWave(i);

			} else {

				i++;
			}
		}
	}

	private void learn(Wave w, EnemyData en, double bearingFromOrigin) {

		GuessFactorStats stats = STATS.get(en.name);

		if (stats == null) {
			return;
		}

		double offset =
			Utils.normalRelativeAngle(bearingFromOrigin - w.directAngle);

		double guessFactor =
			Math.max(
				-1.0,
				Math.min(
					1.0,
					offset / w.maxEscape * w.direction
				)
			);

		int learnedBin = guessFactorToBin(guessFactor);
		stats.add(w.segment, learnedBin, w.weight);
		out.println("[MADARA-LEARNING] GF update | enemy=" + en.name
			+ " | segment=" + w.segment
			+ " | GF=" + fmt(guessFactor)
			+ " | bin=" + learnedBin
			+ " | weight=" + fmt(w.weight)
			+ " | samples=" + fmt(stats.allTotal));
	}

	private void expireWaves() {

		int i = 0;

		while (i < waveCount) {

			if (now - waves[i].fireTime > WAVE_MAX_AGE) {
				removeWave(i);
			} else {
				i++;
			}
		}
	}

	// =====================================================================
	// Enemy shot detection
	// =====================================================================

	private void detectEnemyShot(EnemyData en) {

		double adjust = en.energyAdjust;
		en.energyAdjust = 0;

		if (en.scanCount < 2) {
			return;
		}

		if (now - en.prevScanTime > MAX_SHOT_SCAN_GAP) {
			return;
		}

		double drop = en.prevEnergy - en.energy - adjust;

		if (
			drop < MIN_BULLET_POWER - ENERGY_EPS ||
			drop > MAX_BULLET_POWER + ENERGY_EPS
		) {
			return;
		}

		createShot(en, drop);
		createEnemyWave(en, drop);
	}

	private void createShot(EnemyData en, double power) {

		double dx = myX - en.x;
		double dy = myY - en.y;

		double len = Math.sqrt(dx * dx + dy * dy);

		if (len < 1.0) {
			return;
		}

		EnemyShot s = null;

		for (int i = 0; i < MAX_SHOTS; i++) {
			if (!shots[i].active) {
				s = shots[i];
				break;
			}
		}

		if (s == null) {
			s = shots[shotCursor];
			shotCursor = (shotCursor + 1) % MAX_SHOTS;
		}

		s.active = true;
		s.owner = en;
		s.power = power;
		s.speed = 20.0 - 3.0 * power;
		s.fireTime = now - 1;
		s.ox = en.x;
		s.oy = en.y;
		s.dx = dx / len;
		s.dy = dy / len;

		en.lastShot = s;
		en.lastFireTime = now;
	}

	private void createEnemyWave(EnemyData en, double power) {
		if (enemyWaveCount >= MAX_ENEMY_WAVES) removeEnemyWave(0);
		EnemyWave w = enemyWaves[enemyWaveCount++];
		w.active = true; w.owner = en; w.ox = en.x; w.oy = en.y;
		w.fireTime = now - 1; w.speed = 20.0 - 3.0 * power; w.power = power;
		w.directAngle = Math.atan2(myX - en.x, myY - en.y);
		double lateral = en.velocity * Math.sin(en.heading - w.directAngle);
		w.direction = lateral >= 0 ? 1 : -1;
	}

	private void removeEnemyWave(int index) {
		if (index < 0 || index >= enemyWaveCount) return;
		EnemyWave removed = enemyWaves[index];
		for (int i = index; i < enemyWaveCount - 1; i++) enemyWaves[i] = enemyWaves[i + 1];
		enemyWaves[enemyWaveCount - 1] = removed;
		removed.active = false; enemyWaveCount--;
	}

	private void expireEnemyWaves() {
		for (int i = enemyWaveCount - 1; i >= 0; i--) {
			EnemyWave w = enemyWaves[i];
			double radius = (now - w.fireTime) * w.speed;
			if (radius > Math.hypot(myX - w.ox, myY - w.oy) + SURF_WAVE_MARGIN) removeEnemyWave(i);
		}
	}

	private void learnEnemyWave(EnemyData en) {
		EnemyWave best = null; double bestError = Double.POSITIVE_INFINITY;
		for (int i = 0; i < enemyWaveCount; i++) {
			EnemyWave w = enemyWaves[i]; if (!w.active || w.owner != en) continue;
			double radius = (now - w.fireTime) * w.speed;
			double error = Math.abs(radius - Math.hypot(myX - w.ox, myY - w.oy));
			if (error < bestError) { bestError = error; best = w; }
		}
		if (best == null || bestError > 65.0) return;
		double hitAngle = Math.atan2(myX - best.ox, myY - best.oy);
		double offset = Utils.normalRelativeAngle(hitAngle - best.directAngle);
		double maxEscape = Math.asin(Math.min(1.0, MAX_SPEED / best.speed));
		double gf = Math.max(-1.0, Math.min(1.0, offset / maxEscape)) * best.direction;
		int bin = (int)Math.round((gf + 1.0) * 0.5 * (SURF_BINS - 1));
		bin = Math.max(0, Math.min(SURF_BINS - 1, bin));
		for (int i = 0; i < SURF_BINS; i++) {
			double d = i - bin; best.owner.surfStats[i] += SURF_HIT_WEIGHT / (d * d + 1.0);
		}
		best.owner.surfSamples += SURF_HIT_WEIGHT;
		for (int i = enemyWaveCount - 1; i >= 0; i--) if (enemyWaves[i] == best) removeEnemyWave(i);
	}

	private double enemyWaveDanger(double angle) {
		double total = 0.0;
		for (int i = 0; i < enemyWaveCount; i++) {
			EnemyWave w = enemyWaves[i]; if (!w.active) continue;
			double radius = (now - w.fireTime) * w.speed;
			double remaining = Math.hypot(myX - w.ox, myY - w.oy) - radius;
			if (remaining < -SURF_WAVE_MARGIN || remaining > w.speed * SURF_PREDICT_TICKS) continue;
			int ticks = Math.max(1, Math.min(SURF_PREDICT_TICKS, (int)Math.ceil(Math.max(0.0, remaining) / w.speed)));
			double travel = predictedTravelDistance(ticks);
			double px = myX + Math.sin(angle) * travel;
			double py = myY + Math.cos(angle) * travel;
			double radialError = Math.abs(Math.hypot(px - w.ox, py - w.oy) - (radius + ticks * w.speed));
			if (radialError > SURF_WAVE_MARGIN) continue;
			double hitAngle = Math.atan2(px - w.ox, py - w.oy);
			double offset = Utils.normalRelativeAngle(hitAngle - w.directAngle);
			double maxEscape = Math.asin(Math.min(1.0, MAX_SPEED / w.speed));
			double gf = Math.max(-1.0, Math.min(1.0, offset / maxEscape)) * w.direction;
			int bin = (int)Math.round((gf + 1.0) * 0.5 * (SURF_BINS - 1));
			bin = Math.max(0, Math.min(SURF_BINS - 1, bin));
			double proximity = 1.0 - radialError / SURF_WAVE_MARGIN;
			double confidence = Math.min(1.0, w.owner.surfSamples / 12.0);
			total += adaptiveSurfWeight * proximity * (SURF_BASE_DANGER + w.owner.surfStats[bin] * (0.5 + confidence));
		}
		return total;
	}

	private double predictedTravelDistance(int ticks) {
		double velocity = Math.abs(myVelocity), total = 0.0;
		for (int i = 0; i < ticks; i++) { velocity = Math.min(MAX_SPEED, velocity + 1.0); total += velocity; }
		return total;
	}

	private void expireShots() {

		activeShots = 0;

		for (int i = 0; i < MAX_SHOTS; i++) {

			EnemyShot s = shots[i];

			if (!s.active) {
				continue;
			}

			double traveled = (now - s.fireTime) * s.speed;

			double dx = myX - s.ox;
			double dy = myY - s.oy;

			double dist = Math.sqrt(dx * dx + dy * dy);

			if (traveled > dist + SHOT_EXPIRE_MARGIN) {
				s.active = false;
				continue;
			}

			activeShots++;
		}
	}

	/** Cancels the shot detected this very tick if it was really a known energy loss. */
	private boolean cancelRecentShot(EnemyData en, double amount) {

		EnemyShot s = en.lastShot;

		if (
			s != null &&
			s.active &&
			s.fireTime >= now - 2 &&
			Math.abs(s.power - amount) < 0.12
		) {
			s.active = false;
			return true;
		}

		return false;
	}

	/** Removes the in-flight shot that just hit us. */
	private void consumeShot(EnemyData en, double power) {

		EnemyShot best = null;
		double bestTraveled = -1;

		for (int i = 0; i < MAX_SHOTS; i++) {

			EnemyShot s = shots[i];

			if (
				!s.active ||
				s.owner != en ||
				Math.abs(s.power - power) >= 0.15
			) {
				continue;
			}

			double traveled = (now - s.fireTime) * s.speed;

			if (traveled > bestTraveled) {
				bestTraveled = traveled;
				best = s;
			}
		}

		if (best != null) {
			best.active = false;
		}
	}

	private void buildTravelTable() {

		double v = Math.abs(myVelocity);
		double total = 0;

		for (int t = 1; t <= DODGE_TICKS; t++) {

			v = Math.min(MAX_SPEED, v + 1.0);
			total += v;

			travelDist[t] = total;
		}
	}

	// =====================================================================
	// Movement
	// =====================================================================

	private void updateMovement() {

		double step = TWO_PI / DANGER_SAMPLES;

		double bestAngle = myHeading;
		double lowest = Double.POSITIVE_INFINITY;

		double bestNoShotAngle = myHeading;
		double lowestNoShot = Double.POSITIVE_INFINITY;
		double highestShotDanger = 0.0;

		for (int i = 0; i < DANGER_SAMPLES; i++) {

			double angle = myHeading + step * i;

			double danger = evaluate(angle, true);

			if (activeShots > 0) {
				double currentShotDanger = shotDanger(Math.sin(angle), Math.cos(angle));
				if (currentShotDanger > highestShotDanger) {
					highestShotDanger = currentShotDanger;
				}
			}

			if (danger < lowest) {
				lowest = danger;
				bestAngle = angle;
			}

			if (activeShots > 0) {
				double dangerWithoutShots = evaluate(angle, false);

				if (dangerWithoutShots < lowestNoShot) {
					lowestNoShot = dangerWithoutShots;
					bestNoShotAngle = angle;
				}
			}
		}

		double angleDifference = Math.abs(
			Utils.normalRelativeAngle(bestAngle - bestNoShotAngle)
		);

		boolean shotForcingMovement =
			activeShots > 0 && angleDifference >= DODGE_COLOR_ANGLE;

		boolean shotCloseEnoughToMatter =
			activeShots > 0 && highestShotDanger >= SHOT_DODGE_DEBUG_THRESHOLD;

		if (shotForcingMovement || shotCloseEnoughToMatter) {
			dodgeColorUntil = now + DODGE_COLOR_TICKS;
		}

		if (now < dodgeColorUntil) {
			setDodgeColors();
		} else {
			setNormalColors();
		}

		driveTo(bestAngle);
	}

	private void setNormalColors() {

		setColors(
			VERMELHO_ARMADURA,
			PRETO_UCHIHA,
			PRETO_UCHIHA,
			AZUL_SUSANOO,
			VERMELHO_ARMADURA
		);
	}

	private void setDodgeColors() {

		long phase = now % 9;

		if (phase < 3) {

			setColors(
				ROXO_RINNEGAN_DARK,
				ROXO_RINNEGAN_DARK,
				ROXO_RINNEGAN,
				ROXO_RINNEGAN_DARK,
				ROXO_RINNEGAN_DARK
			);

		} else if (phase < 6) {

			setColors(
				ROXO_RINNEGAN,
				ROXO_RINNEGAN,
				ROXO_RINNEGAN_LIGHT,
				ROXO_RINNEGAN_LIGHT,
				ROXO_RINNEGAN
			);

		} else {

			setColors(
				ROXO_RINNEGAN_DARK,
				ROXO_RINNEGAN,
				ROXO_RINNEGAN_DARK,
				ROXO_RINNEGAN,
				ROXO_RINNEGAN_DARK
			);
		}
	}

	private double evaluate(double angle) {
		return evaluate(angle, true);
	}

	private double evaluate(double angle, boolean includeShots) {

		double sin = Math.sin(angle);
		double cos = Math.cos(angle);

		double px = myX + sin * adaptiveMoveDistance;
		double py = myY + cos * adaptiveMoveDistance;

		double danger = wallDanger(px, py);

		for (int i = 0; i < threatCount; i++) {
			danger += enemyDanger(threatBuf[i], px, py);
		}

		if (threatCount >= 2) {
			// Stronger melee spacing helps avoid drifting into a cluster of enemies.
			double dx = px - clusterX;
			double dy = py - clusterY;
			double clusterDistance = Math.sqrt(dx * dx + dy * dy);
			danger -= Math.min(clusterDistance / 65.0, 36.0);
		}

		if (now < evadeUntil) {

			double dx = px - evadeX;
			double dy = py - evadeY;

			double d = Math.sqrt(dx * dx + dy * dy);

			if (d < EVADE_RADIUS) {
				danger += (EVADE_RADIUS - d) * 3.0;
			}
		}

		if (includeShots) {
			if (activeShots > 0) {
				danger += shotDanger(sin, cos);
			}
			// Experimental ablation: inferred wave danger is disabled while tracked bullet danger remains active.
		}

		/* Cost of the turn needed (forward or reverse, whichever is closer). */
		double turn =
			Math.abs(Utils.normalRelativeAngle(angle - myHeading));

		if (turn > HALF_PI) {
			turn = Math.PI - turn;
		}

		danger += turn * TURN_COST;

		/* Hysteresis: avoids dithering between similar directions. */
		danger +=
			Math.abs(Utils.normalRelativeAngle(angle - lastMoveAngle)) *
			HYSTERESIS_COST;

		return danger;
	}

	/**
	 * Danger from in-flight enemy bullets, evaluated tick by tick along the
	 * candidate path. The path uses realistic accelerating travel distances.
	 */
	private double shotDanger(double sin, double cos) {

		double total = 0;

		for (int i = 0; i < MAX_SHOTS; i++) {

			EnemyShot s = shots[i];

			if (!s.active) {
				continue;
			}

			for (int t = 1; t <= DODGE_TICKS; t++) {

				double traveled = (now + t - s.fireTime) * s.speed;

				if (traveled < 0) {
					continue;
				}

				double bx = s.ox + s.dx * traveled;
				double by = s.oy + s.dy * traveled;

				double rx = myX + sin * travelDist[t];
				double ry = myY + cos * travelDist[t];

				double ddx = rx - bx;
				double ddy = ry - by;

				double d = Math.sqrt(ddx * ddx + ddy * ddy);

				if (d < SHOT_HIT_RADIUS) {

					total += 300.0;

				} else if (d < SHOT_NEAR_RADIUS) {

					total +=
						(1.0 - (d - SHOT_HIT_RADIUS) /
							(SHOT_NEAR_RADIUS - SHOT_HIT_RADIUS)) *
						80.0;
				}
			}
		}

		return total;
	}

	private double enemyDanger(EnemyData en, double px, double py) {

		double dx = px - en.x;
		double dy = py - en.y;

		double d = Math.sqrt(dx * dx + dy * dy);

		double danger = 0;

		if (d < DANGER_SCAN_DISTANCE) {

			danger +=
				(1.0 - d / DANGER_SCAN_DISTANCE) * 40.0 +
				Math.min(en.energy / 100.0, 1.0) * 10.0;
		}

		if (d < COLLISION_RADIUS) {
			danger += (COLLISION_RADIUS - d) * 3.0;
		}

		return danger;
	}

	private double wallDanger(double x, double y) {

		double danger = 0;

		if (x < WALL_MARGIN) {
			danger += (WALL_MARGIN - x) * 2.0;
		} else if (x > fieldW - WALL_MARGIN) {
			danger += (x - (fieldW - WALL_MARGIN)) * 2.0;
		}

		if (y < WALL_MARGIN) {
			danger += (WALL_MARGIN - y) * 2.0;
		} else if (y > fieldH - WALL_MARGIN) {
			danger += (y - (fieldH - WALL_MARGIN)) * 2.0;
		}

		if (
			x < WALL_HARD_MARGIN ||
			x > fieldW - WALL_HARD_MARGIN ||
			y < WALL_HARD_MARGIN ||
			y > fieldH - WALL_HARD_MARGIN
		) {
			danger += 500.0;
		}

		return danger;
	}

	private void driveTo(double angle) {

		lastMoveAngle = angle;

		double turn =
			Utils.normalRelativeAngle(angle - myHeading);

		int direction = 1;

		if (Math.abs(turn) > HALF_PI) {
			turn = Utils.normalRelativeAngle(turn + Math.PI);
			direction = -1;
		}

		setTurnRightRadians(turn);
		setAhead(direction * adaptiveMoveDistance);
	}

	// =====================================================================
	// Events
	// =====================================================================

	@Override
	public void onBulletHit(BulletHitEvent e) {

		syncState();

		EnemyData en = enemies.get(e.getName());

		if (en == null) {
			return;
		}

		bulletsHit++;
		double p = e.getBullet().getPower();

		double damage = 4.0 * p + (p > 1.0 ? 2.0 * (p - 1.0) : 0.0);

		if (!cancelRecentShot(en, damage)) {
			en.energyAdjust += damage;
		}
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e) {

		syncState();

		EnemyData en = enemies.get(e.getName());

		if (en == null) {
			return;
		}

		/* The shooter gains 3 * power when its bullet hits. */
		damageReceived += 4.0 * e.getPower() + (e.getPower() > 1.0 ? 2.0 * (e.getPower() - 1.0) : 0.0);
		en.energyAdjust -= 3.0 * e.getPower();

		learnEnemyWave(en);
		consumeShot(en, e.getPower());
	}

	@Override
	public void onHitRobot(HitRobotEvent e) {

		syncState();

		EnemyData en = enemies.get(e.getName());

		if (en != null && !cancelRecentShot(en, 0.6)) {
			en.energyAdjust += 0.6;
		}

		/* Keep ramming only to finish a weak enemy; otherwise back away. */
		if (e.getEnergy() > RAM_FINISH_ENERGY || getEnergy() < 30.0) {

			double angle = myHeading + e.getBearingRadians();

			evadeX = myX + Math.sin(angle) * ROBOT_RADIUS * 2.0;
			evadeY = myY + Math.cos(angle) * ROBOT_RADIUS * 2.0;
			evadeUntil = now + EVADE_TICKS;
		}
	}

	@Override
	public void onHitWall(HitWallEvent e) {

		syncState();

		lastMoveAngle =
			Math.atan2(fieldW / 2.0 - myX, fieldH / 2.0 - myY);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent e) {

		String name = e.getName();

		EnemyData en = enemies.remove(name);

		if (en == null) {
			return;
		}

		enemyList.remove(en);

		int i = 0;

		while (i < waveCount) {

			if (waves[i].target == en) {
				removeWave(i);
			} else {
				i++;
			}
		}

		if (gunTarget == en) {
			gunTarget = null;
		}

		/*
		 * Shots already in flight stay active (they are real bullets);
		 * STATS is kept so the learning carries to the next rounds.
		 */
	}


	private void loadLearningData() {
		if (learningLoaded) return;
		learningLoaded = true;
		File f = getDataFile(LEARNING_FILE);
		if (!f.exists()) {
			out.println("[MADARA-LEARNING] No saved data yet; starting with default parameters.");
			return;
		}
		try (BufferedReader r = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = r.readLine()) != null) {
				String[] p = line.split("\\|");
				if (p.length >= 8 && "META".equals(p[0])) {
					roundsPlayed = Long.parseLong(p[1]); roundsWon = Long.parseLong(p[2]);
					bulletsFired = Long.parseLong(p[3]); bulletsHit = Long.parseLong(p[4]);
					damageReceived = Double.parseDouble(p[5]);
					adaptiveMoveDistance = clamp(Double.parseDouble(p[6]), 80, 180);
					adaptiveSurfWeight = clamp(Double.parseDouble(p[7]), 0.5, 2.5);
				} else if (p.length == 2 + SEGMENTS * (GF_BINS + 1) + GF_BINS + 1 && "GF".equals(p[0])) {
					GuessFactorStats st = new GuessFactorStats(); int k = 2;
					for (int seg = 0; seg < SEGMENTS; seg++) {
						st.segTotal[seg] = Double.parseDouble(p[k++]);
						for (int b = 0; b < GF_BINS; b++) st.segBins[seg][b] = Double.parseDouble(p[k++]);
					}
					st.allTotal = Double.parseDouble(p[k++]);
					for (int b = 0; b < GF_BINS; b++) st.allBins[b] = Double.parseDouble(p[k++]);
					STATS.put(p[1], st);
				}
			}
		} catch (Exception ex) { out.println("Madara: falha ao carregar aprendizado: " + ex.getMessage()); }
	}

	private void saveLearningData() {
		saveLearningData(true);
	}

	private void saveLearningData(boolean log) {
		try (BufferedWriter w = new BufferedWriter(new FileWriter(getDataFile(LEARNING_FILE)))) {
			w.write("META|" + roundsPlayed + "|" + roundsWon + "|" + bulletsFired + "|" + bulletsHit
				+ "|" + damageReceived + "|" + adaptiveMoveDistance + "|" + adaptiveSurfWeight);
			w.newLine();
			for (Map.Entry<String, GuessFactorStats> entry : STATS.entrySet()) {
				GuessFactorStats st = entry.getValue();
				StringBuilder line = new StringBuilder("GF|").append(entry.getKey());
				for (int seg = 0; seg < SEGMENTS; seg++) {
					line.append('|').append(st.segTotal[seg]);
					for (int b = 0; b < GF_BINS; b++) line.append('|').append(st.segBins[seg][b]);
				}
				line.append('|').append(st.allTotal);
				for (int b = 0; b < GF_BINS; b++) line.append('|').append(st.allBins[b]);
				w.write(line.toString()); w.newLine();
			}
			if (log) {
				out.println("[MADARA-LEARNING] Saved"
					+ " | rounds=" + roundsPlayed
					+ " | wins=" + roundsWon
					+ " | bullets=" + bulletsHit + "/" + bulletsFired
					+ " | GF profiles=" + STATS.size()
					+ " | file=" + getDataFile(LEARNING_FILE).getAbsolutePath());
			}
		} catch (IOException ex) {
			out.println("[MADARA-LEARNING] SAVE ERROR: " + ex.getMessage());
		}
	}

	private void recalibrateLearning() {
		double oldMoveDistance = adaptiveMoveDistance;
		double oldSurfWeight = adaptiveSurfWeight;
		double hitRate = bulletsFired == 0 ? 0 : bulletsHit / (double) bulletsFired;
		double damagePerRound = damageReceived / Math.max(1, roundsPlayed);
		String reason = "insufficient samples";

		/*
		 * Hit rate evaluates targeting, not movement distance. Movement is
		 * adjusted from incoming damage; wave-surf weight follows the same signal.
		 */
		if (bulletsFired >= 10 && roundsPlayed >= 3) {
			if (damagePerRound > 35.0) {
				adaptiveMoveDistance += 4.0;
				adaptiveSurfWeight += 0.08;
				reason = "high incoming damage";
			} else if (damagePerRound < 15.0) {
				adaptiveMoveDistance -= 2.0;
				adaptiveSurfWeight -= 0.05;
				reason = "low incoming damage";
			} else {
				reason = "performance within target range";
			}
		}
		adaptiveMoveDistance = clamp(adaptiveMoveDistance, 80, 180);
		adaptiveSurfWeight = clamp(adaptiveSurfWeight, 0.5, 2.5);

		out.println("[MADARA-LEARNING] Round=" + roundsPlayed
			+ " | Wins=" + roundsWon
			+ " | Bullets=" + bulletsHit + "/" + bulletsFired
			+ " | HitRate=" + formatPercent(hitRate)
			+ " | DamageReceived/round=" + String.format(java.util.Locale.US, "%.2f", damagePerRound));
		out.println("[MADARA-LEARNING] Parameters"
			+ " | MoveDistance=" + fmt(oldMoveDistance) + " -> " + fmt(adaptiveMoveDistance)
			+ " | SurfWeight=" + fmt(oldSurfWeight) + " -> " + fmt(adaptiveSurfWeight)
			+ " | Reason=" + reason
			+ " | Limits=Move[80,180], Surf[0.5,2.5]");
		if (bulletsFired < 10 || roundsPlayed < 3) {
			out.println("[MADARA-LEARNING] Recalibration pending: need 3 rounds and 10 fired bullets.");
		} else if (oldMoveDistance != adaptiveMoveDistance || oldSurfWeight != adaptiveSurfWeight) {
			out.println("[MADARA-LEARNING] ADJUSTED");
		} else {
			out.println("[MADARA-LEARNING] Parameters at limits or unchanged by performance thresholds.");
		}
	}

	private static String formatPercent(double value) {
		return String.format(java.util.Locale.US, "%.1f%%", value * 100.0);
	}

	private static String fmt(double value) {
		return String.format(java.util.Locale.US, "%.2f", value);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	@Override
	public void onWin(robocode.WinEvent e) {
		roundsWon++;
		saveLearningData();
	}

	@Override
	public void onRoundEnded(robocode.RoundEndedEvent e) {
		// Round count is incremented at the next run() start; save whatever was learned.
		saveLearningData();
	}

	@Override
	public void onBattleEnded(robocode.BattleEndedEvent e) {
		saveLearningData();
	}

	// =====================================================================
	// Data classes
	// =====================================================================

	private static final class EnemyData {

		final String name;

		double x;
		double y;

		double heading;
		double velocity;

		double energy;
		double prevEnergy;

		long lastScanTime;
		long prevScanTime;
		int scanCount;

		/** Expected energy drop from known sources since the previous scan. */
		double energyAdjust;

		double threat;

		long lastFireTime = -1000;
		long lastWaveTime = -1000;
		double[] surfStats = new double[SURF_BINS];
		double surfSamples;

		EnemyShot lastShot;

		EnemyData(String name) {
			this.name = name;
		}

		void update(
			double distance,
			double absBearing,
			double ourX,
			double ourY,
			double heading,
			double velocity,
			double energy,
			long time
		) {

			prevEnergy = scanCount == 0 ? energy : this.energy;
			prevScanTime = lastScanTime;

			x = ourX + distance * Math.sin(absBearing);
			y = ourY + distance * Math.cos(absBearing);

			this.heading = heading;
			this.velocity = velocity;
			this.energy = energy;

			lastScanTime = time;
			scanCount++;
		}
	}

	private static final class EnemyWave {
		boolean active;
		EnemyData owner;
		double ox, oy;
		long fireTime;
		double speed, power, directAngle;
		int direction;
	}

	private static final class EnemyShot {

		boolean active;

		EnemyData owner;

		double ox;
		double oy;

		double dx;
		double dy;

		double speed;
		double power;

		long fireTime;
	}

	private static final class Wave {

		double ox;
		double oy;

		long fireTime;

		double speed;
		double directAngle;
		double maxEscape;
		double weight;

		EnemyData target;

		int direction;
		int segment;

		boolean real;
	}

	private static final class GuessFactorStats {

		final double[][] segBins = new double[SEGMENTS][GF_BINS];
		final double[] segTotal = new double[SEGMENTS];

		final double[] allBins = new double[GF_BINS];
		double allTotal;

		/** Returns the bins to aim with, or null if there is not enough data yet. */
		double[] select(int segment) {

			if (segTotal[segment] >= MIN_SEGMENT_SAMPLES) {
				return segBins[segment];
			}

			if (allTotal >= MIN_TOTAL_SAMPLES) {
				return allBins;
			}

			return null;
		}

		void add(int segment, int bin, double weight) {

			splat(segBins[segment], bin, weight);
			segTotal[segment] += weight;

			splat(allBins, bin, weight);
			allTotal += weight;
		}

		private static void splat(double[] bins, int bin, double weight) {

			for (int k = -(KERNEL.length - 1); k < KERNEL.length; k++) {

				int i = bin + k;

				if (i < 0 || i >= GF_BINS) {
					continue;
				}

				bins[i] += weight * KERNEL[Math.abs(k)];
			}
		}
	}
}
