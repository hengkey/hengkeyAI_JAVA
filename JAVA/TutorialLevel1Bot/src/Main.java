//# Prebot (�ǻѸ��������Ϸ� / �˰��� ������ȸ 2017)

import bwapi.TilePosition;

public class Main {

	/// �� ���α׷��� �����մϴ�<br>
	/// <br>
	/// Eclipse �޴� -> Run -> Run Configurations...<br> 
	/// -> Arguments �� -> Working Directory �� Other : C����̺���StarCraft �� �����ϸ�<br>
	/// �� ���α׷��� �� �����Ǿ��ִ� �� ���� �м� ĳ�������� Ȱ���ϰ� �Ǿ<br>
	/// �� ���α׷� ���� �� �� ���� �м��� �ҿ�Ǵ� �����̸� ���� �� �ֽ��ϴ�.
    public static void main(String[] args) {
    	try{
            new MyBotModule().run();

//			ShortPathGuerrilla path = new ShortPathGuerrilla("aaa");
//			path.init();
//			TilePosition curPos = new TilePosition(8, 10);
//			TilePosition targetPos = new TilePosition(119, 119);
//			path.updateWeightValue(curPos, targetPos);
    	}
    	catch(Exception e) {
    		System.out.println(e.toString());
    		e.printStackTrace();
    	}
    }
}