import java.util.ArrayList;

import bwapi.Color;
import bwapi.Order;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

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
	public ArrayList<GuerillaPos>[] list;
	public int prePos = 1;

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
		list = new ArrayList[maxGuerillaPosNum + 1];

		for (int i = 0; i <= maxGuerillaPosNum; i++) {
			list[i] = new ArrayList<GuerillaPos>();
		}

		TilePos pos = new TilePos(0, 0);
		for (int i = 0; i < maxNodeNum; i++) {
			pos.x = guerillaTilePos[nodeInfo[i][0] - 1][0];
			pos.y = guerillaTilePos[nodeInfo[i][0] - 1][1];
			list[nodeInfo[i][0]].add(new GuerillaPos(pos, nodeInfo[i][1], true));

			pos.x = guerillaTilePos[nodeInfo[i][1] - 1][0];
			pos.y = guerillaTilePos[nodeInfo[i][1] - 1][1];
			list[nodeInfo[i][1]].add(new GuerillaPos(pos, nodeInfo[i][0], true));
		}

		for (int i = 1; i <= maxGuerillaPosNum; i++) {
			System.out.print("(" + i + ", " + "x=" + list[i].get(0).x + ",y=" + list[i].get(0).y + ")");
			for (int j = 0; j < list[i].size(); j++) {
				System.out.print("(" + i + "," + list[i].get(j).node + ")");
			}
			System.out.println("");
		}
	}

	public Position getSourcePos() {
		return sourcePos;
	}
	
	public Position getTargetPos() {
		return targetPos;
	}
	
	public void resetValidFlag() {
		for (int i = 1; i <= maxGuerillaPosNum; i++) {
			if (list[i].size() > 0)
				list[i].get(0).validFlag = true;
		}
	}

	public void resetEnemyValidFlag() {
		for (int i = 1; i <= maxGuerillaPosNum; i++) {
			if (list[i].size() > 0)
				list[i].get(0).enemyValidFlag = true;
		}
	}

	public void setEnemyValidFlag(Position pos) {
		for (int i = 0; i < maxGuerillaPosNum; i++) {
			if (guerillaTilePos[i][0] == pos.toTilePosition().getX()
					&& guerillaTilePos[i][1] == pos.toTilePosition().getY()) {
				list[i+1].get(0).enemyValidFlag = false;
				break;
			}
		}
	}
	
	// 업데이트 적군 존재 위치
	public void updateEnemyRegion() {

		resetEnemyValidFlag();

//		System.out.println(
//				"MyBotModule.Broodwar.enemy().getUnits().size()" + MyBotModule.Broodwar.enemy().getUnits().size() + " "
//						+ new Exception().getStackTrace()[0].getLineNumber());
		
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (CommandUtil.IsCombatUnit(unit) && unit.getType()!=UnitType.Unknown) {
//				System.out.println(unit.getType() + unit.getPosition().toTilePosition().toString() + " "
//						+ unit.getType().sightRange() + " " + new Exception().getStackTrace()[0].getLineNumber());
				for (int i = 1; i <= maxGuerillaPosNum; i++) {
					Position tmpPos = new Position(guerillaTilePos[i - 1][0] * 32, guerillaTilePos[i - 1][1] * 32);
					if (unit.getType().groundWeapon().maxRange() > unit.getPosition().getDistance(tmpPos)) {
						list[i].get(0).enemyValidFlag = false;
//						System.out.print("false" + "(" + i + ", " + "x=" + list[i].get(0).x + ",y="
//								+ list[i].get(0).y + ")" + " " + new Exception().getStackTrace()[0].getLineNumber());
//						System.out.print("(" + i + ")");
						MyBotModule.Broodwar.drawCircleMap(tmpPos, 10, Color.Red, true);
					} else {
						MyBotModule.Broodwar.drawCircleMap(tmpPos, 10, Color.Green, true);
					}
				}
//				System.out.println();
			}
		}
	}

	public Position getNextPos(Position curPos, Position targetPos) {
		int curPosIndex = 1, targetPosIndex = 1;
		int nextPosIndex = curPosIndex;
		int nearNode = 0;
		int preDistance = (int) Math.pow(128, 2) * 2 + 1;
		int curDistance = 0;
		double preCurDoubleDistance = 999999;
		double preTargetDoubleDistance = 999999;
//		System.out.println("getNextPos Oh!! My God!!!" + " " + new Exception().getStackTrace()[0].getLineNumber());
		TilePosition curTilePos = curPos.toTilePosition();
		TilePosition targetTilePos = targetPos.toTilePosition();

		updateEnemyRegion();

		// Tile값을 가장 가까운 Position table index값으로 변환
		for (int i = 0; i < maxGuerillaPosNum; i++) {
			// current
			if (curTilePos.getDistance(guerillaTilePos[i][0], guerillaTilePos[i][1]) < preCurDoubleDistance) {
				preCurDoubleDistance = curTilePos.getDistance(guerillaTilePos[i][0], guerillaTilePos[i][1]);
				curPosIndex = i + 1;
			}

			// target
			if (targetTilePos.getDistance(guerillaTilePos[i][0], guerillaTilePos[i][1]) < preTargetDoubleDistance) {
				preTargetDoubleDistance = targetTilePos.getDistance(guerillaTilePos[i][0], guerillaTilePos[i][1]);
				targetPosIndex = i + 1;
			}
		}

		prePos = curPosIndex;
		list[curPosIndex].get(0).validFlag = false;

		prePos = curPosIndex;

		for (int i = 0; i < list[curPosIndex].size(); i++) {
			nearNode = list[curPosIndex].get(i).node;

			if (list[nearNode].get(0).validFlag == false || list[nearNode].get(0).enemyValidFlag == false)
				continue;

			curDistance = (int) Math.pow(guerillaTilePos[targetPosIndex - 1][0] - guerillaTilePos[nearNode - 1][0], 2)
					+ (int) Math.pow(guerillaTilePos[targetPosIndex - 1][1] - guerillaTilePos[nearNode - 1][1], 2);
			if (curDistance < preDistance) {
				preDistance = curDistance;
				nextPosIndex = nearNode;
			}
		}

		TilePosition sourceTilePos = new TilePosition(guerillaTilePos[curPosIndex - 1][0],
				guerillaTilePos[curPosIndex - 1][1]);
		sourcePos = sourceTilePos.toPosition();
		
		TilePosition nextTilePos = new TilePosition(guerillaTilePos[nextPosIndex - 1][0],
				guerillaTilePos[nextPosIndex - 1][1]);
		Position nextPos = nextTilePos.toPosition();
		targetPos = nextPos;

		if (nextPosIndex == prePos) {
			resetValidFlag();
			System.out.println("Oh!! My God!!!");
		}
//		System.out.println("curTilePos="+curTilePos.toString()+",=>"+"nextTilePos=" + nextTilePos.toString() + " " + new Exception().getStackTrace()[0].getLineNumber());
		
		return nextPos;
	}
}

class GuerillaPos {
	int x;
	int y;
	int node;
	boolean validFlag;// 지나온 위치 false로해서 다시 안가도록
	boolean enemyValidFlag;// enemy가 공격 가능한 범위여부 판별

	public GuerillaPos(TilePos pos, int node, boolean value) {
		this.x = pos.x;
		this.y = pos.y;
		this.node = node;
		this.validFlag = value;
		this.enemyValidFlag = value;
	}
}

class TilePos {
	int x;
	int y;

	public TilePos(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
