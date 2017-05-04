import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Segmentation {
	
	public static final int N_CHANNELS = 3;
	public static final int SIZE_OF_MACROBLOCK = 16;
	public static final int K_SEARCH = 8;
	public static final int SIZE_OF_DCT_BLOCK = 8;
	public static final int N_MACROBLOCKS_PER_FRAME = (Constants.WIDTH / SIZE_OF_MACROBLOCK) 
											* (Constants.PADDED_HEIGHT/SIZE_OF_MACROBLOCK);
	
	public static  int n_frames = 0;
	public static  int n_dctBlocksPerFrame = (Constants.WIDTH/SIZE_OF_DCT_BLOCK) * (Constants.PADDED_HEIGHT/SIZE_OF_DCT_BLOCK);
	
	
	public static boolean[][] getSegmentation(String filePath){
		
		//reading File
		byte[] inputVideoByteArray = readFile(filePath);		
		Constants.n_frames = (inputVideoByteArray.length)/(Constants.WIDTH * Constants.HEIGHT * 3);			
		System.out.println("Read the input video file...");
		
		//Initialize an output byte array
		System.out.println("Calculating the motion vectors of video file...");		
		Frame refrenceFrame = new Frame(inputVideoByteArray,0,Constants.PADDED_HEIGHT,Constants.WIDTH);		
		int index = Constants.HEIGHT * Constants.WIDTH * 3;
		ArrayList<MotionVector>  mVectors= new ArrayList<MotionVector>();
		
		int frameCnt =0;
		int Fcnt = 0;
		while(index < inputVideoByteArray.length){
			
			frameCnt++;
			Frame targetFrame = new Frame(inputVideoByteArray,index,Constants.PADDED_HEIGHT,Constants.WIDTH);			
			ArrayList<Macroblock> targetFrameMacroblocks = targetFrame.createMacroblocks(SIZE_OF_MACROBLOCK);
			
			for(int i = 0; i < targetFrameMacroblocks.size();i++){				
				Macroblock currentBlock = targetFrameMacroblocks.get(i);
				ArrayList<CandidateBlock> candidateMblocks = refrenceFrame.getCandidateMacroblocks(currentBlock,K_SEARCH);
				MotionVector mv = currentBlock.getMotionVector(candidateMblocks,frameCnt,i);
				if(mv.getDx() != 0 || mv.getDy() !=0){
					Fcnt++;
				}
				mVectors.add(mv);
			}
			refrenceFrame = targetFrame;
			index += Constants.HEIGHT * Constants.WIDTH * 3;
		}
	
		System.out.println("Motion Vectors of video file calculated..." + Fcnt);
		mVectors.get(0).setSegment(!((Math.abs(mVectors.get(0).getDy()) <= Constants.THRESHOLD_Y) && 
				(Math.abs(mVectors.get(0).getDx()) <= Constants.THRESHOLD_X)));
		
		mVectors.get(mVectors.size()-1).setSegment(!((Math.abs(mVectors.get(mVectors.size()-1).getDy()) <= Constants.THRESHOLD_Y) && 
				(Math.abs(mVectors.get(mVectors.size()-1).getDx()) <= Constants.THRESHOLD_X)));
		for(int i = 1; i <mVectors.size()-1;i++){
			MotionVector c = mVectors.get(i);
			MotionVector p = mVectors.get(i -1);
			MotionVector n = mVectors.get(i +1);
			c.setSegment(findSegment(c,p,n));
		}
	
		System.out.println("Processing the motion vectors and segmenting macroblokcs...");
		boolean[][] segArray = new boolean[Constants.n_frames][n_dctBlocksPerFrame];
		for(int i = 1; i < Constants.n_frames; i++){
			for(int j =0; j< n_dctBlocksPerFrame;j++){				
				segArray[i][j] = mVectors.get(findMacroblock(i,j)).getSegment();
			}
		}
		return segArray;
	}
	
	

	private static boolean findSegment(MotionVector c, MotionVector p, MotionVector n) {
		int dx = c.getDx();
		int dy = c.getDy();
		
		if(((Math.abs(dy) <= Constants.THRESHOLD_Y) && (Math.abs(dx) <= Constants.THRESHOLD_X))){
			//static background
			return false;
		}else{
			int ndx = n.getDx();
			int ndy = n.getDy();
			int pdx = p.getDx();
			int pdy = p.getDy();
			if((Math.abs(dx-pdx) <= Constants.THRESHOLD_X) && (Math.abs(ndx-dx) <= Constants.THRESHOLD_X) 
					&& (Math.abs(dy-pdy) <= Constants.THRESHOLD_Y) && (Math.abs(ndy-dy) <= Constants.THRESHOLD_Y)){
				return false;
			}
			else{
				return true;
			}	
		}
	}



	private static int findMacroblock(int frameNum, int dctBlockNum) {
		
		int mR = ((dctBlockNum /120)/2);
		int mC = ((dctBlockNum%120)/2);
		int mNum = mR * 60 + mC + (frameNum-1) * 2040;
		return mNum;
	}

	private static void displayMacroblock(byte[] inputVideoArr, MotionVector motionVector) {
		
		int frameNum = motionVector.getFrameNo();
		int blockNum = motionVector.getBlockNo();
		
		int ind = Constants.PADDED_HEIGHT * Constants.WIDTH * 3 * frameNum;
		Frame frame = new Frame(inputVideoArr,ind,Constants.PADDED_HEIGHT, Constants.WIDTH);
		
		int startIndex = ((blockNum % (Constants.WIDTH/SIZE_OF_MACROBLOCK)) * SIZE_OF_MACROBLOCK) +
				(Constants.WIDTH * (blockNum / (Constants.WIDTH/SIZE_OF_MACROBLOCK)));
		
		Macroblock mb = new Macroblock(SIZE_OF_MACROBLOCK, 
				frame.createMacroblock( startIndex, SIZE_OF_MACROBLOCK,frame.getYChannel()), startIndex);
		
		mb.display();
		
		//Create frame
		//create macroblock
		//display macroblock
		
	}

	private static byte[] readFile(String fileName) {
		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);

			long len = file.length();
			byte[] bytes = new byte[(int)len];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && 
					(numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			
			is.close();
			return bytes;
		}catch(FileNotFoundException e){			
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	public static byte[] padHeight(byte[] inputVideoByteArray, int padHeightCount) {
		int copySize = Constants.WIDTH * Constants.HEIGHT;
		int padSize = Constants.WIDTH * padHeightCount;
		int inputInd = 0;
		int outputInd = 0;
		byte[] outputByte = new byte[Constants.WIDTH * Constants.PADDED_HEIGHT * 3 * Constants.n_frames];
		byte[] temp = new byte[Constants.WIDTH * padHeightCount];
		while(inputInd < inputVideoByteArray.length){
			System.arraycopy(inputVideoByteArray, inputInd, outputByte, outputInd, copySize);
			inputInd += copySize;
			outputInd+= copySize;
			System.arraycopy(temp, 0, outputByte, outputInd, padSize);
			outputInd+= padSize;	
		}
		return outputByte;
	}
	
	
	
}