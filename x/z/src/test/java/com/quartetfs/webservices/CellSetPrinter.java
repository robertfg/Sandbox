/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.quartetfs.webservices.AxisDTO;
import com.quartetfs.webservices.AxisPositionDTO;
import com.quartetfs.webservices.CellDTO;
import com.quartetfs.webservices.CellSetDTO;
import com.quartetfs.webservices.MemberDTO;

/**
 * 
 * Prints the content of a cell set.
 * 
 * @author Quartet FS
 *
 */
public class CellSetPrinter {

	protected final CellSetDTO cellSet;
	
	protected final AxisDTO slicer;
	
	protected final List<AxisDTO> axes;
	
	protected final List<CellDTO> cells;
	
	public CellSetPrinter(CellSetDTO cellSet) {
		this.cellSet = cellSet;
		this.axes = cellSet.getAxes().getAxis();
		this.slicer = cellSet.getSlicerAxis();
		this.cells = cellSet.getCells().getCell();
	}

	/**
	 * Compute axis positions from the cell ordinal with the classic formula:
	 * <ul>
	 * <li>(x0, x1, x2) -> x0 + x1 * n0 + x2 * n1 * n2
	 * <li>ordinal -> (ordinal % n0, (ordinal / n0) % n1, (ordinal / (n0*n1)) % n2)
	 * </ul>
	 * 
	 * @param ordinal
	 * @return tuple expressed by coordinates
	 */
	protected List<String> getTuple(int ordinal) {
		List<String> tuple = new ArrayList<>();

		// Lookup positions on axes
		final int[] axisCoordinates = new int[axes.size()];

		int coeff = 1;
		for(int a = 0; a < axisCoordinates.length; a++) {
			int positionCount = axes.get(a).getPositions().getPosition().size();
			axisCoordinates[a] = (ordinal / coeff) % positionCount;
			coeff *= positionCount;
		}
		
		for(int a = 0; a < axisCoordinates.length; a++) {
			AxisPositionDTO position = axes.get(a).getPositions().getPosition().get(axisCoordinates[a]);
			for(MemberDTO member : position.getMembers().getMember()) {
				for(String pathElement : member.getPath().getItems().getItem()) {
					if(!"AllMember".equals(pathElement)) {
						tuple.add(pathElement);
					}
				}
			}
		}
		
		// Append slicer content
		for(AxisPositionDTO position : slicer.getPositions().getPosition()) {
			for(MemberDTO member : position.getMembers().getMember()) {
				for(String pathElement : member.getPath().getItems().getItem()) {
					if(!"AllMember".equals(pathElement)) {
						tuple.add(pathElement);
					}
				}
			}
		}
		return tuple;
	}
	
	public void print(PrintStream out) {
		for(CellDTO cell : cells) {
			System.out.println(getTuple(cell.getOrdinal()) + " " + cell.getFormattedValue());
		}
	}
	
}
