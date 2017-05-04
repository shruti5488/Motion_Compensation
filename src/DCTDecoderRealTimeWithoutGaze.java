import static java.lang.Math.cos;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class DCTDecoderRealTimeWithoutGaze {

	static Boolean loopValue = true;
	static Boolean playValue = true;
	static Boolean restartValue = false;
	static JButton playPause;
	static JButton loop;
	static JButton restart;
	static JFrame frame = new JFrame();
	static GridBagLayout gLayout = new GridBagLayout();

	static int ind;
	static long t;
	static int currentFrameIndex = 0;

	static JPanel buttonPanel = new JPanel();
	static JPanel panel = new JPanel();
	static JLabel videoLabel= new JLabel();
	static JLabel gazeLabel = new JLabel();


	// Matrix for IDCT of R, G, B
	static int[][] rMatrix_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static BufferedImage dctImage 	= new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
	static BufferedImage gazeImage  = new BufferedImage(Constants.GAZE_BLOCK, Constants.GAZE_BLOCK, BufferedImage.TYPE_INT_RGB);
	static BufferedImage playFrame = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);

	// Matrix for original R, G, B
	static int[][] rMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];

	//Matrix for IDCT of R, G, B
	static int[][] rMatrix_U_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix_U_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix_U_IDCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];

	// Matrix for DCT of R, G, B
	static int[][] rMatrix_DCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] gMatrix_DCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	static int[][] bMatrix_DCT = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];

	static double[][] cosineMatrix = new double[8][8];


	public static void idctTransformFrame() throws IOException {
		for (int i = 0; i < Constants.PADDED_HEIGHT; i += 8) {
			for (int j = 0; j < Constants.WIDTH; j += 8) {
				ComputeUnQuantizedIDCTBlockValues(i, j);
				ComputeIDCTBlockValues(i, j);
			}
		}
	}

	private static void ComputeIDCTBlockValues(int i, int j) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {

				float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;
				for (int u = 0; u < 8; u++) {
					for (int v = 0; v < 8; v++) {

						float fCu = 1.0f;
						float fCv = 1.0f;
						double iR;
						double iG;
						double iB;
						if (u == 0) {
							fCu = 0.707f;
						}
						if (v == 0) {
							fCv = 0.707f;
						}

						float fCuCv = fCu * fCv;
						iR = rMatrix_DCT[i + u][j + v];
						iG = gMatrix_DCT[i + u][j + v];
						iB = bMatrix_DCT[i + u][j + v];

						// IDCT Formula calculation
						fRRes += computeCosineIDCT(fCuCv, iR, x, u, y, v);
						fGRes += computeCosineIDCT(fCuCv, iG, x, u, y, v);
						fBRes += computeCosineIDCT(fCuCv, iB, x, u, y, v);
					}
				}

				fRRes *= 0.25;
				fGRes *= 0.25;
				fBRes *= 0.25;

				if ((fRRes <= 0) || (fRRes >= 255)) {
					fRRes = checkForRGBOverFlow(fRRes);
				}
				if ((fGRes <= 0) || (fGRes >= 255)) {
					fGRes = checkForRGBOverFlow(fGRes);
				}
				if ((fBRes <= 0) || (fBRes >= 255)) {
					fBRes = checkForRGBOverFlow(fBRes);
				}

				rMatrix_IDCT[i + x][j + y] = (int) fRRes;
				gMatrix_IDCT[i + x][j + y] = (int) fGRes;
				bMatrix_IDCT[i + x][j + y] = (int) fBRes;
			}
		}
	}

	private static void ComputeUnQuantizedIDCTBlockValues(int i, int j) {
		for(int x=0;x<8;x++){
			for(int y=0;y<8;y++){

				float fRRes = 0.00f, fGRes = 0.00f, fBRes = 0.00f;
				for(int u=0;u<8;u++) {
					for(int v=0;v<8;v++) {
						float fCu = 1.0f;
						float fCv = 1.0f;                                
						double iR;
						double iG;
						double iB;  
						if(u == 0){
							fCu =  0.707f;
						}								
						if(v == 0){
							fCv = 0.707f;
						}

						float fCuCv= fCu * fCv;
						iR = rMatrix[i + u][j + v];
						iG = gMatrix[i + u][j + v];
						iB = bMatrix[i + u][j + v];

						//IDCT Formula calculation                               
						fRRes += computeCosineIDCT(fCuCv, iR, x, u, y, v);
						fGRes += computeCosineIDCT(fCuCv, iG, x, u, y, v);
						fBRes += computeCosineIDCT(fCuCv, iB, x, u, y, v);


					}
				}

				fRRes *= 0.25;
				fGRes *= 0.25;
				fBRes *= 0.25;                        

				if((fRRes <= 0) || (fRRes >= 255)){
					fRRes = checkForRGBOverFlow(fRRes);
				}
				if((fGRes <= 0) || (fGRes >= 255)){
					fGRes = checkForRGBOverFlow(fGRes);
				}
				if((fBRes <= 0) || (fBRes >= 255)){
					fBRes = checkForRGBOverFlow(fBRes);
				}

				rMatrix_U_IDCT[i + x][j + y]  = (int)fRRes;
				gMatrix_U_IDCT[i + x][j + y]  = (int)fGRes;
				bMatrix_U_IDCT[i + x][j + y]  = (int)fBRes;

			}
		}
	}

	public static float computeCosineIDCT(float cIcJ, double pixel, int x, int u, int y, int v) {
		return (float) (cIcJ * pixel * cosineMatrix[x][u] * cosineMatrix[y][v]);
	}

	public static float checkForRGBOverFlow(float finalResult) {
		if (finalResult <= 0)
			finalResult = 0;
		else if (finalResult >= 255)
			finalResult = 255;
		return finalResult;

	}

	private static void createCosineMat() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				cosineMatrix[i][j] = cos((2 * i + 1) * j * 3.14159 / 16.00);
			}
		}

	}

	private static BufferedImage[] Decode(BufferedReader br, int nFore, int nBack) throws IOException {

		int frameNum = 0;
		int coefCount = 0;
		String[] DCTCoef = new String[64];
		// we need to calculate the frame count
		createCosineMat();

		int frameCount = Constants.n_frames;
		int index = 0;
		BufferedImage[] bufferedImageList = new BufferedImage[frameCount];

		while (frameNum < frameCount) {
			for (int i = 0; i < Constants.HEIGHT ; i += 8) {
				for (int j = 0; j < Constants.WIDTH; j += 8) {

					double[][] rBlock = new double[8][8];
					double[][] gBlock = new double[8][8];
					double[][] bBlock = new double[8][8];

					DCTCoef = br.readLine().split(" ");
					coefCount = 0;
					int n;
					String blockType = DCTCoef[coefCount];
					if (blockType.equals("f")) {
						n = nFore;
					} else {
						n = nBack;
					}
					for (int u = 0; u < 8; u++) {
						for (int v = 0; v < 8; v++) {
							coefCount++;
							rBlock[u][v] = Integer.parseInt(DCTCoef[coefCount]);
							rMatrix[i+u][j+v] = (int)rBlock[u][v];

							coefCount++;
							gBlock[u][v] = Integer.parseInt(DCTCoef[coefCount]);
							gMatrix[i+u][j+v] = (int)gBlock[u][v];

							coefCount++;
							bBlock[u][v] = Integer.parseInt(DCTCoef[coefCount]);
							bMatrix[i+u][j+v] = (int)bBlock[u][v];
						}
					}
					rBlock = zigZagTraversal(rBlock, n);
					gBlock = zigZagTraversal(gBlock, n);
					bBlock = zigZagTraversal(bBlock, n);

					for (int u = 0; u < 8; u++) {
						for (int v = 0; v < 8; v++) {
							rMatrix_DCT[i + u][j + v] = (int) rBlock[u][v];
							gMatrix_DCT[i + u][j + v] = (int) gBlock[u][v];
							bMatrix_DCT[i + u][j + v] = (int) bBlock[u][v];
						}
					}
				}
			}

			idctTransformFrame();
			BufferedImage dctImage 	= new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < Constants.HEIGHT; i++) {
				for (int j = 0; j < Constants.WIDTH; j++) {
					int r1 = rMatrix_IDCT[i][j];
					int g1 = gMatrix_IDCT[i][j];
					int b1 = bMatrix_IDCT[i][j];
					int pix = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
					dctImage.setRGB(j, i, pix);
				}
			}
			bufferedImageList[index] = dctImage;
			index++;
			frameNum++;
		}
		return bufferedImageList;
	}

	public static double[][] zigZagTraversal(double[][] matrix, int m) {
		int i = 0;
		int j = 0;
		int length = matrix.length - 1;
		int count = 1;

		// for upper triangle of matrix
		if (count > m) {
			matrix[i][j] = 0;
			count++;
		} else {
			count++;
		}

		while (true) {

			j++;
			if (count > m) {
				matrix[i][j] = 0;
				count++;
			} else {
				count++;
			}

			while (j != 0) {
				i++;
				j--;

				if (count > m) {
					matrix[i][j] = 0;
					count++;
				} else {
					count++;
				}
			}
			i++;
			if (i > length) {
				i--;
				break;
			}

			if (count > m) {
				matrix[i][j] = 0;
				count++;
			} else {
				count++;
			}

			while (i != 0) {
				i--;
				j++;
				if (count > m) {
					matrix[i][j] = 0;
					count++;
				} else {
					count++;
				}
			}
		}

		// for lower triangle of matrix
		while (true) {
			j++;
			if (count > m) {
				matrix[i][j] = 0;
				count++;
			} else {
				count++;
			}

			while (j != length) {
				j++;
				i--;

				if (count > m) {
					matrix[i][j] = 0;
					count++;
				} else {
					count++;
				}
			}
			i++;
			if (i > length) {
				i--;
				break;
			}

			if (count > m) {
				matrix[i][j] = 0;
				count++;
			} else {
				count++;
			}

			while (i != length) {
				i++;
				j--;
				if (count > m) {
					matrix[i][j] = 0;
					count++;
				} else {
					count++;
				}
			}
		}
		return matrix;
	}

	private static void playVideo(BufferedImage[] images, boolean gazeControlOn) throws InterruptedException{


		playPause = new JButton("Play/Pause");
		loop = new JButton("Loop ON/OFF");
		restart = new JButton("Restart");
		
		frame.getContentPane().setLayout(gLayout);


		String labelText = String.format("Video height: %d, width: %d", Constants.HEIGHT, Constants.WIDTH);

		JLabel statusText = new JLabel(labelText);
		JLabel playText = new JLabel("Video Playing");
		JLabel loopText = new JLabel("Looping ON");
		JLabel restartText = new JLabel("Retart the Video");

		// Setting Buttons in Button Panel
		buttonPanel.add(playPause);
		buttonPanel.add(loop);
		buttonPanel.add(restart);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		loop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (loopValue == true) {
					loopValue = false;
					loopText.setText("Looping OFF");
				} else {
					loopValue = true;
					loopText.setText("Looping ON");
				}
			}
			// TODO Auto-generated method stub
		});

		// Play/Pause Button Action Listener
		playPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e1) {
				if (playValue == true) {
					playValue = false;
					playText.setText("Video Paused");
				} else {
					playValue = true;
					playText.setText("Video Playing");
				}
			}
		});
		
		// Restart Button Action Listener
		restart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e2) {
				if (restartValue == true) {
					restartValue = false;
					restartText.setText("Video Restarted");
				} else {
					restartValue = true;
					restartText.setText("Video Playing");
				}
			}
		});
		
		int frameNum = 0;
		int frameCount = Constants.n_frames;
		while (loopValue == true || (loopValue == false && playValue == false)) {

			t = System.currentTimeMillis();
			if (playValue == true) {
				ind = 0;
			} else {
				ind = currentFrameIndex;
			}

			currentFrameIndex = ind;

			if((frameNum == frameCount) || (restartValue == false && playValue == true)){
				frameNum = 0;
			}
			while(frameNum < frameCount){

				ind += 2 * Constants.HEIGHT * Constants.WIDTH;
				statusText.setHorizontalAlignment(SwingConstants.RIGHT);
				playText.setHorizontalAlignment(SwingConstants.RIGHT);
				loopText.setHorizontalAlignment(SwingConstants.RIGHT);

				statusText.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
				playText.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));
				loopText.setBorder(BorderFactory.createEmptyBorder(0,0,0,20));

				if (playValue == false) {
					break;
				}
				
				if (restartValue == true) {
					restartValue = false;
					break;
				}

				// Use labels to display the images
				dctImage = images[frameNum];

				videoLabel.setIcon(new ImageIcon(dctImage));
				videoLabel.setBorder(BorderFactory.createEmptyBorder(0,0,20,20));
				//
				// Adding video frame Label to the JFrame
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.anchor = GridBagConstraints.FIRST_LINE_START;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 0;
				frame.getContentPane().add(videoLabel, c);
				//
				// Adding status text to the JFrame
				c.fill = GridBagConstraints.HORIZONTAL;
				c.anchor = GridBagConstraints.FIRST_LINE_END;
				c.weightx = 1;
				c.gridx = 1;
				c.gridy = 1;
				frame.getContentPane().add(statusText, c);
				c.gridy = 2;
				frame.getContentPane().add(playText, c);
				c.gridy = 3;
				frame.getContentPane().add(loopText, c);
				
				// Adding button to the JFrame
				c.fill = GridBagConstraints.VERTICAL;
				c.anchor = GridBagConstraints.LAST_LINE_START;
				c.weightx = 0.5;
				c.gridx = 0;
				c.gridy = 1;
				frame.getContentPane().add(buttonPanel, c);

				// Packing components in JFRAME
				frame.pack();
				frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				frameNum++;
				Thread.sleep(30);
			}
		}
	}
	private static BufferedReader readFile(String filePath) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(filePath));
		return br;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		long t = System.currentTimeMillis();
		BufferedReader br = readFile(args[0]);

		if(args.length!=4){
			System.out.println("Error: Unkown parameters entered. Please enter in the below format:");
			System.out.println("Decoder myImage.txt n1 n2 gazeControlOn");
			System.out.println("Program terminated..");
			System.exit(0);
		}
		System.out.println("Please wait for a while.. The video is buffering !");
		Constants.n_frames = Integer.parseInt(br.readLine());
		int nFore = Integer.parseInt(args[1]);
		int nBack = Integer.parseInt(args[2]);
		boolean gazeControlOn = (Integer.parseInt(args[3]) ==0) ? false : true;

		BufferedImage[] images = Decode(br, nFore, nBack);
		long et = System.currentTimeMillis();
		System.out.println("Video Processed for: " + (et - t)/1000 + " seconds");
		System.out.println("Video playing now...");
		playVideo(images, gazeControlOn);	
	}
}
