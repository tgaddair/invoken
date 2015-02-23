
public class GenerateWallAtlas {
	private static final int SCALE = 32;
	private static final String TEMPLATE =
			"%s/%s\n"
			+ "  rotate: false\n"
			+ "  xy: %d, %d\n"
			+ "  size: %d, %d\n"
			+ "  orig: %d, %d\n"
			+ "  offset: 0, 0\n"
			+ "  index: -1\n";
	
	private final String biome;
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		
		// x and y specify the top-left corner of the block of tiles used for the roof
		// wx and wy specify the same for the walls
		
		// industry
//		GenerateWallAtlas formatter = new GenerateWallAtlas("industry");
//		int x = 192;
//		int y = 320;
//		int wx = 256;
//		int wy = 0;
		
		// grime
//		GenerateWallAtlas formatter = new GenerateWallAtlas("grime");
//		int x = 384;
//		int y = 320;
//		int wx = 448;
//		int wy = 320;
		
		// future
		GenerateWallAtlas formatter = new GenerateWallAtlas("future");
		int x = 256;
		int y = 320;
		int wx = 192;
		int wy = 320;
		
		sb.append(formatter.format("roof", 16, 48, x, y));
		sb.append(formatter.format("mid-wall-top", 16, 96, wx, wy));
		sb.append(formatter.format("mid-wall-center", 16, 112, wx, wy));
		sb.append(formatter.format("mid-wall-bottom", 16, 128, wx, wy));
		sb.append(formatter.format("left-trim", 32, 48, x, y));
		sb.append(formatter.format("right-trim", 0, 48, x, y));
		sb.append(formatter.format("top-left-trim", 0, 32, x, y));
		sb.append(formatter.format("top-right-trim", 32, 32, x, y));
		sb.append(formatter.format("left-corner", 48, 0, x, y, SCALE / 2));
		sb.append(formatter.format("right-corner", 32, 0, x, y, SCALE / 2));
		sb.append(formatter.format("overlay-below-trim", 16, 32, x, y));
		sb.append(formatter.format("overlay-left-trim", 32, 32, x, y));
		sb.append(formatter.format("overlay-right-trim", 0, 32, x, y));
		
		System.out.println(sb.toString());
	}
	
	private GenerateWallAtlas(String biome) {
		this.biome = biome;
	}
	
	private String format(String asset, int dx, int dy, int wallX, int wallY) {
		return format(asset, dx, dy, wallX, wallY, SCALE);
	}
	
	private String format(String asset, int dx, int dy, int wallX, int wallY, int size) {
		return String.format(TEMPLATE, biome, asset, wallX + dx, wallY + dy,
				size, size, size, size);
	}
}
