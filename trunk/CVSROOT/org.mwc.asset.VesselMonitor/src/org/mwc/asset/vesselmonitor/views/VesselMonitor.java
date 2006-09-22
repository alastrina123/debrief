package org.mwc.asset.vesselmonitor.views;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
import org.mwc.cmap.core.CorePlugin;
import org.mwc.cmap.core.property_support.EditableWrapper;
import org.mwc.cmap.core.ui_support.PartMonitor;

import ASSET.ParticipantType;
import ASSET.GUI.Workbench.Plotters.ScenarioParticipantWrapper;
import ASSET.Participants.*;
import MWC.GUI.Editable;

public class VesselMonitor extends ViewPart
{

	StatusIndicator _myIndicator;

	private PartMonitor _myPartMonitor;

	/**
	 * we listen out for participants being selected
	 */
	private ISelectionChangedListener _selectionChangeListener;

	private Action _trackParticipant;

	private ParticipantType _myPart;

	private ParticipantMovedListener _moveListener;

	private ParticipantDecidedListener _decisionListener;

	/**
	 * The constructor.
	 */
	public VesselMonitor()
	{
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent)
	{

		Composite myLayout = new Composite(parent, SWT.NONE);
		_myIndicator = new StatusIndicator(myLayout, SWT.NONE);

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		listenToMyParts();
	}

	private void hookContextMenu()
	{

	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{

	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(_trackParticipant);
	}

	private void makeActions()
	{
		_trackParticipant = new Action("Track", SWT.TOGGLE)
		{
		};
		_trackParticipant.setText("Sync");
		_trackParticipant.setChecked(true);
		_trackParticipant.setToolTipText("Follow selected participant");
		_trackParticipant.setImageDescriptor(CorePlugin.getImageDescriptor("icons/synced.gif"));

	}

	private void hookDoubleClickAction()
	{
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
	}

	private void listenToMyParts()
	{
		_selectionChangeListener = new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				newItemSelected(event);
			}
		};

		// try to add ourselves to listen out for page changes
		// getSite().getWorkbenchWindow().getPartService().addPartListener(this);

		_myPartMonitor = new PartMonitor(getSite().getWorkbenchWindow().getPartService());
		_myPartMonitor.addPartListener(ISelectionProvider.class, PartMonitor.ACTIVATED,
				new PartMonitor.ICallback()
				{
					public void eventTriggered(String type, Object part, IWorkbenchPart parentPart)
					{
						ISelectionProvider iS = (ISelectionProvider) part;
						iS.addSelectionChangedListener(_selectionChangeListener);
					}
				});
		_myPartMonitor.addPartListener(ISelectionProvider.class, PartMonitor.DEACTIVATED,
				new PartMonitor.ICallback()
				{
					public void eventTriggered(String type, Object part, IWorkbenchPart parentPart)
					{
						ISelectionProvider iS = (ISelectionProvider) part;
						iS.removeSelectionChangedListener(_selectionChangeListener);
					}
				});

		// ok we're all ready now. just try and see if the current part is valid
		_myPartMonitor.fireActivePart(getSite().getWorkbenchWindow().getActivePage());
	}

	protected void newItemSelected(SelectionChangedEvent event)
	{

		if (_trackParticipant.isChecked())
		{
			// right, let's have a look at it.
			ISelection theSelection = event.getSelection();

			// get the first element
			if (theSelection instanceof StructuredSelection)
			{
				StructuredSelection sel = (StructuredSelection) theSelection;
				Object first = sel.getFirstElement();
				// hmm, is it adaptable?
				if (first instanceof EditableWrapper)
				{
					EditableWrapper ew = (EditableWrapper) first;
					Editable ed = ew.getEditable();
					if (ed instanceof ScenarioParticipantWrapper)
					{
						ScenarioParticipantWrapper sw = (ScenarioParticipantWrapper) ed;

						updateParticipant(sw.getParticipant());
					}
				}
			}
		}
	}

	/**
	 * right, a new participant has been selected
	 * 
	 * @param part
	 *          the new participant
	 */
	private void updateParticipant(ParticipantType part)
	{
		// is this already our participant
		if (_myPart != part)
		{
			updateName(part);

			// do we have our listener?
			if (_moveListener == null)
			{
				createListeners();
			}

			// ok, stop listening to the old one
			if (_myPart != null)
			{
				_myPart.removeParticipantMovedListener(_moveListener);
				_myPart.removeParticipantDecidedListener(_decisionListener);
			}

			_myPart = part;

			// ok, start listening to him
			_myPart.addParticipantMovedListener(_moveListener);
			_myPart.addParticipantDecidedListener(_decisionListener);

			// let's fire one off to get us started
			updateStatus(_myPart.getStatus());
			
			// and sort out what it's dowing
			updateDecision(_myPart.getActivity(), _myPart.getDemandedStatus());
		}

	}

	/**
	 * @param part
	 */
	private void updateName(final ParticipantType part)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				if (!_myIndicator.isDisposed())
				{
					_myIndicator.setName(part.getName());
				}
			}
		});
	}

	/**
	 * 
	 */
	private void createListeners()
	{
		_moveListener = new ParticipantMovedListener()
		{
			public void moved(Status newStatus)
			{
				updateStatus(newStatus);
			}

			public void restart()
			{
			}
		};

		_decisionListener = new ParticipantDecidedListener()
		{
			public void newDecision(String description, DemandedStatus dem_status)
			{
				updateDecision(description, dem_status);
			}

			public void restart()
			{
			}
		};
	}

	protected void updateDecision(final String description, final DemandedStatus dem_status)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				if (!_myIndicator.isDisposed())
				{
					_myIndicator.setDecision(description, dem_status);
				}
			}
		});
	}

	protected void updateStatus(final Status newStatus)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				if (!_myIndicator.isDisposed())
				{
					_myIndicator.setStatus(newStatus);
				}
			}
		});
	}
}