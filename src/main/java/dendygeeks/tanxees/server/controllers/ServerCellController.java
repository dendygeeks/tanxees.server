package dendygeeks.tanxees.server.controllers;

import java.util.Iterator;

import dendygeeks.tanxees.api.java.interfaces.CellType;
import dendygeeks.tanxees.api.java.model.CellModel;
import dendygeeks.tanxees.server.mechanics.Box;
import dendygeeks.tanxees.server.mechanics.BoxConstruction;
import dendygeeks.tanxees.server.mechanics.DeltaAngle;
import dendygeeks.tanxees.server.mechanics.DeltaXY;

public class ServerCellController implements Box, BoxConstruction<ServerCellController> {

	private ServerGameController game;
	private CellModel cellModel;
	
	private int i, j;
	
	@Override
	public double getExcentricity() {
		return 0;
	}
	
	@Override
	public double getLeft() {
		return (i - 0.5) * game.getGameModel().getCellSize();
	}

	@Override
	public double getTop() {
		return (j - 0.5) * game.getGameModel().getCellSize();
	}

	@Override
	public double getRight() {
		return (i + 0.5) * game.getGameModel().getCellSize();
	}

	@Override
	public double getBottom() {
		return (j + 0.5) * game.getGameModel().getCellSize();
	}
	
	public double getX() {
		return i * game.getGameModel().getCellSize();
	}
	public double getY() {
		return j * game.getGameModel().getCellSize();
	}

	@Override
	public void move(DeltaXY delta) {
		throw new RuntimeException("Unsupported");

	}
	
	public ServerCellController(ServerGameController game, int i, int j, CellModel cellModel) {
		this.game = game;
		this.i = i;
		this.j = j;
		this.cellModel = cellModel;
	}

	public ServerCellController(ServerGameController game, int i, int j) {
		this(game, i, j, new CellModel(CellType.EMPTY));
	}

	public CellModel getCellModel() {
		return cellModel;
	}
	
	@Override
	public Iterator<ServerCellController> iterator() {
		Iterator<ServerCellController> res = new Iterator<ServerCellController>() {
			
			private boolean hasNext = true;
			
			@Override
			public ServerCellController next() {
				hasNext = false;
				return ServerCellController.this;
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
		return res;
	}
	
	public int getI() {
		return i;
	}
	
	public int getJ() {
		return j;
	}
	
	public CellType getType() {
		return cellModel.getType();
	}

	@Override
	public void rotate(DeltaAngle delta) {
		throw new RuntimeException("Not supported");
	}

	public void setMovementTouchMoment(long touched) {
		getCellModel().setMovementTouchMoment(touched);
	}
}
