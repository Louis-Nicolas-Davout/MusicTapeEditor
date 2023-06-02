package priv.louisnicolasdavout.tapeeditor.main;

// 每一个Octave代表一个8度音程
enum Octave {
	DOUBLE_BASS(-2, ".."), BASS(-1, "."), BARITONE(0, ""), TREBLE(1, "^"), DOUBLE_TREBLE(2, "^^");

	private int octave; // 高于或低于中音的八度音程数目。
	private String ocatveNote; // 在导出XML中的记号

	private Octave(int octave, String octaveNote) {
		this.octave = octave;
		this.ocatveNote = octaveNote;
	}

	public int getOctave() {
		return this.octave;
	}
	
	public String getOcatveNote() {
		return ocatveNote;
	}
}
