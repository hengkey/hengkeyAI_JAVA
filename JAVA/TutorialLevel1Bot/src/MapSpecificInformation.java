/* Base Code 출처 : 2017년 알고리즘 경진대회 "피뿌리는 컴파일러" 팀 코드 */

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