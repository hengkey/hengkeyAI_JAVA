//# Steamhammer (version 1.3.1 for AIIDE 2017)
//# Prebot (피뿌리는컴파일러 / 알고리즘 경진대회 2017)

import java.util.List;

import bwapi.Position;
import bwapi.Unit;

public class MicroDropShip extends MicroManager {

	// private int attackFrame = 0;

	@Override
	protected void executeMicro(List<Unit> targets) {
		List<Unit> dropShips = getUnits();
		List<Unit> dropShipTargets = MicroUtils.filterTargets(targets, true);

		KitingOption kitingOption = KitingOption.defaultKitingOption();
		kitingOption.setGoalPosition(order.getPosition());

		for (Unit dropShip : dropShips) {

			Unit target = getTarget(dropShip, dropShipTargets);
			if (target != null) {
				MicroUtils.preciseKiting(dropShip, target, kitingOption);
			} else {
				// if we're not near the order position, go there
				if (dropShip.getDistance(order.getPosition()) > order.getRadius()) {
					CommandUtil.attackMove(dropShip, order.getPosition());
				} else {
					if (dropShip.isIdle()) {
						Position randomPosition = MicroUtils.randomPosition(dropShip.getPosition(), order.getRadius());
						CommandUtil.attackMove(dropShip, randomPosition);
					}
				}
			}
		}
	}

	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (Unit target : targets) {
			if (!target.isDetected())
				continue;

			int priorityScore = TargetPriority.getPriority(rangedUnit, target); // 우선순위 점수
			int distanceScore = 0; // 거리 점수
			int hitPointScore = 0; // HP 점수

			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}

			distanceScore -= rangedUnit.getDistance(target) / 5;
			hitPointScore -= target.getHitPoints() / 10;

			int totalScore = priorityScore + distanceScore + hitPointScore;
			if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}

		return bestTarget;
	}
}