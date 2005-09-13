/**
 * 
 */
package org.mwc.cmap.core.property_support;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

abstract public class ValueWithUnitsCellEditor extends CellEditor
{
	/** hmm, the text bit.
	 * 
	 */
	Text _myText;
	
	/** and the drop-down units bit
	 * 
	 */
	Combo _myCombo;
	
	/** just have the one of each listener, so we can easily remove them when the value gets updated
	 * 
	 */
	private ModifyListener _modifyListener;
	private SelectionListener _selectionListener;
	
	final private String _textTip;
	final private String _comboTip;
	
	public ValueWithUnitsCellEditor(Composite parent, String textTip, String comboTip)
	{
		super(parent);
		_textTip = textTip;
		_comboTip = comboTip;
	}

	protected Control createControl(Composite parent)
	{
		return createControl(parent, _textTip, _comboTip);
	}

	private ModifyListener getModifyListener()
	{
		if(_modifyListener == null)
		{
			_modifyListener = new ModifyListener(){
				public void modifyText(ModifyEvent e)
				{
					System.out.println("new text:" + _myText.getText());
				}};
		}
		return _modifyListener;
	}
	
	private SelectionListener getSelectionListener()
	{
		if(_selectionListener == null)
		{
			_selectionListener = new SelectionListener(){
				public void widgetSelected(SelectionEvent e)
				{
					Combo combo = (Combo) e.getSource();
					System.out.println("new val:" + combo.getSelectionIndex());
				}
				public void widgetDefaultSelected(SelectionEvent e)
				{
				}
			};
		}
		return _selectionListener;
	}
	
	protected Control createControl(Composite parent, String tipOne, String tipTwo)
	{
		System.out.println("creating new control...");
		Composite holder = new Composite(parent, SWT.NONE);
		RowLayout rows = new RowLayout();
		rows.marginLeft = rows.marginRight = 0;
		rows.marginTop = rows.marginBottom	 = 0;
		rows.fill = false;
		rows.spacing = 0;
		rows.pack = false;
		holder.setLayout(rows);
		
		_myText = new Text(holder, SWT.BORDER);
		_myText.addModifyListener(getModifyListener());
		_myText.setTextLimit(7);
		_myCombo = new Combo(holder, SWT.DROP_DOWN);
		_myCombo.addSelectionListener(getSelectionListener());
		_myCombo.setItems(getTagsList());
		
		return holder;
	}

	/**
	 * 
	 */
	final private void doUpdate()
	{
		// get the best units
		final int units = getUnitsValue();
		final String txt = "" + getDoubleValue();
		_myCombo.select(units);
		_myText.setText(txt);
	}

	/**
	 * @return
	 */
	abstract protected int getUnitsValue();

	/**
	 * @return
	 */
	abstract protected double getDoubleValue();

	/**
	 * @return
	 */
	abstract protected String[] getTagsList();

	
	protected Object doGetValue()
	{
		String distTxt = _myText.getText();
		double dist = new Double(distTxt).doubleValue();
		int units = _myCombo.getSelectionIndex();
		Object res = createResultsObject(dist, units);
		return res;
	}

	/**
	 * @param dist the value typed in
	 * @param units the units for the value
	 * @return an object representing the new data value
	 */
	abstract protected Object createResultsObject(double dist, int units);

	protected void doSetFocus()
	{
	}

	protected void doSetValue(Object value)
	{
		storeMe(value);
		doUpdate();
	}
	
	abstract protected void storeMe(Object value);
	
}