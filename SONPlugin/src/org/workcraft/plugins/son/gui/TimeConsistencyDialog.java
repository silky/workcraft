package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.ScenarioRef;
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeConsistencyDialog extends StructureVerifyDialog{

	private static final long serialVersionUID = 1L;

	protected VisualSON vNet;

	protected JPanel infoPanel, scenarioItemPanel, nodeItemPanel, selectionPanel, granularityPanel;
	protected JTabbedPane selectionTabbedPane;
	protected JList<ListItem> scenarioList, nodeList;
	protected JCheckBox inconsistencyHighLight, unspecifyHighlight;
	private JRadioButton year_yearButton, hour_minusButton;
	private ButtonGroup granularityGroup;

	private Color greyoutColor = Color.LIGHT_GRAY;
	protected ScenarioRef selectedScenario;
	protected ArrayList<Node> selectedNodes;

	public enum Granularity{
		YEAR_YEAR,
		CLOCKHOUR_MINUS;
	}

	@SuppressWarnings("rawtypes")
	class ScenarioListRenderer extends JRadioButton implements ListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean hasFocus) {

			setSelected(isSelected);
			setFont(list.getFont());

			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(value.toString());
			return this;
		}
	}

	protected void createGranularityButtons(){
		granularityPanel = new JPanel();
		granularityPanel.setBorder(BorderFactory.createTitledBorder("Time Granularity"));
		granularityPanel.setLayout(new FlowLayout());

		year_yearButton = new JRadioButton();
		year_yearButton.setText("T:year D:year");
		year_yearButton.setSelected(true);

		hour_minusButton = new JRadioButton();
		hour_minusButton.setText("T:24-hour clock D:minus");

		granularityGroup = new ButtonGroup();
		granularityGroup.add(year_yearButton);
		granularityGroup.add(hour_minusButton);

		granularityPanel.add(year_yearButton);
		granularityPanel.add(hour_minusButton);

	}

	@SuppressWarnings("unchecked")
	protected void createScenarioItemPanel(){
		scenarioItemPanel = new JPanel();
		ArrayList<ScenarioRef> scenarioSavelist = net.importScenarios(owner);
		DefaultListModel<ListItem> listModel = new DefaultListModel<ListItem>();

		for(int i=0; i<scenarioSavelist.size(); i++){
			listModel.addElement(new ListItem("Scenario "+(i+1), scenarioSavelist.get(i)));
		}

		scenarioList = new JList<ListItem> (listModel);
		scenarioList.setCellRenderer(new ScenarioListRenderer());
		scenarioList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scenarioList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed (MouseEvent event){
				JList<ListItem> list = (JList<ListItem>) event.getSource();

				int index = list.locationToIndex(event.getPoint());
				try{
					for(int i=0; i<list.getModel().getSize();i++){
						ListItem item;
						item = (ListItem)list.getModel().getElementAt(i);
						if(item != null)item.setSelected(false);
					}

					ListItem item = (ListItem)list.getModel().getElementAt(index);
					item.setSelected(true);
					Object obj = item.getListItem();
					if(obj instanceof ScenarioRef){
						selectedScenario = (ScenarioRef)obj;
						scenarioColorUpdate();
					}
					list.repaint(list.getCellBounds(index, index));
				}catch (ArrayIndexOutOfBoundsException e){}
			}
		});

		JScrollPane listScroller = new JScrollPane(scenarioList);
		listScroller.setPreferredSize(new Dimension(350, 220));
		listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		listScroller.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		listScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 0));

		scenarioItemPanel.add(listScroller);
	}

	@SuppressWarnings("unchecked")
	protected void createNodeItemPanel(){
		nodeItemPanel = new JPanel();
		vNet = (VisualSON)we.getModelEntry().getVisualModel();
		selectedNodes = new ArrayList<Node>();

		DefaultListModel<ListItem> listModel = new DefaultListModel<ListItem>();


		for(Node vn : vNet.getSelection()){
			if(vn instanceof VisualComponent){
				Node node = ((VisualComponent) vn).getReferencedComponent();
				if(node instanceof Time){
					selectedNodes.add(node);
					listModel.addElement(new ListItem(net.getNodeReference(node), node));
				}
			}
		}

		nodeList = new JList<ListItem> (listModel);
		nodeList.setCellRenderer(new ItemListRenderer());

		nodeList.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent event)
			{
				JList<ListItem> list = (JList<ListItem>) event.getSource();

				int index = list.locationToIndex(event.getPoint());
				try{
					ListItem item = (ListItem)list.getModel().getElementAt(index);
					item.setSelected(!item.isSelected());

					if(item.isSelected() ){
						selectedNodes.add((Node)item.getListItem());
						item.setItemColor(Color.ORANGE);
					}
					if(!item.isSelected() ){
						selectedNodes.remove((Node)item.getListItem());
						item.setItemColor(Color.BLACK);
					}
					list.repaint(list.getCellBounds(index, index));

				}catch (ArrayIndexOutOfBoundsException e){}
			}
		});

		nodeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(nodeList);
		listScroller.setPreferredSize(new Dimension(350, 220));
		listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		listScroller.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		listScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 0));
		nodeItemPanel.add(listScroller);
	}

	protected void createSelectionPane(){
		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(1,1,1,1));
		UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", false);

		createGroupItemsPanel();
		createScenarioItemPanel();
		createNodeItemPanel();

		selectionTabbedPane = new JTabbedPane();
		selectionTabbedPane.addTab("Group", groupItemPanel);
		selectionTabbedPane.addTab("Scenario", scenarioItemPanel);
		selectionTabbedPane.addTab("Node", nodeItemPanel);

		selectionTabbedPane.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent e) {
	        	 net.refreshColor();
	    		 addAllButton.setEnabled(true);
	    		 removeAllButton.setEnabled(true);
	    		 int index = getTabIndex();

	    		 if(index == 0){
		    		 for(int i=0;i<groupList.getModel().getSize();i++){
		    			 ListItem item = (ListItem)groupList.getModel().getElementAt(i);
		    			 item.setItemColor(Color.ORANGE);
		    		 }

	    		 }else if(index == 1){
		    		 addAllButton.setEnabled(false);
		    	     removeAllButton.setEnabled(false);
		    	     if(selectedScenario != null)
		    	    	 scenarioColorUpdate();

		    	 }else if(index == 2){
		    		 for(int i=0;i<nodeList.getModel().getSize();i++){
		    			 ListItem item = (ListItem)nodeList.getModel().getElementAt(i);
		    			 item.setItemColor(Color.ORANGE);
		    		 }
		    		 vNet.selectNone();
		    	 }
	        }
	    });
	}

	@Override
	protected void createSelectionButtonsPanel(){

		super.createSelectionButtonsPanel();

		addAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(getTabIndex()==0)
					selectedGroups.clear();
				else if(getTabIndex()==2)
					selectedNodes.clear();

				for (int i = 0; i < getList().getModel().getSize(); i++){
					((ListItem) getList().getModel().getElementAt(i)).setSelected(true);
					Object obj = ((ListItem) getList().getModel().getElementAt(i)).getListItem();
					if(obj instanceof ONGroup)
						selectedGroups.add((ONGroup)obj);
					else if(obj instanceof Time)
						selectedNodes.add((Time)obj);
					((ListItem) getList().getModel().getElementAt(i)).setItemColor(Color.ORANGE);
				}
				getList().repaint();
			}
		});


		removeAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < getList().getModel().getSize(); i++){
					((ListItem) getList().getModel().getElementAt(i)).setSelected(false);
					((ListItem) getList().getModel().getElementAt(i)).setItemColor(Color.BLACK);
				}
				getList().repaint();
				if(getTabIndex()==0)
					selectedGroups.clear();
				else if(getTabIndex()==2)
					selectedNodes.clear();
			}
		});
	}

	@Override
	protected void createSelectionPanel(){
		selectionPanel = new JPanel(new FlowLayout());
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Selection"));

		createSelectionButtonsPanel();
		createSelectionPane();

		selectionPanel.add(selectionTabbedPane);
		selectionPanel.add(selectionButtonPanel);
	}

	@Override
	protected void createSettingPanel(){
		settingPanel = new JPanel(new BorderLayout());
		JPanel leftColumn = new JPanel();
		leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

		settingPanel.setBorder(BorderFactory.createTitledBorder("Setting"));

		inconsistencyHighLight = new JCheckBox("Highlight time inconsistency nodes");
		inconsistencyHighLight.setFont(font);
		inconsistencyHighLight.setSelected(true);

		unspecifyHighlight = new JCheckBox("Highlight nodes with unspecified time values");
		unspecifyHighlight.setFont(font);
		unspecifyHighlight.setSelected(false);

		leftColumn.add(inconsistencyHighLight);
		leftColumn.add(unspecifyHighlight);

		settingPanel.add(leftColumn, BorderLayout.WEST);
	}

	@Override
	protected void createInterface(){
		createSelectionPanel();
		createSettingPanel();
		createButtonsPanel();
		createGranularityButtons();

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add(granularityPanel);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(selectionPanel);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(settingPanel);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(confirmButtonsPanel);

		this.add(content);
		this.setResizable(false);
		this.pack();
	}

	public TimeConsistencyDialog (Window owner, WorkspaceEntry we){
		super(owner, "Time Anayalsis Setting",  ModalityType.APPLICATION_MODAL, we);
	}

	protected void scenarioColorUpdate(){
		net.clearMarking();
		setGrayout(net.getNodes(), greyoutColor);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.addAll(selectedScenario.getNodes(net));
		nodes.addAll(selectedScenario.getConnections(net));
		setGrayout(nodes, Color.BLACK);
	}

	protected void setGrayout(Collection<? extends Node> nodes, Color color){
		for(Node node : nodes){
			net.setForegroundColor(node, color);
		}
	}

	@Override
	protected String groupPanelTitle(){
		return "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public JList<ListItem> getList(){
		if(getTabIndex()==0){
			return groupList;
		}else if(getTabIndex()==2)
			return nodeList;
		else{
			return null;
		}
	}

	public ArrayList<Node> getSelectedNodes(){
		return selectedNodes;
	}

	public ArrayList<ONGroup> getSelectedGroups(){
		return selectedGroups;
	}

	public ScenarioRef getSelectedScenario(){
		return selectedScenario;
	}

	public int getTabIndex(){
		return selectionTabbedPane.getSelectedIndex();
	}

	public Granularity getGranularity(){
		if(year_yearButton.isSelected())
			return Granularity.YEAR_YEAR;
		else if(hour_minusButton.isSelected())
			return Granularity.CLOCKHOUR_MINUS;
		return null;
	}

	public TimeConsistencySettings getTimeConsistencySettings(){
		return new TimeConsistencySettings(inconsistencyHighLight.isSelected(), unspecifyHighlight.isSelected(),
				getSelectedGroups(), getSelectedScenario(), getSelectedNodes(), getTabIndex(), getGranularity());
	}
}