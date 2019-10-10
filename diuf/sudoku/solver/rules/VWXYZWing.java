package diuf.sudoku.solver.rules;

import java.util.*;
import diuf.sudoku.*;
import diuf.sudoku.solver.*;
import diuf.sudoku.tools.*;

/**
 * Implementation of the "VWXYZ-Wing" solving technique.
 * Similar to ALS-XZ with smaller ALS being a bivalue cell
 * can catch the double linked version which is similar to Sue-De-Coq
 * Larger  ALS has 4 cells in which cell candidates any size between 2-5
 */
public class VWXYZWing implements IndirectHintProducer {

    /**
     * 
     * <ul>
     * <li>VWXYZ-Wing</li>
     * <li>ALS-xz with a bivalue cell</li>
	 * <li>By SudokuMonster 2019</li>
     * </ul>
     */

    private boolean isVWXYZWing(BitSet vwxyzValues,BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet aBit, Cell yzCell, Cell xzCell, Cell wzCell, Cell vzCell, Cell vwxyzCell) {
        BitSet inter = (BitSet)aBit.clone();
		inter.and(xzValues);
		if (inter.cardinality() == 1 && !(yzCell.getX() == xzCell.getX() || yzCell.getY() == xzCell.getY() || yzCell.getB() == xzCell.getB()))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(wzValues);
		if (inter.cardinality() == 1 && !(yzCell.getX() == wzCell.getX() || yzCell.getY() == wzCell.getY() || yzCell.getB() == wzCell.getB()))
			return false;		
        inter = (BitSet)aBit.clone();
		inter.and(vzValues);
		if (inter.cardinality() == 1 && !(yzCell.getX() == vzCell.getX() || yzCell.getY() == vzCell.getY() || yzCell.getB() == vzCell.getB()))
			return false;
        inter = (BitSet)aBit.clone();
		inter.and(vwxyzValues);
		if (inter.cardinality() == 1 && !(yzCell.getX() == vwxyzCell.getX() || yzCell.getY() == vwxyzCell.getY() || yzCell.getB() == vwxyzCell.getB()))
			return false;			
        return true;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
		int biggestCardinality = 0;
		int biggestCardinality2 = 0;
		int biggestCardinality3 = 0;
		int biggestCardinality4 = 0;
		int wingSize;
		int w1Value = 0;
		int w2Value = 0;
		int w3Value = 0;
		BitSet w1Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet w2Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet w3Bit = new BitSet(10);//Avoiding the use of y to avoid confusion with x,z of ALS XZ
		BitSet remCand	=  new BitSet(10);//candidates of set not in yzCell	
		for (int i = 0; i < 81; i++) {
			Cell vwxyzCell = Grid.getCell(i);
			BitSet vwxyzValues = grid.getCellPotentialValues(i);
			if (vwxyzValues.cardinality() > 1 && vwxyzValues.cardinality() < 6) {
				// Potential VWXYZ cell found
				biggestCardinality = vwxyzValues.cardinality();
				wingSize = vwxyzValues.cardinality();
				for (int vzCellIndex : vwxyzCell.getForwardVisibleCellIndexes()) {
					BitSet vzValues = grid.getCellPotentialValues(vzCellIndex);
					BitSet inter = (BitSet)vwxyzValues.clone();
					inter.or(vzValues);
					if (vzValues.cardinality() > 1 && inter.cardinality() < 6) {
						// Potential WZ cell found
						Cell vzCell = Grid.getCell(vzCellIndex);
						biggestCardinality2 = biggestCardinality;
						if (vzValues.cardinality() > biggestCardinality2)
							biggestCardinality2 = vzValues.cardinality();
						wingSize = vwxyzValues.cardinality() + vzValues.cardinality();
						CellSet intersection1 = new CellSet(vwxyzCell.getForwardVisibleCells());
						intersection1.retainAll(vzCell.getForwardVisibleCells());
						for (Cell wzCell : intersection1) {
							int wzCellIndex = wzCell.getIndex();
							BitSet wzValues = grid.getCellPotentialValues(wzCellIndex);
							inter = (BitSet)vwxyzValues.clone();
							inter.or(vzValues);
							inter.or(wzValues);
							if (wzValues.cardinality() > 1 && inter.cardinality() < 6) {
								// Potential XZ cell found
								biggestCardinality3 = biggestCardinality2;
								if (wzValues.cardinality() > biggestCardinality3)
									biggestCardinality3 = wzValues.cardinality();
								wingSize = vwxyzValues.cardinality() + vzValues.cardinality() + wzValues.cardinality();
								CellSet intersection2 = new CellSet(wzCell.getForwardVisibleCells());
								intersection2.retainAll(intersection1);
								for (Cell xzCell : intersection2) {
									int xzCellIndex = xzCell.getIndex();
									BitSet xzValues = grid.getCellPotentialValues(xzCellIndex);
									inter = (BitSet)vwxyzValues.clone();
									inter.or(vzValues);
									inter.or(wzValues);
									inter.or(xzValues);
									if (xzValues.cardinality() > 1 && inter.cardinality() == 5) {
										// Potential XZ cell found
										biggestCardinality4 = biggestCardinality3;
										if (xzValues.cardinality() > biggestCardinality4)
											biggestCardinality4 = xzValues.cardinality();
										wingSize = vwxyzValues.cardinality() + vzValues.cardinality() + wzValues.cardinality() + xzValues.cardinality();
										//Restrict potential yzCell to Grid Cells that are visible by one or more of the other cells
										CellSet noYZ = new CellSet(new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80});
										noYZ.removeAll(vwxyzCell.getVisibleCells());
										noYZ.removeAll(vzCell.getVisibleCells());
										noYZ.removeAll(wzCell.getVisibleCells());
										noYZ.removeAll(xzCell.getVisibleCells());	
										CellSet yzCellRange = new CellSet(new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80});
										yzCellRange.removeAll(noYZ);
										yzCellRange.remove(vwxyzCell);
										yzCellRange.remove(vzCell);			
										yzCellRange.remove(wzCell);
										yzCellRange.remove(xzCell);
										for (Cell yzCell : yzCellRange) {
											int yzCellIndex = yzCell.getIndex();
											BitSet yzValues = grid.getCellPotentialValues(yzCellIndex);
											BitSet union = (BitSet)yzValues.clone();
											union.and(inter);
											if (yzValues.cardinality() == 2 && union.cardinality() == 2) {
												// Potential YZ cell found
												// Get the "z" value and the "x" value in ALS-xz
												boolean doubleLink = true;//assume doubly linked until testing
												int zValue;
												int xValue;
												BitSet zBit = new BitSet(10);
												BitSet xBit = new BitSet(10);
													xValue = yzValues.nextSetBit(0);
													xBit.set(xValue);
													zValue = yzValues.nextSetBit(xValue + 1);
													zBit.set(zValue);													
												if (!isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, zBit, yzCell, xzCell, wzCell, vzCell, vwxyzCell))//Test if doubly linked
														doubleLink = false;
												if (isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, xBit, yzCell, xzCell, wzCell, vzCell, vwxyzCell)) {
													if (!doubleLink) {
														// Found VWXYZ-Wing pattern
														VWXYZWingHint hint = createHint(
																grid, vwxyzCell, Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																vzValues, wzValues, xzValues, yzValues, vwxyzValues, xValue, zValue, xBit, zBit, biggestCardinality4, wingSize, doubleLink, w1Value, w2Value, w3Value, w1Bit, w2Bit, w3Bit, remCand, inter);
														if (hint.isWorth())
															accu.add(hint);
													}
													else {
														// Found VWXYZ-Wing doubly linked pattern
														remCand = (BitSet)inter.clone();
														remCand.xor(yzValues);
														w1Value = remCand.nextSetBit(0);
														w1Bit =  new BitSet(10);
														w1Bit.set(w1Value);
														w2Value = remCand.nextSetBit(w1Value+1);
														w2Bit =  new BitSet(10);
														w2Bit.set(w2Value);
														w3Value = remCand.nextSetBit(w2Value+1);
														w3Bit =  new BitSet(10);
														w3Bit.set(w3Value);
														VWXYZWingHint hint = createHint(
																grid, vwxyzCell, Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																vzValues, wzValues, xzValues, yzValues, vwxyzValues, xValue, zValue, xBit, zBit, biggestCardinality4, wingSize, doubleLink, w1Value, w2Value, w3Value, w1Bit, w2Bit, w3Bit, remCand, inter);
														if (hint.isWorth())
															accu.add(hint);
													}
												} // if (isVWXYZWing(vwxyzValues, vzValues, wzValues, xzValues, xBit, yzCell, xzCell, wzCell, vzCell, vwxyzCell))
												else
													if (doubleLink) {
														doubleLink = false;
														VWXYZWingHint hint = createHint(
																grid, vwxyzCell, Grid.getCell(vzCellIndex), Grid.getCell(wzCellIndex), Grid.getCell(xzCellIndex), Grid.getCell(yzCellIndex),
																vzValues, wzValues, xzValues, yzValues, vwxyzValues, zValue, xValue, zBit, xBit, biggestCardinality4, wingSize, doubleLink, w1Value, w2Value, w3Value, w1Bit, w2Bit, w3Bit, remCand, inter);
														if (hint.isWorth())
															accu.add(hint);
													}//if (doubleLink)
											} // if (yzValues.cardinality() == 2 && union.cardinality() == 2) {
										} // for (Cell yzCell : yzCellRange)
									} // if (xzValues.cardinality() > 1 && inter.cardinality() == 5)
								} // for (Cell xzCell : intersection2)
							} // if (wzValues.cardinality() > 1 && inter.cardinality() < 6)
						} // for (Cell wzCell : intersection1)
					} // if (vzValues.cardinality() > 1 && inter.cardinality() < 6)
				} // for (int vzCellIndex : vwxyzCell.getForwardVisibleCellIndexes())
			} // (vwxyzValues.cardinality() > 1 && vwxyzValues.cardinality() < 6) 
        } // for i
    }

    private VWXYZWingHint createHint(
            Grid grid, Cell vwxyzCell, Cell vzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet vzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues, BitSet vwxyzValues, int xValue, int zValue, BitSet xBit, BitSet zBit, int biggestCardinality, int wingSize, boolean doubleLink, int w1Value, int w2Value, int w3Value, BitSet w1Bit, BitSet w2Bit, BitSet w3Bit, BitSet remCand, BitSet wingSet) {
        // Build list of removable potentials
		boolean weakPotentials = false;
		boolean strongPotentialsX = false;//if both remain false then proceed as normal VWXYZ if weak is false strong is true and z is false then swap z to x
		boolean strongPotentialsZ = false;//if both remain false then proceed as normal VWXYZ if weak is false strong is true and z is false then swap z to x        
		BitSet inter = (BitSet)zBit.clone();
		Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
		CellSet victims = null;
		if (doubleLink) {//if no eliminations at all then produce Hint as regular VWXYZ
			inter = (BitSet)w1Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
						victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w1Bit.clone();
			inter.and(vwxyzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vwxyzCell.getVisibleCells());
					else
						victims.retainAll(vwxyzCell.getVisibleCells());
			victims.remove(vwxyzCell);
			victims.remove(vzCell);			
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w1Value)) {		
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w1Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w1Value));
					weakPotentials = true;
				}
			}
			victims = null;
			inter = (BitSet)w2Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
					victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w2Bit.clone();
			inter.and(vwxyzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vwxyzCell.getVisibleCells());
					else
						victims.retainAll(vwxyzCell.getVisibleCells());
			victims.remove(vwxyzCell);
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w2Value)) {
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w2Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w2Value));
					weakPotentials = true;
				}
			}
			victims = null;
			inter = (BitSet)w3Bit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
					victims = new CellSet (xzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (wzCell.getVisibleCells());
					else
						victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vzCell.getVisibleCells());
					else
						victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)w3Bit.clone();
			inter.and(vwxyzValues);
			if (inter.cardinality() == 1) 
					if (victims == null)
						victims = new CellSet (vwxyzCell.getVisibleCells());
					else
						victims.retainAll(vwxyzCell.getVisibleCells());
			victims.remove(vwxyzCell);
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), w3Value)) {
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(w3Value);
					else
						removablePotentials.put(cell, SingletonBitSet.create(w3Value));
					weakPotentials = true;
				}
			}			
			victims = new CellSet(yzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(xzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(xzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(wzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(wzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(vzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(vzCell.getVisibleCells());
			inter = (BitSet)xBit.clone();
			inter.and(vwxyzValues);
			if (inter.cardinality() == 1)
				victims.retainAll(vwxyzCell.getVisibleCells());
			victims.remove(vwxyzCell);
			victims.remove(vzCell);
			victims.remove(wzCell);
			victims.remove(xzCell);
			victims.remove(yzCell);
			for (Cell cell : victims) {
				if (grid.hasCellPotentialValue(cell.getIndex(), xValue)) {
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(xValue);
					else
						removablePotentials.put(cell, SingletonBitSet.create(xValue));
					strongPotentialsX = true;
				}
			}
		}
		victims = new CellSet(yzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(xzValues);
		if (inter.cardinality() == 1)
			victims.retainAll(xzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(wzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(wzCell.getVisibleCells());
		inter = (BitSet)zBit.clone();
		inter.and(vzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(vzCell.getVisibleCells());
 		inter = (BitSet)zBit.clone();
		inter.and(vwxyzValues);
        if (inter.cardinality() == 1)
			victims.retainAll(vwxyzCell.getVisibleCells());
        victims.remove(vwxyzCell);
		victims.remove(vzCell);
        victims.remove(wzCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), zValue)) {
					if (removablePotentials.containsKey(cell))
						removablePotentials.get(cell).set(zValue);
					else
						removablePotentials.put(cell, SingletonBitSet.create(zValue));
					strongPotentialsZ = true;
            }
        }
		if (doubleLink)
			if (!weakPotentials)
				if (!strongPotentialsZ) {
					doubleLink = false;
					return new VWXYZWingHint(this, removablePotentials,
						vwxyzCell, vzCell, wzCell, xzCell, yzCell, xValue, zValue, biggestCardinality, wingSize, doubleLink, wingSet);					
				}
				else
					if (!strongPotentialsX)
						doubleLink = false;
        // Create hint
        return new VWXYZWingHint(this, removablePotentials,
                vwxyzCell, vzCell, wzCell, xzCell, yzCell, zValue, xValue, biggestCardinality, wingSize, doubleLink, wingSet);
    }

    @Override
    public String toString() {
        return "VWXYZ-Wings";
    }
}
