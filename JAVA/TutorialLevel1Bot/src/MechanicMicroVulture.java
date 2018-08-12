//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class MechanicMicroVulture extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> notVultureUnitList = new ArrayList<>();
	private int saveUnitLevel = 1;
	
	private boolean attackWithMechanics = false;
	private int stickToMechanicRadius = 0;
	
	private Map<String, ShortPathGuerrilla> shortPathInfo = new HashMap<>();
	int attackIgnoreFrame = 0;
	int preDistanceFromEnemy = 0xffff;
	
	public ShortPathGuerrilla getShortPath(String squadName) {
		return shortPathInfo.get(squadName);
	}
	
	public void putShortPath(String shortPathName, ShortPathGuerrilla tmpShortPath) {
		shortPathInfo.put(shortPathName, tmpShortPath);
	}
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = MicroUtils.filterTargetInfos(enemiesInfo, false);
	}
	
	public void prepareMechanicAdditional(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel, boolean attackWithMechanics) {
		this.notVultureUnitList.clear();
		this.notVultureUnitList.addAll(tankList);
		this.notVultureUnitList.addAll(goliathList);
		this.saveUnitLevel = saveUnitLevel;
		this.attackWithMechanics = attackWithMechanics && notVultureUnitList.size() > 0;
		if (this.attackWithMechanics) {
			this.stickToMechanicRadius = 120 + (int) (Math.log(vultureList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToMechanicRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit vulture) {
		if (!CommonUtils.executeUnitRotation(vulture, LagObserver.groupsize())) {
			return;
		}
		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawTextMap(vulture.getPosition().getX(), vulture.getPosition().getY() + 10,
				"" + order.getType());
		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawCircleMap(vulture.getPosition(), 10, Color.Cyan, true);
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(vulture, enemiesInfo, null, saveUnitLevel); // 0: flee, 1: kiting, 2: attack

		KitingOption kOpt = KitingOption.vultureKitingOption();
		Position retreatPosition = order.getPosition();
		switch (decision.getDecision()) {
		case 0: // flee 아예 싸울 생각이 없는 도망 : 성큰, 캐논, 시즈 등 접근금지, 또는 regroup 시
			if (order.getType() == SquadOrderType.WATCH || order.getType() == SquadOrderType.CHECK) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseFlee(vulture, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			
			if (order.getType() == SquadOrderType.WATCH) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			} else if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					retreatPosition = travelBase.getPosition();
				}
			}

			boolean haveToFight = true;
			Unit closeMechanic = null;
			if (attackWithMechanics) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit mechanicUnit : notVultureUnitList) {
					int dist = vulture.getDistance(mechanicUnit.getPosition());
					if (dist < closeDist) {
						closeMechanic = mechanicUnit;
						closeDist = dist;
						// 가까운 곳에 메카닉유닛이 있으면 싸운다.
						if (closeDist < stickToMechanicRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			} else {
				for (Unit notVultureUnit : notVultureUnitList) {
					if (vulture.getDistance(notVultureUnit) < MicroSet.Common.MAIN_SQUAD_COVERAGE) {
						kOpt = KitingOption.defaultKitingOption();
						retreatPosition = notVultureUnit.getPosition();
						break;
					}
				}
			}

			if (haveToFight) {
				Unit enemy = MicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && vulture.isInWeaponRange(enemy)) {
					vulture.holdPosition();
				} else {
					kOpt.setGoalPosition(retreatPosition);
					MicroUtils.preciseKiting(vulture, decision.getTargetInfo(), kOpt);
				}
			} else {
				CommandUtil.move(vulture, closeMechanic.getPosition());
			}
			break;
			
		case 2: // attack move
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			
			// checker : 각각의 목표지역(travelBase)으로 이동. (order position은 null이다.)
			// watcher : 목표지역(적base)으로 이동. 앞에 보이지 않는 적이 있으면 본진base로 후퇴.
			Position movePosition = order.getPosition();
			if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					movePosition = travelBase.getPosition();
				}
			}
			
			if (MicroSet.Common.versusMechanicSet() && order.getType() != SquadOrderType.GUERILLA && order.getType() != SquadOrderType.CHECK) {
				// 테란전용 go
				int distToOrder = vulture.getDistance(movePosition);
				if (distToOrder <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition의 둘러싼 대형을 만든다.
					if (vulture.isIdle() || vulture.isBraking()) {
						if (!vulture.isBeingHealed()) {
							Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), 100);
							CommandUtil.attackMove(vulture, randomPosition);
						}
					}
				} else {
					CommandUtil.attackMove(vulture, movePosition);
				}
				
			} else {
				// 이동지역까지 attackMove로 간다.
				if (vulture.getDistance(movePosition) > order.getRadius()) {
//					if (saveUnit) {
						CommandUtil.move(vulture, movePosition);
//					} else {
//						CommandUtil.attackMove(vulture, movePosition);
//					}
					
				} else { // 목적지 도착
					if (vulture.isIdle() || vulture.isBraking()) {
//						System.out.println("reach target" + " " + new Exception().getStackTrace()[0].getLineNumber());
						Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), 100);
						CommandUtil.attackMove(vulture, randomPosition);
					}
				}
			}
			break;
		}
	}
	
	public void executeMechanicMultiGuerillaMicro(Unit vulture, ShortPathGuerrilla tmpShortPathGuerrilla) {
		// checker : 각각의 목표지역(travelBase)으로 이동. (order position은 null이다.)
		// watcher : 목표지역(적base)으로 이동. 앞에 보이지 않는 적이 있으면 본진base로 후퇴.
		Position movePosition = tmpShortPathGuerrilla.getTargetPos();

		//squad 할당되고 맨처음
		if (movePosition==null) {
			movePosition = tmpShortPathGuerrilla.getNextPos(vulture.getPosition(), order.getPosition(), true);
//			System.out.println("move to " + movePosition.toTilePosition().toString() + " "
//					+ new Exception().getStackTrace()[0].getLineNumber());
		}
		//sub target에 도착함.
		else if (vulture.getDistance(movePosition) < MicroSet.Vulture.MULTIGEURILLA_RADIUS / 2) {
			movePosition = tmpShortPathGuerrilla.getNextPos(movePosition, order.getPosition(), true);
//			System.out.println("move to " + movePosition.toTilePosition().toString() + " "
//					+ new Exception().getStackTrace()[0].getLineNumber());
		}
		else
		{
			// source -> target 으로 이동중간에 적을 만나면 target을 변경하여 다른경로로 이동
			for(UnitInfo enemyInfo:enemiesInfo)
			{
				if(enemyInfo.getUnit().getType().isWorker() || !CommandUtil.IsCombatUnit(enemyInfo.getUnit()) )
					continue;
				
//				if (attackIgnoreFrame > 0)
//					attackIgnoreFrame--;
				
				// 공격할수 있는 unit을 만났을경우
				if (vulture.getDistance(enemyInfo.getUnit().getPosition()) <= UnitType.Terran_Siege_Tank_Siege_Mode
						.groundWeapon().maxRange() + 100) {
					tmpShortPathGuerrilla.setPathMapValidFlag(false);
					// movePosition =
					// tmpShortPathGuerrilla.getNextPos(tmpShortPathGuerrilla.getSourcePos(),
					// order.getPosition(), true);

					// movePosition = tmpShortPathGuerrilla.getSourcePos();
					// Position tmpPos=new Position(294, 329);
					movePosition = tmpShortPathGuerrilla.getPosOutOfEnemy(vulture, enemyInfo);

					tmpShortPathGuerrilla.setSourcetPos(movePosition);
					tmpShortPathGuerrilla.setTargetPos(movePosition);
//					attackIgnoreFrame = 1000;
//					preDistanceFromEnemy = vulture.getDistance(enemyInfo.getUnit().getPosition());
//					System.out.println(enemyInfo.getUnit().getType() + vulture.getPosition().toTilePosition().toString()
//							+ "^^^^^^^^^^^" + tmpShortPathGuerrilla.getSourcePos().toTilePosition().toString() + "=>"
//							+ movePosition.toTilePosition().toString());
					if (Config.DrawHengDebugInfo)
					MyBotModule.Broodwar.drawCircleMap(enemyInfo.getUnit().getPosition(),
							UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 100, Color.Red, false);
					break;
				}
//				else
//				{
//					attackIgnoreFrame = 0;
//					preDistanceFromEnemy = 0xffff;
//				}
			}			
		}
				
		// 이동지역까지 attackMove로 간다.
		if (vulture.getDistance(order.getPosition()) > order.getRadius()) {
			vulture.move(movePosition);
			// CommandUtil.move(vulture, movePosition);
			// System.out.println("move to " + movePosition.toTilePosition().toString() + "
			// "
			// + new Exception().getStackTrace()[0].getLineNumber());
			if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(movePosition, MicroSet.Vulture.MULTIGEURILLA_RADIUS / 5, Color.Blue,
					false);
		} else { // 목적지 도착
			if (vulture.isIdle() || vulture.isBraking()) {
//				Position randomPosition = MicroUtils.randomPosition(order.getPosition(), 200);
				
				List<Unit> workers = MapGrid.Instance().getUnitsNear(order.getPosition(),
						MicroSet.Vulture.MULTIGEURILLA_RADIUS+10, false, true,
						InformationManager.Instance().getWorkerType(InformationManager.Instance().enemyRace));
				
				// 워커가 없을때만 범위내 랜덤공격 워커가 있으면 해당 워커 하나씩 공격
				if (workers.isEmpty()) {
//					randomPosition = MicroUtils.randomPosition(order.getPosition(), MicroSet.Vulture.MULTIGEURILLA_RADIUS);
//						CommandUtil.attackMove(vulture, randomPosition);
				} else {
						vulture.rightClick(workers.get(0));
				}
				
//				CommandUtil.attackMove(vulture, randomPosition);
//				vulture.holdPosition();
//				System.out.println("hold to " + workers.get(0).getType() + vulture.getTilePosition().toString() + " "
//						+ new Exception().getStackTrace()[0].getLineNumber());
				if (Config.DrawHengDebugInfo)
				MyBotModule.Broodwar.drawCircleMap(movePosition, MicroSet.Vulture.MULTIGEURILLA_RADIUS / 4,
						Color.Orange, false);
			}
		}
		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawTextMap(vulture.getPosition().getX(), vulture.getPosition().getY() + 10,
				order.getType() + order.getPosition().toTilePosition().toString());
		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawCircleMap(vulture.getPosition(), 10, Color.Orange, true);
	}
	
	private boolean useReservedSpiderMine(Unit vulture) {
		// 마인매설이 예약된 벌처라면 매설실행
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine != null) {
			CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
			return true;
		}
		return false;
	}
	
	private boolean reserveSpiderMine(Unit vulture) {
		
		Position minePosition = null;
		
		if (CombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.MINE_STRATEGY_FOR_TERRAN) > 0 && order.getType() != SquadOrderType.CHECK) {
			BaseLocation enemyFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
			if (enemyFirstExpansion != null) {
				int distance = vulture.getDistance(enemyFirstExpansion.getPosition());
				if (distance < MicroSet.Tank.SIEGE_MODE_MAX_RANGE && MicroUtils.isSafePlace(enemyFirstExpansion.getPosition())) {
					minePosition = SpiderMineManger.Instance().positionToMine(vulture, enemyFirstExpansion.getPosition(), true, MicroSet.Vulture.spiderMineNumPerPosition * 2);
				}
			}
			
			if (minePosition == null) {
				Position enemyReadyPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().enemyPlayer);
				
				int distance = vulture.getDistance(enemyReadyPos);
				if (distance <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
					minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerPosition);
				}
			}
			
		} else {
			minePosition = SpiderMineManger.Instance().goodPositionToMine(vulture, MicroSet.Vulture.spiderMineNumPerGoodPosition);
			
			if (minePosition == null && order.getType() == SquadOrderType.WATCH) {
//				// 적 유닛에게 마인 선물하기
//				if (InformationManager.Instance().enemyRace == Race.Terran && saveUnitLevel == 0) {
//					minePosition = SpiderMineManger.Instance().enemyPositionToMine(vulture, enemiesInfo);
//				}
				int mineCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Vulture_Spider_Mine);
				if (mineCount <= MicroSet.Vulture.MINE_MAX_NUM) {
					// 맵 구석구석 마인 심기
					Region vultureRegion = BWTA.getRegion(vulture.getPosition());
					BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);

					int minePrepareLevel = SpiderMineManger.Instance().getMineInMyBaseLevel(); // 0: 본진매설X, 점령지역조금, 1: 본진매설X, 점령지역많이, 2: 본진조금, 점령지역많이
					boolean vultureInMyBaseRegion = vultureRegion == BWTA.getRegion(base.getPosition());
					if (!vultureInMyBaseRegion || minePrepareLevel >= 2) { // 본진 region에는 마인 설치안함(단 패스트 다크, 패스트 럴커 등인 경우 매설)
						boolean occupiedRegion = false;
						for (BaseLocation occupiedBase : occupiedBases) { // 앞마당 포함한 점령지역에 마인을 적게 매설함(단, 히드라웨이브, 드라군 푸시인 경우 많이 매설)
							if (vultureRegion == BWTA.getRegion(occupiedBase.getPosition())) {
								occupiedRegion = true;
								break;
							}
						}
						if (!occupiedRegion || (!vultureInMyBaseRegion && minePrepareLevel >= 1)) {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerPosition); // 그외에는 좀 많이
						} else {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerGoodPosition);
						}
					}
				}
			}
		}
		if (minePosition != null) { // 매설할 마인이 있다면 종료
			return true;
		}
		return false;
	}
	
	private boolean removeSpiderMine(Unit vulture) {
		return SpiderMineManger.Instance().removeMine(vulture);
	}

}
