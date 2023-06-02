package priv.louisnicolasdavout.tapeeditor.main;

enum Underline {
	NONE(1.0), SINGLE(0.5), DOUBLE(0.25);

	private double duration;

	private Underline(double duration) {
		this.duration = duration;
	}

	public double getDuration() {
		return this.duration;
	}
}
