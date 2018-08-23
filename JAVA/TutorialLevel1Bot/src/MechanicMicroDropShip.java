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
	private int	progressLevel = 0;
	
	private Map<String, ShortPathGuerrilla> shortPathInfo = new HashMap<>();
	private boolean attackWithTank = false;
	private boolean nearestFlag = false;
	public boolean isNearestFlag() {
		return nearestFlag;
	}

	public void setNearestFlag(boolean nearestFlag) {
		this.nearestFlag = nearestFlag;
	}

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
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, List<Unit> dropShipList, int saveUnitLevel, int progressLevel) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.dropShipList = dropShipList;
		this.saveUnitLevel = saveUnitLevel;
		this.progressLevel = progressLevel;
		
		this.attackWithTank = tankList.size() * 6 >= goliathList.size();
		if (this.attackWithTank) {
			this.stickToTankRadius = 140 + (int) (Math.log(goliathList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToTankRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit dropShip) {
		if (order.getType() == SquadOrderType.ATTACK) {
			executeMechanicMicroAttack(dropShip);
			return;
		}
		
		if (progressLevel == Squad.Drop_Init)
			return;
		
		Position movePosition = order.getPosition();

		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawTextMap(dropShip.getPosition().getX(), dropShip.getPosition().getY() + 10,
					"" + order.getType() + movePosition.toTilePosition().toString());
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(dropShip.getPosition(), 10, Color.Blue, true);
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(dropShip.getPosition(), UnitType.Terran_Dropship.sightRange(), Color.Blue, false);
		
		BaseLocation selfmainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		BaseLocation enemymainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		
		// 목적지까지 move
		if (movePosition.equals(enemymainBaseLocations.getPosition()))
			movePosition = new Position((movePosition.getX() / 2048) * 4064, (movePosition.getY() / 2048) * 4064);
		
		switch (progressLevel) {
		case Squad.Drop_Loaded:// 모아야할 단계 그래야 터렛에 덜 맞음
			// System.out.println("progressLevel="+progressLevel+" "+new
			// Exception().getStackTrace()[0].getLineNumber());
			if (nearestFlag == false) {
				int nearCnt = 0;
				for (Unit otherdropShip : dropShipList) {
					if (dropShip.getID() == otherdropShip.getID())
						continue;

					if (dropShip.getDistance(otherdropShip) < (Squad.closestRange / 2)) {
						nearCnt++;
					}
				}

				if (nearCnt < (MaxDropShip - 1)) {
					movePosition = dropShipList.get(0).getPosition();
					if (dropShip.isIdle())
						CommandUtil.move(dropShip, movePosition);
				} else {
					System.out.println("progressLevel=" + progressLevel + " "
							+ new Exception().getStackTrace()[0].getLineNumber());
					nearestFlag = true;
				}
			}
			break;

		case Squad.Drop_AllNeared:// 벽에 붙어야 할 단계
			// System.out.println("progressLevel="+progressLevel+" "+new
			// Exception().getStackTrace()[0].getLineNumber());
			Position tmpPosition = selfmainBaseLocations.getPosition();
			tmpPosition = new Position((tmpPosition.getX() / 2048) * 4064, (tmpPosition.getY() / 2048) * 4064);
			int diffY = Math.abs(dropShip.getPosition().getY() - tmpPosition.getY());
			if (diffY > Squad.closestRange) {
				movePosition = new Position(dropShip.getX(), tmpPosition.getY());
				if (dropShip.isIdle())
					CommandUtil.move(dropShip, movePosition);
			}
			// System.out.println("movePosition"+movePosition.toTilePosition().toString()+
			// new Exception().getStackTrace()[0].getLineNumber());
			break;

		case Squad.Drop_MoveToWallComplete:// 벽타고 이동할 단계
			// System.out.println("progressLevel="+progressLevel+" "+new
			// Exception().getStackTrace()[0].getLineNumber());
			int diffX = Math.abs(dropShip.getPosition().getX() - movePosition.getX());
			if (diffX > Squad.closestRange)
				movePosition = new Position(movePosition.getX(), dropShip.getY());
			else
				movePosition = new Position(dropShip.getX(), movePosition.getY());

			if (dropShip.isIdle())
				CommandUtil.move(dropShip, movePosition);

			// movePosition = new Position((movePosition.getX() / 2048) * 4064,
			// (movePosition.getY() / 2048) * 4064);
			// System.out.println("movePosition"+movePosition.toTilePosition().toString());
			break;

		case Squad.Drop_MoveWithWallToDestComplete:// 도착했으니 무조건 내림
			// 임요환드롭
			if (dropShip.canUnload()) {
				for (Unit unit : dropShip.getLoadedUnits()) {
					dropShip.unload(unit);
				}
			} else if (dropShip.getLoadedUnits().size() > 0) {
				if (dropShip.isIdle() || dropShip.isBraking()) {
					Position randomPosition = MicroUtils.randomPosition(dropShip.getPosition(), 100);
					CommandUtil.unLoadAll(dropShip, randomPosition);
				}
			}
			break;

		default:
			break;
		}
	}
	
	public void executeMechanicMicroAttack(Unit dropShip) {
		//아직도 내려지지 않은 unit 임요환드롭
		if (dropShip.canUnload()) {
			for (Unit unit : dropShip.getLoadedUnits()) {
				dropShip.unload(unit);
			}
		}
		
		BaseLocation selfmainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//		BaseLocation sourceBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//		List<BaseLocation> selfotherBaseLocations = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().selfPlayer);
		BaseLocation enemymainBaseLocations = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
//		BaseLocation enemyfirstBaseLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
//		List<BaseLocation> enemyotherBaseLocations = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
	
		Position movePosition = selfmainBaseLocations.getPosition();
		
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawTextMap(dropShip.getPosition().getX(), dropShip.getPosition().getY() + 10,
					"" + order.getType());
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(dropShip.getPosition(), 10, Color.Teal, true);
		if (Config.DrawHengDebugInfo)
			MyBotModule.Broodwar.drawCircleMap(dropShip.getPosition(), UnitType.Terran_Dropship.sightRange(), Color.Teal, false);
		
		// 목적지까지 move
		movePosition = new Position((movePosition.getX() / 2048) * 4064, (movePosition.getY() / 2048) * 4064);
		if (dropShip.getDistance(movePosition) > Squad.DestRange) {
			// 벽타고 움직이기 위해
			int diffY = Math.abs(dropShip.getPosition().getY() - movePosition.getY());
			if (diffY > 36 * 2)
				movePosition = new Position(dropShip.getX(), movePosition.getY());
			else
				movePosition = new Position(movePosition.getX(), dropShip.getY());

			movePosition = new Position((movePosition.getX() / 2048) * 4064, (movePosition.getY() / 2048) * 4064);
			// System.out.println("movePosition"+movePosition.toTilePosition().toString());
			
			// 일단 적베애 가까운 y축 벽에 붙는다
			Position tmpPosition = enemymainBaseLocations.getPosition();
			tmpPosition = new Position((tmpPosition.getX() / 2048) * 4064, (tmpPosition.getY() / 2048) * 4064);
			int diffX = Math.abs(dropShip.getPosition().getX() - tmpPosition.getX());
			if (diffX > 36*2)
				movePosition = new Position(tmpPosition.getX(), dropShip.getY());

			// System.out.println("movePosition"+movePosition.toTilePosition().toString()+
			// new Exception().getStackTrace()[0].getLineNumber());		
			if (dropShip.isIdle())
				CommandUtil.move(dropShip, movePosition);

		} else { // 목적지 도착
			//임요환드롭
//			if (dropShip.getDistance(order.getPosition()) < Squad.DestRange && dropShip.isMoving()
//					&& dropShip.canUnload()) {
//				for (Unit unit : dropShip.getLoadedUnits()) {
//					dropShip.unload(unit);
//				}
//			}
			
//			if (dropShip.isIdle() || dropShip.isBraking()) {
//				Position randomPosition = MicroUtils.randomPosition(dropShip.getPosition(), 100);
////				CommandUtil.unLoadAll(dropShip, enemymainBaseLocations.getPosition());
//				CommandUtil.unLoadAll(dropShip, randomPosition);
//			}
		}
	}
}