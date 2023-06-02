package priv.louisnicolasdavout.tapeeditor.main;

// 每一个Pitch代表一个音高
enum Pitch {
	REST("0", false, Octave.BARITONE),

	DOUBLE_TREBLE_SI("7", false, Octave.DOUBLE_TREBLE), DOUBLE_TREBLE_LA("6", true, Octave.DOUBLE_TREBLE),
	DOUBLE_TREBLE_SO("5", true, Octave.DOUBLE_TREBLE), DOUBLE_TREBLE_FA("4", true, Octave.DOUBLE_TREBLE),
	DOUBLE_TREBLE_MI("3", false, Octave.DOUBLE_TREBLE), DOUBLE_TREBLE_RE("2", true, Octave.DOUBLE_TREBLE),
	DOUBLE_TREBLE_DO("1", true, Octave.DOUBLE_TREBLE),

	TREBLE_SI("7", false, Octave.TREBLE), TREBLE_LA("6", true, Octave.TREBLE), TREBLE_SO("5", true, Octave.TREBLE),
	TREBLE_FA("4", true, Octave.TREBLE), TREBLE_MI("3", false, Octave.TREBLE), TREBLE_RE("2", true, Octave.TREBLE),
	TREBLE_DO("1", true, Octave.TREBLE),

	SI("7", false, Octave.BARITONE), LA("6", true, Octave.BARITONE), SO("5", true, Octave.BARITONE),
	FA("4", true, Octave.BARITONE), MI("3", false, Octave.BARITONE), RE("2", true, Octave.BARITONE),
	DO("1", true, Octave.BARITONE),

	BASS_SI("7", false, Octave.BASS), BASS_LA("6", true, Octave.BASS), BASS_SO("5", true, Octave.BASS),
	BASS_FA("4", true, Octave.BASS), BASS_MI("3", false, Octave.BASS), BASS_RE("2", true, Octave.BASS),
	BASS_DO("1", true, Octave.BASS),

	DOUBLE_BASS_SI("7", false, Octave.DOUBLE_BASS), DOUBLE_BASS_LA("6", true, Octave.DOUBLE_BASS),
	DOUBLE_BASS_SO("5", true, Octave.DOUBLE_BASS), DOUBLE_BASS_FA("4", true, Octave.DOUBLE_BASS),
	DOUBLE_BASS_MI("3", false, Octave.DOUBLE_BASS), DOUBLE_BASS_RE("2", true, Octave.DOUBLE_BASS),
	DOUBLE_BASS_DO("1", true, Octave.DOUBLE_BASS);

	private String note; // 音高在简谱中的数字
	private String title; // 音高在导出XML时的记号
	private boolean matchingSharpPitch; // 是否可以升半音（而不成为另外的全音）
	private Octave octave;

	private Pitch(String note, boolean hasMatchingSharpPitch, Octave octave) {
		this.note = note;
		this.matchingSharpPitch = hasMatchingSharpPitch;
		this.octave = octave;
		this.title = note + this.octave.getOcatveNote(); // 这里用加法拼接字符串，但考虑到对于每个枚举对象仅调用一次，其开销其实很小
	}

	public String getNote() {
		return this.note;
	}

	public String getTitle() {
		return this.title;
	}

	public boolean hasMatchingSharpPitch() {
		return matchingSharpPitch;
	}

	public Octave getOctave() {
		return this.octave;
	}
}
