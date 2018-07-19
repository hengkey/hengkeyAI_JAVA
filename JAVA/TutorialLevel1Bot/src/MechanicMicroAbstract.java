/* Base Code 출처 : 2017년 알고리즘 경진대회 "피뿌리는 컴파일러" 팀 코드 */

import java.util.List;

import bwapi.Unit;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
