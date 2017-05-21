package entg.chart.datasets;


import java.util.Map; 
import org.jfree.chart.plot.CategoryPlot; 
import org.jfree.chart.axis.NumberAxis; 
import org.jfree.chart.JFreeChart; 
import de.laures.cewolf.ChartPostProcessor; 

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.axis.ValueAxis;

public class StackbarChartProcessor implements ChartPostProcessor { 
public void processChart(Object chart, Map params) { 
	
	CategoryPlot plot = (CategoryPlot) ((JFreeChart) chart).getPlot();
	
	ValueAxis v = plot.getRangeAxis();
	v.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	
	BarRenderer renderer = (BarRenderer) plot.getRenderer();
    GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.green, 
            0.0f, 0.0f, new Color(0, 64, 0));
    Paint gp1 = new Color(0, 0, 0, 0);
    GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red, 
            0.0f, 0.0f, new Color(64, 0, 0));
    renderer.setSeriesPaint(0, gp0);
    renderer.setSeriesPaint(1, gp2);
	
	 
}  
}