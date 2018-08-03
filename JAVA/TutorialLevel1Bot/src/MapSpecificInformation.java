//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.ArrayList;
import java.util.List;

import bwta.BaseLocation;

public class MapSpecificInformation {
	
	private MAP map;
	private List<BaseLocation> startingBaseLocation = new ArrayList<>();

	public MAP getMap() {
		return map;
	}
	public void setMap(MAP map) {
		this.map = map;
	}
	public List<BaseLocation> getStartingBaseLocation() {
		return startingBaseLocation;
	}
	public void setStartingBaseLocation(List<BaseLocation> startingBaseLocation) {
		this.startingBaseLocation = startingBaseLocation;
	}
	
	public boolean notUseReadyToAttackPosition() {
		return map == MAP.TheHunters;
	}

}

enum MAP {
	LostTemple, FightingSpririts, TheHunters, CircuitBreaker, OverWatch, Unknown
};