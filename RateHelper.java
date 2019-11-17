package sc.player2020.logic;

import java.util.ArrayList;
import java.util.List;

public class RateHelper {
	public int value;
	public List<String> rate  = new ArrayList<String>();
	public int isOwn;
	
	public RateHelper() {
		value = 0;
		rate.clear();
		isOwn = 1;
	}
	
	public RateHelper(RateHelper rateHelper) {
		value = rateHelper.value;
		rate.clear();
		rate.addAll(rateHelper.rate);
		isOwn = 1;
	}
}
