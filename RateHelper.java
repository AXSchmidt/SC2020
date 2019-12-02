package sc.player2020.logic;

import java.util.ArrayList;
import java.util.List;

public class RateHelper {
	public int value;
	public int isOwn;
	public List<String> rateStr  = new ArrayList<String>();
	
	public RateHelper() {
		value = 0;
		isOwn = 1;
		rateStr.clear();
	}
	
	public RateHelper(RateHelper rateHelper) {
		value = rateHelper.value;
		isOwn = 1;
		rateStr.clear();
		rateStr.addAll(rateHelper.rateStr);
	}
}
