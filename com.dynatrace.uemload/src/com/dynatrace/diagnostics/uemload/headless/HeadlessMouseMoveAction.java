package com.dynatrace.diagnostics.uemload.headless;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.ActionExecutor;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ResourceFileReader;

import ch.qos.logback.classic.Logger;

public class HeadlessMouseMoveAction extends Action {
	private static final Logger LOGGER = LoggerFactory.make();
	private String fileName;
	private Point currentPoint = new Point(0,0);
	private By startEelment; 

	public HeadlessMouseMoveAction(String fileName) {
		this.fileName = fileName;
	}
	
	public HeadlessMouseMoveAction(String fileName, By startEelment) {
		this.fileName = fileName;
		this.startEelment = startEelment;
	}
		
	@Override
	public void run(ActionExecutor browser, UEMLoadCallback continuation) throws Exception {
		HeadlessActionExecutor exec = (HeadlessActionExecutor) browser;
		ChromeDriver driver = exec.getDriver();
		moveToStart(exec);
		List<Point> points = getPoints();
		setStartingPoint(points);
		for(int i=0; i< points.size(); i+=1) {
			moveToPointByOffset(points.get(i), driver);
		}	
	}
	
	@SuppressWarnings("unused")
	private void enableMouseCursor(ChromeDriver driver) {
		((JavascriptExecutor)driver).executeScript("EnableCursor();");
	}
	
	private void moveToStart(HeadlessActionExecutor exec) {
		if( startEelment != null ) {
			WebElement webElement = exec.getWait().until( ExpectedConditions.elementToBeClickable( startEelment ) );
			Actions actions = new Actions( exec.getDriver() );
			actions.moveToElement( webElement, 0, 0 );
			actions.build().perform();
			currentPoint = webElement.getLocation();
		}
	}
	
	private List<Point> getPoints() {
		List<Point> points = null;	
				
		try( Stream<String> lines = new BufferedReader(new InputStreamReader(ResourceFileReader.getInputStream(fileName))).lines()) {			
			points = lines.map( s -> getPoint(s)).collect(Collectors.toList());			
		} catch (Exception e) {
			LOGGER.error("Error reading file with mouse movements " + fileName, e);
			points = Collections.<Point>emptyList();
		}		
 
		return points;
	}
		
	private Point getPoint(String line) {
		String[] tab = line.split(",");
		return new Point(Integer.parseInt(tab[0]), Integer.parseInt(tab[1]));
	}
	
	private void setStartingPoint(List<Point> points) {
		if( !points.isEmpty() && currentPoint.getX() == 0 && currentPoint.getY() == 0 ) {
			currentPoint = points.get(0);
		}
	}
		
	private void moveToPointByOffset(Point point, ChromeDriver driver) {				
		int xOffset = point.x - currentPoint.x;
		int yOffset = point.y - currentPoint.y;
		Actions actions = new Actions(driver);  //TODO can take all actions and execute together?
		actions.moveByOffset(xOffset, yOffset);
		try {
			actions.build().perform();
		} catch (Exception e) {
			LOGGER.trace("error move " + currentPoint.x + " " + currentPoint.y + " moveto " + point.x + " " + point.y);
		}
		currentPoint = point;		
	}
}
