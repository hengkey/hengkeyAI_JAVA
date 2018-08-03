//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.List;

import bwapi.Unit;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
