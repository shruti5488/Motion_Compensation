
public class FrameRGBMatrix {
	private int[][] rMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	

	private int[][] gMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	private int[][] bMatrix = new int[Constants.PADDED_HEIGHT][Constants.WIDTH];
	
	public FrameRGBMatrix(int[][] rMat, int[][] gMat, int[][] bMat){
		this.rMatrix = rMat;
		this.gMatrix = gMat;
		this.bMatrix = bMat;		
	}
	
	public void setrMatrix(int[][] rMatrix) {
		this.rMatrix = rMatrix;
	}

	public void setgMatrix(int[][] gMatrix) {
		this.gMatrix = gMatrix;
	}

	public void setbMatrix(int[][] bMatrix) {
		this.bMatrix = bMatrix;
	}
	
	public int[][] getrMatrix() {
		return rMatrix;
	}

	public int[][] getgMatrix() {
		return gMatrix;
	}

	public int[][] getbMatrix() {
		return bMatrix;
	}
}
