public class Constants {
	
	public static final int WIDTH = 960;
	public static final int HEIGHT = 540;
	public static final int PADDED_HEIGHT = 544;
	
	public static final int N_CHANNELS = 3;
	public static final int SIZE_OF_MACROBLOCK = 16;
	public static final int K_SEARCH = 8;
	public static final int SIZE_OF_DCT_BLOCK = 8;
	public static final int N_MACROBLOCKS_PER_FRAME = (WIDTH / SIZE_OF_MACROBLOCK) 
											* (PADDED_HEIGHT/SIZE_OF_MACROBLOCK);
	
	public static  int n_frames = 0;
	public static  int n_dctBlocksPerFrame = (WIDTH/SIZE_OF_DCT_BLOCK) * (PADDED_HEIGHT/SIZE_OF_DCT_BLOCK);
	
	public static int GAZE_BLOCK = 64;
	
	//Motion vector constants
	public static final int THRESHOLD_X = 0; 
	public static final int THRESHOLD_Y = 0; 
	public static String fileName;
}