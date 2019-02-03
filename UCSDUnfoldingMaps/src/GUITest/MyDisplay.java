package GUITest;

import processing.core.*;

public class MyDisplay extends PApplet {

	public void setup () {
		background(220,220,220);
		size(500,500);
	}
	
	public void draw () {
		fill(255,255,0);
		ellipse(width/2,height/2,width-20,height-20);
		fill(0,0,0);
		ellipse(width/4, height/3, width/6,height/6 );
		ellipse(width/4*3, height/3, width/6,height/6 );
		arc(width/2,height/3*2,width/3,height/3, 0,PI);
	}
}
