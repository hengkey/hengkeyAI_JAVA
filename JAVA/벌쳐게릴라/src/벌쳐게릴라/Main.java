package ¹úÃÄ°Ô¸±¶ó;

import java.util.ArrayList;

public class Main {
	public static final int maxGuerillaPosNum = 39;
	public static final int maxNodeNum = 70;
	public static ArrayList<GuerillaPos>[] list;

	static int[][] guerillaPos = { { 8, 10 }, { 8, 29 }, { 7, 37 }, { 29, 37 }, { 36, 15 }, { 45, 23 }, { 51, 38 },
			{ 54, 15 }, { 64, 4 }, { 74, 15 }, { 83, 24 }, { 77, 39 }, { 92, 14 }, { 98, 37 }, { 120, 36 }, { 120, 29 },
			{ 119, 10 }, { 16, 64 }, { 27, 63 }, { 64, 64 }, { 101, 63 }, { 112, 64 }, { 8, 93 }, { 30, 90 }, { 9, 99 },
			{ 9, 120 }, { 35, 113 }, { 46, 104 }, { 52, 89 }, { 54, 113 }, { 63, 122 }, { 74, 113 }, { 81, 104 },
			{ 75, 89 }, { 92, 112 }, { 97, 90 }, { 118, 92 }, { 118, 99 }, { 119, 119 } };

	static int[][] nodeInfo = { { 1, 2 }, { 2, 3 }, { 2, 5 }, { 3, 5 }, { 3, 4 }, { 4, 5 }, { 5, 6 }, { 5, 8 },
			{ 4, 6 }, { 6, 8 }, { 6, 7 }, { 8, 9 }, { 17, 16 }, { 16, 15 }, { 16, 13 }, { 15, 13 }, { 15, 14 },
			{ 14, 13 }, { 13, 11 }, { 13, 10 }, { 14, 11 }, { 11, 10 }, { 11, 12 }, { 10, 9 }, { 4, 7 }, { 7, 12 },
			{ 12, 14 }, { 4, 19 }, { 7, 19 }, { 7, 27 }, { 7, 20 }, { 12, 20 }, { 12, 34 }, { 12, 21 }, { 14, 21 },
			{ 18, 19 }, { 19, 24 }, { 21, 22 }, { 19, 24 }, { 20, 29 }, { 20, 34 }, { 21, 34 }, { 21, 36 }, { 23, 25 },
			{ 25, 26 }, { 23, 24 }, { 23, 27 }, { 25, 27 }, { 24, 29 }, { 24, 28 }, { 24, 27 }, { 29, 28 }, { 28, 30 },
			{ 28, 27 }, { 27, 30 }, { 30, 31 }, { 29, 34 }, { 37, 38 }, { 38, 39 }, { 37, 36 }, { 37, 35 }, { 38, 35 },
			{ 36, 34 }, { 36, 33 }, { 36, 35 }, { 34, 33 }, { 33, 32 }, { 33, 35 }, { 35, 32 }, { 32, 31 } };

	public static void main(String[] args) {
		list = new ArrayList[maxGuerillaPosNum + 1];

		for (int i = 0; i <= maxGuerillaPosNum; i++) {
			list[i] = new ArrayList<GuerillaPos>();
		}

		TilePos pos = new TilePos(0, 0);
		for (int i = 0; i < maxNodeNum; i++) {
			pos.x = guerillaPos[nodeInfo[i][0] - 1][0];
			pos.y = guerillaPos[nodeInfo[i][0] - 1][1];
			list[nodeInfo[i][0]].add(new GuerillaPos(pos, nodeInfo[i][1], true));

			pos.x = guerillaPos[nodeInfo[i][1] - 1][0];
			pos.y = guerillaPos[nodeInfo[i][1] - 1][1];
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

	public static TilePos getNextTilePos(int curPosIndex, int targetPosIndex) {
		int nextX = 0;
		int nextY = 0;
		int nearNode = 0;
		int preDistance = 128 * 128 * 2 + 1;
		int curDistance = 0;

		for (int i = 0; i < list[curPosIndex].size(); i++) {
			nearNode = list[curPosIndex].get(i).node;
			nextX = guerillaPos[nearNode][0] ;
			nextY = guerillaPos[nearNode][1] ;
			curDistance = (int) Math.pow(guerillaPos[targetPosIndex][0] - nextX, 2)
					+ (int) Math.pow(guerillaPos[targetPosIndex][1] - nextY, 2);
			if (curDistance < preDistance) {
				TilePos nextPos = new TilePos(nextX,nextY);
			}
		}

		return nextPos;
	}
}

class GuerillaPos {
	int x;
	int y;
	int node;
	boolean validFlag;

	public GuerillaPos(TilePos pos, int node, boolean value) {
		this.x = pos.x;
		this.y = pos.y;
		this.node = node;
		this.validFlag = value;
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
