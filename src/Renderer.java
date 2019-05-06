import javax.swing.SwingUtilities;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A demo graphics interface to show off the z-buffer algorithm
 * @author andrew
 *
 */
public class Renderer 
{	
	/**
	 * Creates the Pixel class that will be used to build polygons
	 * @author andrew
	 *
	 */
	public static class Pixel
	{
		Color color;
		int x, y, z;
		
		Pixel(int x, int y, int z, Color color)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.color = color;
		}
	}
	
	//global variables and constants;
	public static ArrayList<Polygon> polygons = new ArrayList<Polygon>();
	private static BufferedImage bufferedImage;
	private static JFrame f;
	private static final String back = "back";
	private static final String forward = "forward";
	
	//constants for random generation and frame rate
	public static final Boolean presetPolygons = false;
	public static final int amountOfPolygons = 50;
	public static final int approxFPS = 30;
	
	/**
	 * Runner
	 * Builds the polygons, starts the swing render, and begins the main update loop
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException
	{
		//creates the initial empty buffered image
		bufferedImage = new BufferedImage();
		
		if (presetPolygons)
		{
			//builds and adds the polygons to the array list
			polygons.add(buildShape(100, 100, 0, 250, 250, Color.green, forward));
			polygons.add(buildShape(500, 450, 15, 321, 120, Color.red, back));
			polygons.add(buildShape(650, 30, 0, 45, 560, Color.blue, forward));
			polygons.add(buildShape(162, 162, 15, 125, 125, Color.lightGray, back));
			polygons.add(buildShape(300, 300, 12, 300, 250, Color.magenta, back));
			polygons.add(buildShape(550, 200, 20, 78, 725, Color.darkGray, back));
			polygons.add(buildShape(25, 760, 4, 333, 200, Color.pink, back));
			polygons.add(buildShape(275, 830, 18, 600, 70, Color.cyan, forward));
			polygons.add(buildShape(690, 690, 2, 250, 250, Color.orange, forward));
		}
		else
		{
			generateRandomPolygons(amountOfPolygons);
		}
		
		//calls the zBuffer algorithm to build the starting frame
		zBuffer(polygons);
		
		//starts the swing render
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					createAndShow();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            }
        });
		long endTime, curTime;
		endTime = System.currentTimeMillis() + (1000/approxFPS);
		//main running loop
		while(true)
		{
			curTime = System.currentTimeMillis();
			
			//allows for (roughly) a frame rate of [approxFPS]
			while(curTime - endTime < (1000/approxFPS))
			{
				curTime = System.currentTimeMillis();
			}
			
			//moves the polygons and rebuilds the frame using the zBuffer
			Update();
			zBuffer(polygons);	
			
			//repaints the image
			if(f != null)
			{
				f.revalidate();
				f.repaint();
			}
			endTime = System.currentTimeMillis();
			System.out.println("Render Time: " + (endTime - curTime));
		}
	}
	
	/**
	 * populates the polygons array with random polygon definitions
	 * @param polyCount
	 */
	private static void generateRandomPolygons(int polyCount) 
	{
		int x, y, z, length, height, directionRandInt;
		String direction;
		Color color;
		
		for(int i = 0; i < polyCount; i++)
		{
			//Generates random values for the polygon
			x = (int) (Math.random() * 900) + 25;
			y = (int) (Math.random() * 900) + 25;
			z = (int) (Math.random() * 21) + 1;
			length = (int) (Math.random() * 280) + 21;
			if(x+length >= 975)
				length = 975 - x;
			height = (int) (Math.random() * 280) + 21;
			if(y+height >= 975)
				height = 975 - y;
			color = new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256));
			directionRandInt = (int) Math.random() * 2;
			
			if(directionRandInt == 0)
				direction = back;
			else
				direction = forward;
			
			//builds and adds the polygon to the array
			polygons.add(buildShape(x, y, z, length, height, color, direction));
		}
		
	}

	/**
	 * allow a single call to this update method to call the 
	 * update method for every polygon
	 */
	private static void Update()
	{
		for(Polygon poly : polygons)
		{
			poly.update();
		}
	}
	
	/**
	 * Swing method to start GUI
	 * @throws InterruptedException
	 */
	private static void createAndShow() throws InterruptedException 
	{
        System.out.println("Created on EDT? "+ SwingUtilities.isEventDispatchThread());
        f = new JFrame("Z-Buffer Render Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Component c = f.add(bufferedImage);
        f.pack();
        f.setVisible(true);
    }
	
	/**
	 * Given size, color, and movement direction parameters, constructs and 
	 * returns a polygon with the supplied characteristics. 
	 * The first pixel in every polygon is a flag with x, y, and z set to 
	 * the amount of pixels in the array
	 * @param x
	 * @param y
	 * @param z
	 * @param length
	 * @param height
	 * @param color
	 * @param direction
	 * @return
	 */
	public static Polygon buildShape(int x, int y, int z, int length, int height, Color color, String direction)
    {
    	Pixel rect[] = new Pixel[(length*height) + 1];
    	rect[0] = new Pixel((length*height) + 1, (length*height) + 1, (length*height) + 1, Color.black);
    	int index = 1;
    	
    	for(int i = x; i < x + length; i++)
    	{
    		for(int j = y; j < y + height; j++)
    		{
    			rect[index] = new Pixel(i, j, z, color);
    			index++;
    		}
    	}
    	
    	Polygon poly = new Polygon(rect);
    	poly.setDirection(direction);
    	return poly;
    }
	
	/**
	 * modified from buildShape method
	 * returns only the pixel array that would define the shape without
	 * bundling it as a polygon. Used only to create and reset
	 * the buffered image
	 * @param x
	 * @param y
	 * @param z
	 * @param length
	 * @param height
	 * @param color
	 * @return
	 */
	public static Pixel[] buildRender(int x, int y, int z, int length, int height, Color color)
    {
    	Pixel rect[] = new Pixel[(length*height) + 1];
    	rect[0] = new Pixel((length*height) + 1, (length*height) + 1, (length*height) + 1, Color.black);
    	int index = 1;
    	
    	for(int i = x; i < x + length; i++)
    	{
    		for(int j = y; j < y + height; j++)
    		{
    			rect[index] = new Pixel(i, j, z, color);
    			index++;
    		}
    	}
    	
    	return rect;
    }
	
	/**
	 * When given an array list of polygons, with update the buffered image
	 * with the correct frame built from those polygons
	 * @param polygons
	 */
	public static void zBuffer(ArrayList<Polygon> polygons)
	{
		//resets the buffered image to default state
		bufferedImage.reset();
		
		//gets the start time to check elapsed time
		long startTime = System.nanoTime();
		
		//iterates through every polygon in the list
		for(Polygon poly : polygons)
		{
			//iterates through every polygon, using the first pixel to define array length
			for(int i = 1; i < poly.def[0].x; i++)
			{
				//checks if the pixel in the polygon is at a greater z than the pixel in the same
				//x,y position of the buffered image
				if(poly.def[i].z > bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].z)
				{
					//sets the buffered image pixel's z and color to the current polygon's values
					bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].z = poly.def[i].z;
					bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].color = poly.def[i].color;
				}
	
			}
		}
		
		//gets end time to check elapsed time
		long endTime = System.nanoTime();
		
		//Prints out the time for the algorithm to operate
		System.out.println("Elapsed Time (milli-Seconds): " + ((endTime - startTime)/1000000.0));
	}
}

/**
 * defines the polygon object that is used to hold a shapes pixel definition 
 * and its direction of travel
 * @author andrew
 *
 */
class Polygon
{
	Renderer.Pixel def[];
	private String direction;
	
	public Polygon(Renderer.Pixel[] def)
	{
		this.def = def;
	}
	
	/**
	 * Sets the direction of travel and calls the move method
	 */
	public void update()
	{
		if(def[1].z >= 20)
			direction = "back";
		else if (def[1].z <= 0)
			direction = "forward";
		move();
	}
	
	/**
	 * allows the direction to be set outside this class
	 * @param dir
	 */
	public void setDirection(String dir)
	{
		direction = dir;
	}
	
	/**
	 * Updates the x, y, and z based on the direction of movement
	 */
	private void move()
	{
		if (direction.equalsIgnoreCase("forward"))
		{
			for(int i = 1; i < def.length; i++)
			{
				def[i].z++;
				def[i].x++;
				def[i].y++;
			}
		}
		else if (direction.equalsIgnoreCase("back"))
		{
			for(int i = 1; i < def.length; i++)
			{
				def[i].z--;
				def[i].x--;
				def[i].y--;
			}
		}
	}
}

/**
 * Defines the class that will be used as an object to hold the final buffered image
 * created by the zBuffer algorithm
 * @author andrew
 *
 */
class BufferedImage extends JPanel
{
	public Renderer.Pixel def[];
	
	/**
	 * creates an empty definition for the buffered image
	 */
	public BufferedImage()
	{
		Renderer.Pixel temp[] = Renderer.buildRender(0, 0, -1, 1000, 1000, Color.white);
		this.def = temp;
	}
	
	/**
	 * Sets the size of the image to the same as the JFrame (1000 by 1000 pixels)
	 */
	public Dimension getPreferredSize() {
        return new Dimension(1000,1000);
    }

	/**
	 * Defines how the component will be drawn
	 */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
   
        for(int i = 1; i < def[0].x; i++)
        {
        	if(def[i].color != Color.white)
        	{
        		g.setColor(def[i].color);
        		g.fillRect(def[i].x, def[i].y, 1, 1);
        	}
        }
    } 
    
    /**
     * Resets the buffered image to a default empty state
     */
    public void reset()
    {
    	def = Renderer.buildRender(0, 0, -1, 1000, 1000, Color.white);
    }
}
