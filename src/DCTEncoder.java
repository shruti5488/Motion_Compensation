import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class DCTEncoder {
	
	// Matrix for segmentation
	static boolean[][] segArray;

	static double[][] cosineMatrix = new double[8][8];

	//Matrix for original R, G, B
	static int[][] rMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];

	static FrameRGBMatrix frameRGMmat;

	//Matrix for DCT of R, G, B
	static int[][] rMatrix_DCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix_DCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix_DCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];

	//File writer
	static FileWriter fw = null;
	static BufferedWriter bw = null;

	//Matrix for IDCT of R, G, B
	static int[][] rMatrix_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];



	public void readVideo(String path, int xt, int yt){

		try {
			File file = new File(path);
			InputStream is = new FileInputStream(file);

			long fileSize = file.length();
			byte[] bytes = new byte[(int)fileSize];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			createCosineMat();
			int ind = 0;
			int frameNum = 0;
			int frameCount = 0;
			while(frameNum<fileSize){
				ind = frameNum;				
				//For each frame get the rgb matreix values
				frameRGMmat = getframeRGBmat(bytes,ind);

				//compute the dct blocks for each frame
				dctTransformFrame(frameCount);

				//Updating the frame index
				frameNum += Constants.HEIGHT*Constants.WIDTH*3;
				frameCount++;
			}	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	private void createCosineMat() {
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				cosineMatrix[i][j] = cos((2*i+1)*j*3.14159/16.00);				
			}			
		}

	}

	public FrameRGBMatrix getframeRGBmat(byte[] bytes, int ind) {

		int[][] rMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
		int[][] gMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
		int[][] bMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];

		for(int y = 0; y < Constants.PADDED_HEIGHT; y++){
			if(y<Constants.HEIGHT){
				for(int x = 0; x < Constants.WIDTH; x++){					
					int r = bytes[ind];
					int g = bytes[ind+Constants.HEIGHT*Constants.WIDTH];
					int b = bytes[ind+Constants.HEIGHT*Constants.WIDTH*2];

					rMatrix[y][x] = convertToUnsigned(r);
					gMatrix[y][x] = convertToUnsigned(g);
					bMatrix[y][x] = convertToUnsigned(b);
					ind++;
				}
			}
			else{
				for(int x = 0; x < Constants.WIDTH; x++){					
					rMatrix[y][x] = 0;
					gMatrix[y][x] = 0;
					bMatrix[y][x] = 0;

				}
			}
		}
		return new FrameRGBMatrix(rMatrix, gMatrix, bMatrix);
	}

	public static int convertToUnsigned(int unsignedVal){
		return (unsignedVal & 0xFF);
	}


	public static void dctTransformFrame (int frameCount) throws IOException{

		int blockNo = 0;
		for(int i = 0;i<Constants.PADDED_HEIGHT;i+=8){
			for(int j = 0;j<Constants.WIDTH;j+=8){
				
				String type = (segArray[frameCount][blockNo])?"f":"b";				
				bw.write(type  + " ");	
				for(int u = 0; u < 8; u++){
					for(int v = 0; v < 8; v++){

						float cu = 1.0f, cv = 1.0f;
						float rResult = 0.00f, gResult = 0.00f, bResult = 0.00f;

						if(u == 0)
							cu =  0.707f;
						if(v == 0)
							cv = 0.707f;

						for(int x=0;x<8;x++){
							for(int y=0;y<8;y++) {								
								int iR, iG, iB;      
								iR = (int) frameRGMmat.getrMatrix()[i+x][j+y];
								iG = (int) frameRGMmat.getgMatrix()[i+x][j+y];
								iB = (int) frameRGMmat.getbMatrix()[i+x][j+y];

								rResult += computeCosineDCT(iR, x, u, y , v);
								gResult += computeCosineDCT(iG, x, u, y , v);
								bResult += computeCosineDCT(iB, x, u, y , v);
							}
						}
						int rVal = (int) getFinalDCTCoefficients(rResult, cu, cv);
						bw.write(String.valueOf(rVal) + " ");
						int gVal = (int) getFinalDCTCoefficients(gResult, cu, cv);
						bw.write(String.valueOf(gVal) + " ");
						int bVal = (int) getFinalDCTCoefficients(bResult, cu, cv);
						bw.write(String.valueOf(bVal) + " ");
					}
				}
				bw.newLine();
				blockNo++;
			}
			
		}
	}

	public static float computeCosineDCT(int pixel, int x, int u, int y, int v){
		return (float) (pixel*cosineMatrix[x][u]*cosineMatrix[y][v]);
	}

	public static float getFinalDCTCoefficients(float pixelResult, float cI, float cJ){
		return Math.round(0.25*pixelResult*cI*cJ);
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		args =  "/Users/shruti5488/Documents/test_cases/two_people_moving_background.rgb".split(" ");
		
		long t = System.currentTimeMillis();
		
		if(args.length!=1){
			System.out.println("Error: Unkown parameters entered. Please enter in the below format:");
			System.out.println("Encoder myImage.rgb");
			System.out.println("Program terminated..");
			System.exit(0);
		}
		
		segArray = Segmentation.getSegmentation(args[0]);
		
		DCTEncoder ren = new DCTEncoder();
		String compressFilePath = args[0].split("\\.")[0] + ".txt";
		fw = new FileWriter(compressFilePath);
		bw = new BufferedWriter(fw);

		bw.write(String.valueOf(Constants.n_frames));
		bw.newLine();

		ren.readVideo(args[0], Constants.WIDTH, Constants.PADDED_HEIGHT);
		bw.close();
		System.out.println("Done");
		long et = System.currentTimeMillis();
		System.out.println("Played for: " + (et - t)/1000 + " seconds");	
	}
}
