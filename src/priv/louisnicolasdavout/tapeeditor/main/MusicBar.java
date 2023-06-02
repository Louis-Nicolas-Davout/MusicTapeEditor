package priv.louisnicolasdavout.tapeeditor.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class MusicBar implements Serializable{

	private static final long serialVersionUID = -9210765443104588557L;
	
	private List<MusicPart> parts;

	// 在新建小节时，会自动新建一个没有音符的声部，以保证其行高至少为1
	public MusicBar() {
		this.parts = new ArrayList<MusicPart>();
		this.parts.add(new MusicPart());
	}

	public MusicPart getPart(int index) {
		return this.parts.get(index);
	}

	public int getPartsCount() {
		return this.parts.size();
	}

	public void makePart() {
		this.parts.add(new MusicPart());
	}

	public void makePart(int index) {
		this.parts.add(index, new MusicPart());
	}

	public void deletePart(int index) {
		if (this.parts.size() <= 1) {
			throw new MusicPartErasureException("每个小节应至少拥有一个声部");
		}
		this.parts.remove(index);
	}

	public int getBarWidth() {
		int maxPartWidth = 0;
		for (int i = 0; i < this.parts.size(); i++) {
			int width = this.parts.get(i).getNotesCount();
			if (width > maxPartWidth) {
				maxPartWidth = width;
			}
		}
		return maxPartWidth + 1; // 要包括小节线的宽度
	}
}
