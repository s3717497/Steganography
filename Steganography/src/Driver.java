import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import view.StegoFrame;

public class Driver{


	private BufferedImage coverObject;
	private BufferedImage stegoObject;
	private String message;
	private int[][] pixelPos;
	
	public static void main(String[] args) throws IOException, InterruptedException
	{	
		(new Driver()).run();
		
	}
	
	private void run() throws IOException, InterruptedException
	{
		pixelPos = getPixelPos("A//pixelPositionsToAlter");
		message  = createMessage();
		
		coverObject = ImageIO.read(new File("A//mona_lisa.jpg"));
		stegoObject = encodeMessage();
		StegoFrame frame = new StegoFrame(stegoObject);
		ImageIO.write(stegoObject, "jpg", new File("C:\\Users\\Reuben\\Pictures\\stego_mona_lisa.jpg"));

		System.out.println("The extracted message from the stegoObject is: " +extractMessage());
	}
	
	
	
	
	// alters the coverObject with the secret message
	// to create a new stegoObject
	private BufferedImage encodeMessage()
	{
		//stackoverflow get color of each pixel of image 
		stegoObject = coverObject;
		
		for (int[] p : pixelPos)
		{
			// gets the pixel color from co-ordinates    x , y
			Color pixel = new Color(coverObject.getRGB(p[0], p[1]));
			
			int bitToRecord = Integer.parseInt(message.substring(0,1)); // the bit to be recorded is always the first bit
			message = message.substring(1); 							// removes first bit [since already recorded]
			
			// Set RED's LSB by clearing LSB and adding bit
			int stegoRED = (pixel.getRed()&~1) + bitToRecord;
			Color stegoColor = new Color(stegoRED, pixel.getBlue(), pixel.getGreen());
			
			//Create stegoImage
			//set new pixel color
			stegoObject.setRGB(p[0], p[1], stegoColor.getRGB());
			
//			produces larger blotched pixels
//			uncomment if you want to see if stegoImage actually is altered		
			for (int i=-10; i<10; i++)
			for (int j=-10; j<10; j++)
			{
				try 
				{
					int x = p[0] + i;
					int y = p[1] + j;
					stegoObject.setRGB(x, y, Color.BLUE.getRGB());
				}
				catch (ArrayIndexOutOfBoundsException e) {}
				
			}
		}
		
		return stegoObject;
	}

	
	
	
	
	
	
	
	
	
	
	private String extractMessage() throws InterruptedException
	{
		int[][] key = pixelPos;
		
		//compare coverObject w/ stegoObject
		
		// as hidden bits are discovered, they are concatenated
		// to build up the binary message
		String messageBuilder = "";
		for (int[] p : key)
		{
			Color stegoPixel = new Color(stegoObject.getRGB(p[0],p[1]));
			
			// Check the LSB of RED value for hidden bit
			String red = toBinary(stegoPixel.getRed());
			String lsb = red.substring(red.length()-1);
			
			// display the red binary that contains the hidden bit in LSB
			System.out.printf("Binary at pixel position %d, %d: %s \n", p[0], p[1], red);
			messageBuilder += lsb;
		}
		TimeUnit.SECONDS.sleep(2);
		return messageBuilder;
			
	}
	
	
	
	
	
	
	
	
	// Checks the message, To prevent incorrect hiding
	// if the pixel positions are not equal to no. of message bits
	private String createMessage()
	{
		// create message
		System.out.print("Please enter a decimal number (Student number): ");
		Scanner sc = new Scanner(System.in);
		int studentNo = sc.nextInt();
		message = toBinary(studentNo);
		System.out.println("The message: " + message);
		
		//check message
		String extra0s = "";
		for (int i=0; i<pixelPos.length - message.length(); i++)
			extra0s+="0";
		
		// if there are insufficient positions
		// to hide message bits
		if (pixelPos.length - message.length() <0)
		{
			System.out.println("Error : Insufficient pixel positions");
			System.exit(0);
		}
		return extra0s+message;
	}
	
	
	
	
	// array storing pixel position coordinates
	// to hide message bits
	// Reads the pixel positions from a file
	private int[][] getPixelPos(String fileLocation) throws FileNotFoundException
	{
		Scanner sc = new Scanner(new File(fileLocation));
		int[][] pixelPos = new int[100][2];
		
		
		for (int[] p : pixelPos)
		{
			String[] coordinates = sc.nextLine().split("[^0-9]+");
			
			p[0] = Integer.parseInt(coordinates[1]); //x-coordinate
			p[1] = Integer.parseInt(coordinates[2]); //y-coordinate
			if (!sc.hasNext())
			{
				sc.close();
				break;
			}
			
		}
		// since pixelPos is instantiated
		// with arbitrary row length
		return removeNullElements(pixelPos);
	}
	
	
	
//	facilitator methods
	
	
	private static int[][] removeNullElements(int[][] array)
	{
		int nullIndex = 0;
		while (array[nullIndex][0] > 0)
			nullIndex++;
		
		return Arrays.copyOf(array, nullIndex);
	}

	private static String toBinary(int n)
	{
		return Integer.toBinaryString(n);
	}
}

