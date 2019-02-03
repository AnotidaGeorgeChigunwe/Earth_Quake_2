package GUITest;

import processing.core.*;

public class GUIDisplay extends PApplet {
		
		private String  URL = "http://www.mountsinai.on.ca/about_us/news/news-feature/img/demopage/image-3.jpg/image";
		private PImage backgroundImg;
		
		public void setup () {
			size(200, 200);
			backgroundImg = loadImage(URL,"jpg");
		}
		
		public void draw () {
			backgroundImg.resize(width, 0);
			image(backgroundImg,0,0);
			int[] color = sunColorSec(second());			
			fill(color[0],color[1],0);
			ellipse(width/4, height/5, width/5, height/5);
		}
		
		public int [] sunColorSec (float seconds) {
			int [] rgb = new int[3];
			float diffFrom30 = Math.abs(30-seconds);
			
			float ratio = diffFrom30/30;
			rgb[0] = (int)(255*ratio);
			rgb[1] = (int) (255*ratio);
			rgb[2] = 0;
			return rgb;
		}
		
		
	}

