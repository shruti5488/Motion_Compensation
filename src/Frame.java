import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Frame {
	private int height;
	private int width;
	private byte[] y;
	private byte[] frameByte;

	public Frame(byte[] bytes, int startIndex, int h, int w){
		this.height = h;
		this.width = w;
		byte[] temp = new byte[Constants.WIDTH * 4]; 
		this.frameByte = new byte[h * w * 3];
		int ind = startIndex;
		int fInd = 0;
		int len = Constants.HEIGHT * Constants.WIDTH;
		for(int i = 0; i < 3; i++){
			System.arraycopy(bytes, ind, this.frameByte, fInd, len);
			fInd+= len;
			System.arraycopy(temp, 0, this.frameByte, fInd, temp.length);
			fInd+=temp.length;
			ind += len;
		}
		this.y = createYChannel();
	}

	public ArrayList<Macroblock> createMacroblocks(int dimension) {
		ArrayList<Macroblock> macroblocks = new ArrayList <Macroblock>();
		int ind = 0;
		int count = (width /dimension);

		byte[] yChannel = getYChannel();
		while(ind < yChannel.length){
			int srcInd = ind;
			byte[] bytes = new byte[dimension * dimension];
			int destInd =0;
			for(int i = 0; i < dimension;i++){	
				System.arraycopy(yChannel, srcInd, bytes, destInd, dimension);
				srcInd += width;
				destInd+= dimension;
			}
			Macroblock macroblock = new Macroblock(dimension, bytes,ind);
			macroblocks.add(macroblock);
			if(macroblocks.size() % count == 0){
				ind = srcInd - width + dimension;
			}
			else{
				ind += dimension;
			}			
		}	
		return macroblocks;
	}

	public ArrayList<CandidateBlock> getCandidateMacroblocks(Macroblock currentBlock, int kSearch) {
		int start = (-1) * kSearch;	
		ArrayList<CandidateBlock> candidateMblocks = new ArrayList<CandidateBlock>();
		int startIndex = currentBlock.getframeIndex();
		int dim = currentBlock.getDimension();

		byte[] y = getYChannel();
		for(int w = start; w <= kSearch; w++){
			for(int h = start; h <= kSearch; h++){


				int ind = isCandidate(startIndex,w,h, dim);
				if(ind != -1){
					byte[] bytes = 	this.createMacroblock(ind, dim,y);
					CandidateBlock block = new CandidateBlock(dim,bytes,ind,w,h);
					candidateMblocks.add(block);
				}	
			}
		}
		return candidateMblocks;
	}

	public byte[] createMacroblock(int startIndex, int dimension, byte[] yc) {
		//Creates the macroblocks using the start index and frame and dimension
		byte[] bytes = new byte[dimension * dimension];
		int destInd = 0;
		for(int i = 0; i < dimension;i++){	
			System.arraycopy(yc, startIndex, bytes, destInd, dimension);
			startIndex += width;
			destInd+= dimension;
		}
		return bytes;
	}



	private int isCandidate(int startInd, int dx, int dy, int dim) {
		int sInd = startInd;		
		if(sInd  + dx < 0 ) return -1;
		if(sInd + (dy * this.width) < 0 ) return -1;
		//Check the top corner points in same row and bottom point's height is in range of frame
		int ind = startInd + dx + (dy * this.width);

		int r1 =0;
		int r2 =0;

		r1 = ind / width;
		r2 = (ind + dim) / width;

		if((r1 >=0) && (r2 >=0) && (r1 < this.height) && (r2 < this.height) && (r1 == r2) && (r1 + dim < height)) {
			return ind;
		}
		else{
			return -1;
		}

	}

	private byte[] createYChannel(){

		byte[] y = new byte[height * width];
		for(int i = 0; i < height * width; i++){
			byte r = frameByte[i];
			byte g = frameByte[i + height * width];
			byte b = frameByte[i + height * width * 2];
			y[i] = (byte) ((r + g + b)/3);
		}
		return y;
	}

	public byte[] getYChannel(){
		return this.y;
	}
	public void displayFrame(){		
		JFrame frame;
		JLabel lbIm1;
		BufferedImage img;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		String result = String.format("Video height: %d, width: %d", 540, 960);
		int ind = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				byte a = 0;
				byte r = frameByte[ind];
				byte g = frameByte[ind+height*width];
				byte b = frameByte[ind+height*width*2]; 
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x,y,pix);
				ind++;
			}	
		}
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
