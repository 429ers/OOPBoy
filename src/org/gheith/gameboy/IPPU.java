package org.gheith.gameboy;

public interface IPPU {
	public static final int OAM_SEARCH_LENGTH = 80;
	public static final int OAM_SEARCH_START = 0;
	public static final int OAM_SEARCH_END = 79;
	public static final int PIXEL_TRANSFER_LENGTH = 172;
	public static final int PIXEL_TRANSFER_START = 80;
	public static final int PIXEL_TRANSFER_END = 251;
	public static final int H_BLANK_LENGTH = 204;
	public static final int H_BLANK_START = 252;
	public static final int H_BLANK_END = 455;
	public static final int V_BLANK = 10;
	public static final int ACTUAL_LINES = 144;
	public static final int V_BLANK_LINES = 10;
	public static final int LINE_LENGTH = 456;
}
