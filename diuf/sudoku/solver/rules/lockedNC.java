/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.solver.rules;

import java.util.*;

import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;


/**
 * Implementation of the "NC Forcing Cell" solving technique by Tarek Maani.
 * This covers double consecutive claiming, double Middle claiming and triple consecutive claiming
 * http://jcbonsai.free.fr/sudoku/JSudokuUserGuide/relationalTechniques.html
 * http://forum.enjoysudoku.com/sudokuncexplainer-to-solve-and-rate-sudoku-non-consecutive-t36949.html#p285476
 */
public class lockedNC implements IndirectHintProducer {
	
	public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        int firstValue, valueIndex, secondValue;
        for (int value = 1; value <= 9; value++) {
			for (int regionTypeIndex = 4; regionTypeIndex >= (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex--) {
				//DG doesn't have cells in proximity
				if (regionTypeIndex == 3) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				//if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				//if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				int regionsNumber = Grid.getRegions(regionTypeIndex).length;
				for (int i = 0; i < regionsNumber; i++) {
					//Only lines or block regions allowed
					if (regionTypeIndex == 4 && i > 3) continue;
					//Decided on i to make it easier
					Grid.Region region = Grid.getRegions(regionTypeIndex)[i];
					BitSet ncValues = region.getPotentialPositions(grid, value);
					int ncValuesCard = ncValues.cardinality();
					if (ncValuesCard == 2 || ncValuesCard == 3) {
						// Potential Locked NC found
						BitSet potentialNC = (BitSet)ncValues.clone();
						firstValue = potentialNC.nextSetBit(0);
						secondValue = potentialNC.nextSetBit(firstValue + 1);
						valueIndex = firstValue;
						for (int ncValuesIndex = 1; ncValuesIndex < ncValuesCard; ncValuesIndex++)
							valueIndex = potentialNC.nextSetBit(valueIndex + 1);
						Cell[] originalCells =  new Cell[] {region.getCell(firstValue), region.getCell(valueIndex), region.getCell(secondValue)};
						if (ncValuesCard == 2)
							originalCells =  new Cell[] {region.getCell(firstValue), region.getCell(valueIndex)};
						if (regionTypeIndex > 0) {
							if ((valueIndex - firstValue) == ( ncValuesCard - 1 ) || (Settings.getInstance().isToroidal() && (valueIndex - firstValue) == 8))
								if (ncValuesCard == 2) {
									//Found Locked NC in 2 consecutive cells
									lockedNCHint hint = createHint1(grid, region.getCell(firstValue), region.getCell(valueIndex), Settings.getInstance().whichNC() == 1 ? value - 1 : value > 1 ? value - 1 : 9, Settings.getInstance().whichNC() == 1 ? (value + 1) % 10 : value < 9 ? value + 1 : 1, region, regionTypeIndex, value, originalCells);
									if (hint.isWorth())
										accu.add(hint);
								}
								else {
									//Found Locked NC in 3 consecutive cells
									//For NC+ check for 100000011, 110000001
									if ((Settings.getInstance().isToroidal() && (valueIndex - firstValue) == 8)) {//NC+ check
										if (potentialNC.nextSetBit(firstValue + 1) == (firstValue + 1)) {
											lockedNCHint hint = createHint1(grid, region.getCell(firstValue), null, Settings.getInstance().whichNC() == 1 ? value - 1 : value > 1 ? value - 1 : 9, Settings.getInstance().whichNC() == 1 ? (value + 1) % 10 : value < 9 ? value + 1 : 1, region, regionTypeIndex, value, originalCells);
											if (hint.isWorth())
											accu.add(hint);
										}
										if (potentialNC.nextSetBit(firstValue + 1) == (valueIndex - 1)) {
											lockedNCHint hint = createHint1(grid, region.getCell(valueIndex), null, Settings.getInstance().whichNC() == 1 ? value - 1 : value > 1 ? value - 1 : 9, Settings.getInstance().whichNC() == 1 ? (value + 1) % 10 : value < 9 ? value + 1 : 1, region, regionTypeIndex, value, originalCells);
											if (hint.isWorth())
											accu.add(hint);
										}
										continue;
									}
									if ((!Settings.getInstance().isToroidal() && (valueIndex - firstValue) == 8))
											continue;
									lockedNCHint hint = createHint1(grid, region.getCell(firstValue + 1), null, Settings.getInstance().whichNC() == 1 ? value - 1 : value > 1 ? value - 1 : 9, Settings.getInstance().whichNC() == 1 ? (value + 1) % 10 : value < 9 ? value + 1 : 1, region, regionTypeIndex, value, originalCells);
									if (hint.isWorth())
										accu.add(hint);
								}
						}
						//if (ncValuesCard == 2){
							//Found potential Locked NC in 2 Nearby cells
							//For NC+ check for 100000010 and 010000001
							lockedNCHint hint = createHint2(grid, region.getCell(firstValue), region.getCell(valueIndex), Settings.getInstance().whichNC() == 1 ? value - 1 : value > 1 ? value - 1 : 9, Settings.getInstance().whichNC() == 1 ? (value + 1) % 10 : value < 9 ? value + 1 : 1, region, regionTypeIndex, value, region.getCell(secondValue), ncValuesCard);
							if (hint.isWorth())
							accu.add(hint);
						//}
					}//if (ncValuesCard == 2 || ncValuesCard == 3)
					if (ncValuesCard == 4 && (regionTypeIndex == 4 || regionTypeIndex == 0)) {
						BitSet fourCorners = new BitSet();
						fourCorners.set(1);
						fourCorners.set(3);
						fourCorners.set(5);
						fourCorners.set(7);
						if (ncValues.equals(fourCorners)) {
							lockedNCHint hint = createHint3(grid, Settings.getInstance().whichNC() == 3 ? value - 1 : value > 1 ? value - 1 : 9, Settings.getInstance().whichNC() == 3 ? (value + 1) % 10 : value < 9 ? value + 1 : 1, region, regionTypeIndex, value);
							if (hint.isWorth())
								accu.add(hint);
						}
					}
				} // for (int i = 0; i < regionsNumber; i++)
			} // for (int regionTypeIndex = 2; regionTypeIndex >=0; regionTypeIndex--)
		}// for (int value = 1; value <= 9; value++)
	}

    private lockedNCHint createHint1(Grid grid, Cell ncCell1, Cell ncCell2, int value1, int value2, Grid.Region region, int regionTypeIndex, int value,  Cell[] original) {
        // Build list of removable potentials
		//Cell[] finalCells = null;
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		if (value1 != 0)
			if (grid.hasCellPotentialValue(ncCell1.getIndex(), value1))
				removablePotentials.put(ncCell1, SingletonBitSet.create(value1));
		if (value2 != 0)
			if (grid.hasCellPotentialValue(ncCell1.getIndex(), value2))
				if (removablePotentials.containsKey(ncCell1))
					removablePotentials.get(ncCell1).set(value2);
				else
					removablePotentials.put(ncCell1, SingletonBitSet.create(value2));	
		if (ncCell2 != null) {
			if (value1 != 0)
					if (grid.hasCellPotentialValue(ncCell2.getIndex(), value1))
						removablePotentials.put(ncCell2, SingletonBitSet.create(value1));
			if (value2 != 0)
					if (grid.hasCellPotentialValue(ncCell2.getIndex(), value2))
						if (removablePotentials.containsKey(ncCell2))
							removablePotentials.get(ncCell2).set(value2);
						else
							removablePotentials.put(ncCell2, SingletonBitSet.create(value2));
					//finalCells = new Cell[] {ncCell1, ncCell2};
		}
		//else
			//finalCells = new Cell[] {ncCell1};
		int [] finalValues = new int[] {value1, value2};
		if (value1 == 0)
			finalValues = new int[] {value2};
		if (value2 == 0)
			finalValues = new int[] {value1};		
         // Create hint
        return new lockedNCHint(this, removablePotentials, original, finalValues, region, value);
    }

    private lockedNCHint createHint2(Grid grid, Cell ncCell1, Cell ncCell2, int value1, int value2, Grid.Region region, int regionTypeIndex, int value, Cell ncCell3, int card) {
        Cell[] finalCells = new Cell[] {ncCell1, ncCell2};
		// Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		CellSet victims = new CellSet(Grid.wazirCellsRegular[ncCell1.getIndex()]);
		CellSet victims2 = new CellSet(Grid.wazirCellsRegular[ncCell2.getIndex()]);
		CellSet victims3 = new CellSet(Grid.wazirCellsRegular[ncCell3.getIndex()]);
		if (Settings.getInstance().isToroidal()) {
		   victims = new CellSet(Grid.wazirCellsToroidal[ncCell1.getIndex()]);
		   victims2 = new CellSet(Grid.wazirCellsToroidal[ncCell2.getIndex()]);
		   victims3 = new CellSet(Grid.wazirCellsToroidal[ncCell3.getIndex()]);
		}
		victims.retainAll(victims2);
		if (card == 3) {
			victims.retainAll(victims3);
			finalCells = new Cell[] {ncCell1, ncCell2, ncCell3};
		}
        for (Cell cell : victims) {
			if (value1 != 0)
				if (grid.hasCellPotentialValue(cell.getIndex(), value1))
					removablePotentials.put(cell, SingletonBitSet.create(value1));
			if (value2 != 0)
				if (grid.hasCellPotentialValue(cell.getIndex(), value2))
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(value2);
					else
						removablePotentials.put(cell, SingletonBitSet.create(value2));		
        }
		int [] finalValues = new int[] {value1, value2};
		if (value1 == 0)
			finalValues = new int[] {value2};
		if (value2 == 0)
			finalValues = new int[] {value1};	
        // Create hint
        return new lockedNCHint(this, removablePotentials, finalCells, finalValues, region, value);
    }

    private lockedNCHint createHint3(Grid grid, int value1, int value2, Grid.Region region, int regionTypeIndex, int value) {
        Cell[] finalCells = new Cell[] {region.getCell(1), region.getCell(3),region.getCell(5),region.getCell(7)};
		// Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		Cell cell = region.getCell(4);
		if (value1 != 0)
			if (grid.hasCellPotentialValue(cell.getIndex(), value1))
				removablePotentials.put(cell, SingletonBitSet.create(value1));
		if (value2 != 0)
			if (grid.hasCellPotentialValue(cell.getIndex(), value2))
				if (removablePotentials.containsKey(cell))
					removablePotentials.get(cell).set(value2);
				else
					removablePotentials.put(cell, SingletonBitSet.create(value2));		
		int [] finalValues = new int[] {value1, value2};
		if (value1 == 0)
			finalValues = new int[] {value2};
		if (value2 == 0)
			finalValues = new int[] {value1};	
        // Create hint
        return new lockedNCHint(this, removablePotentials, finalCells, finalValues, region, value);
    }

    @Override
    public String toString() {
        return "Locked NC";
    }

}
