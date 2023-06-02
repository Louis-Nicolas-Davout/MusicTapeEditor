package priv.louisnicolasdavout.tapeeditor.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class MusicPart implements Serializable{

	private static final long serialVersionUID = 7175541091139996482L;
	
	private List<MusicNote> notes;

	public MusicPart() {
		this.notes = new ArrayList<MusicNote>();
	}

	public MusicNote getNote(int index) {
		return this.notes.get(index);
	}

	public int getNotesCount() {
		return this.notes.size();
	}

	// 要保证每一个位置都是一个全新的音符，因为在编辑的过程中可能改变其属性，同时不希望任何其他位置的音符发生改动
	public void makeNote(Pitch pitch, Underline underline, boolean dot, boolean sharp, boolean silent) {
		this.notes.add(new MusicNote(pitch, underline, dot, sharp, silent));
	}

	// 要保证每一个位置都是一个全新的音符，因为在编辑的过程中可能改变其属性，同时不希望任何其他位置的音符发生改动
	public void makeNote(int index, Pitch pitch, Underline underline, boolean dot, boolean sharp, boolean silent) {
		this.notes.add(index, new MusicNote(pitch, underline, dot, sharp, silent));
	}

	public void deleteNote(int index) {
		if (this.notes.size() == 0) {
			throw new MusicNoteErasureException("无法对空的声部进行删除操作");
		}
		this.notes.remove(index);
	}
}
