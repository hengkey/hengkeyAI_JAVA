//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (�ǻѸ��������Ϸ� / �˰��� ������ȸ 2017)

import java.util.List;

import bwapi.Unit;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
