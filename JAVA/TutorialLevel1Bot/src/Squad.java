//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.ArrayList;
import java.util.List;

import bwapi.Color;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class Squad {
	
	public Squad(String name, SquadOrder order, int priority) {
		this.name = name;
		this.order = order;
		this.priority = priority;
		this.frameNum = 0;
		this.ignoreDropShipFrame = 0;
		this.progressLevel = 0;
	}
	
	private String name;
	private SquadOrder order;
	private int priority;
	private boolean pushLine;
	private int frameNum;
	private int ignoreDropShipFrame;
	private int progressLevel;

	public static final int Drop_Init = 1;
	public static final int Drop_Loaded = 2;
	public static final int Drop_AllNeared = 3;
	public static final int Drop_MoveToWallComplete = 4;
	public static final int Drop_MoveWithWallToDestComplete = 5;
	public static final int Drop_UnloadComplete = 6;
	
	public int getProgressLevel() {
		return progressLevel;
	}

	public void setProgressLevel(int progressLevel) {
		this.progressLevel = progressLevel;
	}

	public static final int IgnoreFrameValue = 3200;
	public static final int DestRange = 400;
	public static final int closestRange = 32 * 2;
	
	public int getIgnoreDropShipFrame() {
		return ignoreDropShipFrame;
	}

	public void setIgnoreDropShipFrame(int ignoreDropShipFrame) {
		this.ignoreDropShipFrame = ignoreDropShipFrame;
	}

	//	public MicroScv microScv = new MicroScv();
	public MicroMarine microMarine = new MicroMarine();
	public MicroVulture microVulture = new MicroVulture();
	public MicroTank microTank = new MicroTank();
	public MicroGoliath microGoliath = new MicroGoliath();
	public MicroDropShip microDropShip = new MicroDropShip();
	public MicroWraith microWraith = new MicroWraith();
	public MicroVessel microVessel = new MicroVessel();
	public MicroBuilding microBuilding = new MicroBuilding();

	// **** 아래변수들은 팩토리 유닛으로 구성된 Squad 전용이다.
	private int initFrame = 0;
	private MechanicMicroVulture mechanicVulture = new MechanicMicroVulture();
	private MechanicMicroTank mechanicTank = new MechanicMicroTank();
	private MechanicMicroGoliath mechanicGoliath = new MechanicMicroGoliath();
	private MechanicMicroDropShip mechanicDropShip = new MechanicMicroDropShip();

	private List<Unit> unitSet = new ArrayList<>();
//	private Map<Integer, Boolean> nearEnemy = new HashMap<>();

	public String getName() {
		return name;
	}
	public SquadOrder getOrder() {
		return order;
	}
	public void setOrder(SquadOrder order) {
		this.order = order;
	}
	public int getPriority() {
		return priority;
	}

	public List<Unit> getUnitSet() {
		return unitSet;
	}
	
	public void addUnit(Unit unit) {
		unitSet.add(unit);
	}
	
	public void update() {
		
		updateUnits();
		if (unitSet.isEmpty()) {
			return;
		}
		
		if (name.equals(SquadName.MAIN_ATTACK)) 
		{
			updateMainAttackSquad();
			return;
		} 
		else if (name.equals(SquadName.CHECKER) || name.startsWith(SquadName.GUERILLA_)) 
		{
			updateVultureSquad();
			return;
		} 
		else if (name.startsWith(SquadName.MULTIGUERILLA_)) 
		{
			updateMultiGuerillaSquad();
			return;
		} 
		else if (name.startsWith(SquadName.DROPSHIP)) 
		{
			updateDropShipSquad();
			return;
		} 
		else if (name.startsWith(SquadName.BASE_DEFENSE_)) {
			updateDefenseSquad();
			return;
		} 
		else if (name.equals(SquadName.IDLE)) 
		{
			return;
		}
		
		if (microVulture.getUnits().size() > 0
				|| microTank.getUnits().size() > 0
				|| microGoliath.getUnits().size() > 0) {
			MyBotModule.Broodwar.sendText("!!!!!! MECHANIC UNIT ASSIGN ERROR !!!!!! 010 2626 9786");
		}
		
		// 방어병력은 눈앞의 적을 무시하고 방어를 위해 이동해야 한다.
		List<Unit> nearbyEnemies = new ArrayList<>();
		if (order.getType() == SquadOrderType.DEFEND) {
			MapGrid.Instance().getUnitsNear(nearbyEnemies, order.getPosition(), order.getRadius(), false, true);
		} else {
			for (Unit unit : unitSet) {
				MapGrid.Instance().getUnitsNear(nearbyEnemies, unit.getPosition(), order.getRadius(), false, true);
			}
		}
		
//		microScv.setMicroInformation(order, nearbyEnemies);
		microMarine.setMicroInformation(order, nearbyEnemies);
		microVulture.setMicroInformation(order, nearbyEnemies);
		microTank.setMicroInformation(order, nearbyEnemies);
		microGoliath.setMicroInformation(order, nearbyEnemies);
		microDropShip.setMicroInformation(order, nearbyEnemies);
		microWraith.setMicroInformation(order, nearbyEnemies);
		microVessel.setMicroInformation(order, nearbyEnemies);
		microBuilding.setMicroInformation(order, nearbyEnemies);
		
//		microScv.execute();
//		microMarine.execute();
		microVulture.execute();
		microTank.execute();
		microGoliath.execute();
		microDropShip.execute();
		microWraith.execute();
		microVessel.execute();
		microBuilding.execute();
	}
	
	private void updateMainAttackSquad() {
		List<UnitInfo> vultureEnemies = new ArrayList<>();
		List<UnitInfo> attackerEnemies = new ArrayList<>();
		List<UnitInfo> closeTankEnemies = new ArrayList<>();
		
		// 명령 준비
		SquadOrder attackerOrder = this.order;
		SquadOrder watchOrder = null;
		
		Position watchPosition = null;
		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		if (enemyBase != null) {
			watchPosition = enemyBase.getPosition();
		} else {
			watchPosition = CombatManager.Instance().letsFindRat();
		}
		watchOrder = new SquadOrder(SquadOrderType.WATCH, watchPosition, Combat.WATCHER_RADIUS, "Watch over");
		// WATCHER'S ENEMY
		for (Unit vulture : microVulture.getUnits()) {
			InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(), InformationManager.Instance().enemyPlayer, watchOrder.getRadius());
		}
		if (CombatManager.Instance().getCombatStrategy() != CombatStrategy.DEFENCE_INSIDE && CombatManager.Instance().getCombatStrategy() != CombatStrategy.DEFENCE_CHOKEPOINT) {
			// TANK & GOLIATH'S ENEMY
			for (Unit tank : microTank.getUnits()) {
				InformationManager.Instance().getNearbyForce(attackerEnemies, tank.getPosition(), InformationManager.Instance().enemyPlayer, attackerOrder.getRadius());
			}
			for (Unit goliath : microGoliath.getUnits()) {
				InformationManager.Instance().getNearbyForce(attackerEnemies, goliath.getPosition(), InformationManager.Instance().enemyPlayer, attackerOrder.getRadius());
			}
		}
		InformationManager.Instance().getNearbyForce(attackerEnemies, attackerOrder.getPosition(), InformationManager.Instance().enemyPlayer, attackerOrder.getRadius());

		// TANK 거리재기 상대
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			List<UnitInfo> nearTankEnemies = new ArrayList<>();
			for (Unit tank : microTank.getUnits()) {
				InformationManager.Instance().getNearbyForce(nearTankEnemies, tank.getPosition(), InformationManager.Instance().enemyPlayer, 0);
			}
			for (UnitInfo enemyInfo : nearTankEnemies) {
				Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);
				if (enemy != null) {
					if (!CommandUtil.IsValidUnit(enemy)) {
						continue;
					}
				}
				
				if (enemyInfo.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || enemyInfo.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					closeTankEnemies.add(enemyInfo);
				}
			}
		}
		
//		List<Unit> peloton = new ArrayList<>();
//		Unit leader = MicroUtils.leaderOfUnit(microVulture.getUnits(), watchOrder.getPosition());
//		for (Unit vulture : microVulture.getUnits()) {
//			if (vulture == leader || vulture.getDistance(leader.getPosition()) < MicroSet.Vulture.PELOTON_RADIUS) {
//				peloton.add(vulture);
//			}
//		}
//		boolean saveUnit = !CombatExpectation.expectByUnitInfo(peloton, vultureEnemies);
//		mechanicVulture.prepareMechanic(watchOrder, vultureEnemies);
//		mechanicVulture.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), saveUnit);
//		for (Unit vulture : microVulture.getUnits()) {
//			mechanicVulture.executeMechanicMicro(vulture);
//		}
		
		// 시즈, 골리앗 병력의 적이 있을 때를 전투시작(initiate)으로 정한다.
		if (attackerEnemies.isEmpty()) {
			if (initFrame > 0) {
//				MyBotModule.Broodwar.sendText("finished");
				initFrame = 0;
			}
		} else {
			if (initFrame == 0) {
				boolean timeToInitiate = false;
				for (UnitInfo enemy : attackerEnemies) {
					if (enemy.getType() != UnitType.Terran_Vulture_Spider_Mine
							&& enemy.getType() != UnitType.Zerg_Larva
							&& !enemy.getType().isBuilding()
							&& !enemy.getType().isWorker()
							&& !enemy.getType().isFlyer()) {
						timeToInitiate = true;
						break;
					}
				}
				
				if (timeToInitiate) {
					initFrame = MyBotModule.Broodwar.getFrameCount();
//					MyBotModule.Broodwar.sendText("initiated");
				}
			}
		}
		
		// 벌처 전투 여부 판단
		boolean attackWithMechanic = false;
		int saveUnitLevelVulture = 1;
		if (initFrame > 0) { // initiate 됐다면 탱크, 골리앗과 함께 싸운다.
//			if (CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY) {
				attackWithMechanic = true;
//			}
//			List<UnitInfo> allEnemies = new ArrayList<>();
//			allEnemies.addAll(vultureEnemies);
//			allEnemies.addAll(attackerEnemies);
//			
//			List<Unit> allMyUnits = new ArrayList<>();
//			allMyUnits.addAll(microVulture.getUnits());
//			allMyUnits.addAll(microTank.getUnits());
//			allMyUnits.addAll(microGoliath.getUnits());
			
//			Result result = CombatExpectation.expectByUnitInfo(microVulture.getUnits(), vultureEnemies, false);
//			if (result == Result.Loss || result == Result.Win) {
//				saveUnitLevelVulture = 1;
//			} else if (result == Result.BigWin) {
//				saveUnitLevelVulture = 0;
//			}
			
		} else {
			// initiate가 아닌 벌처 단독 활동시 임무
			if (CombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.VULTURE_JOIN_SQUAD) > 0) { // 후퇴명령
				watchOrder.setPosition(attackerOrder.getPosition());
				if (CombatManager.Instance().getCombatStrategy() != CombatStrategy.ATTACK_ENEMY) {
					vultureEnemies = attackerEnemies;
				}
				saveUnitLevelVulture = 2;
			} else {
				boolean skipBuilding = false;
//				if (CommonUtils.executeRotation(0, 20 * 24)) {
//					skipBuilding = false;
//				}
				Result result = CombatExpectation.expectByUnitInfo(microVulture.getUnits(), vultureEnemies, skipBuilding);
				if (result == Result.Loss) {
					CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.VULTURE_JOIN_SQUAD, MicroSet.Vulture.getVultureJoinSquadFrame(InformationManager.Instance().enemyRace));
					saveUnitLevelVulture = 2;
				} else if (result == Result.Win) {
					saveUnitLevelVulture = 1;
				} else if (result == Result.BigWin) {
					if (MicroSet.Common.versusMechanicSet()) {
						saveUnitLevelVulture = 0;
					} else {
						saveUnitLevelVulture = 1;
					}
				}
//				else if (result == Result.BigWin) {
//					saveUnitLevelVulture = 0;
//				} 
			}
		}
		
		// 탱크 vs 탱크 전투 판단여부
		int saveUnitLevelTank = 1;
		int saveUnitLevelGoliath = 1;
		int saveUnitLevelDropShip = 1;
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if (closeTankEnemies.size() * 3 <= microTank.getUnits().size()) {
//				System.out.println("go ahead");
				saveUnitLevelTank = 1; // 거리재기 전진
			} else {
//				System.out.println("keep in line");
				saveUnitLevelTank = 2; // 안전거리 유지
			}
		}
		
		if (CombatManager.Instance().getCombatStrategy() == CombatStrategy.ATTACK_ENEMY
				&& CombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.ATTACK_NO_MERCY) > 0) { // strategy manager 판단
			saveUnitLevelVulture = saveUnitLevelTank = saveUnitLevelGoliath = 0;
		} else if (InformationManager.Instance().enemyRace != Race.Terran && MyBotModule.Broodwar.self().supplyUsed() >= 360) { // combat manager 자체 판단
			saveUnitLevelVulture = saveUnitLevelTank = saveUnitLevelGoliath = 0;
		} else if (InformationManager.Instance().enemyRace == Race.Terran && CombatManager.Instance().pushSiegeLine) {
			saveUnitLevelVulture = saveUnitLevelTank = saveUnitLevelGoliath = 0;
		}
		
//		if (InformationManager.Instance().enemyRace == Race.Terran) {
//			if (!pushLine && MyBotModule.Broodwar.self().supplyUsed() >= 380
//					&& MyBotModule.Broodwar.self().minerals() >= 2000) {
//				MyBotModule.Broodwar.printf("LET'S GO!!");
//				pushLine = true;
//			} else if (pushLine && MyBotModule.Broodwar.self().supplyUsed() < 310) {
//				MyBotModule.Broodwar.printf("RETREAT");
//				pushLine = false;
//			}
//		}
		
		mechanicVulture.prepareMechanic(watchOrder, vultureEnemies);
		mechanicVulture.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), saveUnitLevelVulture, attackWithMechanic);
		mechanicTank.prepareMechanic(attackerOrder, attackerEnemies);
		mechanicTank.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), saveUnitLevelTank, initFrame);
		mechanicGoliath.prepareMechanic(attackerOrder, attackerEnemies);
		mechanicGoliath.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), saveUnitLevelGoliath);
		mechanicDropShip.prepareMechanic(attackerOrder, attackerEnemies);
		mechanicDropShip.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), saveUnitLevelDropShip, progressLevel);
		
		//부하분산을 위해 maxGroupNum frame으로 나누어 수행
		//제공해주는 frame값이 튀는 현상이 있는것 같아 자체 frame값 사용
		if (frameNum == 655000)
			frameNum = 0;
		else
			frameNum++;
		
		int groupNum = 0, maxGroupNum=3;
		for (Unit vulture : microVulture.getUnits()) {
			if(frameNum % maxGroupNum == groupNum)
				mechanicVulture.executeMechanicMicro(vulture);

			groupNum++;
			if (groupNum >= maxGroupNum)
				groupNum = 0;
		}

		groupNum = 0;
		for (Unit tank : microTank.getUnits()) {
			if(frameNum % maxGroupNum == groupNum)
				mechanicTank.executeMechanicMicro(tank);
			
			groupNum++;
			if (groupNum >= maxGroupNum)
				groupNum = 0;
		}

		groupNum = 0;
		for (Unit goliath : microGoliath.getUnits()) {
			if(frameNum % maxGroupNum == groupNum)
				mechanicGoliath.executeMechanicMicro(goliath);

			groupNum++;
			if (groupNum >= maxGroupNum)
				groupNum = 0;
		}
		
		for (Unit dropShip : microDropShip.getUnits()) {
			mechanicDropShip.executeMechanicMicro(dropShip);
		}
	}
	
	// checker, guerilla squad
	private void updateVultureSquad() {
		List<UnitInfo> vultureEnemies = new ArrayList<>();

		int saveUnitLevel = 1;
		// 적 정보 수집. 명령이 하달된 3초간은 근처 적을 무시
		if (name.equals(SquadName.CHECKER)) {
			for (Unit vulture : microVulture.getUnits()) {
				TravelSite site = VultureTravelManager.Instance().getSquadSiteMap().get(vulture.getID());
				if (site != null) {
					if (site.visitAssignedFrame != 0 && site.visitAssignedFrame > MyBotModule.Broodwar.getFrameCount() - MicroSet.Vulture.IGNORE_MOVE_FRAME) {
						continue;
					}
				}
				InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
			}
			saveUnitLevel = 2;
		} else if (name.startsWith(SquadName.GUERILLA_)) {
			if (!VultureTravelManager.Instance().guerillaIgnoreModeEnabled(name)) {
				for (Unit vulture : microVulture.getUnits()) {
					InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
				}
			}
			InformationManager.Instance().getNearbyForce(vultureEnemies, order.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
			saveUnitLevel = 0;
//			System.out.println("LET'S GUEGUEGUERILLA!");
		}

//		List<Unit> peloton = new ArrayList<>();
//		Unit leader = MicroUtils.leaderOfUnit(microVulture.getUnits(), order.getPosition());
//		for (Unit vulture : microVulture.getUnits()) {
//			if (vulture == leader || vulture.getDistance(leader.getPosition()) < MicroSet.Vulture.PELOTON_RADIUS) {
//				peloton.add(vulture);
//			}
//		}

//		boolean saveUnit = !CombatExpectation.expectByUnitInfo(microVulture.getUnits(), vultureEnemies);
		
		mechanicVulture.prepareMechanic(order, vultureEnemies);
		mechanicVulture.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), saveUnitLevel, false);
		
		for (Unit vulture : microVulture.getUnits()) {
			mechanicVulture.executeMechanicMicro(vulture);
		}
	}
	
	private void updateMultiGuerillaSquad() {
		List<UnitInfo> vultureEnemies = new ArrayList<>();
		List<UnitInfo> goliathEnemies = new ArrayList<>();

		if (Config.DrawHengDebugInfo)
		MyBotModule.Broodwar.drawCircleMap(order.getPosition(), 100, Color.Orange, false);
		
		//vulture
		int saveUnitLevel = 0;
		for (Unit vulture : microVulture.getUnits()) {
			InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(),
					InformationManager.Instance().enemyPlayer,
					UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 50);
		}
		
		InformationManager.Instance().getNearbyForce(vultureEnemies, order.getPosition(),
				InformationManager.Instance().enemyPlayer, order.getRadius());
	
		mechanicVulture.prepareMechanic(order, vultureEnemies);
		mechanicVulture.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), saveUnitLevel, false);
		
		for (Unit vulture : microVulture.getUnits()) {
			String shortPathName = SquadName.MULTIGUERILLA_ + vulture.getType() + vulture.getID();
			ShortPathGuerrilla tmpShortPath = mechanicVulture.getShortPath(shortPathName);
			if(tmpShortPath==null)
			{
				tmpShortPath = new ShortPathGuerrilla(shortPathName);
				mechanicVulture.putShortPath(shortPathName, tmpShortPath);
			}
			
			mechanicVulture.executeMechanicMultiGuerillaMicro(vulture, tmpShortPath);
		}
		
		//Goliath
		saveUnitLevel = 0;
		for (Unit goliath : microGoliath.getUnits()) {
			InformationManager.Instance().getNearbyForce(goliathEnemies, goliath.getPosition(),
					InformationManager.Instance().enemyPlayer,
					UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 50);
		}
		
		InformationManager.Instance().getNearbyForce(goliathEnemies, order.getPosition(),
				InformationManager.Instance().enemyPlayer, order.getRadius());
	
		mechanicGoliath.prepareMechanic(order, goliathEnemies);
		mechanicGoliath.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), saveUnitLevel);
		
		for (Unit goliath : microGoliath.getUnits()) {
			String shortPathName = SquadName.MULTIGUERILLA_ + goliath.getType() + goliath.getID();
			ShortPathGuerrilla tmpShortPath = mechanicGoliath.getShortPath(shortPathName);
			if(tmpShortPath==null)
			{
				tmpShortPath = new ShortPathGuerrilla(shortPathName);
				mechanicGoliath.putShortPath(shortPathName, tmpShortPath);
			}
			
			mechanicGoliath.executeMechanicMultiGuerillaMicro(goliath, tmpShortPath);
		}
	}
	
	private void updateDropShipSquad() {
		List<UnitInfo> nearbyEnemiesInfo = new ArrayList<>();
		BaseLocation selfmainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);

		for (Unit dropShip : microDropShip.getUnits()) {
			InformationManager.Instance().getNearbyForce(nearbyEnemiesInfo, dropShip.getPosition(),
					InformationManager.Instance().enemyPlayer, UnitType.Terran_Dropship.sightRange());
		}
		
		//이동하다 중간에 적군멀티나 미사일터렛을 발견했을때 바로 내린다
		boolean enemyFlag = false;
		for (UnitInfo enemyInfo : nearbyEnemiesInfo) {
			if (enemyInfo.getType() == UnitType.Terran_Missile_Turret)
				enemyFlag = true;
			if (enemyInfo.getType() == UnitType.Terran_Command_Center)
				enemyFlag = true;
			// if (enemyInfo.getType() == UnitType.Terran_Wraith) {
			// enemyFlag = true;
			// }
			
			if (enemyFlag == true) {				
				System.out.println("enemyInfo.getType()="+enemyInfo.getType()+enemyInfo.getUnit().getPosition());
				order = new SquadOrder(SquadOrderType.DROPSHIP, enemyInfo.getUnit().getPosition(),
						UnitType.Terran_Dropship.sightRange(), "DropShip");
				break;
			}
		}
//		System.out.println("microTank.getUnits().size()="+microTank.getUnits().size()+" "+new Exception().getStackTrace()[0].getLineNumber());
//		System.out.println("microGoliath.getUnits().size()="+microGoliath.getUnits().size()+" "+new Exception().getStackTrace()[0].getLineNumber());
		if(microTank.getUnits().size()==0 && microGoliath.getUnits().size()==0)
		{
			if (progressLevel == Squad.Drop_Init) {
				progressLevel = Squad.Drop_Loaded;
				System.out.println(
						"progressLevel=" + progressLevel + " " + new Exception().getStackTrace()[0].getLineNumber());
			}
		}

		int fullDropShipCnt=0;//꽉찬 드랍십
		int nearestToWallCnt=0;//벽에붙은 드랍십
		int destToCnt=0;//목적지에 도착한 드랍십
		int canUnloadCnt=0;//목적지에 도착한 드랍십
		Position tmpPosition = selfmainBaseLocations.getPosition();
		for (Unit tmpDropShip : microDropShip.getUnits()) {
			if (tmpDropShip.canLoad() == false) {
				fullDropShipCnt++;
			}
			
			tmpPosition = new Position((tmpPosition.getX() / 2048) * 4064, (tmpPosition.getY() / 2048) * 4064);
			int diffY = Math.abs(tmpDropShip.getPosition().getY() - tmpPosition.getY());
			if (diffY < closestRange)
				nearestToWallCnt++;
			
			if (tmpDropShip.getDistance(order.getPosition()) < Squad.DestRange)
				destToCnt++;
			
			if (tmpDropShip.canUnload())
				canUnloadCnt++;
		}
		
		if (fullDropShipCnt >= microDropShip.getUnits().size()) {
			if (progressLevel == Squad.Drop_Init) {
				progressLevel = Squad.Drop_Loaded;
				System.out.println(
						"progressLevel=" + progressLevel + " " + new Exception().getStackTrace()[0].getLineNumber());
			}

		}
		
		if( mechanicDropShip.isNearestFlag() == true) {
			if (progressLevel == Squad.Drop_Loaded) {
				progressLevel = Squad.Drop_AllNeared;
				System.out.println(
						"progressLevel=" + progressLevel + " " + new Exception().getStackTrace()[0].getLineNumber());
			}
		}
		
		if (nearestToWallCnt >= microDropShip.getUnits().size()) {
			if (progressLevel == Squad.Drop_AllNeared) {
				progressLevel = Squad.Drop_MoveToWallComplete;
				System.out.println(
						"progressLevel=" + progressLevel + " " + new Exception().getStackTrace()[0].getLineNumber());
			}
		}	
		
		if (destToCnt >= microDropShip.getUnits().size()) {
			if (progressLevel == Squad.Drop_MoveToWallComplete) {
				progressLevel = Squad.Drop_MoveWithWallToDestComplete;
				System.out.println(
						"progressLevel=" + progressLevel + " " + new Exception().getStackTrace()[0].getLineNumber());
			}
		}	
		
		if (canUnloadCnt == 0) {
			if (progressLevel == Squad.Drop_MoveWithWallToDestComplete)
				progressLevel = Squad.Drop_UnloadComplete;
		}
		
		mechanicTank.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicTank.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), 1, 0);
		mechanicGoliath.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicGoliath.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), 1);
		mechanicDropShip.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicDropShip.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), 1, progressLevel);
		
		for (Unit tank : microTank.getUnits()) {
			mechanicTank.executeMechanicMicro(tank);
		}
		for (Unit goliath : microGoliath.getUnits()) {
			mechanicGoliath.executeMechanicMicroForDR(goliath);
		}
		for (Unit dropShip : microDropShip.getUnits()) {
			mechanicDropShip.executeMechanicMicro(dropShip);
		}
	}
	
	private void updateDefenseSquad() {
		List<UnitInfo> nearbyEnemiesInfo = new ArrayList<>();
		if (MicroSet.Common.versusMechanicSet()) {
			for (Unit vulture : microVulture.getUnits()) {
				InformationManager.Instance().getNearbyForce(nearbyEnemiesInfo, vulture.getPosition(), InformationManager.Instance().enemyPlayer, UnitType.Terran_Vulture.sightRange());
			}
			for (Unit tank : microTank.getUnits()) {
				InformationManager.Instance().getNearbyForce(nearbyEnemiesInfo, tank.getPosition(), InformationManager.Instance().enemyPlayer, UnitType.Terran_Siege_Tank_Tank_Mode.sightRange());
			}
			for (Unit goliath : microGoliath.getUnits()) {
				InformationManager.Instance().getNearbyForce(nearbyEnemiesInfo, goliath.getPosition(), InformationManager.Instance().enemyPlayer, UnitType.Terran_Goliath.sightRange());
			}
			
		}
		InformationManager.Instance().getNearbyForce(nearbyEnemiesInfo, order.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
		
		mechanicVulture.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicVulture.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), 1, true);
		mechanicTank.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicTank.prepareMechanicAdditional(microVulture.getUnits(), microTank.getUnits(), microGoliath.getUnits(), microDropShip.getUnits(), 1, 0);
		mechanicGoliath.prepareMechanic(order, nearbyEnemiesInfo);
		
//		LagTest lagTest = LagTest.startTest(true);
//		lagTest.setDuration(3000);
		for (Unit vulture : microVulture.getUnits()) {
			mechanicVulture.executeMechanicMicro(vulture);
//			lagTest.estimate();
		}
		for (Unit tank : microTank.getUnits()) {
			mechanicTank.executeMechanicMicro(tank);
//			lagTest.estimate();
		}
		for (Unit goliath : microGoliath.getUnits()) {
			mechanicGoliath.executeMechanicMicro(goliath);
//			lagTest.estimate();
		}

		List<Unit> nearbyEnemies = new ArrayList<>();
		if (order.getType() == SquadOrderType.DEFEND) {
			MapGrid.Instance().getUnitsNear(nearbyEnemies, order.getPosition(), order.getRadius(), false, true);
		}
		microMarine.setMicroInformation(order, nearbyEnemies);
		microMarine.execute();
//		lagTest.estimate();
	}
	
	private void updateUnits() {
		setAllUnits();
//		setNearEnemyUnits();
		addUnitsToMicroManagers();
	}
	
	public void setAllUnits() {
		// TOOD 필요시 사용
//		_hasAir = false;
//		_hasGround = false;
//		_hasAntiAir = false;
//		_hasAntiGround = false;
		List<Unit> validUnits = new ArrayList<>();
		for (Unit unit : unitSet) {
			if (CommandUtil.IsValidUnit(unit)) {
				validUnits.add(unit);
			}
		}
		unitSet = validUnits;
	}
	
//	public void setNearEnemyUnits() {
//		nearEnemy.clear();
//		
//		for (Unit unit : unitSet) {
//			if (!unit.exists() || unit.isLoaded()) {
//				continue;
//			}
//			nearEnemy.put(unit.getID(), unitNearEnemy(unit));
//		}
//	}
	
	public boolean unitNearEnemy(Unit unit) {
//		List<Unit> enemyNear = MapGrid.Instance().getUnitsNear(unit.getPosition(), 400, false, true);
//		return enemyNear.size() > 0;
		List<Unit> unitsInRadius = unit.getUnitsInRadius(400);
		for (Unit u : unitsInRadius) {
			if (u.getPlayer() == InformationManager.Instance().enemyPlayer) {
				return true;
			}
		}
		return false;
	}
	
	public void addUnitsToMicroManagers() {
		List<Unit> scvUnits = new ArrayList<>();
		List<Unit> marineUnits = new ArrayList<>();
		List<Unit> vultureUnits = new ArrayList<>();
		List<Unit> tankUnits = new ArrayList<>();
		List<Unit> goliathUnits = new ArrayList<>();
		List<Unit> dropShipUnits = new ArrayList<>();
		List<Unit> wraithUnits = new ArrayList<>();
		List<Unit> vesselUnits = new ArrayList<>();
		List<Unit> buildingUnits = new ArrayList<>();

		for (Unit unit : unitSet) {
			if (!CommandUtil.IsValidUnit(unit) || unit.isLoaded()) {
				continue;
			}
			
			if (unit.getType() == UnitType.Terran_SCV) {
				scvUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Marine) {
				marineUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Vulture) {
				vultureUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				tankUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Goliath) {
				goliathUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Dropship) {
				dropShipUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Wraith) {
				wraithUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Science_Vessel) {
				vesselUnits.add(unit);
			} else if ((unit.getType() == UnitType.Terran_Engineering_Bay || unit.getType() == UnitType.Terran_Barracks) && unit.isLifted()){
				buildingUnits.add(unit);
			}
		}

//s		microScv.setUnits(scvUnits);
		microMarine.setUnits(marineUnits);
		microVulture.setUnits(vultureUnits);
		microTank.setUnits(tankUnits);
		microGoliath.setUnits(goliathUnits);
		microDropShip.setUnits(dropShipUnits);
		microWraith.setUnits(wraithUnits);
		microVessel.setUnits(vesselUnits);
		microBuilding.setUnits(buildingUnits);
		
	}
	
	public void removeUnit(Unit unit) {
		// TODO Unit Object로 찾으면 제대로 못찾는 현상이 발생하는 것 같다. -> 추가확인필요
		// 그래서 일단은 unit ID로 index를 찾아 remove로 한다.
		for (int idx = 0; idx < unitSet.size(); idx ++) {
			if (unitSet.get(idx).getID() == unit.getID()) {
				unitSet.remove(idx);
				return;
			}
		}
	}
	
	public boolean isEmpty() {
		return unitSet.isEmpty();
	}
	
	public void clear() {
		// 전투에 참여한 일꾼이 있다면 idle 상태로 변경
		for (Unit unit : unitSet) {
			if (unit.getType().isWorker()) {
				WorkerManager.Instance().setIdleWorker(unit);
			}
		}
		unitSet.clear();
	}
	
	@Override
	public String toString() {
		return "Squad [name=" + name + ", unitSet.size()=" + unitSet.size() + ", order=" + order + "]";
	}
	
}
