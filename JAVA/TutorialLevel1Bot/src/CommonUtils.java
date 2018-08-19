//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

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
