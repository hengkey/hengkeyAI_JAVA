import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class ShortPathGuerrilla {
	public ShortPathGuerrilla(String name) {
		this.name = name;
		sourcePos = null;
		targetPos = null;
		init();
	}

	private String name;
	private Position sourcePos;
	private Position targetPos;

	public static final int maxGuerillaPosNum = 39;
	public static final int maxNodeNum = 70;
	private Map<String, GuerillaPos> posMap = new HashMap<>();
	private Map<String, GuerillaPath> pathMap = new HashMap<>();
	public TilePosition prePos = null;

	static int[][] guerillaTilePos = { { 8, 10 }, { 8, 29 }, { 7, 37 }, { 29, 37 }, { 36, 15 }, { 45, 23 }, { 51, 38 },
			{ 54, 15 }, { 64, 4 }, { 74, 15 }, { 83, 24 }, { 77, 39 }, { 92, 14 }, { 98, 37 }, { 120, 36 }, { 120, 29 },
			{ 119, 10 }, { 16, 64 }, { 27, 63 }, { 64, 64 }, { 101, 63 }, { 112, 64 }, { 8, 93 }, { 30, 90 }, { 9, 99 },
			{ 9, 120 }, { 35, 113 }, { 46, 104 }, { 52, 89 }, { 54, 113 }, { 63, 122 }, { 74, 113 }, { 81, 104 },
			{ 75, 89 }, { 92, 112 }, { 97, 90 }, { 118, 92 }, { 118, 99 }, { 119, 119 } };

	static int[][] nodeInfo = { { 1, 2 }, { 2, 3 }, { 2, 5 }, { 3, 5 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 5, 8 },
			{ 4, 6 }, { 6, 8 }, { 6, 7 }, { 8, 9 }, { 17, 16 }, { 16, 15 }, { 16, 13 }, { 15, 13 }, { 15, 14 },
			{ 14, 13 }, { 13, 11 }, { 13, 10 }, { 14, 11 }, { 11, 10 }, { 11, 12 }, { 10, 9 }, { 4, 7 }, { 7, 12 },
			{ 12, 14 }, { 4, 19 }, { 7, 19 }, { 7, 27 }, { 7, 20 }, { 12, 20 }, { 12, 34 }, { 12, 21 }, { 14, 21 },
			{ 18, 19 }, { 19, 24 }, { 21, 22 }, { 19, 29 }, { 20, 29 }, { 20, 34 }, { 21, 34 }, { 21, 36 }, { 23, 25 },
			{ 25, 26 }, { 23, 24 }, { 23, 27 }, { 25, 27 }, { 24, 29 }, { 24, 28 }, { 24, 27 }, { 29, 28 }, { 28, 30 },
			{ 28, 27 }, { 27, 30 }, { 30, 31 }, { 29, 34 }, { 37, 38 }, { 38, 39 }, { 37, 36 }, { 37, 35 }, { 38, 35 },
			{ 36, 34 }, { 36, 33 }, { 36, 35 }, { 34, 33 }, { 33, 32 }, { 33, 35 }, { 35, 32 }, { 32, 31 } };

	public void init() {
		for (int i = 0; i < maxGuerillaPosNum; i++) {
			posMap.put("[" + guerillaTilePos[i][0] + " ," + guerillaTilePos[i][0] + "]",
					new GuerillaPos(new TilePosition(guerillaTilePos[i][0], guerillaTilePos[i][1]), true, true));
		}
		
		for (int i = 0; i < maxNodeNum; i++) {
			TilePosition srcPos = new TilePosition(guerillaTilePos[nodeInfo[i][0] - 1][0],
					guerillaTilePos[nodeInfo[i][0] - 1][1]);
			TilePosition targetPos = new TilePosition(guerillaTilePos[nodeInfo[i][1] - 1][0],
					guerillaTilePos[nodeInfo[i][1] - 1][1]);

			// 정방향설정
			pathMap.put("{" + srcPos.toString() + "=>" + targetPos.toString() + "}",
					new GuerillaPath(srcPos, targetPos, true, true));

			// 역방향설정
			pathMap.put("{" + targetPos.toString() + "=>" + srcPos.toString() + "}",
					new GuerillaPath(targetPos, srcPos, true, true));
		}

		for (String iterator : pathMap.keySet()) {
			System.out.println(iterator);
		}
	}

	public Position getSourcePos() {
		return sourcePos;
	}

	public Position getTargetPos() {
		return targetPos;
	}

	public void resetValidFlag() {
		for (String iterator : pathMap.keySet()) {
			pathMap.get(iterator).validFlag = true;
		}
	}

	public void resetEnemyValidFlag() {
		for (String iterator : pathMap.keySet()) {
			pathMap.get(iterator).enemyValidFlag = true;
		}
	}

	public void setPosValidFlag(TilePosition pos, boolean flag) {
		pathMap.get(pos.toString()).validFlag = flag;
	}
	
	public void setPosEnemyValidFlag(TilePosition pos, boolean flag) {
		pathMap.get(pos.toString()).enemyValidFlag = flag;
	}
	
	public void setPathValidFlag(TilePosition srcPos, TilePosition targetPos, boolean flag) {
		pathMap.get("{" + srcPos.toString() + "=>" + targetPos.toString() + "}").validFlag = flag;
	}
	
	public void setPathEnemyValidFlag(TilePosition srcPos, TilePosition targetPos, boolean flag) {
		pathMap.get("{" + srcPos.toString() + "=>" + targetPos.toString() + "}").enemyValidFlag = flag;
	}

	// 업데이트 적군 존재 위치
	public void updateEnemyRegion() {

		resetEnemyValidFlag();

		// System.out.println(
		// "MyBotModule.Broodwar.enemy().getUnits().size()" +
		// MyBotModule.Broodwar.enemy().getUnits().size() + " "
		// + new Exception().getStackTrace()[0].getLineNumber());

		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (CommandUtil.IsCombatUnit(unit) && unit.getType() != UnitType.Unknown) {
				// System.out.println(unit.getType() +
				// unit.getPosition().toTilePosition().toString() + " "
				// + unit.getType().sightRange() + " " + new
				// Exception().getStackTrace()[0].getLineNumber());
				for (int i = 1; i <= maxGuerillaPosNum; i++) {
					Position tmpPos = new Position(guerillaTilePos[i - 1][0] * 32, guerillaTilePos[i - 1][1] * 32);
					if (unit.getType().groundWeapon().maxRange() > unit.getPosition().getDistance(tmpPos)) {
//						list[i].get(0).enemyValidFlag = false;
						// System.out.print("false" + "(" + i + ", " + "x=" + list[i].get(0).x + ",y="
						// + list[i].get(0).y + ")" + " " + new
						// Exception().getStackTrace()[0].getLineNumber());
						// System.out.print("(" + i + ")");
						MyBotModule.Broodwar.drawCircleMap(tmpPos, 10, Color.Red, true);
					} else {
						MyBotModule.Broodwar.drawCircleMap(tmpPos, 10, Color.Green, true);
					}
				}
				// System.out.println();
			}
		}
	}
	
	// 큐를 사용한 업데이트 무게값 업데이트
	public void updateWeightValue(TilePosition curPos, TilePosition targetPos) {
		Queue<TilePosition> tmpQueue = new LinkedList<>();

		tmpQueue.offer(targetPos);

		while(!tmpQueue.isEmpty()){
			TilePosition tmpPos = tmpQueue.poll();
			posMap.get(tmpPos.toString());
		}
	}

	public Position getNextPos(Position curPos, Position targetPos) {
		int curPosIndex = 1, targetPosIndex = 1;
		Position nextPos = curPos;
		TilePosition nearNode = null;
		double preDistance = Math.pow(128, 2) * 2 + 1;
		double curDistance = 0;
		double preCurDoubleDistance = 999999;
		double preTargetDoubleDistance = 999999;

		// System.out.println("getNextPos Oh!! My God!!!" + " " + new
		// Exception().getStackTrace()[0].getLineNumber());
		TilePosition curTilePos = curPos.toTilePosition();
		TilePosition targetTilePos = targetPos.toTilePosition();

//		updateEnemyRegion();

		setPosValidFlag(curTilePos, false);

		for (String iterator : pathMap.keySet()) {
			if (!iterator.contains("{" + curTilePos.toString()))
				continue;

			nearNode = pathMap.get(iterator).targetPos;

			if (posMap.get(nearNode.toString()).validFlag == false || posMap.get(nearNode.toString()).enemyValidFlag == false)
				continue;

			curDistance = curTilePos.getDistance(nearNode);
			if (curDistance < preDistance) {
				preDistance = curDistance;
				nextPos = nearNode.toPosition();
			}
		}

		if (nextPos == null) {
			resetValidFlag();
			System.out.println("Oh!! My God!!!");
		}
		// System.out.println("curTilePos="+curTilePos.toString()+",=>"+"nextTilePos=" +
		// nextTilePos.toString() + " " + new
		// Exception().getStackTrace()[0].getLineNumber());

		return nextPos;
	}
}

class GuerillaPath {
	TilePosition srcPos;
	TilePosition targetPos;
	boolean validFlag;// 지나온 위치 false로해서 다시 안가도록
	boolean enemyValidFlag;// enemy가 공격 가능한 범위여부 판별

	public GuerillaPath(TilePosition srcPos, TilePosition targetPos, boolean validFlag, boolean enemyValidFlag) {
		this.srcPos = srcPos;
		this.targetPos = targetPos;
		this.validFlag = validFlag;
		this.enemyValidFlag = enemyValidFlag;
	}
}

class GuerillaPos {
	TilePosition Pos;
	int weightValue;
	boolean validFlag;// 해당 Position false로해서 다시 안가도록
	boolean enemyValidFlag;// enemy가 공격 가능한 범위여부 판별

	public GuerillaPos(TilePosition pos, boolean validFlag, boolean enemyValidFlag) {
		this.Pos = pos;
		this.weightValue = 0xFFFF;
		this.validFlag = validFlag;
		this.enemyValidFlag = enemyValidFlag;
	}
}