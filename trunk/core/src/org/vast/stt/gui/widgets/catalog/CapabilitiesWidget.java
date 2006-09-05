/***************************************************************
 (c) Copyright 2005, University of Alabama in Huntsville (UAH)
 ALL RIGHTS RESERVED

 This software is the property of UAH.
 It cannot be duplicated, used, or distributed without the
 express written consent of UAH.

 This software developed by the Vis Analysis Systems Technology
 (VAST) within the Earth System Science Lab under the direction
 of Mike Botts (mike.botts@atmos.uah.edu)
 ***************************************************************/

package org.vast.stt.gui.widgets.catalog;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.vast.ows.OWSCapabilitiesReader;
import org.vast.ows.OWSException;
import org.vast.ows.OWSLayerCapabilities;
import org.vast.ows.OWSServiceCapabilities;
import org.vast.ows.server.OWSServlet;
import org.vast.ows.sos.SOSCapabilitiesReader;
import org.vast.ows.sos.SOSLayerCapabilities;
import org.vast.ows.wcs.WCSCapabilitiesReader;
import org.vast.ows.wfs.WFSCapabilitiesReader;
import org.vast.ows.wms.WMSCapabilitiesReader;

/**
 * <p><b>Title:</b>
 *  TODO:  Capabilities Widget
 * </p>
 *
 * <p><b>Description:</b><br/>
 * </p>
 *
 * <p>Copyright (c) 2006</p>
 * @author Tony Cook
 * @date Aug 17, 2006
 * @version 1.0
 */

public class CapabilitiesWidget implements SelectionListener 
{
	CapServers capServers;
	Composite mainGroup;
	private Button getCapsBtn;
	private Combo typesCombo;
	private Combo serverCombo;
	private LayerTree layerTree;
	
	public CapabilitiesWidget(Composite parent) {
		capServers = new CapServers();
		capServers.loadServerData();
		initGui(parent);
	}

	public void initGui(Composite parent){
		GridData gd;
		
		mainGroup = new Composite(parent, 0x0);
		mainGroup.setLayout(new GridLayout());
		
		Group topGroup = new Group(mainGroup, 0x0);
		topGroup.setLayout(new GridLayout(2, false));
		topGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false ));
		topGroup.setText("Servers");

		//  ServerType
		Label typesLabel = new Label(topGroup, SWT.LEFT);  
		typesLabel.setText("Service Types:");
		typesCombo = new Combo(topGroup, SWT.READ_ONLY);
		typesCombo.setItems(capServers.getServiceTypes());
		//typesCombo.setItems(new String [] {"WMS", "WCS", "WFS", "SOS"});
		gd = new GridData(SWT.LEFT, SWT.FILL, false, true);
		typesCombo.setLayoutData(gd);
		typesCombo.select(3);
		typesCombo.addSelectionListener(this);
		
		//  Server  
		Label serverLabel = new Label(topGroup, SWT.LEFT);
		serverLabel.setText("Server:");
		serverCombo = new Combo(topGroup, SWT.READ_ONLY);
		serverCombo.setItems(new String [] {"AAA", "bbb"});
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		serverCombo.setLayoutData(gd);
		setServerComboItems();
		serverCombo.addSelectionListener(this);
		
		//  Edit/GetCaps btns
		//  NOTE that I had to put in a new composite to get buttons right aligned
		Composite btnComp = new Composite(topGroup, 0x0);
		btnComp.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.END, SWT.CENTER, false, false);
		gd.horizontalSpan = 2;
		btnComp.setLayoutData(gd);
		
		Button editBtn = new Button(btnComp, SWT.PUSH);
		editBtn.setText("Edit");
		editBtn.setToolTipText("Edit Capabilities Server List");
		gd = new GridData(SWT.END, SWT.CENTER, false, false);
		gd.widthHint = 60;
		editBtn.setLayoutData(gd);
		editBtn.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e){
				new EditCapServerDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), capServers);
			}
			
			public void widgetDefaultSelected(SelectionEvent e){};
		});
		
		getCapsBtn = new Button(btnComp, SWT.PUSH);
		getCapsBtn.setText("Get Caps");
		getCapsBtn.setToolTipText("Get Capabilities from selected Server");
		gd = new GridData(SWT.END, SWT.CENTER, false, false);
		gd.widthHint = 60;
		getCapsBtn.setLayoutData(gd);
		getCapsBtn.addSelectionListener(this);
		
		layerTree = new LayerTree(mainGroup);
		layerTree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
	}

	//  repopulate serverCombo with selected type's servers 
	//  and select the 0th entry
	protected void setServerComboItems(){
		String type = typesCombo.getText();
		String [] types = capServers.getServers(type);
		//  types can be null still, so check it
		if(types == null)
			types =  new String[]{};
		serverCombo.setItems(types);
		serverCombo.select(0);
	}
	
	/**
	 *  get the Capabilities from the currently selected server
	 */
	protected void getCapabilities(String server, ServiceType type){
		System.err.println("Get caps for " + server);
		ServerInfo info = capServers.getServerInfo(server, type);
		OWSCapabilitiesReader reader;
		switch(type){
		case WMS:
			reader = new WMSCapabilitiesReader();
			break;
		case WCS:
			reader = new WCSCapabilitiesReader();
			break;
		case WFS:
			reader = new WFSCapabilitiesReader();
			break;
		case SOS:
			reader = new SOSCapabilitiesReader();
			break;
		default:
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
            		"STT Error", "Error reading caps from " + info.url);
			return;
		}
		OWSServiceCapabilities caps = null;
		try {		
			caps = reader.readCapabilities(info.url, info.version);
		} catch (OWSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<OWSLayerCapabilities> layers = caps.getLayers();
//		for(OWSLayerCapabilities layerTmp: layers){
//			System.err.println("Layer is " + layerTmp.getName());
//		}
		layerTree.setInput(layers);
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		Control control = (Control)e.getSource();
		
		if(control == getCapsBtn){
			String server = serverCombo.getText();
			getCapabilities(server, ServiceType.getServiceType(typesCombo.getText()));
		} else if(control == typesCombo) {
			//  repopulate servers with selected type
			setServerComboItems();
		} else if(control == serverCombo) {
			//  do nothing
		}
	}
}
