package priv.louisnicolasdavout.tapeeditor.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

public class Main extends JFrame {

	private static final long serialVersionUID = 3506028077116953087L;

	private JPanel panel;
	private Music music;
	private MusicEditor musicEditor;
	private int selectedNoteRow = -1;
	private int selectedNoteColumn = -1;
	private int lineStart = 0;
	private JTextField promptLine;
	private FileFilter serFileFilter;
	private FileFilter xmlFileFilter;
	private JTextField toneMarkTextField;
	private JTextField barDurationTextField;
	private MusicToDOM musicToDOM;

	public Main() {
		final Container container = this.getContentPane();

		this.musicEditor = new MusicEditor(0, 0, Settings.UNIT_WIDTH, Settings.UNIT_HEIGHT);
		this.musicToDOM = new MusicToDOM();

		// 文件过滤设置
		this.serFileFilter = new FileFilter() {

			@Override
			public String getDescription() {
				return "序列化文件(.tape.ser)";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".tape.ser");
			}
		};
		this.xmlFileFilter = new FileFilter() {

			@Override
			public String getDescription() {
				return "XML文件(.tape.xml)";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".tape.xml");
			}
		};

		// 主窗口设置
		this.setTitle("纸带编辑器");
		this.setLayout(new BorderLayout());
		this.setSize(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 提示栏设置
		this.promptLine = new JTextField("( ^ v ^ )");
		this.promptLine.setEditable(false);
		container.add(this.promptLine, BorderLayout.SOUTH);

		// 上方面板设置
		JPanel topPanel = new JPanel();
		BoxLayout topPanelLayout = new BoxLayout(topPanel, BoxLayout.X_AXIS);
		topPanel.setLayout(topPanelLayout);
		topPanel.add(new JLabel("1="));
		this.toneMarkTextField = new JTextField("C3");
		this.barDurationTextField = new JTextField("4");
		topPanel.add(this.toneMarkTextField);
		topPanel.add(this.barDurationTextField);
		topPanel.add(new JLabel("/4"));
		JButton lastLineButton = new JButton("上一行");
		lastLineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.lineStart >= 1) {
					Main.this.lineStart--;
				}
				Main.this.repaint();
			}
		});
		topPanel.add(lastLineButton);

		JButton nextLineButton = new JButton("下一行");
		nextLineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.lineStart < Main.this.music.getLinesCount() - 1) {
					Main.this.lineStart++;
				}
				Main.this.repaint();
			}
		});
		topPanel.add(nextLineButton);

		container.add(topPanel, BorderLayout.NORTH);

		// 菜单栏设置
		JMenuBar jMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("文件");
		JMenu erasureMenu = new JMenu("删除");
		JMenuItem openFromSerial = new JMenuItem("从文件中解序列化");
		openFromSerial.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Main.this.getFromSerFile();
				// 由于新打开文件在选中位置处可能没有音符，因此要取消选中
				Main.this.selectedNoteRow = -1;
				Main.this.selectedNoteColumn = -1;
				Main.this.repaint();
			}
		});
		JMenuItem saveAsXml = new JMenuItem("保存为文本");
		saveAsXml.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Main.this.saveAsXml();
			}
		});
		JMenuItem saveAsSerial = new JMenuItem("保存为序列化对象");
		saveAsSerial.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Main.this.saveAsSerializedObject();
			}
		});
		JMenuItem deleteLine = new JMenuItem("删除行");
		deleteLine.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow >= 0 && Main.this.selectedNoteColumn >= 0) { // 保证选中的是音符或小节线
					try {
						MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
								Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
						int line = musicalPosition.getLine();
						Main.this.music.deleteLine(line);
					} catch (MusicErasureException ex) {
						Main.this.prompt(ex.getMessage());
					}

					// 必须取消选中，否则重新排版时选中的音符发生变化甚至产生没有音符的情况
					Main.this.selectedNoteRow = -1;
					Main.this.selectedNoteColumn = -1;
				}
				Main.this.repaint();
			}
		});
		JMenuItem deleteBar = new JMenuItem("删除小节");
		deleteBar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow >= 0 && Main.this.selectedNoteColumn >= 0) { // 保证选中的是音符或小节线
					try {
						MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
								Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
						int line = musicalPosition.getLine();
						MusicLine musicLine = Main.this.music.getLine(line);
						int bar = musicalPosition.getBar();
						musicLine.deleteBar(bar);
					} catch (MusicErasureException ex) {
						Main.this.prompt(ex.getMessage());
					}

					// 必须取消选中，否则重新排版时选中的音符发生变化甚至产生没有音符的情况
					Main.this.selectedNoteRow = -1;
					Main.this.selectedNoteColumn = -1;
				}
				Main.this.repaint();
			}
		});
		JMenuItem deletePart = new JMenuItem("删除声部");
		deletePart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow >= 0 && Main.this.selectedNoteColumn >= 0) { // 保证选中的是音符或小节线
					try {
						MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
								Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
						int line = musicalPosition.getLine();
						MusicLine musicLine = Main.this.music.getLine(line);
						int bar = musicalPosition.getBar();
						MusicBar musicBar = musicLine.getBar(bar);
						int part = musicalPosition.getPart();
						int note = musicalPosition.getNote();
						if (note >= 0) { // 保证选择的是音符
							musicBar.deletePart(part);
						}
					} catch (MusicErasureException ex) {
						Main.this.prompt(ex.getMessage());
					}

					// 必须取消选中，否则重新排版时选中的音符发生变化甚至产生没有音符的情况
					Main.this.selectedNoteRow = -1;
					Main.this.selectedNoteColumn = -1;
				}
				Main.this.repaint();
			}
		});
		JMenuItem deleteNote = new JMenuItem("删除音符");
		deleteNote.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow >= 0 && Main.this.selectedNoteColumn >= 0) { // 保证选中的是音符或小节线
					try {
						MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
								Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
						int line = musicalPosition.getLine();
						MusicLine musicLine = Main.this.music.getLine(line);
						int bar = musicalPosition.getBar();
						MusicBar musicBar = musicLine.getBar(bar);
						int part = musicalPosition.getPart();
						MusicPart musicPart = musicBar.getPart(part);
						int note = musicalPosition.getNote();
						if (note >= 0) { // 保证选择的是音符
							musicPart.deleteNote(note);
						}
					} catch (MusicErasureException ex) {
						Main.this.prompt(ex.getMessage());
					}

					// 必须取消选中，否则重新排版时选中的音符发生变化甚至产生没有音符的情况
					Main.this.selectedNoteRow = -1;
					Main.this.selectedNoteColumn = -1;
				}
				Main.this.repaint();
			}
		});
		fileMenu.add(openFromSerial);
		fileMenu.add(saveAsXml);
		fileMenu.add(saveAsSerial);
		erasureMenu.add(deleteLine);
		erasureMenu.add(deleteBar);
		erasureMenu.add(deletePart);
		erasureMenu.add(deleteNote);
		jMenuBar.add(fileMenu);
		jMenuBar.add(erasureMenu);
		this.setJMenuBar(jMenuBar);

		// 音符添加按钮设置
		JPanel leftPanel = new JPanel();
		BoxLayout leftPanelLayout = new BoxLayout(leftPanel, BoxLayout.Y_AXIS);
		leftPanel.setLayout(leftPanelLayout);
		for (Pitch pitch : Pitch.values()) {
			JButton button = new JButton(pitch.getTitle());
			button.addActionListener(this.new PitchButtonListener(pitch));
			leftPanel.add(button);
		}
		JScrollPane jScrollPane = new JScrollPane(leftPanel);
		container.add(jScrollPane, BorderLayout.WEST);

		// 右侧按钮面板设置
		JPanel rightPanel = new JPanel();
		BoxLayout rightPanelLayout = new BoxLayout(rightPanel, BoxLayout.Y_AXIS);
		rightPanel.setLayout(rightPanelLayout);

		// 新建行按钮
		JButton newLineButton = new JButton("创建行");
		newLineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 没有选中时在末尾插入行
					Main.this.music.makeLine();
				} else { // 否则在上方插入
					MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
							Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
					int line = musicalPosition.getLine();
					Main.this.music.makeLine(line);

					// 必须取消选中，否则重新排版时选中的音符发生变化甚至产生没有音符的情况
					Main.this.selectedNoteRow = -1;
					Main.this.selectedNoteColumn = -1;
				}
				Main.this.repaint();
			}
		});
		rightPanel.add(newLineButton);

		// 新建小节按钮
		JButton newBarButton = new JButton("创建小节");
		newBarButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 没有选中时在末尾插入
					MusicLine musicLine = Main.this.music.getLine(Main.this.music.getLinesCount() - 1);
					musicLine.makeBar();
				} else { // 否则在本行插入
					MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
							Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
					int line = musicalPosition.getLine();
					int bar = musicalPosition.getBar();
					int note = musicalPosition.getNote();
					if (note < 0) { // 如果选中小节线，在前方插入小节并取消选中
						MusicLine musicLine = Main.this.music.getLine(line);
						musicLine.makeBar(bar);
						Main.this.selectedNoteRow = -1;
						Main.this.selectedNoteColumn = -1;
					} else { // 否则在后方插入且不取消选中
						MusicLine musicLine = Main.this.music.getLine(line);
						musicLine.makeBar(bar + 1);
					}
				}
				Main.this.repaint();
			}
		});
		rightPanel.add(newBarButton);

		// 新建声部按钮
		JButton newPartButton = new JButton("创建声部");
		newPartButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 没有选中时在末尾插入
					MusicLine musicLine = Main.this.music.getLine(Main.this.music.getLinesCount() - 1);
					MusicBar musicBar = musicLine.getBar(musicLine.getBarsCount() - 1);
					musicBar.makePart();
				} else { // 否则在本小节插入
					MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
							Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
					int line = musicalPosition.getLine();
					int bar = musicalPosition.getBar();
					int part = musicalPosition.getPart();
					int note = musicalPosition.getNote();
					MusicBar musicBar = Main.this.music.getLine(line).getBar(bar);
					if (note >= 0) { // 选中音符，在下方插入
						musicBar.makePart(part + 1);
					} else { // 选中小节线，开始处插入，由于选中的是小节线，重新绘制后即使不清空选中对象，也仅仅导致选中小节线的上一段，仍能保证选中的是同一小节线，因此不必清空选中。
						musicBar.makePart(0);
					}
				}
				Main.this.repaint();
			}
		});
		rightPanel.add(newPartButton);

		// 半音按钮
		JButton sharpButton = new JButton("半音");
		sharpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 未选中时，改变最后一个音符半音
					MusicLine musicLine = Main.this.music.getLine(Main.this.music.getLinesCount() - 1);
					MusicBar musicBar = musicLine.getBar(musicLine.getBarsCount() - 1);
					MusicPart musicPart = musicBar.getPart(musicBar.getPartsCount() - 1);
					if (musicPart.getNotesCount() > 0) { // 确保真的有音符
						MusicNote musicNote = musicPart.getNote(musicPart.getNotesCount() - 1);
						musicNote.setSharp(!musicNote.isSharp());
					}
				} else { // 选中时，改变选中音符半音
					MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
							Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
					int line = musicalPosition.getLine();
					int bar = musicalPosition.getBar();
					int part = musicalPosition.getPart();
					int note = musicalPosition.getNote();
					if (note >= 0) { // 确保选中的是音符
						MusicNote musicNote = Main.this.music.getLine(line).getBar(bar).getPart(part).getNote(note);
						musicNote.setSharp(!musicNote.isSharp());
					}
				}
				Main.this.repaint();
			}
		});
		rightPanel.add(sharpButton);

		// 改变时长按钮
		JButton timeButton = new JButton("时长");
		timeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 未选中时，改变最后一个音符时长
					MusicLine musicLine = Main.this.music.getLine(Main.this.music.getLinesCount() - 1);
					MusicBar musicBar = musicLine.getBar(musicLine.getBarsCount() - 1);
					MusicPart musicPart = musicBar.getPart(musicBar.getPartsCount() - 1);
					if (musicPart.getNotesCount() > 0) { // 确保真的有音符
						MusicNote musicNote = musicPart.getNote(musicPart.getNotesCount() - 1);
						this.changeTime(musicNote);
					}
				} else { // 选中时，改变选中音符时长
					MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
							Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
					int line = musicalPosition.getLine();
					int bar = musicalPosition.getBar();
					int part = musicalPosition.getPart();
					int note = musicalPosition.getNote();
					if (note >= 0) { // 确保选中的是音符
						MusicNote musicNote = Main.this.music.getLine(line).getBar(bar).getPart(part).getNote(note);
						this.changeTime(musicNote);
					}
				}
				Main.this.repaint();
			}

			private void changeTime(MusicNote musicNote) {
				/*
				 * 音符的四种时长是0.25、0.50、0.75、1.00 即两线、一线、一线加符点和无符号
				 */
				Underline[] underlineStatus = new Underline[] { Underline.DOUBLE, Underline.SINGLE, Underline.SINGLE,
						Underline.NONE };
				boolean[] dotStatus = new boolean[] { false, false, true, false };
				int[][] transitionMatrix = new int[][] {
						// NONE, SINGLE, DOUBLE
						new int[] { 0, 2, 1 }, // false
						new int[] { 3, 3, 3 } // true
				};
				boolean dot = musicNote.isDot();
				Underline underline = musicNote.getUnderline();
				int newStat = transitionMatrix[dot ? 1 : 0][underline.ordinal()];
				musicNote.setDot(dotStatus[newStat]);
				musicNote.setUnderline(underlineStatus[newStat]);
			}
		});
		rightPanel.add(timeButton);

		JButton silentButton = new JButton("静音");
		silentButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 未选中时，改变最后一个音符时长
					MusicLine musicLine = Main.this.music.getLine(Main.this.music.getLinesCount() - 1);
					MusicBar musicBar = musicLine.getBar(musicLine.getBarsCount() - 1);
					MusicPart musicPart = musicBar.getPart(musicBar.getPartsCount() - 1);
					if (musicPart.getNotesCount() > 0) { // 确保真的有音符
						MusicNote musicNote = musicPart.getNote(musicPart.getNotesCount() - 1);
						musicNote.setSilent(!musicNote.isSilent());
					}
				} else { // 选中时，改变选中音符时长
					MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
							Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
					int line = musicalPosition.getLine();
					int bar = musicalPosition.getBar();
					int part = musicalPosition.getPart();
					int note = musicalPosition.getNote();
					if (note >= 0) { // 确保选中的是音符
						MusicNote musicNote = Main.this.music.getLine(line).getBar(bar).getPart(part).getNote(note);
						musicNote.setSilent(!musicNote.isSilent());
					}
				}
				Main.this.repaint();
			}
		});
		rightPanel.add(silentButton);

		container.add(rightPanel, BorderLayout.EAST);

		// 加载乐谱显示面板
		this.panel = this.new MainPanel();
		this.panel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				MusicEditor.Position position = Main.this.musicEditor.getSelectedPosition(Main.this.music,
						Main.this.lineStart, e.getX(), e.getY());
				Main.this.selectedNoteRow = position.getRow();
				Main.this.selectedNoteColumn = position.getColumn();
				Main.this.repaint();
			}
		});
		container.add(this.panel, BorderLayout.CENTER);

		// 初始化音乐对象
		this.music = new Music();

	}

	public static void main(String[] args) {
		Main m = new Main();
		m.setVisible(true);
	}

	private void prompt(String prompt) {
		this.promptLine.setText(prompt);
	}

	private void saveAsSerializedObject() {
		JFileChooser fileSave = new JFileChooser();
		fileSave.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSave.setFileFilter(this.serFileFilter);
		fileSave.showSaveDialog(this);
		File saveFile = fileSave.getSelectedFile();
		if (saveFile == null) {
			return;
		}
		try (FileOutputStream fileStream = new FileOutputStream(saveFile);
				ObjectOutputStream os = new ObjectOutputStream(fileStream)) {
			os.writeObject(this.music);
		} catch (Exception e) {
			this.prompt(e.getMessage());
		}
	}

	private void getFromSerFile() {
		JFileChooser fileOpen = new JFileChooser();
		fileOpen.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileOpen.setFileFilter(this.serFileFilter);
		fileOpen.showOpenDialog(this);
		File openFile = fileOpen.getSelectedFile();
		if (openFile == null) {
			return;
		}
		try (FileInputStream fileStream = new FileInputStream(openFile);
				ObjectInputStream os = new ObjectInputStream(fileStream)) {
			this.music = (Music) os.readObject();
		} catch (Exception e) {
			this.prompt(e.getMessage());
		}
	}

	private void saveAsXml() {
		JFileChooser fileSave = new JFileChooser();
		fileSave.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSave.setFileFilter(this.xmlFileFilter);
		fileSave.showSaveDialog(this);
		File saveFile = fileSave.getSelectedFile();
		if (saveFile == null) {
			return;
		}

		try {
			Document document = this.musicToDOM.parseMusic(this.music, this.getToneMark(), this.getBarDuration());
			this.musicToDOM.writeXML(document, saveFile);
		} catch (IOException | TransformerException | ParserConfigurationException e) {
			this.prompt(e.getMessage());
		}
	}

	private String getToneMark() {
		return this.toneMarkTextField.getText();
	}

	private String getBarDuration() {
		return this.barDurationTextField.getText();
	}

	private class MainPanel extends JPanel {

		private static final long serialVersionUID = -4071010648059043361L;

		@Override
		public void paintComponent(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			Main.this.musicEditor.setPanelSize(this.getWidth(), this.getHeight()); // 更新可能因为拖动发生了变化的面板尺寸，不出意外的话，每次拖动都会自动调用此方法，因此不必在别处调用
			Main.this.musicEditor.drawMusic(g2, music, Main.this.lineStart, Main.this.selectedNoteRow,
					Main.this.selectedNoteColumn);
		}

	}

	private class PitchButtonListener implements ActionListener {

		private final Pitch pitch;

		public PitchButtonListener(Pitch pitch) {
			this.pitch = pitch;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (Main.this.selectedNoteRow < 0 || Main.this.selectedNoteColumn < 0) { // 未选中任何音符或小节线，在整首音乐末尾添加
				int currentLineIndex = Main.this.music.getLinesCount() - 1;
				int currentBarIndex = Main.this.music.getLine(currentLineIndex).getBarsCount() - 1;
				int currentPartIndex = Main.this.music.getLine(currentLineIndex).getBar(currentBarIndex).getPartsCount()
						- 1;
				Main.this.music.getLine(currentLineIndex).getBar(currentBarIndex).getPart(currentPartIndex)
						.makeNote(this.pitch, Underline.NONE, false, false, false);
			} else { // 在选中音符之后添加
				MusicEditor.MusicalPosition musicalPosition = Main.this.musicEditor.getSelectedMusicalPosition(
						Main.this.music, Main.this.selectedNoteRow, Main.this.selectedNoteColumn);
				int line = musicalPosition.getLine();
				int bar = musicalPosition.getBar();
				int part = musicalPosition.getPart();
				int note = musicalPosition.getNote();
				MusicBar musicBar = Main.this.music.getLine(line).getBar(bar);
				if (note >= 0) { // 必须保证选中的不是小节线
					musicBar.getPart(part).makeNote(note + 1, this.pitch, Underline.NONE, false, false, false);
					Main.this.selectedNoteColumn++; // 切换至下一音符
				} else { // 如果果真选中了小节线，将会在该小节第一个空的声部或最后一个声部末尾添加音符
					for (part = 0; part < musicBar.getPartsCount() - 1; part++) {
						if (musicBar.getPart(part).getNotesCount() == 0) {
							break;
						}
					}
					musicBar.getPart(part).makeNote(pitch, Underline.NONE, false, false, false);
				}
			}
			Main.this.repaint();
		}
	}
}
