package priv.louisnicolasdavout.tapeeditor.main;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * 用于编辑和显示乐谱
 */
class MusicEditor {
	private int panelWidth;
	private int panelHeight;
	private int unitWidth;
	private int unitHeight;
	private int width;
	private int height;

	public MusicEditor(int panelWidth, int panelHeight, int unitWidth, int unitHeight) {
		this.panelWidth = panelWidth;
		this.panelHeight = panelHeight;
		this.unitWidth = unitWidth;
		this.unitHeight = unitHeight;
		this.width = 3 * unitWidth;
		this.height = 5 * unitHeight;
	}

	/*
	 * get/set方法
	 */
	public int getPanelWidth() {
		return this.panelWidth;
	}

	public int getPanelHeight() {
		return this.panelHeight;
	}

	public int getUnitWidth() {
		return this.unitWidth;
	}

	public void setUnitWidth(int unitWidth) {
		this.unitWidth = unitWidth;
	}

	public int getUnitHeight() {
		return this.unitHeight;
	}

	public void setUnitHeight(int unitHeight) {
		this.unitHeight = unitHeight;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	// 用于更新面板尺寸信息，应在每次刷新前调用，传入当前绘图用JPanel的尺寸
	public void setPanelSize(int panelWidth, int panelHeight) {
		this.panelWidth = panelWidth;
		this.panelHeight = panelHeight;
	}

	/*
	 * 绘图方法
	 */

	/*
	 * 在Graphics2D对象上绘制简谱 music是要绘制的简谱 lineStart是绘制开始的行
	 * selectedNoteRow和selectedNoteColumn是被选中音符在引入偏移前(在JPanel上的)的行和列
	 * 音符的显示位置是由初始位置和偏移量决定的，自动排版仅改变音符的偏移量。 由于初始位置(在一定程度上的)不变性，可以利用初始位置判断是否选中
	 */
	public void drawMusic(Graphics2D g2, Music music, int lineStart, int selectedNoteRow, int selectedNoteColumn) {
		final int linesPerPanel = this.panelHeight / this.height;
		int row = 0;
		int startBarIndex = 1;
		for (int i = 0; i < lineStart; i++) {
			startBarIndex += music.getLine(i).getBarsCount();
		}
		int previousRows = 0;
		for (int i = 0; i < lineStart; i++) {
			previousRows += music.getLine(i).getLineHeight();
		}
		for (int i = lineStart; i < music.getLinesCount(); i++) {
			if (row >= linesPerPanel) {
				break;
			}
			MusicLine line = music.getLine(i);

			this.drawLine(g2, row, line, selectedNoteRow, selectedNoteColumn, startBarIndex, previousRows);
			row += line.getLineHeight();
			startBarIndex += line.getBarsCount();
		}
	}

	/*
	 * 绘制单行 row是绘制开始处(JPanel上的)行数，一个音符占据一行 startBarIndex是绘制开始时的小节标号
	 */
	private void drawLine(Graphics2D g2, int row, MusicLine line, int selectedNoteRow, int selectedNoteColumn,
			int startBarIndex, int previousRows) {
		int column = 0;
		for (int i = 0; i < line.getBarsCount(); i++) {
			MusicBar bar = line.getBar(i);
			this.drawBar(g2, row, column, bar, selectedNoteRow, selectedNoteColumn, startBarIndex + i, previousRows);
			column += bar.getBarWidth();
		}
	}

	/*
	 * 绘制小节 row是绘制开始处(JPanel上的)行数，一个音符占据一行 column是绘制开始处(JPanel上的)列数，一个音符或一条小节线占据一列
	 * barIndex是小节标号
	 */
	private void drawBar(Graphics2D g2, int row, int column, MusicBar bar, int selectedNoteRow, int selectedNoteColumn,
			int barIndex, int previousRows) {
		this.drawBarLine(g2, row, column, bar.getPartsCount(), barIndex, selectedNoteRow, selectedNoteColumn,
				previousRows);
		for (int i = 0; i < bar.getPartsCount(); i++) {
			this.drawPart(g2, row + i, column + 1, bar.getPart(i), bar.getBarWidth(), selectedNoteRow,
					selectedNoteColumn, previousRows);
		}
	}

	// 在窗口指定行列处绘制小节线
	private void drawBarLine(Graphics2D g2, int row, int column, int parts, int barIndex, int selectedRow,
			int selectedColumn, int previousRows) {
		if (selectedRow - previousRows >= row && selectedRow - previousRows < row + parts && selectedColumn == column) {
			this.drawBarLineBackground(g2, row, column, parts);
		}
		final int positionX = column * this.width + this.width / 2;
		final int positionY = row * this.height + this.unitHeight;
		final int length = parts * this.height - this.unitHeight;
		g2.drawLine(positionX, positionY, positionX, positionY + length); // 小节线
		g2.drawString(String.valueOf(barIndex), positionX, positionY + this.unitHeight); // 小节标号
	}

	/*
	 * 绘制声部 row是绘制开始处(JPanel上的)行数，一个音符占据一行 column是绘制开始处(JPanel上的)列数，一个音符或一条小节线占据一列
	 * barWidth是声部所在小节的宽度
	 */
	private void drawPart(Graphics2D g2, int row, int column, MusicPart part, int barWidth, int selectedNoteRow,
			int selectedNoteColumn, int previousRows) {
		int offsetPerNote = this.calculateOffsetPerNote(part, barWidth);
		for (int i = 0; i < part.getNotesCount(); i++) {
			this.drawNote(g2, row, column + i, offsetPerNote * i, part.getNote(i), selectedNoteRow, selectedNoteColumn,
					previousRows);
		}
	}

	/*
	 * 绘制音符 row是绘制开始处(JPanel上的)行数，一个音符占据一行 column是绘制开始处(JPanel上的)列数，一个音符或一条小节线占据一列
	 * offset是偏移量
	 */
	private void drawNote(Graphics2D g2, int row, int column, int offset, MusicNote note, int selectedNoteRow,
			int selectedNoteColumn, int previousRows) {
		final boolean selected = row == selectedNoteRow - previousRows && column == selectedNoteColumn;
		if (selected) {
			this.drawBackground(g2, row, column, offset);
		}
		this.drawNote(g2, row, column, offset, note.getPitch(), note.getUnderline(), note.isDot(), note.isSharp(),
				note.isSilent());
	}

	// 在窗口中指定行列处绘制音符
	private void drawNote(Graphics2D g2, int row, int column, int offset, Pitch pitch, Underline underline, boolean dot,
			boolean sharp, boolean silent) {
		Color defaultColor = g2.getColor();
		if (silent) {
			g2.setColor(Color.GREEN);
		}
		final int positionX = column * this.width + offset;
		final int positionY = row * this.height + this.unitHeight;

		// 绘制升半音记号
		if (sharp) {
			g2.drawString(" ♯", positionX, positionY + this.unitHeight * 3 / 2);
		}

		// 绘制高低音记号
		switch (pitch.getOctave()) {
		case DOUBLE_TREBLE:
			g2.drawString("：", positionX + this.unitWidth, positionY + this.unitHeight);
			break;
		case TREBLE:
			g2.drawString("•", positionX + this.unitWidth, positionY + this.unitHeight);
			break;
		case BASS:
			g2.drawString("•", positionX + this.unitWidth,
					positionY + this.unitHeight * (Underline.NONE.compareTo(underline) == 0 ? 3 : 4));
			break;
		case DOUBLE_BASS:
			g2.drawString("：", positionX + this.unitWidth,
					positionY + this.unitHeight * (Underline.NONE.compareTo(underline) == 0 ? 3 : 4));
			break;
		default:
			break;
		}

		// 绘制音符数字
		g2.drawString(pitch.getNote(), positionX + this.unitWidth, positionY + this.unitHeight * 2);

		// 绘制下划线
		switch (underline) {
		case SINGLE:
			g2.drawString("─", positionX + this.unitWidth, positionY + this.unitHeight * 3);
			break;
		case DOUBLE:
			g2.drawString("═", positionX + this.unitWidth, positionY + this.unitHeight * 3);
			break;
		default:
			break;
		}

		// 绘制附点
		if (dot) {
			g2.drawString("•", positionX + 2 * this.unitWidth, positionY + this.unitHeight * 2);
		}

		g2.setColor(defaultColor);
	}

	// 绘制选中音符背景
	private void drawBackground(Graphics2D g2, int row, int column, int offset) {
		final int positionX = column * this.width + offset;
		final int positionY = row * this.height + this.unitHeight;
		Color defaultColor = g2.getColor();
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(positionX, positionY, this.width, this.height - this.unitHeight);
		g2.setColor(defaultColor);
	}

	// 绘制选中小节线背景
	private void drawBarLineBackground(Graphics2D g2, int row, int column, int parts) {
		final int positionX = column * this.width;
		final int positionY = row * this.height + this.unitHeight;
		final int length = parts * this.height - this.unitHeight;
		Color defaultColor = g2.getColor();
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(positionX, positionY, this.width, length);
		g2.setColor(defaultColor);
	}

	// 计算偏移量
	private int calculateOffsetPerNote(MusicPart part, int barWidth) {
		// 为了避免DivisionByZeroException，这里引入一个条件判断
		int offsetPerNote = 0;
		if (part.getNotesCount() - 1 != 0) {
			offsetPerNote = (barWidth - part.getNotesCount() - 1) * this.width / (part.getNotesCount() - 1);
		}
		return offsetPerNote;
	}

	/*
	 * 控制选中用方法
	 */

	/*
	 * 获取选中位置在乐曲中的位置，如果输入不正确会抛出PositionInvalidException的某个子类
	 * 如果选中小节线，返回MusicalPosition对象的note属性<0 如果程序正常运行，则此方法不应抛出异常，如果抛出异常则说明设计有缺陷
	 * 换言之，必须保证传入此方法的位置上确实存在音符或小节线
	 */
	public MusicalPosition getSelectedMusicalPosition(Music music, int selectedRow, int selectedColumn) {
		// 计算选中行，计算完成后rowsCount为选中行之前的总行数
		int rowsCount = 0;
		int selectedLine = -1;
		for (int i = 0; i < music.getLinesCount(); i++) {
			int nextRowsCount = rowsCount + music.getLine(i).getLineHeight();
			if (nextRowsCount > selectedRow) {
				selectedLine = i;
				break;
			}
			rowsCount = nextRowsCount;
		}
		if (selectedLine < 0) {
			throw new LinePositionInvalidException("无法从选中的位置获得对应的行");
		}

		// 计算选中小节，计算完成后columnsCount为选中小节之前的总列数
		MusicLine musicLine = music.getLine(selectedLine);
		int columnsCount = 0;
		int selectedBar = -1;
		for (int i = 0; i < musicLine.getBarsCount(); i++) {
			int nextColumnsCount = columnsCount + musicLine.getBar(i).getBarWidth();
			if (nextColumnsCount > selectedColumn) {
				selectedBar = i;
				break;
			}
			columnsCount = nextColumnsCount;
		}
		if (selectedBar < 0) {
			throw new BarPositionInvalidException("无法从选中的位置获得对应的小节");
		}

		// 计算选中的声部
		MusicBar musicBar = musicLine.getBar(selectedBar);
		final int selectedPart = selectedRow - rowsCount;
		if (selectedPart >= musicBar.getPartsCount() || selectedPart < 0) {
			throw new PartPositionInvalidException("无法从选中的位置获得对应的声部");
		}

		// 计算选中的音符
		MusicPart musicPart = musicBar.getPart(selectedPart);
		final int selectedNote = selectedColumn - columnsCount - 1;
		if (selectedNote >= musicPart.getNotesCount()) {
			throw new NotePositionInvalidException("无法从选中的位置获得对应的音符");
		}

		return new MusicalPosition(selectedLine, selectedBar, selectedPart, selectedNote);
	}

	/*
	 * 获取选中位置，如果位置无效，返回(-1,-1)
	 */
	public Position getSelectedPosition(Music music, int lineStart, int x, int y) {
		int previousRows = 0;
		for (int i = 0; i < lineStart; i++) {
			previousRows += music.getLine(i).getLineHeight();
		}

		final int selectedRow = previousRows + y / this.height;

		// 计算选中行，计算完成后rowsCount为选中行之前的总行数
		int rowsCount = 0;
		int selectedLine = -1;
		for (int i = 0; i < music.getLinesCount(); i++) {
			int nextRowsCount = rowsCount + music.getLine(i).getLineHeight();
			if (nextRowsCount > selectedRow) {
				selectedLine = i;
				break;
			}
			rowsCount = nextRowsCount;
		}
		if (selectedLine < 0) {
			return new Position(-1, -1);
		}

		// 计算选中小节，计算完成后xCount为选中小节之前的总像素宽度，columnsCount为选中小节之前的总列数
		MusicLine musicLine = music.getLine(selectedLine);
		int xCount = 0;
		int columnsCount = 0;
		int selectedBar = -1;
		for (int i = 0; i < musicLine.getBarsCount(); i++) {
			int nextXCount = xCount + musicLine.getBar(i).getBarWidth() * this.width;
			if (nextXCount > x) {
				selectedBar = i;
				break;
			}
			xCount = nextXCount;
			columnsCount += musicLine.getBar(i).getBarWidth();
		}
		if (selectedBar < 0) {
			return new Position(-1, -1);
		}

		// 计算选中的声部
		MusicBar musicBar = musicLine.getBar(selectedBar);
		final int selectedPart = selectedRow - rowsCount;
		if (selectedPart >= musicBar.getPartsCount() || selectedPart < 0) {
			return new Position(-1, -1);
		}

		// 计算选中的列
		MusicPart musicPart = musicBar.getPart(selectedPart);
		final int offsetPerNote = this.calculateOffsetPerNote(musicPart, musicBar.getBarWidth());
		final int xRelative = x - xCount - this.width; // 去除掉之前小节和本小节线后剩余宽度
		int selectedColumn = 0;
		if (xRelative < 0 || musicPart.getNotesCount() == 0) {
			// 如果选中小节线本身，则xRelative是负的
			// 或者，如果本声部没有内容，也认为选择小节线
			selectedColumn = columnsCount;
		} else if (musicPart.getNotesCount() == 1) {
			// 在音符只有1个的情况下，其宽度不再是offsetPerNote + this.width
			// 不过，在选中声部只有一个音符并且xRelative>=0的情况下，可以合理推断出用户其实希望选择唯一的那个音符
			selectedColumn = columnsCount + 1;
		} else {
			/*
			 * 除去第一个和最后一个音符，每一个音符都是offsetPerNote + this.width宽，因此在这里补足首尾的宽度。
			 * 同时，被补足的那部分不会影响代码的整体逻辑 因为前面的代码已经排除了选中这些区域时执行到此处的可能
			 */
			selectedColumn = columnsCount + 1 + (xRelative + offsetPerNote / 2) / (offsetPerNote + this.width);
		}

		return new Position(selectedRow, selectedColumn);
	}

	/*
	 * 控制音乐编辑用方法
	 */

	// 暂无

	public static final class Position {
		private final int row;
		private final int column;

		public Position(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public int getRow() {
			return this.row;
		}

		public int getColumn() {
			return this.column;
		}
	}

	public static final class MusicalPosition {
		private final int line;
		private final int bar;
		private final int part;
		private final int note;

		public MusicalPosition(int line, int bar, int part, int note) {
			this.line = line;
			this.bar = bar;
			this.part = part;
			this.note = note;
		}

		public int getLine() {
			return this.line;
		}

		public int getBar() {
			return this.bar;
		}

		public int getPart() {
			return this.part;
		}

		public int getNote() {
			return this.note;
		}
	}
}
