package com.demo;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: Steph
 * @date: 2018/10/31-21:00
 */
public class Notepad extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L; // 序列化
	private static int windowsNum = 0; // 定义窗口个数，可以控制窗口的打开和关闭
	private JPanel panelEnd; // 页脚的panel
	private static boolean isSaved = true; // 判断文件是否保存
	private UndoManager undomanager = new UndoManager(); // 用于撤销和重做功能
	private File file = null;

	// 下拉菜单按钮
	private JMenuItem jmiCreate, jmiOpen, jmiSave, jmiSaveAs, jmiExit, jmiCut, jmiPaste, jmiCopy, jmiFind, jmiReplace,
			jmiSelectAll, jmiInsertTime, jmiUndo, jmiRedo, jmiAutoEnter, jmiFont, jmiStatus, jmiTopic, jmiAbout;

	private JTextArea ta = new JTextArea(); // 文本区
	private JLabel labelTips, labelLine, labelColumn, labelNum; // 标签
	private JFileChooser chooser = new JFileChooser(); // 文件选择器
	private boolean ture;
	// 右键菜单
	private JPopupMenu jp = new JPopupMenu();

	// 右键按钮
	private JMenuItem jbackout = new JMenuItem("撤销(U)");
	private JMenuItem jredo = new JMenuItem("重做(R)");
	private JMenuItem jcut = new JMenuItem("剪切(T)");
	private JMenuItem jcopy = new JMenuItem("复制(C)");
	private JMenuItem jpaste = new JMenuItem("粘贴(P)");
	private JMenuItem jselectall = new JMenuItem("全选(A)");

	public Notepad() {
		// TODO Auto-generated constructor stub
		// 设置窗口大小
		setSize(800, 600);
		// 设置窗体的标题
		String titlestr = "无标题-记事本";
		setTitle(titlestr);
		// 设置窗口的关闭时的响应，此处设置的是释放本窗口资源但不出程序
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// 设置窗口位置。null居中
		setLocationRelativeTo(null);
		// 为chooser添加文件过滤器
		setFileFilter();
		setJMenuBar(createJMenuBar());// 菜单
		rightKey(); // 右鍵
		ta.setFont(new Font("", Font.PLAIN, 20)); // 文本字体大小
		add(new JScrollPane(ta), BorderLayout.CENTER);// 文本框，位置为中部
		add(createPanel(), BorderLayout.PAGE_END);// 页脚，位置为底部
		// 设置窗口可见
		setVisible(true);
		// 为JFrame添加组件事件监听器
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				int width = (int) (Notepad.this.getWidth() * 0.67);
				labelTips.setPreferredSize(new Dimension(width, 20));
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});
		// 为JFrame添加窗口状态时间监听器
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				windowsNum++;
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				/*
				 * JDialog jclosing=new JDialog(); jclosing.setPreferredSize(new Dimension(400,
				 * 200)); jclosing.setTitle("提示"); jclosing.setLocationRelativeTo(null);
				 */
				windowsNum--;
				if (windowsNum == 0) {
					DIYquit();
				}

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});
		// 为文本编辑器添加DocumentListener事件监听器
		ta.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				taChange();
				// System.out.println("remove");
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				taChange();
				// System.out.println("insert");
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				// 只有改变属性时才会通知
				// taChange();
				// System.out.println("change");
			}
		});
		ta.getDocument().addUndoableEditListener(new UndoableEditListener() {

			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				// TODO Auto-generated method stub
				undomanager.addEdit(e.getEdit());
				changeEnable();
			}
		});
		ta.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				// TODO Auto-generated method stub
				try {
					int pos = ta.getCaretPosition();
					// 获取行数
					int line = ta.getLineOfOffset(pos) + 1;
					// 获取列数
					int col = pos - ta.getLineStartOffset(line - 1) + 1;
					labelLine.setText("行数：" + line);
					labelColumn.setText("列数：" + col);
					labelNum.setText("字数：" + ta.getText().length());
				} catch (Exception ex) {
					labelTips.setText("无法获得当前光标位置 ");
				}
			}
		});
	}

	// 改变撤销重做的状态
	private void changeEnable() {
		jmiUndo.setEnabled(undomanager.canUndo()); // 菜单设置可选/不可选
		jmiRedo.setEnabled(undomanager.canRedo());
		jbackout.setEnabled(undomanager.canUndo());
		jredo.setEnabled(undomanager.canRedo());
	}

	// 设置标题的状态为未保存，前面加*
	private void taChange() {
		// TODO Auto-generated method stub
		isSaved = false;
		String title = this.getTitle();
		if (!title.startsWith("*")) { // 头标题状态
			title = "*" + title;
		}
		this.setTitle(title);
	}

	// 设置文件过滤器
	private void setFileFilter() {
		// TODO Auto-generated method stub
		javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return ".txt";
			}

			@Override
			public boolean accept(File f) {
				// TODO Auto-generated method stub
				String name = f.getName();
				return f.isDirectory() || name.toLowerCase().endsWith(".txt"); // 仅显示目录和txt文件
				// ;
			}
		};
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);
	}

	// 创建(底部状态栏)部分
	private JPanel createPanel() {
		// TODO Auto-generated method stub
		panelEnd = new JPanel();
		panelEnd.setPreferredSize(new Dimension(800, 25)); // 宽800，高25
		panelEnd.setBackground(Color.getColor("rgb(194, 255, 170)")); // 背景颜色
		panelEnd.setLayout(new FlowLayout(FlowLayout.LEFT)); // 流布局，浮于左侧
		panelEnd.add(labelTips = new JLabel("按F1以获取帮助"));
		panelEnd.add(labelLine = new JLabel("行数：1"));
		panelEnd.add(labelColumn = new JLabel("列数：1"));
		panelEnd.add(labelNum = new JLabel("字数：0"));
		labelLine.setVisible(true);
		labelColumn.setVisible(true);
		labelTips.setPreferredSize(new Dimension(500, 20));

		return panelEnd;
	}

	// 创建JMenuBar（顶部菜单栏）
	private JMenuBar createJMenuBar() {
		// TODO Auto-generated method stub
		JMenuBar menu = new JMenuBar();
		// 设置JMenuBar的大小
		menu.setPreferredSize(new Dimension(800, 25));
		// 创建菜单
		JMenu menuFile = new JMenu("文件(F)"), menuEdit = new JMenu("编辑(E)"), menuFormat = new JMenu("格式(O)"),
				menuView = new JMenu("查看(V)"), menuHelp = new JMenu("帮助(H)");
		// 设置按钮字体大小
		menuFile.setFont(new Font("", Font.BOLD, 14));
		menuEdit.setFont(new Font("", Font.BOLD, 14));
		menuFormat.setFont(new Font("", Font.BOLD, 14));
		menuView.setFont(new Font("", Font.BOLD, 14));
		menuHelp.setFont(new Font("", Font.BOLD, 14));
		// 设置热键，需使用alt+所设定的字母以选中。
		menuFile.setMnemonic('F');
		menuEdit.setMnemonic('E');
		menuFormat.setMnemonic('O');
		menuView.setMnemonic('V');
		menuHelp.setMnemonic('H');
		// 给文件菜单添加具体选项
		menuFile.add(jmiCreate = new JMenuItem("新建(N)"));
		jmiCreate.setMnemonic('N');
		jmiCreate.setFont(new Font("", Font.BOLD, 14));
		jmiCreate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuFile.add(jmiOpen = new JMenuItem("打开(O)"));
		jmiOpen.setMnemonic('O');
		jmiOpen.setFont(new Font("", Font.BOLD, 14));
		jmiOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuFile.add(jmiSave = new JMenuItem("保存(S)"));
		jmiSave.setMnemonic('S');
		jmiSave.setFont(new Font("", Font.BOLD, 14));
		jmiSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuFile.add(jmiSaveAs = new JMenuItem("另存为(A)"));
		jmiSaveAs.setMnemonic('A');
		jmiSaveAs.setFont(new Font("", Font.BOLD, 14));
		menuFile.addSeparator();
		menuFile.add(jmiExit = new JMenuItem("退出(X)"));
		jmiExit.setMnemonic('X');
		jmiExit.setFont(new Font("", Font.BOLD, 14));
		// 给编辑菜单添加具体选项
		menuEdit.add(jmiUndo = new JMenuItem("撤销(U)"));
		jmiUndo.setMnemonic('U');
		jmiUndo.setFont(new Font("", Font.BOLD, 14));
		jmiUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		menuEdit.add(jmiRedo = new JMenuItem("重做(Y)"));
		jmiRedo.setMnemonic('Y');
		jmiRedo.setFont(new Font("", Font.BOLD, 14));
		jmiRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		changeEnable(); // 改变撤销重做的状态
		menuEdit.addSeparator();
		menuEdit.add(jmiCut = new JMenuItem("剪切(T)"));
		jmiCut.setMnemonic('T');
		jmiCut.setFont(new Font("", Font.BOLD, 14));
		jmiCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		menuEdit.add(jmiCopy = new JMenuItem("复制(C)"));
		jmiCopy.setMnemonic('C');
		jmiCopy.setFont(new Font("", Font.BOLD, 14));
		jmiCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		menuEdit.add(jmiPaste = new JMenuItem("粘贴(P)"));
		jmiPaste.setMnemonic('P');
		jmiPaste.setFont(new Font("", Font.BOLD, 14));
		jmiPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		menuEdit.addSeparator();

		menuEdit.add(jmiFind = new JMenuItem("查找(F)"));
		jmiFind.setMnemonic('F');
		jmiFind.setFont(new Font("", Font.BOLD, 14));
		jmiFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));

		menuEdit.add(jmiReplace = new JMenuItem("替换(G)"));
		jmiReplace.setMnemonic('G');
		jmiReplace.setFont(new Font("", Font.BOLD, 14));
		jmiReplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		menuEdit.addSeparator();
		menuEdit.add(jmiSelectAll = new JMenuItem("全选(A)"));
		jmiSelectAll.setMnemonic('A');
		jmiSelectAll.setFont(new Font("", Font.BOLD, 14));
		jmiSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuEdit.add(jmiInsertTime = new JMenuItem("时间/日期(D)"));
		jmiInsertTime.setMnemonic('D');
		jmiInsertTime.setFont(new Font("", Font.BOLD, 14));
		jmiInsertTime.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		// menuEdit.addSeparator();
		// 格式
		menuFormat.add(jmiAutoEnter = new JCheckBoxMenuItem("自动换行(W)", false));
		jmiAutoEnter.setMnemonic('W');
		jmiAutoEnter.setFont(new Font("", Font.BOLD, 14));
		menuFormat.add(jmiFont = new JMenuItem("字体(F)"));
		jmiFont.setMnemonic('F');
		jmiFont.setFont(new Font("", Font.BOLD, 14));
		// 查看
		menuView.add(jmiStatus = new JCheckBoxMenuItem("状态栏(S)", true));
		jmiStatus.setMnemonic('S');
		jmiStatus.setFont(new Font("", Font.BOLD, 14));
		// 给帮助菜单添加具体选项
		menuHelp.add(jmiTopic = new JMenuItem("查看帮助(H)"));
		jmiTopic.setMnemonic('H');
		jmiTopic.setFont(new Font("", Font.BOLD, 14));
		jmiTopic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuHelp.add(jmiAbout = new JMenuItem("关于(A)"));
		jmiAbout.setMnemonic('A');
		jmiAbout.setFont(new Font("", Font.BOLD, 14));
		// 将菜单添加到JMenuBar中
		menu.add(menuFile);
		menu.add(menuEdit);
		menu.add(menuFormat);
		menu.add(menuView);
		menu.add(menuHelp);
		// 给选项添加ActionEvent
		addActionListener();

		return menu;
	}

	// 右键菜单按钮及功能
	private void rightKey() {

		// 右键按钮添加
		jp.add(jbackout);
		jbackout.setFont(new Font("", Font.BOLD, 14));
		jbackout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		jbackout.setEnabled(undomanager.canUndo());
		jp.add(jredo);
		jredo.setFont(new Font("", Font.BOLD, 14));
		jredo.setEnabled(undomanager.canRedo());
		jredo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		jp.addSeparator();
		jp.add(jcut);
		jcut.setFont(new Font("", Font.BOLD, 14));
		jcut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		jp.add(jcopy);
		jcopy.setFont(new Font("", Font.BOLD, 14));
		jcopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		jp.add(jpaste);
		jpaste.setFont(new Font("", Font.BOLD, 14));
		jpaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		jp.addSeparator();
		jp.add(jselectall);
		jselectall.setFont(new Font("", Font.BOLD, 14));
		jselectall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

		// 右键菜单值撤销的事件
		jbackout.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					// jbackout.setEnabled(undomanager.canUndo());
					undomanager.undo();
					// jbackout.setEnabled(undomanager.canUndo());
				} catch (CannotRedoException cre) {
					cre.printStackTrace();
				}

			}
		});

		// 右键菜单值重做的事件
		jredo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					// jredo.setEnabled(undomanager.canRedo());
					undomanager.redo();
					// jredo.setEnabled(undomanager.canRedo());
				} catch (CannotRedoException cre) {
					cre.printStackTrace();
				}

			}
		});

		// 右键菜单之复制的事件
		jcopy.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ta.copy();
			}

		});

		// 右键菜单之粘贴的事件
		jpaste.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ta.paste();
			}

		});

		// 右键菜单之剪切的事件
		jcut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ta.cut();
			}

		});

		// 右键菜单之全选的事件
		jselectall.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ta.selectAll();
			}

		});

		// 右键菜单之增加鼠标事件
		ta.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					jp.show((Component) (e.getSource()), e.getX(), e.getY());

					String temp = ta.getSelectedText();
					if (temp != null) {
						jcopy.setEnabled(true);
						jcut.setEnabled(true);
					} else if (temp == null) {
						jcopy.setEnabled(false);
						jcut.setEnabled(false);
					}
					String temp1 = ta.getText();
					if (temp1 == null) {
						jselectall.setEnabled(false);
					} else if (temp1 != null) {
						jselectall.setEnabled(true);
					}
				}
			}
		});
	}

	// 给菜单增加事件监听器
	private void addActionListener() {
		// TODO Auto-generated method stub
		// 文件选项
		jmiCreate.addActionListener(this); // 新建
		jmiOpen.addActionListener(this); // 打开
		jmiSave.addActionListener(this); // 保存
		jmiSaveAs.addActionListener(this); // 另存为
		jmiExit.addActionListener(this); // 退出
		// 编辑选项
		jmiUndo.addActionListener(this); // 撤销
		jmiRedo.addActionListener(this); // 重做
		jmiCopy.addActionListener(this); // 复制
		jmiPaste.addActionListener(this); // 粘贴
		jmiCut.addActionListener(this); // 剪切
		jmiFind.addActionListener(this);
		jmiReplace.addActionListener(this);
		jmiSelectAll.addActionListener(this); // 全选
		jmiInsertTime.addActionListener(this);// 时间/日期
		// 格式
		jmiAutoEnter.addActionListener(this); // 自动换行
		jmiFont.addActionListener(this); // 字体
		// 查看
		jmiStatus.addActionListener(this); // 状态栏
		// 帮助
		jmiTopic.addActionListener(this); // 主题
		jmiAbout.addActionListener(this); // 关于

	}

	// 监听菜单选项以调用相应方法
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		// 打开文件
		if (e.getSource() == jmiOpen) {
			open();
		} else if (e.getSource() == jmiSave) {
			// 保存文件
			save();
		} else if (e.getSource() == jmiExit) {
			// 退出当前窗口，如果是最后一个窗口，则退出程序
			windowsNum--;
			if (windowsNum == 0) {
				DIYquit();
			}
		} else if (e.getSource() == jmiCreate) {
			// 创建新的窗口
			create();
		} else if (e.getSource() == jmiSaveAs) {
			saveAs();
		} else if (e.getSource() == jmiCut) {
			ta.cut();
		} else if (e.getSource() == jmiCopy) {
			ta.copy();
		} else if (e.getSource() == jmiPaste) {
			ta.paste();
		} else if (e.getSource() == jmiAbout) {
			// 关于
			JOptionPane.showMessageDialog(this, "Team:\n_11_9_12_\nMember:\n_TZM_WF_DMF_\n版本号：\nv2.0.10", "关于记事本",
					JOptionPane.CLOSED_OPTION);
		} else if (e.getSource() == jmiUndo) {
			// 撤销操作
			try {
				undomanager.undo();
			} catch (CannotRedoException cre) {
				cre.printStackTrace();
			}
			changeEnable();
		} else if (e.getSource() == jmiRedo) {
			try {
				undomanager.redo();
			} catch (CannotRedoException cre) {
				cre.printStackTrace();
			}
			changeEnable();
		} else if (e.getSource() == jmiTopic) {
			// 帮助
			Help();
		} else if (e.getSource() == jmiSelectAll) {
			ta.selectAll();
		} else if (e.getSource() == jmiAutoEnter) {
			if (jmiAutoEnter.isSelected()) {
				ta.setLineWrap(true); // 自动换行方法，JTextarea.class中的方法
				labelTips.setText("自动换行已打开");
				labelLine.setVisible(ture);
				labelColumn.setVisible(ture);
			} else {
				ta.setLineWrap(false);
				labelTips.setText("自动换行已关闭");
				labelLine.setVisible(false);
				labelColumn.setVisible(false);
			}
		} else if (e.getSource() == jmiStatus) {
			if (jmiStatus.isSelected()) {
				panelEnd.setVisible(true);
			} else {
				panelEnd.setVisible(false);
			}
		} else if (e.getSource() == jmiFont) {
			font();
		} else if (e.getSource() == jmiInsertTime) {
			Calendar rightNow = Calendar.getInstance(); // Calendar对象
			Date date = rightNow.getTime(); // 获取时间
			ta.insert(date.toString(), ta.getCaretPosition()); // 插入时间到文本
		} else if (e.getSource() == jmiFind) {
			find();
		} else if (e.getSource() == jmiReplace) {
			replace();
		}
	}

	// 查找
	private void find() {

		JDialog findDialog = new JDialog(this, "查找", false);
		findDialog.setPreferredSize(new Dimension(600, 500));
		Container con = findDialog.getContentPane(); // 获得容器
		con.setLayout(new BorderLayout());
		JLabel findLabel = new JLabel("内容：");
		JTextField findText = new JTextField(20);

		JButton next = new JButton("下一条");
		JButton okButton = new JButton("确定");
		JButton cancel = new JButton("取消");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findDialog.dispose();
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findDialog.dispose();
			}
		});

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int k = 0;
				final String str1, str2;
				str1 = ta.getText();
				str2 = findText.getText();
				if (ta.getSelectedText() == null)
					k = str1.indexOf(str2);
				else
					k = str1.indexOf(str2, ta.getCaretPosition() - findText.getText().length() + 1);
				if (k > -1) {
					ta.setCaretPosition(k);
					ta.select(k, k + str2.length());
				} else {
					JOptionPane.showMessageDialog(null, "找不到下一个" + '"' + str2 + '"', "查找",
							JOptionPane.INFORMATION_MESSAGE);
				}

			}
		});

		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();

		findLabel.setPreferredSize(new Dimension(50, 20));
		findLabel.setVisible(true);

		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		p1.add(findLabel);
		p1.add(findText);
		p1.add(next);

		p2.setLayout(new FlowLayout(FlowLayout.CENTER));
		p2.add(okButton);
		p2.add(cancel);

		con.add(p1, BorderLayout.NORTH);
		con.add(p2, BorderLayout.SOUTH);

		findDialog.setSize(385, 120);
		findDialog.setLocationRelativeTo(null);
		findDialog.setResizable(false);
		findDialog.setVisible(true);

	}

	// 替换
	private void replace() {

		JDialog replaceDialog = new JDialog(this, "替换", false);
		replaceDialog.setPreferredSize(new Dimension(600, 500));
		Container con = replaceDialog.getContentPane(); // 获得容器
		con.setLayout(new BorderLayout());
		JLabel findLabel = new JLabel("查找内容：");
		JTextField findText = new JTextField(20);
		JLabel replaceLabel = new JLabel("替换为：");
		JTextField replaceText = new JTextField(20);

		JButton next = new JButton("下一条");
		JButton replaceButton = new JButton("替换");
		JButton okButton = new JButton("确定");
		JButton cancel = new JButton("取消");

		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();

		findLabel.setPreferredSize(new Dimension(70, 20));
		findLabel.setVisible(true);
		replaceLabel.setPreferredSize(new Dimension(70, 20));
		replaceLabel.setVisible(true);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceDialog.dispose();
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceDialog.dispose();
			}
		});

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int k = 0;
				final String str1, str2;
				str1 = ta.getText();
				str2 = findText.getText();
				if (ta.getSelectedText() == null)
					k = str1.indexOf(str2);
				else
					k = str1.indexOf(str2, ta.getCaretPosition() - findText.getText().length() + 1);
				if (k > -1) {
					ta.setCaretPosition(k);
					ta.select(k, k + str2.length());
				} else {
					JOptionPane.showMessageDialog(null, "找不到下一个" + '"' + str2 + '"', "查找",
							JOptionPane.INFORMATION_MESSAGE);
				}

			}
		});

		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (replaceText.getText().length() == 0 && ta.getSelectedText() != null) {
					ta.replaceSelection("");
				}
				if (replaceText.getText().length() > 0 && ta.getSelectedText() != null) {
					ta.replaceSelection(replaceText.getText());
				}
			}
		});

		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		p1.add(findLabel);
		p1.add(findText);
		p1.add(next);

		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(replaceLabel);
		p2.add(replaceText);
		p2.add(replaceButton);

		p3.setLayout(new FlowLayout(FlowLayout.CENTER));
		p3.add(okButton);
		p3.add(cancel);

		con.add(p1, BorderLayout.NORTH);
		con.add(p2, BorderLayout.CENTER);
		con.add(p3, BorderLayout.SOUTH);

		replaceDialog.setSize(400, 160);
		replaceDialog.setLocationRelativeTo(null);
		replaceDialog.setResizable(false);
		replaceDialog.setVisible(true);
	}

	// 设置字体的方法
	private void font() {
		final JDialog fontDialog = new JDialog(this, "字体设置", false);
		fontDialog.setPreferredSize(new Dimension(600, 500));
		Container con = fontDialog.getContentPane(); // 获得容器
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel fontLabel = new JLabel("字体(F)：");
		fontLabel.setPreferredSize(new Dimension(100, 20));
		JLabel styleLabel = new JLabel("字形(Y)：");
		styleLabel.setPreferredSize(new Dimension(100, 20));
		JLabel sizeLabel = new JLabel("大小(S)：");
		sizeLabel.setPreferredSize(new Dimension(100, 20));
		final JLabel sample = new JLabel("TZM's Notepad-记事本");
		final JTextField fontText = new JTextField(9);
		fontText.setPreferredSize(new Dimension(200, 20));
		final JTextField styleText = new JTextField(8);
		styleText.setPreferredSize(new Dimension(200, 20));
		final int style[] = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC };
		final JTextField sizeText = new JTextField(5);
		sizeText.setPreferredSize(new Dimension(200, 20));
		JButton okButton = new JButton("确定");
		JButton cancel = new JButton("取消");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fontDialog.dispose(); // 释放由此 Window、其子组件及其拥有的所有子组件所使用的所有本机屏幕资源
			}
		});
		Font currentFont = ta.getFont(); // 获取字体
		fontText.setText(currentFont.getFontName());
		fontText.selectAll();
		if (currentFont.getStyle() == Font.PLAIN)
			styleText.setText("常规");
		else if (currentFont.getStyle() == Font.BOLD)
			styleText.setText("粗体");
		else if (currentFont.getStyle() == Font.ITALIC)
			styleText.setText("斜体");
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleText.setText("粗斜体");
		styleText.selectAll();
		String str = String.valueOf(currentFont.getSize());
		sizeText.setText(str);
		sizeText.selectAll();
		final JList<Object> fontList, styleList, sizeList;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String fontName[] = ge.getAvailableFontFamilyNames();
		fontList = new JList<Object>(fontName);
		fontList.setFixedCellWidth(92);
		fontList.setFixedCellHeight(20);
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 一次只能选择一项
		final String fontStyle[] = { "常规", "粗体", "斜体", "粗斜体" };
		styleList = new JList<Object>(fontStyle);
		styleList.setFixedCellWidth(92);
		styleList.setFixedCellHeight(20);
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (currentFont.getStyle() == Font.PLAIN)
			styleList.setSelectedIndex(0);
		else if (currentFont.getStyle() == Font.BOLD)
			styleList.setSelectedIndex(1);
		else if (currentFont.getStyle() == Font.ITALIC)
			styleList.setSelectedIndex(2);
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleList.setSelectedIndex(3);
		final String fontSize[] = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36",
				"48", "72" };
		sizeList = new JList<Object>(fontSize);
		sizeList.setFixedCellWidth(50);
		sizeList.setFixedCellHeight(20);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				fontText.setText(fontName[fontList.getSelectedIndex()]); // 设置字体
				fontText.selectAll(); // 全选 //字体功能不完备：只能变换所有字体
				Font sampleFont1 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont1);
			}
		});
		styleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				int s = style[styleList.getSelectedIndex()];
				styleText.setText(fontStyle[s]);
				styleText.selectAll();
				Font sampleFont2 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont2);
			}
		});
		sizeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				sizeText.setText(fontSize[sizeList.getSelectedIndex()]);
				// sizeText.requestFocus();
				sizeText.selectAll();
				Font sampleFont3 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont3);
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font okFont = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				ta.setFont(okFont);
				fontDialog.dispose();
			}
		});
		JPanel samplePanel = new JPanel();
		samplePanel.setBorder(BorderFactory.createTitledBorder("示例"));
		samplePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		samplePanel.add(sample);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();

		panel2.add(fontText);
		panel2.add(styleText);
		panel2.add(sizeText);
		panel2.add(okButton);
		panel3.add(new JScrollPane(fontList));// JList不支持直接滚动，所以要让JList作为JScrollPane的视口视图
		panel3.add(new JScrollPane(styleList));
		panel3.add(new JScrollPane(sizeList));
		panel3.add(cancel);
		con.add(panel1);
		con.add(panel2);
		con.add(panel3);
		con.add(samplePanel);
		fontDialog.setSize(460, 410);
		fontDialog.setLocationRelativeTo(null);
		fontDialog.setResizable(false);
		fontDialog.setVisible(true);

	}

	// 帮助
	private void Help() {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(this, "基于Java编写的一个简易的记事本程序\n操作请参考Windows的记事本" + "", "帮助",
				JOptionPane.CLOSED_OPTION);
	}

	// 另存为
	private void saveAs() {
		// TODO Auto-generated method stub
		chooser.setDialogTitle("保存文件到");
		getDirectory();
		// save();
		String name = file.getAbsolutePath(); // 获取文件绝对路径
		if (!name.toLowerCase().endsWith(".txt")) {
			name = name + ".txt";
			file = new File(name);
		}
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			byte[] b = ta.getText().getBytes(); // 写入文件
			out.write(b);
			isSaved = true;
			labelTips.setText("文件已保存");
			// this.setTitle(file.getName() + " - 记事本");
		} catch (IOException e) {
			labelTips.setText("文件保存失败");
		}
	}

	// 新建窗口
	private void create() {
		// TODO Auto-generated method stub
		new Notepad();
	}

	// 保存文件
	private void save() {
		// TODO Auto-generated method stub
		if (file == null) {
			// chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("保存文件");
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			} else {
				labelTips.setText("您取消了保存操作");
				return;
			}
		}
		String name = file.getAbsolutePath();
		if (!name.toLowerCase().endsWith(".txt")) {
			name = name + ".txt";
			file = new File(name);
		}
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			byte[] b = ta.getText().getBytes();
			out.write(b);
			isSaved = true;
			labelTips.setText("文件已保存");
			this.setTitle(file.getName() + " - 记事本");
		} catch (IOException e) {
			labelTips.setText("文件保存失败");
		}
	}

	// 获得路径
	private void getDirectory() {
		// TODO Auto-generated method stub
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile(); // 获得选中的文件对象
		} else {
			labelTips.setText("您取消了保存操作");
			return;
		}
	}

	// 打开，在打开之前先判断是否需要保存之前的文件
	private void open() {
		// TODO Auto-generated method stub
		// 判断是否保存，如果已经保存了，则直接进入打开窗口，如果没有则询问
		if (isSaved) {
			openTo();
		} else {
			// 询问是否保存窗口，返回值有0，1，2，，分别表示是，否，取消，如点击关闭按钮则返回-1
			int r = JOptionPane.showConfirmDialog(null, "您还没有保存，是否要保存该文件？", "提示", JOptionPane.YES_NO_CANCEL_OPTION);
			if (r == 0) {
				labelTips.setText("保存文件");
				save();
			} else if (r == 2 || r == -1) {
				labelTips.setText("您没有选中任何文件.");
			} else
				openTo();
		}
	}

	// 打开文件
	private void openTo() {
		// 判断点击的是哪一个按钮,APPROVE_OPTION表示确认选择
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("打开文件");
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
			try (// 字符缓存输入流
					BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
				String line = null;
				// ta.requestFocus();
				ta.setText("");
				// 按行读取file里的字符并
				while ((line = bfr.readLine()) != null) {
					ta.append(line);
					ta.append(System.getProperty("line.separator"));
				}
				labelTips.setText("打开文件：" + file.getName());
				this.setTitle(file.toString() + " - 记事本");
			} catch (IOException e) {
				// 打开文件失败的提示信息.
				labelTips.setText("文件打开错误：" + file.toString());
			}
		}
	}

	private void DIYquit() {
		/**
		 * 退出函数退出时进行判断是否需要保存
		 */
		String tips = null;
		// System.out.println(ta.getText());
		String title = this.getTitle();
		if (title.charAt(0) != '*') {
			System.exit(0);
		}
		String content = ta.getText();
		Container cp = this.getContentPane();
		if (content != null) { // 文件目录不为空，说明有打开着的文件，需要询问是否保存
			tips = "是否保存？\n" + "如果不保存，文件将丢失";
			int n = JOptionPane.showConfirmDialog(null, tips, "记事本", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) { // 是
				save(); // 点是，则保存文件然后打开文件
			} else if (n == JOptionPane.NO_OPTION) { // 否
				System.exit(0);
			} else if (n == JOptionPane.CLOSED_OPTION) { // 关闭
				Notepad newNote = new Notepad();
				newNote.setTitle(title);
				newNote.setContentPane(cp);
			}
		}

	}
}
