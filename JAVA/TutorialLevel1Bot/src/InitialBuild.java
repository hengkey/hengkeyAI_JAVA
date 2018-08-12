//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

/// 봇 프로그램 설정
public class InitialBuild {

	private static InitialBuild instance = new InitialBuild();
	
	public static InitialBuild Instance() {
		return instance;
	}
	
	
	public void setInitialBuildOrder() {
	
		//@@@@@@ 맵과 상대 종족에 따른 initial build 를 따져봐야된다.
		BlockingEntrance.Instance().SetBlockingPosition();
		
		TilePosition firstSupplyPos = new TilePosition(BlockingEntrance.Instance().first_suppleX,BlockingEntrance.Instance().first_suppleY);
		TilePosition secondSupplyPos= new TilePosition(BlockingEntrance.Instance().second_suppleX,BlockingEntrance.Instance().second_suppleY);
		TilePosition barrackPos     = new TilePosition(BlockingEntrance.Instance().barrackX,BlockingEntrance.Instance().barrackY);
		TilePosition factoryPos     = new TilePosition(BlockingEntrance.Instance().factoryX,BlockingEntrance.Instance().factoryY);
		TilePosition factoryPos2    = new TilePosition(BlockingEntrance.Instance().factoryX+7,BlockingEntrance.Instance().factoryY);
		TilePosition factoryPos3    = new TilePosition(BlockingEntrance.Instance().factoryX,BlockingEntrance.Instance().factoryY+3);
		TilePosition bunkerPos      = new TilePosition(BlockingEntrance.Instance().bunkerX,BlockingEntrance.Instance().bunkerY);
		TilePosition turret1Pos     = new TilePosition(BlockingEntrance.Instance().turret1X,BlockingEntrance.Instance().turret1Y);
		TilePosition engineeringPos = new TilePosition(BlockingEntrance.Instance().build_engineeringX,BlockingEntrance.Instance().build_engineeringY);
		 
		if (InformationManager.Instance().enemyRace == Race.Terran) 
		{
			System.out.println("setInitialBuildOrder : Terran");
			queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true,true);
			queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true,true);
			queueBuild(true, UnitType.Terran_Refinery);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true,true);
			queueBuild(true, UnitType.Terran_SCV);
//			queueBuild(true, UnitType.Terran_Refinery);
			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
			queueBuild(false, UnitType.Terran_SCV);
			queueBuild(true, UnitType.Terran_Marine);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true,true);
//			queueBuild(true, UnitType.Terran_Marine);
//			queueBuild(true, UnitType.Terran_Marine);
//			queueBuild(true, UnitType.Terran_Marine);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos2,false, true);
			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
			queueBuild(true, UnitType.Terran_Vulture);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Starport, factoryPos,false, false);
			queueBuild(false, UnitType.Terran_SCV);
			queueBuild(false, UnitType.Terran_SCV);
			queueBuild(false, UnitType.Terran_Machine_Shop);
			queueBuild(false, UnitType.Terran_Vulture);
			queueBuild(false, UnitType.Terran_Vulture);
			queueBuild(false, UnitType.Terran_Vulture);
			queueBuild(true, UnitType.Terran_Siege_Tank_Tank_Mode);
//			queueBuildSeed(true, UnitType.Terran_Starport, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
		}
		else if (InformationManager.Instance().enemyRace == Race.Protoss) 
		{
			System.out.println("setInitialBuildOrder : Protoss");
			queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true,true);
			queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true,true);
			queueBuild(true, UnitType.Terran_SCV);
			queueBuild(true, UnitType.Terran_Refinery);
			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
			queueBuild(false, UnitType.Terran_SCV);
			queueBuild(false, UnitType.Terran_Marine);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true,true);
			queueBuild(false, UnitType.Terran_Marine);
			queueBuild(false, UnitType.Terran_Marine);
			queueBuild(false, UnitType.Terran_Marine);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay,engineeringPos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret,turret1Pos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos2,false, true);
			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
			queueBuild(true, UnitType.Terran_Vulture);
			queueBuild(false, UnitType.Terran_Machine_Shop);
			queueBuild(false, UnitType.Terran_SCV);
		}
		else
		{   
            System.out.println("setInitialBuildOrder : Zerg");			
            queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks, barrackPos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, firstSupplyPos,true,true);
	        queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV);  
	        queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
	        BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Bunker, bunkerPos,true,true);
	        queueBuild(false, UnitType.Terran_SCV);
	        queueBuild(true, UnitType.Terran_Refinery);
	        queueBuild(true, UnitType.Terran_Marine);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot, secondSupplyPos,true,true);
	        queueBuild(false, UnitType.Terran_Marine);
	        queueBuild(false, UnitType.Terran_Marine);
	        queueBuild(false, UnitType.Terran_Marine);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay,engineeringPos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret,turret1Pos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos,true,true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Factory, factoryPos2,false, true);
			
			for(int i=0;i<2;i++) 
			{
				TilePosition turretPos = new TilePosition(BlockingEntrance.Instance().turretX[i],BlockingEntrance.Instance().turretY[i]);
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret, turretPos,true,true);
			}
			
			queueBuild(true, UnitType.Terran_Vulture);
			queueBuild(false, UnitType.Terran_Machine_Shop);
			queueBuild(false, UnitType.Terran_SCV, UnitType.Terran_SCV);
			queueBuild(false, UnitType.Terran_Vulture);
			queueBuild(false, UnitType.Terran_Vulture);
			
			
			for(int i=3; i<10 ; i++) 
			{
				if ((i == 3) || (i == 5))
				{
					TilePosition turretPos = new TilePosition(BlockingEntrance.Instance().turretX[i],BlockingEntrance.Instance().turretY[i]);
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret, turretPos,true,true);
				}
			}
			
			//queueBuild(true, UnitType.Terran_Armory);
		}
	}
	
	public void queueBuild(boolean blocking, UnitType... types) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		BuildOrderItem.SeedPositionStrategy defaultSeedPosition = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
		for (UnitType type : types) {
			bq.queueAsLowestPriority(type, defaultSeedPosition, blocking);
		}
	}
	
	public void queueBuildSeed(boolean blocking, UnitType type, BuildOrderItem.SeedPositionStrategy seedPosition) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type, seedPosition, blocking);
	}
	
	public void queueBuild(TechType type) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type);
	}
	
	public void queueBuild(UpgradeType type) {
		BuildOrderQueue bq = BuildManager.Instance().buildQueue;
		bq.queueAsLowestPriority(type);
	}
}