package priv.louisnicolasdavout.tapeeditor.main;

import java.io.Serializable;

class MusicNote implements Serializable {

	private static final long serialVersionUID = 3435870402383422145L;
	
	private Pitch pitch;
	private Underline underline;
	private boolean dot;
	private boolean sharp;
	private boolean silent;

	public MusicNote(Pitch pitch, Underline underline, boolean dot, boolean sharp, boolean silent) {
		this.pitch = pitch;
		this.underline = underline;
		this.dot = dot;
		this.sharp = this.pitch.hasMatchingSharpPitch() && sharp;
		this.silent = silent;
	}

	public Pitch getPitch() {
		return pitch;
	}

	public void setPitch(Pitch pitch) {
		this.pitch = pitch;
	}

	public Underline getUnderline() {
		return underline;
	}

	public void setUnderline(Underline underline) {
		this.underline = underline;
	}

	public boolean isDot() {
		return dot;
	}

	public void setDot(boolean dot) {
		this.dot = dot;
	}

	public boolean isSharp() {
		return sharp;
	}

	public void setSharp(boolean sharp) {
		this.sharp = this.pitch.hasMatchingSharpPitch() && sharp;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isSilent() {
		return this.silent;
	}

	public double getDuration() {
		return (this.dot ? 1.5 : 1.0) * this.underline.getDuration();
	}
}
