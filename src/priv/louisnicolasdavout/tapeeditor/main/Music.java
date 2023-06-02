package priv.louisnicolasdavout.tapeeditor.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Music implements Serializable {

	private static final long serialVersionUID = 4933182347110480673L;

	private List<MusicLine> lines;

	// 新建乐谱时，自动创建第一行
	public Music() {
		this.lines = new ArrayList<MusicLine>();
		this.lines.add(new MusicLine());
	}

	public MusicLine getLine(int index) {
		return this.lines.get(index);
	}

	public int getLinesCount() {
		return this.lines.size();
	}

	public void makeLine() {
		this.lines.add(new MusicLine());
	}

	public void makeLine(int index) {
		this.lines.add(index, new MusicLine());
	}

	public void deleteLine(int index) {
		if (this.lines.size() <= 1) {
			throw new MusicLineErasureException("音乐中应至少包括一行");
		}
		this.lines.remove(index);
	}
}
