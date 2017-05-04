
public class CandidateBlock extends Macroblock{
	
	private int dx;
	private int dy;
	
	public CandidateBlock(int dim, byte[] bytes, int index, int dx, int dy){
		super(dim, bytes, index);
		this.dx = dx;
		this.dy = dy;
	}
	
	public int getDx() {
		return dx;
	}

	public int getDy() {
		return dy;
	}

}
