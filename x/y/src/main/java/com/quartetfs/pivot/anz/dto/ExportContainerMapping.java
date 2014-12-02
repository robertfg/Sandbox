package com.quartetfs.pivot.anz.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ExportContainerMapping {

	private String container;
	private List<String> levels = new ArrayList<String>();
	private List<String> measures = new ArrayList<String>();
	private List<String> dimensions = new ArrayList<String>();
	private List<String> levelsForOutput = new ArrayList<String>();
	 

	public List<String> getDimensions() {
		return dimensions;
	}

	public void setDimensions(List<String> dimensions) {
		this.dimensions = dimensions;
	}

	public ExportContainerMapping(String containerName) {
		this.container = containerName;
	}

	public ExportContainerMapping(String containerName, List<String> levels, List<String> measures,List<String> dimensions) {
		this.container = containerName;
		this.levels = levels;
		this.measures = measures;
		this.dimensions = dimensions;
	}

	public String getContainer() {
		return container;
	}

	public void setLevels(List<String> levels) {
		this.levels = levels;
	}

	public List<String> getLevels() {
		return levels;
	}

	public List<String> getMeasures() {
		return measures;
	}

	public void setMeasures(List<String> measures) {
		this.measures = measures;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("container", container).append("levels", levels)
				.append("measures", measures).toString();
	}

	public List<String> getLevelsForOutput() {
		return levelsForOutput;
	}
	
	public List<String> getDefaultLevelsForOutput() {
		levelsForOutput.add("COB Date@COB Date");
		levelsForOutput.add("Container@Container");
		levelsForOutput.add("Base Currency@Base Currency");
		levelsForOutput.addAll(getLevels());
		
		return levelsForOutput;
	}
	
	

	public void setLevelsForOutput(List<String> levelsForOutput) {
		this.levelsForOutput = levelsForOutput;
	}

}
