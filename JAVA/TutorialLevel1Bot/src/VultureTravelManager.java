//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class VultureTravelManager {

	private Map<String, Integer> guerillaTimeMap = new HashMap<>();
	private Map<Integer, TravelSite> vultureSiteMap = new HashMap<>();
	
	public Map<Integer, TravelSite> getSquadSiteMap() {
		return vultureSiteMap;
	}
	
	private final List<TravelSite> travelSites = new ArrayList<>();
	
	private boolean initialized = false;

	private static VultureTravelManager instance = new VultureTravelManager();
	
	private VultureTravelManager() {}
	
	public static VultureTravelManager Instance() {
		return instance;
	}
	
	private static boolean timeToShiftDuty = false;
	
	public static boolean timeToShiftDuty() {
		if (VultureTravelManager.timeToShiftDuty) {
			VultureTravelManager.timeToShiftDuty = false;
			return true;
		}
		return false;
	}
	
	public void init() {
		List<BaseLocation> otherBases = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
		
		if (!otherBases.isEmpty()) {
			travelSites.clear();
			for (BaseLocation base : otherBases) {
				if (base.getTilePosition().getX() > 60 && base.getTilePosition().getX() < 70
						&& base.getTilePosition().getY() > 60
						&& base.getTilePosition().getY() < 70) {
					continue;
				}
				
				System.out.println("travelSites="+base.getTilePosition().toString()+" "+ new Exception().getStackTrace()[0].getLineNumber());
				travelSites.add(new TravelSite(base, 0, 0, 0));
			}
			initialized = true;
		}
	}
	
	public void update() {
		if (!initialized) {
			init();
			return;
		}
		
		// 1. 벌처 이동지 관리
		for (TravelSite travelSite : travelSites) {
			if (MyBotModule.Broodwar.isVisible(travelSite.baseLocation.getTilePosition())) {
				// 1. 시야가 밝혀졌다면 visitFrame을 계속 업데이트 한다.
				travelSite.visitFrame = MyBotModule.Broodwar.getFrameCount();
				
				// 2. 시야가 밝혀진 travelSite를 방문예정인 스쿼드의 travelSite를 변경한다.
				Integer relatedVultureId = null;
				for (Integer id : vultureSiteMap.keySet()) {
					TravelSite site = vultureSiteMap.get(id);
					if (site.baseLocation.getPosition().equals(travelSite.baseLocation.getPosition())) {
						relatedVultureId = id; // 방문예정 스쿼드 명
						break;
					}
				}
				
				if (relatedVultureId != null) {
//					System.out.println("change travel site");
					BaseLocation currentBase = vultureSiteMap.get(relatedVultureId).baseLocation;
					vultureSiteMap.remove(relatedVultureId);
					getBestTravelSite(relatedVultureId, currentBase); // travelSite 변경(currentBase에서 가까운 곳)
				}
			}
		}

		// 2. 방문자 만료시간
		Integer expiredVisitor = null;
		for (Integer vultureId : vultureSiteMap.keySet()) {
			TravelSite site = vultureSiteMap.get(vultureId);
			if (site.visitAssignedFrame < MyBotModule.Broodwar.getFrameCount() - MicroSet.Vulture.CHECKER_INTERVAL_FRAME) {
				expiredVisitor = vultureId;
				break;
			}
		}
		if (expiredVisitor != null) {
			vultureSiteMap.remove(expiredVisitor);
		}

		// 3. 게릴라 적 무시 시간 관리
		String ignoreExpiredSquad = null;
		for (String squadName : guerillaTimeMap.keySet()) {
			Integer startTime = guerillaTimeMap.get(squadName);
			if (startTime != null && startTime < MyBotModule.Broodwar.getFrameCount() - MicroSet.Vulture.CHECKER_INTERVAL_FRAME) {
				ignoreExpiredSquad = squadName;
				break;
			}
		}
		if (ignoreExpiredSquad != null) {
			guerillaTimeMap.remove(ignoreExpiredSquad);
		}
		
		// 4. 벌처 정책 조정(각 주요 포인트 매설 마인수, checker수)
		if (CommonUtils.executeRotation(0, 48)) {
			int vultureCount = InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture);
			int mineCount = InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture_Spider_Mine);
			
			int mineNumPerPosition = vultureCount / 3 + 1;
			MicroSet.Vulture.spiderMineNumPerPosition = mineNumPerPosition;
			
			int bonumNumPerPosition = (mineCount - 10) / 8;
			if (bonumNumPerPosition > 0) {
				MicroSet.Vulture.spiderMineNumPerPosition += bonumNumPerPosition;
			}

			MicroSet.Vulture.spiderMineNumPerGoodPosition = vultureCount / 10 + 1;

			int checkerNum = vultureCount / 4; // 3대1 비율이다.
			MicroSet.Vulture.maxNumChecker = checkerNum >= 3 ? 3 : checkerNum; // 정찰벌처 최대 3기
//			System.out.println("vultureCount / maxNumChecker : " + vultureCount + " / " + MicroSet.Vulture.maxNumChecker);
		}
	}

	// 벌처스쿼드가 정찰할 base를 선택한다.
	// 1. 방문한지 가장 오래된 location으로 이동
	// 2. 방문한지 가장 오래된 장소가 복수개이면(아예 정찰이 안된) 할당된지 가장 오래된 location으로 이동
	// 3. 방문 및 할당된지 동일하게 오래된 장소가 복수개이면, 가까운 곳부터 간다. (currentBase에서 가장 가까운 곳, squadName만 입력받는 경우 적 앞마당에서 가장 가까운 곳)
	public BaseLocation getBestTravelSite(Integer vultureId) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		return getBestTravelSite(vultureId, firstExpansion);
	}
	
	public BaseLocation getBestTravelSite(Integer vultureId, BaseLocation currentBase) {
		if (!initialized) {
			return null;
		}
		
		TravelSite site = vultureSiteMap.get(vultureId);
		if (site != null) {
			return site.baseLocation;
		}
		
		int longestVisitPassedFrame = MicroSet.Vulture.CHECKER_INTERVAL_FRAME;
		double shortestDistance = 999999.0;
		
		TravelSite bestTravelSite = null;
		
		int currentFrame = MyBotModule.Broodwar.getFrameCount();
		
		for (TravelSite travelSite : travelSites) {
			if (vultureSiteMap.values().contains(travelSite)) {
				continue;
			}
			
//			System.out.println(travelSite.toString());
			
			int visitPassedFrame = currentFrame - travelSite.visitFrame;
			double distance = currentBase.getGroundDistance(travelSite.baseLocation);
			
			if (visitPassedFrame > longestVisitPassedFrame ||
					(visitPassedFrame == longestVisitPassedFrame && distance < shortestDistance)) {
				longestVisitPassedFrame = visitPassedFrame;
				shortestDistance = distance;
				bestTravelSite = travelSite;
			}
		}
//		System.out.println("====================================================");
		
		//앞마당 이후 적군의 다음 확장들에 최우선으로 마인을 박아 견재하기 위해
		if (MyBotModule.Broodwar.getFrameCount() < 12000) {
			if (MapGrid.Instance().getUnitsNear(travelSites.get(0).baseLocation.getPosition(), 100, true, false,
					UnitType.Terran_Vulture_Spider_Mine).isEmpty()) {
//				System.out
//						.println(travelSites.get(0).toString() + " " + new Exception().getStackTrace()[0].getLineNumber());
				if (!vultureSiteMap.values().contains(travelSites.get(0)))
					bestTravelSite = travelSites.get(0);
			} else if (MapGrid.Instance().getUnitsNear(travelSites.get(1).baseLocation.getPosition(), 100, true, false,
					UnitType.Terran_Vulture_Spider_Mine).isEmpty()) {
//				System.out
//						.println(travelSites.get(1).toString() + " " + new Exception().getStackTrace()[0].getLineNumber());
				if (!vultureSiteMap.values().contains(travelSites.get(1)))
					bestTravelSite = travelSites.get(1);
			} else if (MapGrid.Instance().getUnitsNear(travelSites.get(2).baseLocation.getPosition(), 100, true, false,
					UnitType.Terran_Vulture_Spider_Mine).isEmpty()) {
//				System.out
//						.println(travelSites.get(2).toString() + " " + new Exception().getStackTrace()[0].getLineNumber());
				if (!vultureSiteMap.values().contains(travelSites.get(2)))
					bestTravelSite = travelSites.get(2);
			}
		}
		
		if (bestTravelSite != null) {
			vultureSiteMap.put(vultureId, bestTravelSite);
			bestTravelSite.visitAssignedFrame = MyBotModule.Broodwar.getFrameCount();
			return bestTravelSite.baseLocation;
		} else {
			vultureSiteMap.remove(vultureId);
			timeToShiftDuty = true;
			return null;
		}
	}
	
	// 1) 게릴라지역은 적군이 존재하는 travelSites이어야 한다.
	// 2) 게릴라지격은 guerillaFrame이(타깃으로 결정된 frame) 일정시간 이상 지났어야 한다.(게릴라로 벌처가 계속 소모되는 것을 방지하기 위함)
	// 3) 일꾼이 많으면 우선순위가 높다. 병력이 많으면 우선순위가 낮다.(특히 방어타워가 많으면)
	public BaseLocation getBestGuerillaSite(List<Unit> assignableVultures) {
		int vulturePower = CombatExpectation.getVulturePower(assignableVultures);
		int currFrame = MyBotModule.Broodwar.getFrameCount();

		int bestScore = 0;
		TravelSite bestTravelSite = null;

		for (TravelSite travelSite : travelSites) {
			if (assignableVultures.size() < MicroSet.Vulture.GEURILLA_FREE_VULTURE_COUNT
					&& currFrame - travelSite.guerillaExamFrame < MicroSet.Vulture.GEURILLA_INTERVAL_FRAME) {
				continue;
			}

			// System.out.println(travelSite.baseLocation.getTilePosition().toString() + " "
			// + new Exception().getStackTrace()[0].getLineNumber());

			// MyBotModule.Broodwar.drawCircleMap(travelSite.baseLocation.getPosition(),
			// MicroSet.Vulture.GEURILLA_RADIUS,
			// Color.Orange);

			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(
					travelSite.baseLocation.getPosition(), InformationManager.Instance().enemyPlayer,
					MicroSet.Vulture.GEURILLA_RADIUS, true);
			if (enemiesInfo.isEmpty()) { // 적군이 존재하지 않음
				continue;
			}
						
//			for (UnitInfo unitInfo : enemiesInfo) {
				// System.out.println(unitInfo.getType() +
				// unitInfo.getUnit().getTilePosition().toString() + " "
				// + new Exception().getStackTrace()[0].getLineNumber());

//				if (unitInfo.getType() == UnitType.Terran_Command_Center) {
					// 안개속의 적 구성을 가늠해 게릴라 타게팅이 가능한지 확인한다.
					int enemyPower = CombatExpectation.enemyPowerByUnitInfo(enemiesInfo, false);
					int score = CombatExpectation.guerillaScoreByUnitInfo(enemiesInfo);

					if (vulturePower > enemyPower && score > bestScore) {
						bestScore = score;

						bestTravelSite = travelSite;
						System.out.println(bestTravelSite.baseLocation.getTilePosition().toString() + " "
								+ new Exception().getStackTrace()[0].getLineNumber());
//						 MyBotModule.Broodwar.drawCircleMap(travelSite.baseLocation.getPosition(),
//						 MicroSet.Vulture.GEURILLA_RADIUS,
//						 Color.White, true);
					}
//				}
//			}
		}
		
		if (bestTravelSite != null) {
//			System.out.println();
			bestTravelSite.guerillaExamFrame = currFrame;
			return bestTravelSite.baseLocation;
		} else {
			return null;
		}
	}
	
	public BaseLocation getBestMultiGuerillaSite(List<Unit> assignableVultures) {
		TravelSite bestTravelSite = null;

		for (TravelSite travelSite : travelSites) {
			// System.out.println(travelSite.baseLocation.getTilePosition().toString() + " "
			// + new Exception().getStackTrace()[0].getLineNumber());

			// MyBotModule.Broodwar.drawCircleMap(travelSite.baseLocation.getPosition(),
			// MicroSet.Vulture.GEURILLA_RADIUS,
			// Color.Orange);

			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(
					travelSite.baseLocation.getPosition(), InformationManager.Instance().enemyPlayer,
					MicroSet.Vulture.MULTIGEURILLA_RADIUS, true);

			if (enemiesInfo.isEmpty()) { // 적군이 존재하지 않음
				continue;
			}
			
			int workerCnt = 0;
			boolean baseBuildingFlag = false;
			for (UnitInfo unitInfo : enemiesInfo) {
				// System.out.println(unitInfo.getType() +
				// unitInfo.getUnit().getTilePosition().toString() + " "
				// + new Exception().getStackTrace()[0].getLineNumber());

				if (unitInfo.getUnit().canAttack() && !unitInfo.getType().isWorker()) {
					System.out.println("There is attacker " + unitInfo.getType() + " "
							+ new Exception().getStackTrace()[0].getLineNumber());
					bestTravelSite = null;
					break;
				}
				
				if (unitInfo.getType().isWorker())
					workerCnt++;

				if (unitInfo.getType() == UnitType.Terran_Command_Center)
					baseBuildingFlag = true;

				if (workerCnt > 0 && baseBuildingFlag == true) {
					bestTravelSite = travelSite;
//					System.out.println(bestTravelSite.baseLocation.getTilePosition().toString() + " "
//							+ new Exception().getStackTrace()[0].getLineNumber());
//					MyBotModule.Broodwar.drawCircleMap(travelSite.baseLocation.getPosition(),
//							MicroSet.Vulture.MULTIGEURILLA_RADIUS, Color.White, false);
				}
			}
			if (bestTravelSite != null)
				break;
		}

		if (bestTravelSite != null) {
			// System.out.println();
			if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(bestTravelSite.baseLocation.getPosition(),
					MicroSet.Vulture.MULTIGEURILLA_RADIUS, Color.White, false);
			return bestTravelSite.baseLocation;
		} else {
			return null;
		}
	}
	
	public boolean guerillaIgnoreModeEnabled(String squadName) {
		return guerillaTimeMap.containsKey(squadName);
	}
	public void guerillaStart(String squadName) {
		guerillaTimeMap.put(squadName, MyBotModule.Broodwar.getFrameCount());
	}
}

class TravelSite {
	TravelSite(BaseLocation baseLocation, int visitFrame, int visitAssignedFrame, int guerillaExamFrame) {
		this.baseLocation = baseLocation;
		this.visitFrame = visitFrame;
		this.visitAssignedFrame = visitAssignedFrame;
		this.guerillaExamFrame = guerillaExamFrame;
	}
	BaseLocation baseLocation;
	int visitFrame;
	int visitAssignedFrame;
	int guerillaExamFrame;

	@Override
	public String toString() {
		return "TravelSite [baseLocation=" + baseLocation.getTilePosition() + ", visitFrame=" + visitFrame + ", visitAssignedFrame="
				+ visitAssignedFrame + ", guerillaExamFrame=" + guerillaExamFrame + "]";
	}
}