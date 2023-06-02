package priv.louisnicolasdavout.tapeeditor.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class MusicLine implements Serializable{

	private static final long serialVersionUID = -692659852694703012L;
	
	private List<MusicBar> bars;

	// 在新建行时，会自动新建包含一个无音符声部的小节，以保证其行高至少为1
	public MusicLine() {
		this.bars = new ArrayList<MusicBar>();
		this.bars.add(new MusicBar());
	}

	public MusicBar getBar(int index) {
		return this.bars.get(index);
	}

	public int getBarsCount() {
		return this.bars.size();
	}

	public void makeBar() {
		this.bars.add(new MusicBar());
	}

	public void makeBar(int index) {
		this.bars.add(index, new MusicBar());
	}

	public void deleteBar(int index) {
		if (this.bars.size() <= 1) {
			throw new MusicBarErasureException("每一行至少应包括一个小节");
		}
		this.bars.remove(index);
	}

	public int getLineHeight() {
		int maxHeight = 1; // Music类的实现方式确保最小的行高为1
		for (int i = 0; i < this.bars.size(); i++) {
			int height = this.bars.get(i).getPartsCount();
			if (height > maxHeight) {
				maxHeight = height;
			}
		}
		return maxHeight;
	}
}
