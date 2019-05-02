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

public class Renderer 
{
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
	
	public static ArrayList<Polygon> polygons = new ArrayList<Polygon>();
	private static BufferedImage bufferedImage;
	private static String back = "back";
	private static String forward = "forward";
	private static JFrame f;
	
	public static void main(String[] args) throws InterruptedException
	{
		
		bufferedImage = new BufferedImage();
		polygons.add(buildShape(100, 100, 0, 250, 250, Color.green, forward));
		polygons.add(buildShape(500, 450, 15, 321, 120, Color.red, back));
		polygons.add(buildShape(650, 30, 0, 45, 560, Color.blue, forward));
		polygons.add(buildShape(162, 162, 15, 125, 125, Color.lightGray, back));
		polygons.add(buildShape(300, 300, 12, 300, 250, Color.magenta, back));
		polygons.add(buildShape(550, 200, 20, 78, 725, Color.darkGray, back));
		polygons.add(buildShape(25, 760, 4, 333, 200, Color.pink, back));
		polygons.add(buildShape(275, 830, 18, 600, 70, Color.cyan, forward));
		polygons.add(buildShape(690, 690, 2, 250, 250, Color.orange, forward));
		zBuffer(polygons);
		
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
		
		while(true)
		{
			Thread.sleep(200);
			Update();
			zBuffer(polygons);	
			if(f != null)
			{
				f.revalidate();
				f.repaint();
			}
		}
	}
	
	private static void Update()
	{
		for(Polygon poly : polygons)
		{
			poly.update();
		}
	}
	
	private static void createAndShow() throws InterruptedException 
	{
        System.out.println("Created on EDT? "+ SwingUtilities.isEventDispatchThread());
        f = new JFrame("Z-Buffer Render Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Component c = f.add(bufferedImage);
        f.pack();
        f.setVisible(true);
    }
	
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
	
	public static void zBuffer(ArrayList<Polygon> polygons)
	{
		bufferedImage.reset();
		for(Polygon poly : polygons)
		{
			for(int i = 1; i < poly.def[0].x; i++)
			{
				if(poly.def[i].z > bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].z)
				{
					bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].z = poly.def[i].z;
					bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].color = poly.def[i].color;
				}
	
			}
		}
	}
}

class Polygon
{
	Renderer.Pixel def[];
	private String direction;
	
	public Polygon(Renderer.Pixel[] def)
	{
		this.def = def;
	}
	
	public void update()
	{
		if(def[1].z >= 20)
			direction = "back";
		else if (def[1].z <= 0)
			direction = "forward";
		move();
	}
	
	public void setDirection(String dir)
	{
		direction = dir;
	}
	
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

class BufferedImage extends JPanel
{
	public Renderer.Pixel def[];
	
	public BufferedImage()
	{
		Renderer.Pixel temp[] = Renderer.buildRender(0, 0, -1, 1000, 1000, Color.white);
		this.def = temp;
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(1000,1000);
    }

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
    
    public void reset()
    {
    	def = Renderer.buildRender(0, 0, -1, 1000, 1000, Color.white);
    }
}
