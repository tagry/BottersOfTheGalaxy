import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

enum UnitType {
	UNIT, HERO, TOWER, GROOT

}

enum HeroType {
	DEADPOOL, HULK, VALKYRIE, IRONMAN, DOCTOR_STRANGE
}

enum MoveType {
	WAIT("WAIT"), MOVE("MOVE"), ATTACK_ID("ATTACK"), ATTACK_NEAREST("ATTACK_NEAREST"), MOVE_ATTACK("MOVE_ATTACK"), BUY(
			"BUY"), SELL("SELL");

	final private String command;

	private MoveType(String command) {
		this.command = command;
	}

	public String command() {
		return this.command;
	}
}

/**
 * Class global using to group the game in one class
 * 
 * @author Tom Agry
 *
 */
class Global {
	int heroMovesLeft = 0;
	Team myTeam = new Team();
	Team hisTeam = new Team();
	Map<Integer, Entity> entitiesById = new HashMap<>();
	List<Item> items = new ArrayList<>();

	List<Bush> bushes = new ArrayList<>();
	List<Spawn> spawns = new ArrayList<>();

	public void initEntities() {
		for (Entity entity : entitiesById.values()) {
			entity.isAlive = false;
		}
	}

	public void purgeEntitiesMap() {
		List<Integer> listEntitiesToBeRemoved = new ArrayList<>();

		// Find dead entities
		for (Entity entity : entitiesById.values()) {
			if (!entity.isAlive) {
				listEntitiesToBeRemoved.add(entity.id);
			}
		}

		// Purge entities
		for (Integer idToBeRemoved : listEntitiesToBeRemoved) {
			entitiesById.remove(idToBeRemoved);
		}
	}
	
	public void purgeTeamsEntities() {
		myTeam.units = purgeEntities(myTeam.units);
		myTeam.heros = purgeEntities(myTeam.heros);
		
		hisTeam.units = purgeEntities(hisTeam.units);
		hisTeam.heros = purgeEntities(hisTeam.heros);
	}
	
	private <T extends Entity> List<T> purgeEntities(List<T> entities) {
		return entities.stream().filter(entity -> entity.isAlive).collect(Collectors.toList());
	}

	public void updateTower(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth,
			int attackDamage, int movementSpeed) {
		if (entitiesById.containsKey(id)) {
			Tower tower = (Tower) entitiesById.get(id);
			tower.update(x, y, health);
		} else {
			initTower(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed);
		}
	}

	private void initTower(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth,
			int attackDamage, int movementSpeed) {
		Tower newTower = new Tower(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed);

		// Add to global entities map
		entitiesById.put(id, newTower);

		// Add to team
		if (myTeam.teamId == teamId) {
			myTeam.tower = newTower;
		} else {
			hisTeam.tower = newTower;
		}
	}

	public void updateUnit(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth,
			int attackDamage, int movementSpeed) {
		if (entitiesById.containsKey(id)) {
			Unit unit = (Unit)entitiesById.get(id);
			unit.update(x, y, health);
		} else {
			Unit newUnit = new Unit(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed);

			// Add to global entities map
			entitiesById.put(id, newUnit);

			// Add to team
			if (myTeam.teamId == teamId) {
				myTeam.units.add(newUnit);
			} else {
				hisTeam.units.add(newUnit);
			}
		}
	}

	public void updateHero(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth,
			int attackDamage, int movementSpeed, int mana, int maxMana, int manaRegeneration, String heroType,
			int isVisible) {
		if (entitiesById.containsKey(id)) {
			Hero hero = (Hero) entitiesById.get(id);
			hero.update(x, y, health, mana, isVisible);
		} else {
			addNewHero(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, mana, maxMana,
					manaRegeneration, heroType, isVisible);
		}
	}

	// Add the same hero object to all lists to update it easily
	private void addNewHero(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth,
			int attackDamage, int movementSpeed, int mana, int maxMana, int manaRegeneration, String heroType,
			int isVisible) {
		Hero newHero = new Hero(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, mana,
				maxMana, manaRegeneration, heroType, isVisible);
		// Add to global entities map
		entitiesById.put(id, newHero);

		// Add to team
		if (myTeam.teamId == teamId) {
			myTeam.heros.add(newHero);
		} else {
			hisTeam.heros.add(newHero);
		}
	}

	public void addItem(String itemName, int itemCost, int damage, int health, int maxHealth, int mana, int maxMana,
			int moveSpeed, int manaRegeneration, int isPotion) {
		items.add(new Item(itemName, itemCost, damage, health, maxHealth, mana, maxMana, moveSpeed, manaRegeneration,
				isPotion));
	}
}

class Position {
	static final int MAX_X = 1920;
	static final int MAX_Y = 750;

	int x;
	int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Return the position in front of position by distance depend of the team
	 * 
	 * @param distance
	 * @team team
	 * @return
	 */
	public Position frontPosition(int distance, Team team) {
		Position frontPosition = new Position(x, y);

		if (team.tower.position.x > MAX_X / 2) {
			frontPosition.x = Math.min(0, x - distance);
		} else {
			frontPosition.x = Math.max(MAX_X, x + distance);
		}

		return frontPosition;
	}

	/**
	 * Return the position in behind of position by distance depend of the team
	 * 
	 * @param distance
	 * @team team
	 * @return
	 */
	public Position behindPosition(int distance, Team team) {
		Position behindPosition = new Position(x, y);
		if (team.tower.position.x < MAX_X / 2) {
			behindPosition.x = Math.max(0, x - distance);
		} else {
			behindPosition.x = Math.min(MAX_X, distance - x);
		}

		return behindPosition;
	}

	public double distance(Position otherPosition) {
		return Math.sqrt((this.x - otherPosition.x) * (this.x - otherPosition.x)
				+ (this.y - otherPosition.y) * (this.y - otherPosition.y));
	}
}

class Team {
	int teamId;
	int gold;
	List<Hero> heros = new ArrayList<>();
	List<Unit> units = new ArrayList<>();
	Tower tower;

	public boolean hasUnits() {
		return !units.isEmpty();
	}

	public Unit getFrontUnit() {
		if (!hasUnits()) {
			return null;
		}

		List<Unit> unitsCopy = new ArrayList<>(units);
		if (tower.position.x < Position.MAX_X / 2) {
			unitsCopy.sort(new ComparatorFewerXtoGreater());
		} else {
			unitsCopy.sort(new ComparatorGreaterXtoFewer());
		}

		Play.printErrorLog("Unit frontest:" + unitsCopy.get(0).position.x);
		return unitsCopy.get(0);
	}
}

class ComparatorFewerXtoGreater implements Comparator<Entity> {
	@Override
	public int compare(Entity arg0, Entity arg1) {
		if (arg0.position.x < arg1.position.x) {
			return 1;
		} else if (arg0.position.x > arg1.position.x) {
			return -1;
		}
		return 0;
	}

}

class ComparatorGreaterXtoFewer implements Comparator<Entity> {
	@Override
	public int compare(Entity arg0, Entity arg1) {
		if (arg0.position.x > arg1.position.x) {
			return 1;
		} else if (arg0.position.x < arg1.position.x) {
			return -1;
		}
		return 0;
	}

}

class Item {
	String itemName;
	int itemCost;
	int damage;
	int health;
	int maxHealth;
	int mana;
	int maxMana;
	int moveSpeed;
	int manaRegeneration;
	boolean isPotion;

	public Item(String itemName, int itemCost, int damage, int health, int maxHealth, int mana, int maxMana,
			int moveSpeed, int manaRegeneration, int isPotion) {
		this.itemName = itemName;
		this.itemCost = itemCost;
		this.damage = damage;
		this.health = health;
		this.maxHealth = maxHealth;
		this.mana = mana;
		this.maxMana = maxMana;
		this.moveSpeed = moveSpeed;
		this.manaRegeneration = manaRegeneration;
		this.isPotion = isPotion == 1;
	}

}

abstract class MapObject {
	Position position = new Position(-1, -1);
}

abstract class Entity extends MapObject {
	final int id;
	final UnitType entityType;
	final int teamId;
	final int attackRange;
	final int maxHealth;
	final int attackDamage;
	final int movementSpeed;
	int health;
	boolean isAlive;

	protected Entity(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed, UnitType entityType) {
		// Init Character final values
		this.id = id;
		this.teamId = teamId;
		this.entityType = entityType;
		this.attackRange = attackRange;
		this.maxHealth = maxHealth;
		this.attackDamage = attackDamage;
		this.movementSpeed = movementSpeed;

		// Init Character variable values
		update(x, y, health);
	}

	protected void update(int x, int y, int health) {
		this.position.x = x;
		this.position.y = y;
		this.health = health;

		if (health > 0) {
			isAlive = true;
		}
	}

	/**
	 * return true if this can attack entity without moving
	 * 
	 * @param entity
	 */
	public boolean canAttack(Entity entity) {
		return distance(entity) < attackRange;
	}

	/**
	 * Return the list of attackable entities without moving
	 * 
	 * @return
	 */
	public List<Entity> getAttackableEntities(Global g) {
		List<Entity> attackableEntities = new ArrayList<>();

		for (Entity entity : g.entitiesById.values()) {
			if (this.id != entity.id && this.canAttack(entity)) {
				attackableEntities.add(entity);
			}
		}

		return attackableEntities;
	}

	/**
	 * Return the list of enemy (NOT neutral) entity attackable without moving
	 * 
	 * @param g
	 * @return
	 */
	public List<Entity> getAttackableEnemy(Global g) {
		List<Entity> attackableEntities = getAttackableEntities(g);
		return StrategyUtile.getEnemyAmongList(attackableEntities, teamId);
	}

	public Entity getClosestAmongList(List<Entity> entities) {
		double min = 1000000;
		Entity closestEntity = null;

		for (Entity entity : entities) {
			double distance = distance(entity);
			if (distance < min) {
				closestEntity = entity;
				min = distance;
			}
		}
		return closestEntity;
	}

	public double distance(MapObject entity) {
		return position.distance(entity.position);
	}
}

class Tower extends Entity {

	public Tower(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed) {
		super(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, UnitType.TOWER);
	}

}

class Unit extends Entity {

	public Unit(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed) {
		super(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, UnitType.UNIT);
	}

}

class Hero extends Entity {
	HeroType heroType;
	final int maxMana;
	final int manaRegeneration;
	int mana;
	boolean isVisible;

	public Hero(Hero hero) {
		this(hero.id, hero.teamId, hero.position.x, hero.position.y, hero.attackRange, hero.health, hero.maxHealth,
				hero.attackDamage, hero.movementSpeed, hero.mana, hero.maxMana, hero.manaRegeneration,
				hero.heroType.name(), hero.isVisible ? 1 : 0);
	}

	public Hero(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed, int mana, int maxMana, int manaRegeneration, String heroType, int isVisible) {
		super(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, UnitType.HERO);
		this.heroType = HeroType.valueOf(heroType);
		this.maxMana = maxMana;
		this.manaRegeneration = manaRegeneration;

		this.update(x, y, health, mana, isVisible);
	}

	public void update(int x, int y, int health, int mana, int isVisible) {
		super.update(x, y, health);
		this.mana = mana;
		this.isVisible = isVisible == 1;
	}

	public List<Entity> getAttackableEntitiesAfterMove(Global g, Position target) {
		HeroSimulated copyHero = new HeroSimulated(this);
		Play.printErrorLog("X : " +copyHero.position.x );
		copyHero.simulateMove(target);
Play.printErrorLog("X : " +copyHero.position.x );

		if (copyHero.hasEnoughTimeToAttack()) {
			return copyHero.getAttackableEntities(g);
		} else {
			return new ArrayList<>();
		}
	}
}

class HeroSimulated extends Hero {
	final static double TIME_TO_ATTACK = 0.1;

	// Time for the turn
	double timeLeft = 1;

	public HeroSimulated(Hero hero) {
		super(hero);
	}

	public void simulateMove(Position target) {
		double distanceToTarget = position.distance(target);
		double distanceTravelled = Math.min(this.movementSpeed, distanceToTarget);
		timeLeft = timeLeft - distanceTravelled / movementSpeed;

		int xAfter = (int) (position.x + (target.x - position.x) * distanceTravelled / distanceToTarget);
		int yAfter = (int) (position.y + (target.y - position.y) * distanceTravelled / distanceToTarget);

		position.x = xAfter;
		position.y = yAfter;
	}

	public boolean hasEnoughTimeToAttack() {
		return timeLeft > TIME_TO_ATTACK;
	}
}

abstract class BushOrSpawn extends MapObject {
	int radius;

	public BushOrSpawn(int x, int y, int radius) {
		this.position = new Position(x, y);
		this.radius = radius;
	}
}

class Bush extends BushOrSpawn {
	public Bush(int x, int y, int radius) {
		super(x, y, radius);
	}
}

class Spawn extends BushOrSpawn {
	public Spawn(int x, int y, int radius) {
		super(x, y, radius);
	}

}

final class Play {
	private static StrategyOrchestrator strategyOrchestrator = new StrategyOrchestrator();
	private static String log = null;

	public static void initTurn(Global g) {
		// Init all entities as not alive
		g.initEntities();
		resetLog();
	}

	private static void preparingBeforePlay(Global g) {
		g.purgeEntitiesMap();
		g.purgeTeamsEntities();
	}

	public static void play(Global g) {
		preparingBeforePlay(g);

		if (g.heroMovesLeft < 0) {
			Strategy.playFirstMove();
		}

		// Orchestrator decide the strategy and play it
		for (int i = 0; i < g.heroMovesLeft; i++) {
			Hero heroPlaying = g.myTeam.heros.get(i);
			strategyOrchestrator.decideStrategy(g, heroPlaying).decideMove(g, heroPlaying).playCommand(getLog());
		}
	}

	public static void addLog(String msg) {
		if (log == null) {
			log = msg;
		} else {
			log = log + " | " + msg;
		}
	}

	public static void printErrorLog(String msg) {
		System.err.println(msg);
	}

	public static void resetLog() {
		log = null;
	}

	public static String getLog() {
		return log;
	}
}

class Move {
	MoveType moveType = null;
	Position position = new Position(-1, -1);
	int unitId = -1;
	UnitType unitType = null;
	String itemName = null;

	private Move() {
	};

	public static Move buildWaitMove() {
		Move move = new Move();
		move.moveType = MoveType.WAIT;
		return move;
	}

	public static Move buildMoveMove(int x, int y) {
		Move move = new Move();
		move.moveType = MoveType.MOVE;
		move.position.x = x;
		move.position.y = y;
		return move;
	}

	public static Move buildMoveMove(Position position) {
		return buildMoveMove(position.x, position.y);
	}

	public static Move buildMoveAttackId(int unitId) {
		Move move = new Move();
		move.moveType = MoveType.ATTACK_ID;
		move.unitId = unitId;
		return move;
	}

	public static Move buildMoveAttackNearest(UnitType unitType) {
		Move move = new Move();
		move.moveType = MoveType.ATTACK_NEAREST;
		move.unitType = unitType;
		return move;
	}

	public static Move buildMoveAttackMove(int x, int y, int unitId) {
		Move move = new Move();
		move.moveType = MoveType.MOVE_ATTACK;
		move.position.x = x;
		move.position.y = y;
		move.unitId = unitId;
		return move;
	}

	public static Move buildMoveAttackMove(Position position, int unitId) {
		return buildMoveAttackMove(position.x, position.y, unitId);
	}

	public static Move buildMoveBuy(String itemName) {
		Move move = new Move();
		move.moveType = MoveType.BUY;
		move.itemName = itemName;
		return move;
	}

	public static Move buildMoveSell(String itemName) {
		Move move = new Move();
		move.moveType = MoveType.SELL;
		move.itemName = itemName;
		return move;
	}

	public void playCommand(String log) {
		System.out.println(command() + ";" + log);
	}

	private String command() {
		String command = moveType.command();

		switch (moveType) {
		case WAIT:
			// Do nothing
			break;

		case ATTACK_ID:
			command = command + " " + unitId;
			break;

		case ATTACK_NEAREST:
			command = command + " " + unitType.name();
			break;

		case MOVE:
			command = command + " " + position.x + " " + position.y;
			break;

		case MOVE_ATTACK:
			command = command + " " + position.x + " " + position.y + " " + unitId;
			break;

		case BUY:
			command = command + " " + itemName;
			break;

		case SELL:
			command = command + " " + itemName;
			break;

		default:
			System.err.println("ERROR not Move Type !");
			command = MoveType.WAIT.command();
			break;
		}

		return command;
	}
}

enum StrategyType {
	BEHIND_UNIT, PROTECT_TOWER
}

class StrategyOrchestrator {
	Map<StrategyType, Strategy> strategies = new HashMap<>();

	public StrategyOrchestrator() {
		strategies.put(StrategyType.BEHIND_UNIT, new StillBehindUnitStrategy());
		strategies.put(StrategyType.PROTECT_TOWER, new ProtectTowerStrategy());
	}

	public Strategy decideStrategy(Global g, Hero heroPlaying) {
		return strategies.get(StrategyType.BEHIND_UNIT);
	}
}

abstract class Strategy {
	public abstract Move decideMove(Global g, Hero hero);

	public static void playFirstMove() {
		System.out.println(HeroType.DEADPOOL);
	}
}

final class StrategyUtile {
	public static Entity getBestEnemyToBeAttacked(Global g, Hero hero) {
		List<Entity> enemyEntityToBeAttacked = hero.getAttackableEnemy(g);
		return hero.getClosestAmongList(enemyEntityToBeAttacked);
	}

	public static List<Entity> getEnemyAmongList(List<Entity> entities, int teamId) {
		return entities.stream().filter(entity -> entity.teamId != teamId && entity.teamId != -1)
				.collect(Collectors.toList());
	}
}

class StillBehindUnitStrategy extends Strategy {
	final int DISTANCE_BEHIND_UNITS = 10;

	@Override
	public Move decideMove(Global g, Hero hero) {
		{
			Play.addLog("S:BEHIND");

			Move move = null;
			Position target = null;

			if (g.myTeam.hasUnits()) {
				// Target position is behind the frontest unit
				target = g.myTeam.getFrontUnit().position.behindPosition(DISTANCE_BEHIND_UNITS, g.myTeam);
			}

			// No unit => don't move and attack if possible
			if (target == null) {
				Entity bestEnemy = StrategyUtile.getBestEnemyToBeAttacked(g, hero);
				if (bestEnemy != null) {
					move = Move.buildMoveAttackId(bestEnemy.id);
				} else {
					move = Move.buildWaitMove();
				}
			} else {
				List<Entity> attackableAfterMove = hero.getAttackableEntitiesAfterMove(g, target);
				if (!attackableAfterMove.isEmpty()) {
					move = Move.buildMoveAttackMove(target, attackableAfterMove.get(0).id);
				} else {
					move = Move.buildMoveMove(target);
				}
			}

			return move;
		}
	}
}

class ProtectTowerStrategy extends Strategy {
	@Override
	public Move decideMove(Global g, Hero hero) {
		{
			Entity entityToAttack = StrategyUtile.getBestEnemyToBeAttacked(g, hero);

			if (entityToAttack != null) {
				return Move.buildMoveAttackId(entityToAttack.id);
			}

			return Move.buildWaitMove();
		}
	}

}

class Player {
	private static Global g = new Global();

	private static final String BUSH_TYPE = "BUSH";
	private static final String SPAWN_TYPE = "SPAWN";

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int myTeam = in.nextInt();
		g.myTeam.teamId = myTeam;
		g.hisTeam.teamId = (myTeam == 0) ? 1 : 0;

		int bushAndSpawnPointCount = in.nextInt();

		for (int i = 0; i < bushAndSpawnPointCount; i++) {
			String entityType = in.next(); // BUSH, from wood1 it can also be SPAWN

			int x = in.nextInt();
			int y = in.nextInt();
			int radius = in.nextInt();

			if (entityType.equals(BUSH_TYPE)) {
				g.bushes.add(new Bush(x, y, radius));
			} else if (entityType.equals(SPAWN_TYPE)) {
				g.spawns.add(new Spawn(x, y, radius));
			}
		}
		int itemCount = in.nextInt(); // useful from wood2
		for (int i = 0; i < itemCount; i++) {
			String itemName = in.next(); // contains keywords such as BRONZE, SILVER and BLADE, BOOTS connected by "_"
											// to help you sort easier
			int itemCost = in.nextInt(); // BRONZE items have lowest cost, the most expensive items are LEGENDARY
			int damage = in.nextInt(); // keyword BLADE is present if the most important item stat is damage
			int health = in.nextInt();
			int maxHealth = in.nextInt();
			int mana = in.nextInt();
			int maxMana = in.nextInt();
			int moveSpeed = in.nextInt(); // keyword BOOTS is present if the most important item stat is moveSpeed
			int manaRegeneration = in.nextInt();
			int isPotion = in.nextInt(); // 0 if it's not instantly consumed

			g.addItem(itemName, itemCost, damage, health, maxHealth, mana, maxMana, moveSpeed, manaRegeneration,
					isPotion);
		}

		// game loop
		while (true) {
			Play.initTurn(g);
			int gold = in.nextInt();
			int enemyGold = in.nextInt();
			g.myTeam.gold = gold;
			g.hisTeam.gold = enemyGold;
			int roundType = in.nextInt(); // a positive value will show the number of heroes that await a command
			g.heroMovesLeft = roundType;
			int entityCount = in.nextInt();
			for (int i = 0; i < entityCount; i++) {
				int unitId = in.nextInt();
				int team = in.nextInt();
				String unitType = in.next(); // UNIT, HERO, TOWER, can also be GROOT from wood1
				int x = in.nextInt();
				int y = in.nextInt();
				int attackRange = in.nextInt();
				int health = in.nextInt();
				int maxHealth = in.nextInt();
				int shield = in.nextInt(); // useful in bronze
				int attackDamage = in.nextInt();
				int movementSpeed = in.nextInt();
				int stunDuration = in.nextInt(); // useful in bronze
				int goldValue = in.nextInt();
				int countDown1 = in.nextInt(); // all countDown and mana variables are useful starting in bronze
				int countDown2 = in.nextInt();
				int countDown3 = in.nextInt();
				int mana = in.nextInt();
				int maxMana = in.nextInt();
				int manaRegeneration = in.nextInt();
				String heroType = in.next(); // DEADPOOL, VALKYRIE, DOCTOR_STRANGE, HULK, IRONMAN
				int isVisible = in.nextInt(); // 0 if it isn't
				int itemsOwned = in.nextInt(); // useful from wood1

				if (unitType.equals(UnitType.UNIT.name())) {
					g.updateUnit(unitId, team, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed);
				} else if (unitType.equals(UnitType.HERO.name())) {
					g.updateHero(unitId, team, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, mana,
							maxMana, manaRegeneration, heroType, isVisible);
				} else if (unitType.equals(UnitType.TOWER.name())) {
					g.updateTower(unitId, team, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed);
				} else if (unitType.equals(UnitType.GROOT.name())) {

				}
			}

			// Write an action using System.out.println()
			// To debug: System.err.println("Debug messages...");

			// If roundType has a negative value then you need to output a Hero name, such
			// as "DEADPOOL" or "VALKYRIE".
			// Else you need to output roundType number of any valid action, such as "WAIT"
			// or "ATTACK unitId"
			Play.play(g);
		}
	}
}