
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

import javax.swing.*;


public class RenderVideo {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;

	public void showIms(String[] args){

		int width = Integer.parseInt(args[1]);
		int height = Integer.parseInt(args[2]);
		int rps = Integer.parseInt(args[3]);
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			long len = file.length();
			byte[] bytes = new byte[(int)len];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			is.close();
			bytes = Segmentation.padHeight(bytes, 4);
			//Calculate the pixel array and delay
			int delay = (int)((1000/rps));

			//Render video
			frame = new JFrame();
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);
			String result = String.format("Video height: %d, width: %d", height, width);

			while(true){//To play the video in loop
				long t = System.currentTimeMillis();
				int ind = 0;
				offset = bytes.length;
				while(ind < offset ){
					for(int y = 0; y < height; y++){					
						for(int x = 0; x < width; x++){						
							byte a = 0;						
							byte r = bytes[ind];
							byte g = bytes[ind+height*width];
							byte b = bytes[ind+height*width*2]; 						

							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
							img.setRGB(x,y,pix);					
							ind++;
						}	
					}

					ind += 2 * height * width;
					TimeUnit.MILLISECONDS.sleep(delay);	

					// Use labels to display the images			
					JLabel lbText1 = new JLabel(result);
					lbText1.setHorizontalAlignment(SwingConstants.CENTER);

					lbIm1 = new JLabel(new ImageIcon(img));

					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.CENTER;
					c.weightx = 0.5;
					c.gridx = 0;
					c.gridy = 0;
					frame.getContentPane().add(lbText1, c);


					c.fill = GridBagConstraints.HORIZONTAL;
					c.gridx = 0;
					c.gridy = 1;
					frame.getContentPane().add(lbIm1, c);

					frame.pack();
					frame.setVisible(true);	
				}
			}	

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		args = "oneperson_960_540.rgb 960 544 100".split(" ");
		RenderVideo ren = new RenderVideo();		
		ren.showIms(args);
	}

}