
public class MotionVector {
	
	private int dx;
	private int dy;
	private int frameNo;
	private int blockNo;
	private boolean segment;
	// if false  is background if true is foreground

	public MotionVector(int x, int y, int fNo, int bNo, boolean segment)
	{
		this.dx = x;
		this.dy = y;
		this.frameNo = fNo;
		this.blockNo = bNo;
		this.segment = segment;
	}
	
	public int getDx() {
		return dx;
	}

	public int getDy() {
		return dy;
	}
	
	public int getFrameNo(){
		return frameNo;
		
	}
	public int getBlockNo(){
		return blockNo;
		
	}
	
	public boolean getSegment(){
		return segment;
		
	}
	public void setSegment(boolean segment) {
		this.segment = segment;
	}	
}
