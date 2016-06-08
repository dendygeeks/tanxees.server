package dendygeeks.tanxees.server;

import com.google.gson.annotations.Expose;

public class PointIJ {
	@Expose
	public final int i, j;
	public PointIJ(int i, int j) {
		this.i = i;
		this.j = j;
	}
}
