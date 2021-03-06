/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package com.borlander.rac525791.dashboard;

import org.eclipse.swt.widgets.Canvas;

import com.borlander.rac525791.dashboard.data.DashboardDataModel;
import com.borlander.rac525791.dashboard.data.DashboardListener;
import com.borlander.rac525791.dashboard.data.ThresholdListener;

public class DashboardUpdater implements DashboardListener, ThresholdListener {
	private final DashboardDataModel myModel;
	private final DashboardFigure myDashboard;
	private final Canvas myHost;

	public DashboardUpdater(DashboardDataModel model, DashboardFigure dashboard, Canvas control){
		myModel = model;
		myDashboard = dashboard;
		myHost = control;
		myModel.setListener(this);
		myModel.setThresholdListener(this);
	}

	public void statusChanged() {
		myDashboard.setVesselStatus(myModel.getVesselStatus());
		myHost.redraw();
	}
	
	public void nameChanged() {
		myDashboard.setVesselName(myModel.getVesselName());
		myHost.redraw();
	}

	public void actualDepthChanged() {
		myDashboard.setDepth(myModel.getActualDepth());
		myHost.redraw();
	}
	
	public void demandedDepthChanged() {
		myDashboard.setDemandedDepth(myModel.getDemandedDepth());
		myDashboard.setIgnoreDemandedDepth(myModel.isIgnoreDemandedDepth());
		myHost.redraw();
	}
	
	public void actualSpeedChanged() {
		myDashboard.setSpeed(myModel.getActualSpeed());
		myHost.redraw();
	}

	public void demandedSpeedChanged() {
		myDashboard.setDemandedSpeed(myModel.getDemandedSpeed());
		myDashboard.setIgnoreDemandedSpeed(myModel.isIgnoreDemandedSpeed());
		myHost.redraw();
	}

	public void actualDirectionChanged() {
		myDashboard.setDirection(myModel.getActualDirection());
		myHost.redraw();
	}

	public void demandedDirectionChanged() {
		myDashboard.setDemandedDirection(myModel.getDemandedDirection());
		myDashboard.setIgnoreDemandedDirection(myModel.isIgnoreDemandedDirection());
		myHost.redraw();
	}

	public void depthUnitsChanged() {
		myDashboard.setDepthUnits(myModel.getDepthUnits());
		myHost.redraw();
	}

	public void speedUnitsChanged() {
		myDashboard.setSpeedUnits(myModel.getSpeedUnits());
		myHost.redraw();
	}
	
	public void depthOnThresholdChanged(boolean isOkNow) {
		myDashboard.updateDepthOnThreshold(isOkNow);
		myHost.redraw();
	}
	
	public void directionOnThresholdChanged(boolean isOkNow) {
		myDashboard.updateDirectionOnThreshold(isOkNow);
		myHost.redraw();
	}

	public void speedOnThresholdChanged(boolean isOkNow) {
		myDashboard.updateSpeedOnThreshold(isOkNow);
		myHost.redraw();
	}
}
