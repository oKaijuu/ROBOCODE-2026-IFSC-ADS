package TriStateRobots;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

/**
 * MadaraV2 (ex-MadaraV1) - DangerBasedBot.
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

public class MadaraV2 extends AdvancedRobot {

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
	private static final int DANGER_SAMPLES = 16;
	private static final double MOVE_DISTANCE = 100.0;
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
	private static final int DODGE_TICKS = 14;
	private static final int MAX_SHOT_SCAN_GAP = 3;
	private static final double SHOT_HIT_RADIUS = 26.0;
	private static final double SHOT_NEAR_RADIUS = 56.0;
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

	private final Map<String, EnemyData> enemies = new HashMap<>();
	private final ArrayList<EnemyData> enemyList = new ArrayList<>();
	private final EnemyData[] threatBuf = new EnemyData[MAX_THREATS];
	private int threatCount;

	private final EnemyShot[] shots = new EnemyShot[MAX_SHOTS];
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

	{
		for (int i = 0; i < MAX_SHOTS; i++) {
			shots[i] = new EnemyShot();
		}
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

		syncState();
		lastMoveAngle = myHeading;

		while (true) {

			syncState();
			refreshState();
			updateRadar();
			updateMovement();

			execute();
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

		double minX = ROBOT_RADIUS;
		double minY = ROBOT_RADIUS;
		double maxX = fieldW - ROBOT_RADIUS;
		double maxY = fieldH - ROBOT_RADIUS;

		for (int t = 1; t <= LINEAR_MAX_TICKS; t++) {

			ex += vx;
			ey += vy;

			if (ex < minX) {
				ex = minX;
				vx = 0;
			} else if (ex > maxX) {
				ex = maxX;
				vx = 0;
			}

			if (ey < minY) {
				ey = minY;
				vy = 0;
			} else if (ey > maxY) {
				ey = maxY;
				vy = 0;
			}

			double dx = ex - myX;
			double dy = ey - myY;

			if (Math.sqrt(dx * dx + dy * dy) <= bulletSpeed * t) {
				break;
			}
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

		stats.add(w.segment, guessFactorToBin(guessFactor), w.weight);
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

		for (int i = 0; i < DANGER_SAMPLES; i++) {

			double angle = myHeading + step * i;
			double danger = evaluate(angle);

			if (danger < lowest) {
				lowest = danger;
				bestAngle = angle;
			}
		}

		driveTo(bestAngle);
	}

	private double evaluate(double angle) {

		double sin = Math.sin(angle);
		double cos = Math.cos(angle);

		double px = myX + sin * MOVE_DISTANCE;
		double py = myY + cos * MOVE_DISTANCE;

		double danger = wallDanger(px, py);

		for (int i = 0; i < threatCount; i++) {
			danger += enemyDanger(threatBuf[i], px, py);
		}

		if (threatCount >= 3) {

			double dx = px - clusterX;
			double dy = py - clusterY;

			danger -=
				Math.min(Math.sqrt(dx * dx + dy * dy) / 100.0, 20.0);
		}

		if (now < evadeUntil) {

			double dx = px - evadeX;
			double dy = py - evadeY;

			double d = Math.sqrt(dx * dx + dy * dy);

			if (d < EVADE_RADIUS) {
				danger += (EVADE_RADIUS - d) * 3.0;
			}
		}

		if (activeShots > 0) {
			danger += shotDanger(sin, cos);
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
		setAhead(direction * MOVE_DISTANCE);
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
		en.energyAdjust -= 3.0 * e.getPower();

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
