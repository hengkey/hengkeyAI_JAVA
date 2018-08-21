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

public class MechanicMicroDropShip extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	private List<Unit> dropShipList = new ArrayList<>();
	
	private int saveUnitLevel = 1;
	
	private Map<String, ShortPathGuerrilla> shortPathInfo = new HashMap<>();
	private boolean attackWithTank = false;
	private boolean nearestFlag = false;
	private int stickToTankRadius = 0;
	
    public static int MaxDropShip=2;
    public static int MaxDropTank=2;
    public static int MaxDropGoliath=4;
	
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
	
	public void executeMechanicMicro(Unit dropShip) {
		Position movePosition = order.getPosition();
		BaseLocation selfmainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//		BaseLocation sourceBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//		List<BaseLocation> selfotherBaseLocations = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().selfPlayer);
		BaseLocation enemymainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
//		BaseLocation enemyfirstBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
//		List<BaseLocation> enemyotherBaseLocations = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
	
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawTextMap(dropShip.getPosition().getX(), dropShip.getPosition().getY() + 10,
					"" + order.getType());
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(dropShip.getPosition(), 10, Color.Purple, true);
		
		// 목적지까지 move
		movePosition = new Position((movePosition.getX() / 2048) * 4064, (movePosition.getY() / 2048) * 4064);
		if (dropShip.getDistance(movePosition) > order.getRadius()) {
			
			//모두 실으면 unit이 안보이므로 size가 0이다
			if (tankList.size() > 0)
				return;

			//모두 실으면 unit이 안보이므로 size가 0이다
			if (goliathList.size() > 0)
				return;

//			if (dropShip.getDistance(movePosition) > 500) {
				// 벽타고 움직이기 위해
				int diffX = Math.abs(dropShip.getPosition().getX() - movePosition.getX());
				if (diffX > 36)
					movePosition = new Position(movePosition.getX(), dropShip.getY());
				else
					movePosition = new Position(dropShip.getX(), movePosition.getY());

				movePosition = new Position((movePosition.getX() / 2048) * 4064, (movePosition.getY() / 2048) * 4064);
				// System.out.println("movePosition"+movePosition.toTilePosition().toString());
//			}
			
			// 일단 울베애 가까운 y축 벽에 붙는다
			Position tmpPosition = selfmainBaseLocations.getPosition();
			tmpPosition = new Position((tmpPosition.getX() / 2048) * 4064, (tmpPosition.getY() / 2048) * 4064);
			int diffY = Math.abs(dropShip.getPosition().getY() - tmpPosition.getY());
			if (diffY > 36*2)
				movePosition = new Position(dropShip.getX(), tmpPosition.getY());
			// System.out.println("movePosition"+movePosition.toTilePosition().toString()+
			// new Exception().getStackTrace()[0].getLineNumber());		
			//모든 dropShip을 모으기 위해(그래야 터렛피해를 덜 받는다)
			if (nearestFlag == false) {
				int nearCnt = 0;
				for (Unit otherdropShip : dropShipList) {
					if (dropShip.equals(otherdropShip))
						continue;

					// System.out.println("dropShip.getDistance(otherdropShip)=" +
					// dropShip.getDistance(otherdropShip));
					if (dropShip.getDistance(otherdropShip) < 36) {
						nearCnt++;
					}
				}
				
				if (nearCnt < (MaxDropShip-1)) {
					movePosition = dropShipList.get(0).getPosition();
				} else {
					nearestFlag = true;
				}
			}
			
			if (dropShip.isIdle())
				CommandUtil.move(dropShip, movePosition);
		} else { // 목적지 도착
			if (dropShip.isIdle() || dropShip.isBraking()) {
				Position randomPosition = MicroUtils.randomPosition(dropShip.getPosition(), 100);
//				CommandUtil.unLoadAll(dropShip, enemymainBaseLocations.getPosition());
				CommandUtil.unLoadAll(dropShip, randomPosition);
			}
		}
	}
}