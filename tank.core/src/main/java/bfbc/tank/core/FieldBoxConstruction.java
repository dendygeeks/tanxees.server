package bfbc.tank.core;

import java.util.ArrayList;

import bfbc.tank.core.mechanics.BoxConstruction;
import bfbc.tank.core.mechanics.DeltaXY;

@SuppressWarnings("serial")
public class FieldBoxConstruction extends ArrayList<Cell> implements BoxConstruction<Cell> {
	public void move(DeltaXY delta) {
		for (Cell t : this) {
			t.move(delta);
		}
	}
}
