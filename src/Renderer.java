import javax.swing.SwingUtilities;

import sun.security.provider.certpath.BuildStep;

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
	
	public static void main(String[] args)
	{
		
		bufferedImage = new BufferedImage();
		polygons.add(new Polygon(buildShape(100, 100, 1, 100, 100, Color.green)));
		polygons.add(new Polygon(buildShape(500, 450, 1, 321, 120, Color.red)));
		polygons.add(new Polygon(buildShape(650, 30, 2, 45, 560, Color.blue)));
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
	}
	
	private void Update()
	{
		for(Polygon poly : polygons)
		{
			poly.update();
		}
	}
	
	private static void createAndShow() throws InterruptedException 
	{
        System.out.println("Created on EDT? "+ SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Z-Buffer Render Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Component c = f.add(bufferedImage);
        f.pack();
        f.setVisible(true);
    }
	
	public static Pixel[] buildShape(int x, int y, int z, int length, int height, Color color)
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
		for(Polygon poly : polygons)
		{
			for(int i = 1; i < poly.def[0].x; i++)
			{
//				System.out.println("Poly Z: "  +poly.def[i].z+ "Buffer z: " + bufferedImage.def[((poly.def[i].x-1) *1000) + poly.def[i].y].z );
//				System.out.println("Poly index: " +i + "Buffer Index: " + (((poly.def[i].x-1) *1000) + poly.def[i].y));
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
	
	public Polygon(Renderer.Pixel[] def)
	{
		this.def = def;
	}
	
	public void update()
	{
		//TODO add movement here
	}
}

class BufferedImage extends JPanel
{
	public Renderer.Pixel def[];
	
	public BufferedImage()
	{
		Renderer.Pixel temp[] = Renderer.buildShape(0, 0, -1, 1000, 1000, Color.white);
		this.def = temp;
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(1000,1000);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
   
        for(int i = 1; i < def[0].x; i++)
        {
        	g.setColor(def[i].color);
        	g.fillRect(def[i].x, def[i].y, 1, 1);
        }
    } 
}
