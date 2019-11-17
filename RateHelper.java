package sc.player2020.logic;

import java.util.ArrayList;
import java.util.List;

public class RateHelper {
	public int value;
	public List<String> rate  = new ArrayList<String>();
	public boolean current;
	
	public RateHelper() {
		value = 0;
		rate.clear();
		current = true;
	}
	
	public RateHelper(RateHelper rateHelper) {
		value = rateHelper.value;
		rate.clear();
		rate.addAll(rateHelper.rate);
		current = true;
	}
}
