//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.List;

import bwapi.Color;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

//
public class BlockingEntrance {
	//필요한것
	//스타팅 위치
	//첫 서플 위치
	//배럭 위치
	//첫팩 위치
	//투서플 위치
	//테란&플토는 입막, 저그는 심시티
	/// 건물과 건물간 띄울 최소한의 간격 - 일반적인 건물의 경우
	private static int BuildingSpacingOld = Config.BuildingSpacing;
	/// 건물과 건물간 띄울 최소한의 간격 - ResourceDepot 건물의 경우 (Nexus, Hatchery, Command Center)
	private static int BuildingResourceDepotSpacingOld = Config.BuildingResourceDepotSpacing;
	
	public int startingX = 0;
	public int startingY = 0;
	
	public int first_suppleX = 0;
	public int first_suppleY = 0;
	
	public int second_suppleX = 0;
	public int second_suppleY = 0;
	
	public int fixSuppleX = 0;
	public int fixSuppleY = 0;
	
	public int barrackX = 0;
	public int barrackY = 0;
	
	public int factoryX = 0;
	public int factoryY = 0;
	
	public int bunkerX = 0;
	public int bunkerY = 0;
	
	public int turret1X = 0;
	public int turret1Y = 0;
	
	public int build_first_suppleX = 0;
	public int build_first_suppleY = 0;
	
	public int build_barrackX = 0;
	public int build_barrackY = 0;
	
    public int build_engineeringX = 0;
	public int build_engineeringY = 0;
	
	public int[] turretX = {0};
	public int[] turretY = {0};
	
	public static boolean blockingEntranceNow = true;
	
	private static int first_suppleX_array[] = null;// new int [];//{29, 52, 96, 102, 93, 55, 12, 23};
	private static int first_suppleY_array[] = null;//new int []{19, 23, 21,   61, 95, 94, 97, 54};

	private static int second_suppleX_array[] = null; //new int []{29, 52, 96, 102, 93, 55, 12, 23};
	private static int second_suppleY_array[] = null; //new int []{19, 23, 21,   61, 95, 94, 97, 54};

	private static int barrackX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int barrackY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private static int factoryX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int factoryY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private static int bunkerX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int bunkerY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private static int fix_supplyX[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int fix_supplyY[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
    private static int engineeringX_array[] = null; //new int []{26, 54, 98, 104, 90, 52, 14, 20};
	private static int engineeringY_array[] = null; //new int []{21, 25, 23,   63, 97, 96, 99, 56};
	
	private static int turretX_array[][] = null;
	private static int turretY_array[][] = null;
	
	private static int turret1X_array[] = null;
	private static int turret1Y_array[] = null;
	
	private int starting_int = 0;

	private static BlockingEntrance instance = new BlockingEntrance();
	
	public static BlockingEntrance Instance() {
		return instance;
	}
	
	/*public void SetBlockingPosition(){
		getStartingLocation();
	}*/
	

	public void SetBlockingPosition() { 
		Config.BuildingSpacing = 0;
		Config.BuildingResourceDepotSpacing = 0;
		//헌터
		if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters) {
			// ConstructionPlaceFinder.Instance().maxSupplyCntX = 3;
			int[] fix_supplyXX = { 0, 52, 118, 118, 118, 46, 0, 0 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 10, 0, 17, 66, 105, 110, 102, 54 };
			fix_supplyY = fix_supplyYY;
			// 입막용 11시 부터 시계방향으로 세팅
			if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
				int[] first_suppleXX_array = { 29, 55, 100, 102, 99, 55, 12, 24 };// private
																					// static
																					// intnew
																					// int
																					// []
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 19, 26, 23, 61, 100, 94, 97, 55 };
				first_suppleY_array = first_suppleYY_array;
				int[] second_suppleXX_array = { 3, 71, 113, 114, 107, 56, 3, 9 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 9, 11, 11, 83, 115, 116, 113, 50 };
				second_suppleY_array = second_suppleYY_array;
				int[] barrackXX_array = { 25, 51, 96, 104, 95, 52, 14, 20 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 20, 24, 21, 63, 101, 96, 99, 56 };
				barrackY_array = barrackYY_array;
				int[] factoryXX_array = { 20, 53, 100, 119, 108, 64, 0, 0 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 9, 11, 5, 59, 107, 109, 106, 55 };
				factoryY_array = factoryYY_array;
				int[] bunkerXX_array = { 26, 55, 99, 105, 99, 56, 11, 21 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 18, 24, 21, 61, 102, 96, 99, 54 };
			    bunkerY_array = bunkerYY_array;
			} else {
				int[] first_suppleXX_array = { 10, 71, 113, 114, 115, 63, 10, 8 };
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 11, 13, 13, 85, 112, 113, 111, 52 };
				first_suppleY_array = first_suppleYY_array;
				int[] second_suppleXX_array = { 3, 76, 120, 120, 107, 72, 3, 14 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 9, 9, 14, 87, 115, 121, 113, 48 };
				second_suppleY_array = second_suppleYY_array;
				int[] barrackXX_array = { 13, 67, 109, 110, 111, 60, 13, 11 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 9, 11, 10, 82, 113, 114, 112, 50 };
				barrackY_array = barrackYY_array;
				int[] factoryXX_array = { 14, 63, 109, 108, 109, 69, 14, 12 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 11, 14, 79, 110, 114, 109, 53 };
				factoryY_array = factoryYY_array;
				int[] bunkerXX_array = { 10, 71, 113, 114, 115, 63, 10, 8 };
				bunkerX_array = bunkerXX_array;
				int[] bunkerYY_array = { 9, 11, 11, 83, 114, 115, 113, 50 };
				bunkerY_array = bunkerYY_array;
			}
		} else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.OverWatch) {
			int[] fix_supplyXX = { 18, 106, 94, 0 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 20, 119, 91 };
			fix_supplyY = fix_supplyYY;
			
            System.out.println("fix_supplyY = OverWatch "+ InformationManager.Instance().enemyRace);			
			
			if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
				int[] first_suppleXX_array = { 18, 95, 109, 34 };
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 21, 16, 106, 110 };
				first_suppleY_array = first_suppleYY_array;

				int[] second_suppleXX_array = { 16, 94, 107, 35 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 19, 14, 104, 112 };
				second_suppleY_array = second_suppleYY_array;

				int[] barrackXX_array = { 12, 90, 112, 30 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 18, 16, 107, 111 };
				barrackY_array = barrackYY_array;

				int[] factoryXX_array = { 14, 114, 100, 7 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 8, 15, 109, 101 };
				factoryY_array = factoryYY_array;

				int[] bunkerXX_array = { 21, 97, 113, 35 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 20, 13, 108, 114 };
			    bunkerY_array = bunkerYY_array;
			} else {
				int[] first_suppleXX_array = { 18, 95, 109, 34 };
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 21, 16, 106, 110 };
				first_suppleY_array = first_suppleYY_array;

				int[] second_suppleXX_array = { 16, 94, 107, 35 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 19, 14, 104, 112 };
				second_suppleY_array = second_suppleYY_array;

				int[] barrackXX_array = { 12, 90, 112, 30 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 18, 16, 107, 111 };
				barrackY_array = barrackYY_array;

				int[] factoryXX_array = { 14, 114, 100, 7 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 8, 15, 109, 101 };
				factoryY_array = factoryYY_array;

				int[] bunkerXX_array = { 21, 97, 113, 35 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 20, 13, 108, 114 };
			    bunkerY_array = bunkerYY_array;
			}
		}else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple) {
			int[] fix_supplyXX = { 66, 118, 40, 0 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 37, 118, 74 };
			fix_supplyY = fix_supplyYY;
			if (InformationManager.Instance().enemyRace == Race.Protoss
					|| InformationManager.Instance().enemyRace == Race.Terran) {
				int[] first_suppleXX_array = { 77, 117, 53, 13 };// private
																	// static
																	// intnew
																	// int []
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 6, 51, 106, 61 };
				first_suppleY_array = first_suppleYY_array;
				int[] second_suppleXX_array = { 80, 119, 56, 10 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 5, 53, 105, 60 };
				second_suppleY_array = second_suppleYY_array;
				int[] barrackXX_array = { 76, 113, 52, 14 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 8, 50, 108, 63 };
				barrackY_array = barrackYY_array;
				int[] factoryXX_array = { 67, 107, 41, 20 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 38, 109, 82 };
				factoryY_array = factoryYY_array;
				int[] bunkerXX_array = { 77, 110, 53, 16 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 4, 51, 104, 61 };
			    bunkerY_array = bunkerYY_array;
			} else {
				int[] first_suppleXX_array = { 57, 117, 27, 7 };// private
																// static intnew
																// int []
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 11, 32, 114, 92 };
				first_suppleY_array = first_suppleYY_array;
				int[] second_suppleXX_array = { 50, 114, 20, 11 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 8, 27, 116, 86 };
				second_suppleY_array = second_suppleYY_array;
				int[] barrackXX_array = { 60, 113, 30, 10 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 9, 29, 115, 90 };
				barrackY_array = barrackYY_array;
				int[] factoryXX_array = { 60, 113, 30, 10 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 33, 112, 93 };
				factoryY_array = factoryYY_array;
				int[] bunkerXX_array = { 57, 117, 27, 7 };
				bunkerX_array = bunkerXX_array;
				int[] bunkerYY_array = { 9, 30, 116, 90 };
				bunkerY_array = bunkerYY_array;
			}
		} else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.CircuitBreaker) 
		{
			int[] fix_supplyXX = { 0, 97, 100, 8 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 1, 122, 121 };
			fix_supplyY = fix_supplyYY;
		 				
			if (InformationManager.Instance().enemyRace == Race.Protoss 	|| 
				InformationManager.Instance().enemyRace == Race.Terran) 
			{
				int[] first_suppleXX_array = {  7, 125, 125, 7};
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 24, 24, 102, 102 };
				first_suppleY_array = first_suppleYY_array;

				int[] second_suppleXX_array = { 4, 122, 122, 4 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 24, 24, 102, 102 };
				second_suppleY_array = second_suppleYY_array;

				int[] barrackXX_array = { 0, 118, 118, 0 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 24, 23, 102, 101 };
				barrackY_array = barrackYY_array;

				int[] factoryXX_array = { 8, 104, 104, 12 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 10, 109, 110 };
				factoryY_array = factoryYY_array;

				int[] bunkerXX_array = { 4, 123, 123, 5 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 22, 22, 104, 104 };
			    bunkerY_array = bunkerYY_array;

				int[] engineeringXX_array = { 4, 123, 117, 5 };
			    engineeringX_array = engineeringXX_array;
			    int[] engineeringYY_array = { 18, 18, 110, 110 };
			    engineeringY_array = engineeringYY_array;
			    
			    int[] turret1XX_array = {7, 116, 116, 10};
			    turret1X_array = turret1XX_array;
			    int[] turret1YY_array = {22, 23, 103, 103};
			    turret1Y_array = turret1YY_array;
			    
			    int[][] turretXX_array = {
			    		{ 30, 30,  25,  22,  22,  22,  20,  17,  11,   0},
			    		{ 96, 96, 100, 104, 104, 104, 108, 115, 125, 125},
			    		{ 96, 96,  99, 103, 104, 105, 107, 114, 124, 125},
			    		{ 28, 28,  23,  22,  22,  22,  18,  13,   0,   0}
			    };
			    
			    int[][] turretYY_array = {
			    		{  0,   3,   5,   7,  11, 15,   20,  23,  23, 16},
			    		{  0,   3,   5,   7,  12, 16,   21,  23,  20, 17},
			    		{125, 123, 121, 119, 115, 107, 103, 102, 100, 106},
			    		{125, 122, 120, 117, 113, 109, 103, 103, 106, 109}
			    };
			    
			    turretX_array = turretXX_array;
	    		turretY_array = turretYY_array;
			} 
			else 
			{
				int[] first_suppleXX_array = {  7, 125, 125, 7};
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 24, 24, 102, 102 };
				first_suppleY_array = first_suppleYY_array;

				int[] second_suppleXX_array = { 4, 122, 122, 4 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 24, 24, 102, 102 };
				second_suppleY_array = second_suppleYY_array;

				int[] barrackXX_array = { 0, 118, 118, 0 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 24, 23, 102, 101 };
				barrackY_array = barrackYY_array;

				int[] factoryXX_array = { 8, 104, 104, 12 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 10, 109, 109 };
				factoryY_array = factoryYY_array;

				int[] bunkerXX_array = { 4, 123, 123, 5 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 22, 22, 104, 104 };
			    bunkerY_array = bunkerYY_array;
	    
				int[] engineeringXX_array = { 4, 123, 117, 5 };
			    engineeringX_array = engineeringXX_array;
			    int[] engineeringYY_array = { 18, 18, 110, 110 };
			    engineeringY_array = engineeringYY_array;
			    
			    int[] turret1XX_array = {7, 116, 116, 10};
			    turret1X_array = turret1XX_array;
			    int[] turret1YY_array = {22, 23, 103, 103};
			    turret1Y_array = turret1YY_array;
			    
			    int[][] turretXX_array = {
			    		{ 28, 28,  25,  22,  22,  22,  20,  17,  11,   0},
			    		{ 98, 98, 100, 104, 104, 106, 108, 115, 125, 125},
			    		{ 98, 98,  99, 104, 104, 106, 108, 114, 124, 125},
			    		{ 28, 28,  23,  22,  22,  20,  18,  13,   0,   0}
			    };
			    
			    int[][] turretYY_array = {
			    		{  0,   3,   5,   7,  11,  15,   20,  23,  23, 16},
			    		{  0,   3,   5,   7,  12,  19,   21,  23,  20, 17},
			    		{125, 123, 121, 118, 119, 106, 103, 102, 100, 106},
			    		{125, 122, 120, 118, 113, 106, 103, 103, 106, 109}
			    };
			    
			    turretX_array = turretXX_array;
	    		turretY_array = turretYY_array;
			}
		} 
		else if (InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.FightingSpririts) 
		{
            System.out.println("FightingSpririts "+ InformationManager.Instance().enemyRace);
            
			int[] fix_supplyXX = { 10, 98, 104, 13 };
			fix_supplyX = fix_supplyXX;
			int[] fix_supplyYY = { 0, 0, 118, 103 };
			fix_supplyY = fix_supplyYY;
			
			if (InformationManager.Instance().enemyRace == Race.Protoss ||
				InformationManager.Instance().enemyRace == Race.Terran) 
			{
				int[] first_suppleXX_array = { 7, 100, 118, 25 };
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 26, 7, 100, 120 };
				first_suppleY_array = first_suppleYY_array;
				
				int[] second_suppleXX_array = { 10, 97, 120, 28 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 26, 5, 98, 121 };
				second_suppleY_array = second_suppleYY_array;
				
				int[] barrackXX_array = { 4, 102, 114, 21 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 28, 9, 101, 118 };
				barrackY_array = barrackYY_array;
				
				int[] factoryXX_array = { 11, 114, 102, 0 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 15, 107, 99 };
				factoryY_array = factoryYY_array;
				
				int[] bunkerXX_array = { 9, 103, 118, 22 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 24, 6, 102, 121 };
			    bunkerY_array = bunkerYY_array; 

			    int[] turret1XX_array = {13, 101, 112, 25};
			    turret1X_array = turret1XX_array;
			    int[] turret1YY_array = {26,  5, 101, 122};
			    turret1Y_array = turret1YY_array;
			    
				int[] engineeringXX_array = { 4, 117, 117, 9 };
			    engineeringX_array = engineeringXX_array;
			    int[] engineeringYY_array = { 18, 14, 123, 122 };
			    engineeringY_array = engineeringYY_array;			    
			    
			    int[][] turretXX_array = {
			    		{ 22, 21,  21,  23,  25,  25,  26,  22,  16,   0},
			    		{114,110, 102, 102, 106,  93,  96, 116, 119, 125},
			    		{103,102, 102, 100, 100, 100, 103, 107, 124, 125},
			    		{ 32, 32,  19,  22,  23,  16,  11,   7,    3,  0}
			    };
			    
			    int[][] turretYY_array = {
			    		{  0,   3,   5,   8,  10,  15,  18,  23, 26,  28},
			    		{ 25,  21,  19,  17,  12,   3,   0,  27, 28,  28},
			    		{126, 122, 118, 115, 110, 107, 102, 100, 98, 104},
			    		{125, 122, 115, 109, 105, 104,  98,  98, 98, 105}
			    };
			    
			    turretX_array = turretXX_array;
	    		turretY_array = turretYY_array;
			} 
			else 
			{
				int[] first_suppleXX_array = { 7, 100, 118, 25 };
				first_suppleX_array = first_suppleXX_array;
				int[] first_suppleYY_array = { 26, 7, 100, 120 };
				first_suppleY_array = first_suppleYY_array;
				
				int[] second_suppleXX_array = { 10, 97, 120, 28 };
				second_suppleX_array = second_suppleXX_array;
				int[] second_suppleYY_array = { 26, 5, 98, 121 };
				second_suppleY_array = second_suppleYY_array;
				
				int[] barrackXX_array = { 4, 102, 114, 21 };
				barrackX_array = barrackXX_array;
				int[] barrackYY_array = { 28, 9, 101, 118 };
				barrackY_array = barrackYY_array;
				
				int[] factoryXX_array = { 11, 114, 102, 0 };
				factoryX_array = factoryXX_array;
				int[] factoryYY_array = { 12, 15, 107, 99 };
				factoryY_array = factoryYY_array;
				
				int[] bunkerXX_array = { 9, 103, 118, 22 };
			    bunkerX_array = bunkerXX_array;
			    int[] bunkerYY_array = { 24, 6, 102, 121 };
			    bunkerY_array = bunkerYY_array; 

			    int[] turret1XX_array = {13, 101, 112, 25};
			    turret1X_array = turret1XX_array;
			    int[] turret1YY_array = {26,  5, 101, 122};
			    turret1Y_array = turret1YY_array;
			    
				int[] engineeringXX_array = { 4, 117, 117, 9 };
			    engineeringX_array = engineeringXX_array;
			    int[] engineeringYY_array = { 18, 14, 123, 122 };
			    engineeringY_array = engineeringYY_array;			    
				
			    int[][] turretXX_array = {
			    		{ 22, 21,  21,  23,  25,  25,  26,  22,  16,   0}, //11시 X
			    		{114,110, 102, 102, 106,  93,  96, 116, 119, 125}, // 1시 X
			    		{102,102, 102, 100, 100, 100, 103, 107, 124, 125}, // 5시 X
			    		{ 32, 32,  19,  22,  23,  16,  11,   7,    3,  0}  // 7시 x
			    };
			    
			    int[][] turretYY_array = {
			    		{  0,   3,   5,   8,  10,  15,  18,  23, 26,  28},
			    		{ 25,  21,  19,  17,  12,   3,   0,  27, 28,  28},
			    		{124, 122, 118, 115, 110, 107, 102, 100, 98, 104},
			    		{125, 122, 115, 109, 105, 104,  98,  98, 98, 105}
			    };
			    
			    turretX_array = turretXX_array;
	    		turretY_array = turretYY_array;
			}
		}
		
		
		
		
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) 
		{
			if(unit.getType() == UnitType.Terran_Command_Center)
			{
				startingX = unit.getTilePosition().getX();
				startingY = unit.getTilePosition().getY();
			}
		}

		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
			if(startingX == 10 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 70 && startingY == 8){
				starting_int = 1;
			}else if(startingX == 113 && startingY == 8){
				starting_int = 2;
			}else if(startingX == 114 && startingY == 80){
				starting_int = 3;
			}else if(startingX == 114 && startingY == 116){
				starting_int = 4;
			}else if(startingX == 63 && startingY == 117){
				starting_int = 5;
			}else if(startingX == 10 && startingY == 115){
				starting_int = 6;
			}else if(startingX == 8 && startingY == 47){
				starting_int = 7;
			}
		}
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple){
			if(startingX == 57 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 117 && startingY == 27){
				starting_int = 1;
			}else if(startingX == 27 && startingY == 118){
				starting_int = 2;
			}else if(startingX == 7&& startingY == 87){
				starting_int = 3;
			}
		}
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.OverWatch)
		{
			System.out.println("startingX="+startingX+" startingY="+startingY);
			if(startingX == 7 && startingY == 7)			starting_int = 0;
			else if(startingX == 117 && startingY == 7)		starting_int = 1;
			else if(startingX == 117 && startingY == 117)	starting_int = 2;
			else if(startingX == 7 && startingY == 117)		starting_int = 3;
		} 
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.CircuitBreaker)
		{
            System.out.println("startingX="+startingX+" startingY="+startingY);
	    	if(startingX == 7 && startingY == 9)			starting_int = 0;
		    else if(startingX == 117 && startingY == 9)	    starting_int = 1;
		    else if(startingX == 117 && startingY == 118)   starting_int = 2;
		    else if(startingX == 7 && startingY == 118)     starting_int = 3;
		}
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.FightingSpririts)
		{
			if(startingX == 7 && startingY == 6)			starting_int = 0;
			else if(startingX == 117 && startingY == 7)		starting_int = 1;
			else if(startingX == 117 && startingY == 117)	starting_int = 2;
			else if(startingX == 7 && startingY == 116)		starting_int = 3;
		}

		first_suppleX = first_suppleX_array[starting_int];
		first_suppleY = first_suppleY_array[starting_int];
		
		second_suppleX = second_suppleX_array[starting_int];
		second_suppleY = second_suppleY_array[starting_int];
		
		barrackX = barrackX_array[starting_int];
		barrackY = barrackY_array[starting_int];
		
		factoryX = factoryX_array[starting_int];
		factoryY = factoryY_array[starting_int];
		
		bunkerX = bunkerX_array[starting_int];
		bunkerY = bunkerY_array[starting_int];
		
		build_engineeringX = engineeringX_array[starting_int];
		build_engineeringY = engineeringY_array[starting_int];
		
		turretX = turretX_array[starting_int];
		turretY = turretY_array[starting_int];
		
		turret1X = turret1X_array[starting_int];
		turret1Y = turret1Y_array[starting_int];

		fixSuppleX = fix_supplyX[starting_int];
		fixSuppleY = fix_supplyY[starting_int];
		
		ConstructionPlaceFinder.Instance().setTilesToAvoidSupply();
	}
	
/*
	public void CheckBlockingPosition() {
		if(InformationManager.Instance().enemyRace == Race.Protoss || InformationManager.Instance().enemyRace == Race.Terran){
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if(unit.getType() == UnitType.Terran_Supply_Depot){
					build_first_suppleX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
					build_first_suppleY = unit.getTilePosition().getY();
					if(first_suppleX != build_first_suppleX || first_suppleY != build_first_suppleY){
						blockingEntranceNow = false;
					}
				}
				if(unit.getType() == UnitType.Terran_Barracks){
					build_barrackX = unit.getTilePosition().getX(); //unit.getPosition().getX();// getTilePosition().getX();
					build_barrackY = unit.getTilePosition().getY();
					if(barrackX != build_barrackX || barrackY != build_barrackY){
						blockingEntranceNow = false;
					}
				}
			}
		}
	}
*/
	
	
	public final TilePosition getSupplyPosition(TilePosition tilepos)
	{
		int startingX = tilepos.getX();
		int startingY = tilepos.getY();
		int starting_int =10000;
		if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.TheHunters){
			if(startingX == 10 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 70 && startingY == 8){
				starting_int = 1;
			}else if(startingX == 113 && startingY == 8){
				starting_int = 2;
			}else if(startingX == 114 && startingY == 80){
				starting_int = 3;
			}else if(startingX == 114 && startingY == 116){
				starting_int = 4;
			}else if(startingX == 63 && startingY == 117){
				starting_int = 5;
			}else if(startingX == 10 && startingY == 115){
				starting_int = 6;
			}else if(startingX == 8 && startingY == 47){
				starting_int = 7;
			}
		}else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.LostTemple){
			if(startingX == 57 && startingY == 6){
				//11시부터 시계방향
				starting_int = 0;
			}else if(startingX == 117 && startingY == 27){
				starting_int = 1;
			}else if(startingX == 27 && startingY == 118){
				starting_int = 2;
			}else if(startingX == 7&& startingY == 87){
				starting_int = 3;
			}
		}
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.OverWatch)
		{
			System.out.println("startingX="+startingX+" startingY="+startingY);
			if(startingX == 7 && startingY == 7)			starting_int = 0;
			else if(startingX == 117 && startingY == 7)		starting_int = 1;
			else if(startingX == 117 && startingY == 117)	starting_int = 2;
			else if(startingX == 7 && startingY == 117)		starting_int = 3;
		} 
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.CircuitBreaker)
		{
	        System.out.println("startingX="+startingX+" startingY="+startingY);
		   	if(startingX == 7 && startingY == 9)			starting_int = 0;
		    else if(startingX == 117 && startingY == 9)	    starting_int = 1;
		    else if(startingX == 117 && startingY == 118)   starting_int = 2;
		    else if(startingX == 7 && startingY == 118)     starting_int = 3;
		}
		else if(InformationManager.Instance().getMapSpecificInformation().getMap() == MAP.FightingSpririts)
		{
			if(startingX == 7 && startingY == 6)			starting_int = 0;
			else if(startingX == 117 && startingY == 7)		starting_int = 1;
			else if(startingX == 117 && startingY == 117)	starting_int = 2;
			else if(startingX == 7 && startingY == 116)		starting_int = 3;
		}
		
		if(starting_int==10000)
		{
			return null;
		}
		else
		{
			TilePosition supply_pos= new TilePosition(fix_supplyX[starting_int], fix_supplyY[starting_int]);
			return supply_pos;
		}
	}
	
	public final TilePosition getSupplyPosition()
	{
			TilePosition supply_pos= new TilePosition(fix_supplyX[starting_int], fix_supplyY[starting_int]);
			return supply_pos;
	}
	
//	public void ReturnBuildSpacing() {
//		Config.BuildingSpacing = BuildingSpacingOld;
//		Config.BuildingResourceDepotSpacing = BuildingResourceDepotSpacingOld;
//
//	}
	
	public int getStartingInt(){
		return starting_int;
	}
}