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
			{ 12, 14 }, { 4, 19 }, { 7, 19 }, { 7, 29 }, { 7, 20 }, { 12, 20 }, { 12, 34 }, { 12, 21 }, { 14, 21 },
			{ 18, 19 }, { 19, 24 }, { 21, 22 }, { 19, 29 }, { 20, 29 }, { 20, 34 }, { 21, 34 }, { 21, 36 }, { 23, 25 },
			{ 25, 26 }, { 23, 24 }, { 23, 27 }, { 25, 27 }, { 24, 29 }, { 24, 28 }, { 24, 27 }, { 29, 28 }, { 28, 30 },
			{ 28, 27 }, { 27, 30 }, { 30, 31 }, { 29, 34 }, { 37, 38 }, { 38, 39 }, { 37, 36 }, { 37, 35 }, { 38, 35 },
			{ 36, 34 }, { 36, 33 }, { 36, 35 }, { 34, 33 }, { 33, 32 }, { 33, 35 }, { 35, 32 }, { 32, 31 } };

	public void init() {
		for (int i = 0; i < maxGuerillaPosNum; i++) {
			TilePosition tmpTilePos = new TilePosition(guerillaTilePos[i][0], guerillaTilePos[i][1]);
			posMap.put(tmpTilePos.toString(),
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
	
	public Position setTargetPos(Position pos) {
		this.targetPos = pos;
		return this.targetPos;
	}
	
	public Position setSourcetPos(Position pos) {
		this.sourcePos = pos;
		return this.sourcePos;
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
	
	public void setPathMapValidFlag(boolean flag) {
//		System.out.println(
//				"{" + this.sourcePos.toTilePosition().toString() + "=>" + this.targetPos.toTilePosition().toString()
//						+ "}" + " " + new Exception().getStackTrace()[0].getLineNumber());

		//양방향에 모두 적용 함.
		if (pathMap.get("{" + this.sourcePos.toTilePosition().toString() + "=>"
				+ this.targetPos.toTilePosition().toString() + "}") != null) {
			pathMap.get("{" + this.sourcePos.toTilePosition().toString() + "=>"
					+ this.targetPos.toTilePosition().toString() + "}").validFlag = flag;
		}
		
		//양방향에 모두 적용 함.
		if (pathMap.get("{" + this.targetPos.toTilePosition().toString() + "=>"
				+ this.sourcePos.toTilePosition().toString() + "}") != null) {
			pathMap.get("{" + this.targetPos.toTilePosition().toString() + "=>"
					+ this.sourcePos.toTilePosition().toString() + "}").validFlag = flag;
		}
	}
	
	public void setPathValidFlag(TilePosition srcPos, TilePosition targetPos, boolean flag) {
		pathMap.get("{" + srcPos.toString() + "=>" + targetPos.toString() + "}").validFlag = flag;
	}
	
	public void setPathEnemyValidFlag(TilePosition srcPos, TilePosition targetPos, boolean flag) {
		pathMap.get("{" + srcPos.toString() + "=>" + targetPos.toString() + "}").enemyValidFlag = flag;
	}

	public Position getPosOutOfEnemy(Unit vulture, UnitInfo enemyInfo) {
		int bestDistance = 0xffff;
		Position retPos=null;

		for (String iterator : posMap.keySet()) {
			if (enemyInfo.getUnit().getDistance(posMap.get(iterator).Pos
					.toPosition()) > UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 100) {
				if (vulture.getDistance(posMap.get(iterator).Pos.toPosition()) < bestDistance) {
					bestDistance = vulture.getDistance(posMap.get(iterator).Pos.toPosition());
					retPos = posMap.get(iterator).Pos.toPosition();
				}
			}
		}
		
		return retPos;
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
						if (Config.DrawHengDebugInfo)
						MyBotModule.Broodwar.drawCircleMap(tmpPos, 10, Color.Red, true);
					} else {
						if (Config.DrawHengDebugInfo)
						MyBotModule.Broodwar.drawCircleMap(tmpPos, 10, Color.Green, true);
					}
				}
				// System.out.println();
			}
		}
	}
	
	public void resetWeightValue() {
		for (String iterator : posMap.keySet()) {
			posMap.get(iterator).weightValue = 0xffff;
		}
	}
	
	// 큐를 사용한 업데이트 무게값 업데이트
	public void updateWeightValue(TilePosition curPos, TilePosition targetPos) {
		Queue<TilePosition> tmpQueue = new LinkedList<>();
		
		resetWeightValue();
		
		posMap.get(targetPos.toString()).weightValue=0;
		tmpQueue.offer(targetPos);

		pathMap.get("{[46, 104]=>[54, 113]}").validFlag = false;
		
		while(!tmpQueue.isEmpty()){
//			System.out.println("==============================");
			TilePosition tmpPos = tmpQueue.poll();
			for (String iterator : pathMap.keySet()) {
				if (iterator.contains("{" + tmpPos.toString())
						&& posMap.get(pathMap.get(iterator).targetPos.toString()).weightValue == 0xffff
						&& pathMap.get(iterator).validFlag == true) {
					posMap.get(pathMap.get(iterator).targetPos.toString()).weightValue = posMap
							.get(tmpPos.toString()).weightValue + 1;
					tmpQueue.offer(pathMap.get(iterator).targetPos);
					// System.out.println(iterator.toString());
				}
			}
//			System.out.println(tmpQueue);
//			break;
		}
		
		//탐색위치 무게값 출력
		for (String iterator : posMap.keySet()) {
			if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(posMap.get(iterator).Pos.toPosition(), 10, Color.Green, true);
			if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawTextMap(posMap.get(iterator).Pos.toPosition(),
					posMap.get(iterator).Pos.toString() + "(" + posMap.get(iterator).weightValue + ")");
//			System.out.println(posMap.get(iterator).Pos.toString() + "(" + posMap.get(iterator).weightValue + ")");
		}

		//탐색 path 정보 출력
		for (String iterator : pathMap.keySet()) {
			if (!pathMap.get(iterator).enemyValidFlag || !pathMap.get(iterator).validFlag) {
				if (Config.DrawHengDebugInfo)
				MyBotModule.Broodwar.drawLineMap(pathMap.get(iterator).srcPos.toPosition(),
						pathMap.get(iterator).targetPos.toPosition(), Color.Yellow);
			} else {
				if (Config.DrawHengDebugInfo)
				MyBotModule.Broodwar.drawLineMap(pathMap.get(iterator).srcPos.toPosition(),
						pathMap.get(iterator).targetPos.toPosition(), Color.Blue);
			}
		}
	}

	public Position getNextPos(Position curPos, Position targetPos, boolean weightFlag) {
		Position nextPos = curPos;
		TilePosition nearNode = null;
		double preCurDoubleDistance = 9999;
		double preTargetDoubleDistance = 9999;
		int bestLowWeight=0xffff;

		// System.out.println("getNextPos Oh!! My God!!!" + " " + new
		// Exception().getStackTrace()[0].getLineNumber());
		TilePosition curTilePos = curPos.toTilePosition();
		TilePosition targetTilePos = targetPos.toTilePosition();
		for (String iterator : posMap.keySet()) {
//			System.out.println(curPos.toTilePosition().getDistance(posMap.get(iterator).Pos) + " "
//					+ targetPos.toTilePosition().getDistance(posMap.get(iterator).Pos) + " " + preCurDoubleDistance
//					+ " " + preTargetDoubleDistance);
			if (curPos.toTilePosition().getDistance(posMap.get(iterator).Pos) < preCurDoubleDistance) {
//				System.out.println("current"+posMap.get(iterator).Pos.toString());
				curTilePos = posMap.get(iterator).Pos;
				preCurDoubleDistance = curPos.toTilePosition().getDistance(posMap.get(iterator).Pos);
			}
			
			if (targetPos.toTilePosition().getDistance(posMap.get(iterator).Pos) < preTargetDoubleDistance) {
//				System.out.println("target"+posMap.get(iterator).Pos.toString());				
				targetTilePos = posMap.get(iterator).Pos;
				preTargetDoubleDistance = targetPos.toTilePosition().getDistance(posMap.get(iterator).Pos);
			}
		}
		
//		System.out.println(curTilePos.toString()+targetTilePos.toString());
		
		if (weightFlag)
			updateWeightValue(curTilePos, targetTilePos);

		for (String iterator : pathMap.keySet()) {
			if (!iterator.contains("{" + curTilePos.toString()))
				continue;

			nearNode = pathMap.get(iterator).targetPos;

//			if (posMap.get(nearNode.toString()).validFlag == false || posMap.get(nearNode.toString()).enemyValidFlag == false)
//				continue;
			
			if (pathMap.get(iterator).validFlag == false)
				continue;

			if (posMap.get(nearNode.toString()).weightValue < bestLowWeight) {
				nextPos = nearNode.toPosition();
				bestLowWeight = posMap.get(nearNode.toString()).weightValue;
			}
		}

		if (nextPos == curPos) {
			resetValidFlag();
			System.out.println("Oh!! My God!!!");
		}
		// System.out.println("curTilePos="+curTilePos.toString()+",=>"+"nextTilePos=" +
		// nextTilePos.toString() + " " + new
		// Exception().getStackTrace()[0].getLineNumber());

		this.sourcePos = curTilePos.toPosition();
		this.targetPos = nextPos;
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