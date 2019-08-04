import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

enum UnitType {
	UNIT, HERO, TOWER, GROOT

}

enum HeroType {
	DEADPOOL, HULK, VALKYRIE, IRONMAN, DOCTOR_STRANGE
}

enum MoveType {
	WAIT("WAIT"), MOVE("MOVE"), ATTACK_ID("ATTACK"), ATTACK_NEAREST("ATTACK_NEAREST"), MOVE_ATTACK("MOVE_ATTACK");

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
	Map<Integer, Character> charactersById = new HashMap<>();

	List<Bush> bushes = new ArrayList<>();
	List<Spawn> spawns = new ArrayList<>();

	public void updateHero(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth,
			int attackDamage, int movementSpeed, int mana, int maxMana, int manaRegeneration, String heroType,
			int isVisible) {
		if (charactersById.containsKey(id)) {
			charactersById.get(id).update(maxMana, y, maxHealth, isVisible);
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
		// Add to global character map
		charactersById.put(id, newHero);

		// Add to team
		if (myTeam.teamId == teamId) {
			myTeam.heros.add(newHero);
		} else {
			hisTeam.heros.add(newHero);
		}
	}
}

class Position {
	int x;
	int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class Team {
	int teamId;
	int gold;
	List<Hero> heros = new ArrayList<>();
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
	int isPotion;
}

abstract class Character {
	final int id;
	final int teamId;
	final int attackRange;
	final int maxHealth;
	final int attackDamage;
	final int movementSpeed;
	Position position = new Position(-1, -1);
	int health;
	boolean isVisible;

	protected Character(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed, int isVisible) {
		// Init Character final values
		this.id = id;
		this.teamId = teamId;
		this.attackRange = attackRange;
		this.maxHealth = maxHealth;
		this.attackDamage = attackDamage;
		this.movementSpeed = movementSpeed;

		// Init Character variable values
		update(x, y, health, isVisible);
	}

	protected void update(int x, int y, int health, int isVisible) {
		this.position.x = x;
		this.position.y = y;
		this.isVisible = isVisible == 0;
		this.health = health;
	}
}

class Unit extends Character {

	protected Unit(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed, int isVisible) {
		super(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, isVisible);
	}

}

class Hero extends Character {
	HeroType heroType;
	final int maxMana;
	int mana;

	protected Hero(int id, int teamId, int x, int y, int attackRange, int health, int maxHealth, int attackDamage,
			int movementSpeed, int mana, int maxMana, int manaRegeneration, String heroType, int isVisible) {
		super(id, teamId, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, isVisible);
		this.heroType = HeroType.valueOf(heroType);
		this.maxMana = maxMana;

		this.update(x, y, health, mana, isVisible);
	}

	public void update(int x, int y, int health, int mana, int isVisible) {
		super.update(x, y, health, isVisible);
		this.mana = mana;
	}
}

abstract class BushOrSpawn {
	private Position position;
	private int radius;

	public BushOrSpawn(int x, int y, int radius) {
		this.position = new Position(x, y);
		this.radius = radius;
	}

	public Position getPosition() {
		return position;
	}

	public int getRadius() {
		return radius;
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

	public static void play(Global g) {
		if (g.heroMovesLeft < 0) {
			Strategy.playFirstMove();
		}

		for (int i = 0; i < g.heroMovesLeft; i++) {
			Strategy.decideMove(g).playCommand();
		}
	}
}

class Move {
	MoveType moveType = null;
	Position position = new Position(-1, -1);
	int unitId = -1;
	UnitType unitType = null;

	String msgToPrint = null;

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

	public void addMessage(String msg) {
		if (this.msgToPrint == null) {
			msgToPrint = msg;
		} else {
			msgToPrint = msgToPrint + " | " + msg;
		}
	}

	public void playCommand() {
		System.out.println(command());
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

		default:
			System.err.println("ERROR not Move Type !");
			command = MoveType.WAIT.command();
			break;
		}

		if (msgToPrint != null) {
			command = command + ";" + msgToPrint;
		}

		return command;
	}
}

final class Strategy {
	public static Move decideMove(Global g) {
		return Move.buildMoveAttackNearest(UnitType.TOWER);
	}

	public static void playFirstMove() {
		System.out.println(HeroType.DEADPOOL);
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
		}

		// game loop
		while (true) {
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

				} else if (unitType.equals(UnitType.HERO.name())) {
					g.updateHero(unitId, team, x, y, attackRange, health, maxHealth, attackDamage, movementSpeed, mana,
							maxMana, manaRegeneration, heroType, isVisible);
				} else if (unitType.equals(UnitType.TOWER.name())) {

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