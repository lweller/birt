/*******************************************************************************
 * Copyright (c) 2010 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.chart.examples.radar.ui.series;

import java.math.BigInteger;
import java.text.ParseException;

import org.eclipse.birt.chart.examples.radar.i18n.Messages;
import org.eclipse.birt.chart.examples.radar.model.type.RadarSeries;
import org.eclipse.birt.chart.examples.radar.render.Radar;
import org.eclipse.birt.chart.examples.view.util.UIHelper;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.util.ChartDefaultValueUtil;
import org.eclipse.birt.chart.model.util.ChartElementUtil;
import org.eclipse.birt.chart.ui.swt.AbstractChartTextEditor;
import org.eclipse.birt.chart.ui.swt.ChartCheckbox;
import org.eclipse.birt.chart.ui.swt.composites.LineAttributesComposite;
import org.eclipse.birt.chart.ui.swt.wizard.ChartWizardContext;
import org.eclipse.birt.chart.ui.swt.wizard.format.popup.AbstractPopupSheet;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import com.ibm.icu.text.NumberFormat;

/**
 * 
 */

public class RadarLineSheet extends AbstractPopupSheet implements Listener
{

	private final RadarSeries series;
	private static final int MAX_STEPS = 20;
	private ChartCheckbox btnAutoScale = null;
	private Label lblWebMax = null;
	private Label lblWebMin = null;
	private AbstractChartTextEditor webMax = null;
	private AbstractChartTextEditor webMin = null;
	private LineAttributesComposite wliacLine = null;
	private Spinner iscScaleCnt = null;
	private ChartCheckbox btnTranslucentBullseye = null;
	private Button btnScaleCntAuto;
	private RadarSeries defSeries;

	public RadarLineSheet( String title, ChartWizardContext context,
			boolean needRefresh, RadarSeries series )
	{
		super( title, context, needRefresh );
		this.series = series;
		this.defSeries = (RadarSeries) ChartDefaultValueUtil.getDefaultSeries( series );;
	}

	@Override
	protected Composite getComponent( Composite parent )
	{
		Composite cmpContent = new Composite( parent, SWT.NONE );
		{
			GridLayout glMain = new GridLayout( );
			glMain.numColumns = 2;
			cmpContent.setLayout( glMain );
		}

		Group grpLine = new Group( cmpContent, SWT.NONE );
		GridLayout glLine = new GridLayout( 2, false );
		grpLine.setLayout( glLine );
		grpLine.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		grpLine.setText( Messages.getString( "RadarSeriesMarkerSheet.Label.Web" ) ); //$NON-NLS-1$
		
		int lineStyles = LineAttributesComposite.ENABLE_COLOR
				| LineAttributesComposite.ENABLE_STYLES
				| LineAttributesComposite.ENABLE_VISIBILITY
				| LineAttributesComposite.ENABLE_WIDTH;
		lineStyles |= getContext( ).getUIFactory( ).supportAutoUI( ) ? LineAttributesComposite.ENABLE_AUTO_COLOR
				: lineStyles;
		wliacLine = new LineAttributesComposite( grpLine,
				SWT.NONE,
				lineStyles,
				getContext( ),
				series.getWebLineAttributes( ),
				defSeries.getWebLineAttributes( ) );
		GridData wgdLIACLine = new GridData( GridData.FILL_HORIZONTAL );
		wgdLIACLine.horizontalSpan = 2;
		wgdLIACLine.widthHint = 200;
		wliacLine.setLayoutData( wgdLIACLine );
		wliacLine.addListener( this );

		GridLayout glRangeValue = new GridLayout( );
		glRangeValue.numColumns = 3;
		glRangeValue.horizontalSpacing = 2;
		glRangeValue.verticalSpacing = 5;
		glRangeValue.marginHeight = 0;
		glRangeValue.marginWidth = 0;

		Composite cmpMinMax = new Composite( grpLine, SWT.NONE );
		GridData gdMinMax = new GridData( GridData.FILL_HORIZONTAL );
		cmpMinMax.setLayoutData( gdMinMax );
		cmpMinMax.setLayout( glRangeValue );
		
		btnAutoScale = getContext( ).getUIFactory( )
				.createChartCheckbox( cmpMinMax,
						SWT.NONE,
						defSeries.isRadarAutoScale( ) );
		{
			btnAutoScale.setText( Messages.getString( "Radar.Composite.Label.ScaleAuto" ) ); //$NON-NLS-1$
			GridData gd = new GridData();
			gd.horizontalSpan = 3;
			btnAutoScale.setLayoutData( gd );
			btnAutoScale.setToolTipText( Messages.getString( "Radar.Composite.Label.ScaleAutoTooltip" ) ); //$NON-NLS-1$
			btnAutoScale.setSelectionState( series.isSetRadarAutoScale( ) ? ( series.isRadarAutoScale( ) ? ChartCheckbox.STATE_SELECTED
					: ChartCheckbox.STATE_UNSELECTED )
					: ChartCheckbox.STATE_GRAYED );
			btnAutoScale.addListener( SWT.Selection, this );
		}

		lblWebMin = new Label( cmpMinMax, SWT.NONE );
		{
			lblWebMin.setText( Messages.getString( "Radar.Composite.Label.ScaleMin" ) ); //$NON-NLS-1$
			lblWebMin.setToolTipText( Messages.getString( "Radar.Composite.Label.ScaleMinToolTip" ) ); //$NON-NLS-1$
		}

		webMin = getContext( ).getUIFactory( )
				.createChartTextEditor( cmpMinMax,
						SWT.BORDER | SWT.SINGLE,
						series,
						"webLabelMin" ); //$NON-NLS-1$
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			gd.horizontalSpan = 2;
			webMin.setLayoutData( gd );
			if ( series.getWebLabelMin( ) != Double.NaN )
			{
				webMin.setText( Double.toString( series.getWebLabelMin( ) ) );
			}
			webMin.setToolTipText( Messages.getString( "Radar.Composite.Label.ScaleMinToolTip" ) ); //$NON-NLS-1$
			webMin.addListener( this );
		}
		webMin.setEnabled( getContext().getUIFactory( ).canEnableUI( btnAutoScale ) );
		
		lblWebMax = new Label( cmpMinMax, SWT.NONE );
		{
			lblWebMax.setText( Messages.getString( "Radar.Composite.Label.ScaleMax" ) ); //$NON-NLS-1$
			lblWebMax.setToolTipText( Messages.getString( "Radar.Composite.Label.ScaleMaxToolTip" ) ); //$NON-NLS-1$
		}

		webMax = getContext( ).getUIFactory( )
				.createChartTextEditor( cmpMinMax,
						SWT.BORDER | SWT.SINGLE,
						series,
						"webLabelMax" );//$NON-NLS-1$
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			gd.horizontalSpan = 2;
			webMax.setLayoutData( gd );
			if ( series.getWebLabelMax( ) != Double.NaN )
			{
				webMax.setText( Double.toString( series.getWebLabelMax( ) ) );
			}
			webMax.setToolTipText( Messages.getString( "Radar.Composite.Label.ScaleMaxToolTip" ) ); //$NON-NLS-1$
			webMax.addListener( this );
		}
		
		webMax.setEnabled( getContext().getUIFactory( ).canEnableUI( btnAutoScale ) );
		
		boolean enabled = getContext().getUIFactory( ).canEnableUI( btnAutoScale );
		updateScaleUI( enabled );

		Label lblWebStep = new Label( cmpMinMax, SWT.NONE );
		{
			lblWebStep.setText( Messages.getString( "Radar.Composite.Label.ScaleCount" ) ); //$NON-NLS-1$
			lblWebStep.setToolTipText( Messages.getString( "Radar.Composite.Label.ScaleCountToolTip" ) ); //$NON-NLS-1$
		}

		iscScaleCnt = new Spinner( cmpMinMax, SWT.BORDER );
		GridData gdISCLeaderLength = new GridData( );
		gdISCLeaderLength.widthHint = 100;
		iscScaleCnt.setLayoutData( gdISCLeaderLength );
		iscScaleCnt.setMinimum( 1 );
		iscScaleCnt.setMaximum( MAX_STEPS );
		iscScaleCnt.setSelection( series.getPlotSteps( ).intValue( ) );
		iscScaleCnt.addListener( SWT.Selection, this );

		btnScaleCntAuto = new Button( cmpMinMax, SWT.CHECK );
		btnScaleCntAuto.setText( UIHelper.getAutoMessage( ) );
		btnScaleCntAuto.setSelection( !series.isSetPlotSteps( ) );
		iscScaleCnt.setEnabled( getContext().getUIFactory( ).canEnableUI( btnAutoScale )
				&& !btnScaleCntAuto.getSelection( ) );
		btnScaleCntAuto.addListener( SWT.Selection, this );
		
		if ( getChart( ).getSubType( )
					.equals( Radar.BULLSEYE_SUBTYPE_LITERAL ) )
		{
			btnTranslucentBullseye = getContext( ).getUIFactory( )
					.createChartCheckbox( cmpMinMax,
							SWT.NONE,
							defSeries.isBackgroundOvalTransparent( ) );
			btnTranslucentBullseye.setText(  Messages.getString( "Radar.Composite.Label.bullsEye" ) ); //$NON-NLS-1$
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			gd.horizontalSpan = 3;
			gd.verticalAlignment = SWT.TOP;
			btnTranslucentBullseye.setLayoutData( gd );
			btnTranslucentBullseye.setSelectionState( series.isSetBackgroundOvalTransparent( ) ? ( series.isBackgroundOvalTransparent( ) ? ChartCheckbox.STATE_SELECTED
					: ChartCheckbox.STATE_UNSELECTED )
					: ChartCheckbox.STATE_GRAYED );
			btnTranslucentBullseye.addListener( SWT.Selection, this );
		}
		return cmpContent;
	}

	public void handleEvent( Event event )
	{
		if ( event.widget.equals( wliacLine ) )
		{
			boolean isUnset = ( event.detail == ChartElementUtil.PROPERTY_UNSET );
			if ( event.type == LineAttributesComposite.VISIBILITY_CHANGED_EVENT )
			{
				ChartElementUtil.setEObjectAttribute( series.getWebLineAttributes( ),
						"visible",//$NON-NLS-1$
						( (Boolean) event.data ).booleanValue( ),
						isUnset );
				// enableLineSettings( series.getWebLineAttributes( ).isVisible(
				// ) );
			}
			else if ( event.type == LineAttributesComposite.STYLE_CHANGED_EVENT )
			{
				ChartElementUtil.setEObjectAttribute( series.getWebLineAttributes( ),
						"style",//$NON-NLS-1$
						event.data,
						isUnset );
			}
			else if ( event.type == LineAttributesComposite.WIDTH_CHANGED_EVENT )
			{
				ChartElementUtil.setEObjectAttribute( series.getWebLineAttributes( ),
						"thickness",//$NON-NLS-1$
						( (Integer) event.data ).intValue( ),
						isUnset );
			}
			else if ( event.type == LineAttributesComposite.COLOR_CHANGED_EVENT )
			{
				series.getWebLineAttributes( )
						.setColor( (ColorDefinition) event.data );
			}
		}
		else if ( event.widget.equals( webMin ) )
		{
			double tmin = this.getTypedDataElement( webMin.getText( ) );
			double tmax = this.getTypedDataElement( webMax.getText( ) );
			if ( tmin > tmax )
				tmin = tmax;
			series.setWebLabelMin( tmin );
			webMin.setText( Double.toString( tmin ) );
		}
		else if ( event.widget.equals( webMax ) )
		{

			double tmin = this.getTypedDataElement( webMin.getText( ) );
			double tmax = this.getTypedDataElement( webMax.getText( ) );
			if ( tmax < tmin )
				tmax = tmin;
			series.setWebLabelMax( tmax );
			webMax.setText( Double.toString( tmax ) );

		}
		else if ( event.widget.equals( btnTranslucentBullseye ) )
		{
			ChartElementUtil.setEObjectAttribute( series,
					"backgroundOvalTransparent",//$NON-NLS-1$
					btnTranslucentBullseye.getSelectionState( ) == ChartCheckbox.STATE_SELECTED,
					btnTranslucentBullseye.getSelectionState( ) == ChartCheckbox.STATE_GRAYED );
		}
		else if ( event.widget.equals( iscScaleCnt ) )
		{
			series.setPlotSteps( BigInteger.valueOf( iscScaleCnt.getSelection( ) ) );
		}
		else if ( event.widget.equals( btnAutoScale ) )
		{
			ChartElementUtil.setEObjectAttribute( series,
					"radarAutoScale",//$NON-NLS-1$
					btnAutoScale.getSelectionState( ) == ChartCheckbox.STATE_SELECTED,
					btnAutoScale.getSelectionState( ) == ChartCheckbox.STATE_GRAYED );
			
			boolean enabled = getContext().getUIFactory( ).canEnableUI( btnAutoScale );
			updateScaleUI( enabled );
		}
		else if ( event.widget == btnScaleCntAuto )
		{
			ChartElementUtil.setEObjectAttribute( series,
					"plotSteps",//$NON-NLS-1$
					BigInteger.valueOf( iscScaleCnt.getSelection( ) ),
					btnScaleCntAuto.getSelection( ) );
			iscScaleCnt.setEnabled( getContext().getUIFactory( ).canEnableUI( btnAutoScale )
					&& !btnScaleCntAuto.getSelection( ) );
		}
	}

	protected void updateScaleUI( boolean enabled )
	{
		lblWebMin.setEnabled( enabled );
		lblWebMax.setEnabled( enabled );
		webMin.setEnabled( enabled );
		webMax.setEnabled( enabled );
	}

	private double getTypedDataElement( String strDataElement )
	{
		if ( strDataElement.trim( ).length( ) == 0 )
		{
			return 0.0;
		}
		NumberFormat nf = ChartUIUtil.getDefaultNumberFormatInstance( );

		try
		{
			Number numberElement = nf.parse( strDataElement );
			return numberElement.doubleValue( );
		}
		catch ( ParseException e1 )
		{
			return 0.0;
		}
	}
}
