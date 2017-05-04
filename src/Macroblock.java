import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Macroblock {
	private int dimension;
	private byte[] macroblock;
	private int frameIndex;
	
	
	public Macroblock(int dim, byte[] bytes, int index){
		this.dimension = dim;
		this.frameIndex = index;
		this.macroblock = new byte[dim * dim];
		System.arraycopy(bytes, 0, macroblock, 0, dim * dim);
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public byte[] getMacroblock() {
		return macroblock;
	}
	
	public int getframeIndex() {
		return frameIndex;
	}
	
	public int SAD(CandidateBlock candidate){
		int diff = 0;
		for(int i = 0; i < candidate.getMacroblock().length;i++){
			diff += Math.abs(this.macroblock[i] - candidate.getMacroblock()[i]);
		}
		return diff;
	}
	
	public MotionVector getMotionVector(ArrayList<CandidateBlock> candidateMblocks, int fNumber, int blockNumber) {
		try{
			int ind = 0;
			int sad = Integer.MAX_VALUE;
			for(int i = 0; i < candidateMblocks.size(); i++ ){
				int diff = this.SAD(candidateMblocks.get(i));
				if(diff < sad){
					ind = i;
					sad = diff;
				}
			}
			if(ind >= candidateMblocks.size()){
				System.out.println("exception");
			}
			int dx = candidateMblocks.get(ind).getDx();
			int dy =candidateMblocks.get(ind).getDy();
			return new MotionVector(dx, dy, fNumber, blockNumber,!(dx==0 && dy ==0));
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("exception");
			return null;
		}
	}
	
	public void display(){
		JFrame frame;
		JLabel lbIm1;
		BufferedImage img;
		img = new BufferedImage(dimension,dimension, BufferedImage.TYPE_INT_RGB);
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		String result = String.format("Video height: %d, width: %d", dimension, dimension);
		int ind = 0;
		for(int y = 0; y < dimension; y++){
			for(int x = 0; x < dimension; x++){
				byte a = 0;
				byte r = macroblock[ind];
				 
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((r & 0xff) << 8) | (r & 0xff);
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
