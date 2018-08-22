//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class MechanicMicroGoliath extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	private List<Unit> dropShipList = new ArrayList<>();
	
	private int saveUnitLevel = 1;
	
	private Map<String, ShortPathGuerrilla> shortPathInfo = new HashMap<>();
	private boolean attackWithTank = false;
	private int stickToTankRadius = 0;
	
	public ShortPathGuerrilla getShortPath(String squadName) {
		return shortPathInfo.get(squadName);
	}
	
	public void putShortPath(String shortPathName, ShortPathGuerrilla tmpShortPath) {
		shortPathInfo.put(shortPathName, tmpShortPath);
	}
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, List<Unit> dropShipList, int saveUnitLevel) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.dropShipList = dropShipList;
		this.saveUnitLevel = saveUnitLevel;
		
		this.attackWithTank = tankList.size() * 6 >= goliathList.size();
		if (this.attackWithTank) {
			this.stickToTankRadius = 140 + (int) (Math.log(goliathList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToTankRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit goliath) {
		if (!CommonUtils.executeUnitRotation(goliath, LagObserver.groupsize())) {
			return;
		}

		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawTextMap(goliath.getPosition().getX(), goliath.getPosition().getY() + 10,
					"" + order.getType());
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(goliath.getPosition(), 10, Color.Cyan, true);
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(goliath, enemiesInfo, saveUnitLevel); // 0: flee, 1: kiting, 2: attack
		KitingOption kOpt = KitingOption.defaultKitingOption();
		switch (decision.getDecision()) {
		case 0: // flee
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseFlee(goliath, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			boolean haveToFight = true;
			Unit closeTank = null;
			if (attackWithTank) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit tank : tankList) {
					int dist = goliath.getDistance(tank.getPosition());
					if (dist < closeDist) {
						closeTank = tank;
						closeDist = dist;
						// 가까운 곳에 탱크가 있으면 싸운다.
						if (closeDist < stickToTankRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			}
			if (haveToFight) {
				Unit enemy = MicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && goliath.isInWeaponRange(enemy)) {
					goliath.holdPosition();
				} else {
					kOpt.setGoalPosition(order.getPosition());
					MicroUtils.preciseKiting(goliath, decision.getTargetInfo(), kOpt);
				}
			} else {
				// 가까운 곳에 없으면 탱크로 이동
//				kOpt.setGoalPosition(closeTank.getPosition());
//				kOpt.setCooltimeAlwaysAttack(false);
				CommandUtil.move(goliath, closeTank.getPosition());
			}
			break;
			
		case 2: // attack move
			if (MicroSet.Common.versusMechanicSet()) {
				// 테란전용 go
				int distToOrder = goliath.getDistance(order.getPosition());
				if (distToOrder <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition의 둘러싼 대형을 만든다.
					if (goliath.isIdle() || goliath.isBraking()) {
						if (!goliath.isBeingHealed()) {
							Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), 100);
							CommandUtil.attackMove(goliath, randomPosition);
						}
					}
				} else {
					CommandUtil.attackMove(goliath, order.getPosition());
				}
				
			} else {
				Position movePosition = order.getPosition();
				
				// 이동지역까지 attackMove로 간다.
				if (goliath.getDistance(movePosition) > order.getRadius()) {
//					CommandUtil.attackMove(goliath, movePosition);
					CommandUtil.move(goliath, movePosition);
					
				} else { // 목적지 도착
					if (goliath.isIdle() || goliath.isBraking()) {
						if (!goliath.isBeingHealed()) {
							Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), 100);
							CommandUtil.attackMove(goliath, randomPosition);
						}
					}
				}
			}
			break;
		}
	}
	
	public void executeMechanicMicroForDR(Unit goliath) {
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawTextMap(goliath.getPosition().getX(), goliath.getPosition().getY() + 10,
					"" + order.getType());
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(goliath.getPosition(), 10, Color.Purple, true);
		
		if (goliath.getDistance(order.getPosition()) > 300) {
			for (Unit dropShip : dropShipList) {
				if (dropShip.canLoad(goliath))
					CommandUtil.rightClick(goliath, dropShip);
			}
		} else {
			CommandUtil.attackMove(goliath, order.getPosition());
		}
	}
	
	public void executeMechanicMultiGuerillaMicro(Unit goliath, ShortPathGuerrilla tmpShortPathGuerrilla) {
		// checker : 각각의 목표지역(travelBase)으로 이동. (order position은 null이다.)
		// watcher : 목표지역(적base)으로 이동. 앞에 보이지 않는 적이 있으면 본진base로 후퇴.
		Position movePosition = tmpShortPathGuerrilla.getTargetPos();

		//squad 할당되고 맨처음
		if (movePosition==null) {
			movePosition = tmpShortPathGuerrilla.getNextPos(goliath.getPosition(), order.getPosition(), true);
//			System.out.println("move to " + movePosition.toTilePosition().toString() + " "
//					+ new Exception().getStackTrace()[0].getLineNumber());
		}
		//sub target에 도착함.
		else if (goliath.getDistance(movePosition) < MicroSet.Vulture.MULTIGEURILLA_RADIUS / 2) {
			movePosition = tmpShortPathGuerrilla.getNextPos(movePosition, order.getPosition(), true);
//			System.out.println("move to " + movePosition.toTilePosition().toString() + " "
//					+ new Exception().getStackTrace()[0].getLineNumber());
		}
				
		List<Unit> enemies = MapGrid.Instance().getUnitsNear(goliath.getPosition(),
				MicroSet.Vulture.MULTIGEURILLA_RADIUS + 200, false, true,
				InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace));

		// 워커가 없을때만 범위내 랜덤공격 워커가 있으면 해당 워커 하나씩 공격
		if (enemies.isEmpty()) {
			CommandUtil.attackMove(goliath, movePosition);
		} else {
			for (Unit worker : enemies) {
				if (worker.getType() == UnitType.Terran_SCV) {
					CommandUtil.rightClick(goliath, worker);
					if (Config.DrawHengDebugInfo)
						MyBotModule.Broodwar.drawCircleMap(worker.getPosition(),
								MicroSet.Vulture.MULTIGEURILLA_RADIUS / 4, Color.Yellow, false);
					break;
				} else {
					CommandUtil.attackMove(goliath, movePosition);
				}
			}
		}

		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(movePosition, MicroSet.Vulture.MULTIGEURILLA_RADIUS / 5, Color.Blue,
					false);

		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawTextMap(goliath.getPosition().getX(), goliath.getPosition().getY() + 10,
				order.getType() + order.getPosition().toTilePosition().toString());
		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawCircleMap(goliath.getPosition(), 10, Color.Orange, true);
	}
}
