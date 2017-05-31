#pragma once

#include "Common.h"
#include "UnitData.h"

namespace MyBot
{
	/// ���� ��Ȳ���� �� �Ϻθ� ��ü �ڷᱸ�� �� �����鿡 �����ϰ� ������Ʈ�ϴ� class
	/// ���� ���� ��Ȳ������ BWAPI::Broodwar �� ��ȸ�Ͽ� �ľ��� �� ������, ���� ���� ��Ȳ������ BWAPI::Broodwar �� ���� ��ȸ�� �Ұ����ϱ� ������ InformationManager���� ���� �����ϵ��� �մϴ�
	/// ����, BWAPI::Broodwar �� BWTA ���� ���� ��ȸ�� �� �ִ� ���������� ��ó�� / ���� �����ϴ� ���� ������ �͵� InformationManager���� ���� �����ϵ��� �մϴ�
	class InformationManager 
	{
		InformationManager();

		/// Player - UnitData(�� Unit �� �� Unit�� UnitInfo �� Map ���·� �����ϴ� �ڷᱸ��) �� �����ϴ� �ڷᱸ�� ��ü
		std::map<BWAPI::Player, UnitData>							_unitData;

		/// �ش� Player�� �ֿ� �ǹ����� �ִ� BaseLocation. 
		/// ó������ StartLocation ���� ����. mainBaseLocation �� ��� �ǹ��� �ı��� ��� ������
		/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
		std::map<BWAPI::Player, BWTA::BaseLocation * >				_mainBaseLocations;

		/// �ش� Player�� mainBaseLocation �� ����Ǿ��°� (firstChokePoint, secondChokePoint, firstExpansionLocation �� ������ �ߴ°�)
		std::map<BWAPI::Player, bool>								_mainBaseLocationChanged;

		/// �ش� Player�� �����ϰ� �ִ� Region �� �ִ� BaseLocation
		/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
		std::map<BWAPI::Player, std::list<BWTA::BaseLocation *> >	_occupiedBaseLocations;

		/// �ش� Player�� �����ϰ� �ִ� Region
		/// �ǹ� ���θ� �������� �ľ��ϱ� ������ �������ϰ� �Ǵ��Ҽ��� �ֽ��ϴ� 
		std::map<BWAPI::Player, std::set<BWTA::Region *> >			_occupiedRegions;

		/// �ش� Player�� mainBaseLocation ���� ���� ����� ChokePoint
		std::map<BWAPI::Player, BWTA::Chokepoint *>					_firstChokePoint;
		/// �ش� Player�� mainBaseLocation ���� ���� ����� BaseLocation
		std::map<BWAPI::Player, BWTA::BaseLocation *>				_firstExpansionLocation;
		/// �ش� Player�� mainBaseLocation ���� �ι�°�� ����� (firstChokePoint�� �ƴ�) ChokePoint
		/// ���� �ʿ� ����, secondChokePoint �� �Ϲ� ��İ� �ٸ� ������ �� ���� �ֽ��ϴ�
		std::map<BWAPI::Player, BWTA::Chokepoint *>					_secondChokePoint;
	
		/// ��ü unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
		void                    updateUnitsInfo();

		/// �ش� unit �� ������ ������Ʈ �մϴ� (UnitType, lastPosition, HitPoint ��)
		void                    updateUnitInfo(BWAPI::Unit unit);
		void                    updateBaseLocationInfo();
		void					updateChokePointAndExpansionLocation();
		void                    updateOccupiedRegions(BWTA::Region * region, BWAPI::Player player);

	public:

		/// static singleton ��ü�� �����մϴ�
		static InformationManager & Instance();
			
		BWAPI::Player       selfPlayer;		///< �Ʊ� Player		
		BWAPI::Race			selfRace;		///< �Ʊ� Player�� ����		
		BWAPI::Player       enemyPlayer;	///< ���� Player		
		BWAPI::Race			enemyRace;		///< ���� Player�� ����  
		
		/// Unit �� BaseLocation, ChokePoint � ���� ������ ������Ʈ�մϴ�
		void                    update();

		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitShow(BWAPI::Unit unit)        { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitHide(BWAPI::Unit unit)        { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitCreate(BWAPI::Unit unit)		{ updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitComplete(BWAPI::Unit unit)    { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitMorph(BWAPI::Unit unit)       { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ�
		void					onUnitRenegade(BWAPI::Unit unit)    { updateUnitInfo(unit); }
		/// Unit �� ���� ������ ������Ʈ�մϴ� 
		/// ������ �ı�/����� ���, �ش� ���� ������ �����մϴ�
		void					onUnitDestroy(BWAPI::Unit unit);
			
		
		/// �ش� BaseLocation �� player�� �ǹ��� �����ϴ��� �����մϴ�
		/// @param baseLocation ��� BaseLocation
		/// @param player �Ʊ� / ����
		/// @param radius TilePosition ����
		bool					hasBuildingAroundBaseLocation(BWTA::BaseLocation * baseLocation, BWAPI::Player player, int radius = 10);
		
		/// �ش� Region �� �ش� Player�� �ǹ��� �����ϴ��� �����մϴ�
		bool					existsPlayerBuildingInRegion(BWTA::Region * region, BWAPI::Player player);		

		/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ Region ����� �����մϴ�
		std::set<BWTA::Region *> &  getOccupiedRegions(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� �ǹ��� �Ǽ��ؼ� ������ BaseLocation ����� �����մϴ�		 
		std::list<BWTA::BaseLocation *> & getOccupiedBaseLocations(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation �� �����մϴ�		 
		BWTA::BaseLocation *	getMainBaseLocation(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� ChokePoint �� �����մϴ�		 
		BWTA::Chokepoint *      getFirstChokePoint(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� ���� ����� Expansion BaseLocation �� �����մϴ�		 
		BWTA::BaseLocation *    getFirstExpansionLocation(BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� Main BaseLocation ���� �ι�°�� ����� ChokePoint �� �����մϴ�		 
		/// ���� �ʿ� ����, secondChokePoint �� �Ϲ� ��İ� �ٸ� ������ �� ���� �ֽ��ϴ�
		BWTA::Chokepoint *      getSecondChokePoint(BWAPI::Player player);



		/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� (���� �ֱٰ�) UnitAndUnitInfoMap �� �����մϴ�		 
		/// �ľǵ� �������� �����ϱ� ������ ������ ������ Ʋ�� ���� �� �ֽ��ϴ�
		const UnitAndUnitInfoMap &           getUnitAndUnitInfoMap(BWAPI::Player player) const;
		/// �ش� Player (�Ʊ� or ����) �� ��� ���� ��� UnitData �� �����մϴ�		 
		const UnitData &        getUnitData(BWAPI::Player player) const;


		/// �ش� Player (�Ʊ� or ����) �� �ش� UnitType ���� ���ڸ� �����մϴ� (�Ʒ�/�Ǽ� ���� ���� ���ڱ��� ����)
		int						getNumUnits(BWAPI::UnitType type, BWAPI::Player player);

		/// �ش� Player (�Ʊ� or ����) �� position ������ ���� ����� unitInfo �� �����մϴ�		 
		void                    getNearbyForce(std::vector<UnitInfo> & unitInfo, BWAPI::Position p, BWAPI::Player player, int radius);

		/// �ش� UnitType �� ���� �������� �����մϴ�
		bool					isCombatUnitType(BWAPI::UnitType type) const;



		// �ش� ������ UnitType �� ResourceDepot ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicResourceDepotBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Refinery ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getRefineryBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� SupplyProvider ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicSupplyProviderUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Worker �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getWorkerType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Basic Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicCombatUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Basic Combat Unit �� �����ϱ� ���� �Ǽ��ؾ��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicCombatBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Advanced Combat Unit �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getAdvancedCombatUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Observer �� �ش��ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getObserverUnitType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Basic Depense ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getBasicDefenseBuildingType(BWAPI::Race race = BWAPI::Races::None);

		// �ش� ������ UnitType �� Advanced Depense ����� �ϴ� UnitType�� �����մϴ�
		BWAPI::UnitType			getAdvancedDefenseBuildingType(BWAPI::Race race = BWAPI::Races::None);
	};
}