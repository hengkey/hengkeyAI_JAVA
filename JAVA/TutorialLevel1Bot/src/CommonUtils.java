/* Base Code 출처 : 2017년 알고리즘 경진대회 "피뿌리는 컴파일러" 팀 코드 */

import bwapi.Unit;

public class CommonUtils {
	public static boolean executeRotation(int group, int rotationSize) {
		return MyBotModule.Broodwar.getFrameCount() % rotationSize == group;
	}
	
	public static boolean executeUnitRotation(Unit unit, int rotationSize) {
		int unitGroup = unit.getID() % rotationSize;
		return executeRotation(unitGroup, rotationSize);
	}
	
	public static void consoleOut(int testid, int unitid, String msg) {
		if (unitid == testid) System.out.println(msg);
	}
}
