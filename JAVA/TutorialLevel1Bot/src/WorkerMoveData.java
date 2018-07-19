/* Base Code 출처 : 2017년 알고리즘 경진대회 "피뿌리는 컴파일러" 팀 코드 */

import bwapi.Position;

public class WorkerMoveData
{
	private int mineralsNeeded;
	private int gasNeeded;
	private Position position;

	public WorkerMoveData(int m, int g, Position p)
	{
		mineralsNeeded = m;
		gasNeeded = g;
		position = p;
	}

	public int getMineralsNeeded() {
		return mineralsNeeded;
	}

	public int getGasNeeded() {
		return gasNeeded;
	}

	public Position getPosition() {
		return position;
	}
};