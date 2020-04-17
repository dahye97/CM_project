import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMPosition;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMEventManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMWinClient extends JFrame {

	private static final long serialVersionUID = 1L;
	//private JTextArea m_outTextArea;
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private JButton m_loginLogoutButton;
	//private JPanel m_leftButtonPanel;
	//private JScrollPane m_westScroll;
	private JButton m_composeSNSContentButton;
	private JButton m_readNewSNSContentButton;
	private JButton m_readNextSNSContentButton;
	private JButton m_readPreviousSNSContentButton;
	private JButton m_findUserButton;
	private JButton m_addFriendButton;
	private JButton m_removeFriendButton;
	private JButton m_friendsButton;
	private JButton m_friendRequestersButton;
	private JButton m_biFriendsButton;
	private MyMouseListener cmMouseListener;
	private CMClientStub m_clientStub;
	private CMWinClientEventHandler m_eventHandler;
	
	CMWinClient()
	{		
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		cmMouseListener = new MyMouseListener();
		setTitle("CM Client");
		setSize(600, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setMenus();
		setLayout(new BorderLayout());

		m_outTextPane = new JTextPane();
		m_outTextPane.setBackground(new Color(245,245,245));
		//m_outTextPane.setForeground(Color.WHITE);
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);
		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane centerScroll = new JScrollPane (m_outTextPane, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//add(centerScroll);
		getContentPane().add(centerScroll, BorderLayout.CENTER);
		
		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setBackground(new Color(220,220,220));
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Client CM");
		//m_startStopButton.setBackground(Color.LIGHT_GRAY);	// not work on Mac
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		//add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);
		
		m_loginLogoutButton = new JButton("Login");
		m_loginLogoutButton.addActionListener(cmActionListener);
		m_loginLogoutButton.setEnabled(false);
		topButtonPanel.add(m_loginLogoutButton);
		
		/*
		m_leftButtonPanel = new JPanel();
		m_leftButtonPanel.setBackground(new Color(220,220,220));
		m_leftButtonPanel.setLayout(new BoxLayout(m_leftButtonPanel, BoxLayout.Y_AXIS));
		add(m_leftButtonPanel, BorderLayout.WEST);
		m_westScroll = new JScrollPane (m_leftButtonPanel, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//add(westScroll);
		getContentPane().add(m_westScroll, BorderLayout.WEST);

		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, "SNS");
		JPanel snsPanel = new JPanel();
		snsPanel.setLayout(new BoxLayout(snsPanel, BoxLayout.Y_AXIS));
		snsPanel.setBorder(titledBorder);
		
		m_composeSNSContentButton = new JButton("Compose");
		m_composeSNSContentButton.addActionListener(cmActionListener);
		m_readNewSNSContentButton = new JButton("Read New");
		m_readNewSNSContentButton.addActionListener(cmActionListener);
		m_readNextSNSContentButton = new JButton("Read Next");
		m_readNextSNSContentButton.addActionListener(cmActionListener);
		m_readPreviousSNSContentButton = new JButton("Read Prev");
		m_readPreviousSNSContentButton.addActionListener(cmActionListener);
		m_findUserButton = new JButton("Find user");
		m_findUserButton.addActionListener(cmActionListener);
		m_addFriendButton = new JButton("Add Friend");
		m_addFriendButton.addActionListener(cmActionListener);
		m_removeFriendButton = new JButton("Remove Friend");
		//m_removeFriendButton.setMaximumSize(new Dimension(150,10));
		m_removeFriendButton.addActionListener(cmActionListener);
		m_friendsButton = new JButton("Friends");
		m_friendsButton.addActionListener(cmActionListener);
		m_friendRequestersButton = new JButton("Friend requests");
		//m_friendRequestersButton.setMaximumSize(new Dimension(150,10));
		m_friendRequestersButton.addActionListener(cmActionListener);
		m_biFriendsButton = new JButton("Bi-friends");
		m_biFriendsButton.addActionListener(cmActionListener);
		snsPanel.add(m_composeSNSContentButton);
		snsPanel.add(m_readNewSNSContentButton);
		snsPanel.add(m_readNextSNSContentButton);
		snsPanel.add(m_readPreviousSNSContentButton);
		snsPanel.add(m_findUserButton);
		snsPanel.add(m_addFriendButton);
		snsPanel.add(m_removeFriendButton);
		snsPanel.add(m_friendsButton);
		snsPanel.add(m_friendRequestersButton);
		snsPanel.add(m_biFriendsButton);
		m_leftButtonPanel.add(snsPanel);
		
		m_leftButtonPanel.setVisible(false);
		m_westScroll.setVisible(false);
		*/
		
		setVisible(true);

		// create a CM object and set the event handler
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMWinClientEventHandler(m_clientStub, this);
		
		// start CM
		testStartCM();
		
		m_inTextField.requestFocus();
	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
		
		Style linkStyle = doc.addStyle("link", defStyle);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	
	public CMWinClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	
	// set menus
	private void setMenus()
	{
		MyMenuListener menuListener = new MyMenuListener();
		JMenuBar menuBar = new JMenuBar();
		
		JMenu helpMenu = new JMenu("Help");
		//helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem showAllMenuItem = new JMenuItem("show all menus");
		showAllMenuItem.addActionListener(menuListener);
		showAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
		
		helpMenu.add(showAllMenuItem);
		menuBar.add(helpMenu);
		
		JMenu cmNetworkMenu = new JMenu("Network Participation");
		
		JMenu startStopSubMenu = new JMenu("Start/Stop");
		JMenuItem startMenuItem = new JMenuItem("start CM");
		startMenuItem.addActionListener(menuListener);
		startStopSubMenu.add(startMenuItem);
		JMenuItem terminateMenuItem = new JMenuItem("terminate CM");
		terminateMenuItem.addActionListener(menuListener);
		startStopSubMenu.add(terminateMenuItem);
		
		cmNetworkMenu.add(startStopSubMenu);
		
		JMenu connectSubMenu = new JMenu("Connection");
		JMenuItem connDefaultMenuItem = new JMenuItem("connect to default server");
		connDefaultMenuItem.addActionListener(menuListener);
		connectSubMenu.add(connDefaultMenuItem);
		JMenuItem disconnDefaultMenuItem = new JMenuItem("disconnect from default server");
		disconnDefaultMenuItem.addActionListener(menuListener);
		connectSubMenu.add(disconnDefaultMenuItem);
		JMenuItem connDesigMenuItem = new JMenuItem("connect to designated server");
		connDesigMenuItem.addActionListener(menuListener);
		connectSubMenu.add(connDesigMenuItem);
		JMenuItem disconnDesigMenuItem = new JMenuItem("disconnect from designated server");
		disconnDesigMenuItem.addActionListener(menuListener);
		connectSubMenu.add(disconnDesigMenuItem);

		cmNetworkMenu.add(connectSubMenu);
		
		JMenu loginSubMenu = new JMenu("Login");
		JMenuItem loginDefaultMenuItem = new JMenuItem("login to default server");
		loginDefaultMenuItem.addActionListener(menuListener);
		loginDefaultMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
		loginSubMenu.add(loginDefaultMenuItem);
		JMenuItem syncLoginDefaultMenuItem = new JMenuItem("synchronously login to default server");
		syncLoginDefaultMenuItem.addActionListener(menuListener);
		loginSubMenu.add(syncLoginDefaultMenuItem);
		JMenuItem logoutDefaultMenuItem = new JMenuItem("logout from default server");
		logoutDefaultMenuItem.addActionListener(menuListener);
		loginSubMenu.add(logoutDefaultMenuItem);
		JMenuItem loginDesigMenuItem = new JMenuItem("login to designated server");
		loginDesigMenuItem.addActionListener(menuListener);
		loginSubMenu.add(loginDesigMenuItem);
		JMenuItem logoutDesigMenuItem = new JMenuItem("logout from designated server");
		logoutDesigMenuItem.addActionListener(menuListener);
		loginSubMenu.add(logoutDesigMenuItem);

		cmNetworkMenu.add(loginSubMenu);

		JMenu sessionSubMenu = new JMenu("Session/Group");
		JMenuItem reqSessionInfoDefaultMenuItem = new JMenuItem("request session information from default server");
		reqSessionInfoDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(reqSessionInfoDefaultMenuItem);
		JMenuItem syncReqSessionInfoDefaultMenuItem = new JMenuItem("synchronously request session information "
				+ "from default server");
		syncReqSessionInfoDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(syncReqSessionInfoDefaultMenuItem);
		JMenuItem joinSessionDefaultMenuItem = new JMenuItem("join session of default server");
		joinSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(joinSessionDefaultMenuItem);
		JMenuItem syncJoinSessionDefaultMenuItem = new JMenuItem("synchronously join session of default server");
		syncJoinSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(syncJoinSessionDefaultMenuItem);
		JMenuItem leaveSessionDefaultMenuItem = new JMenuItem("leave session of default server");
		leaveSessionDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(leaveSessionDefaultMenuItem);
		JMenuItem changeGroupDefaultMenuItem = new JMenuItem("change group of default server");
		changeGroupDefaultMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(changeGroupDefaultMenuItem);
		JMenuItem printGroupMembersMenuItem = new JMenuItem("print group members");
		printGroupMembersMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(printGroupMembersMenuItem);
		JMenuItem reqSessionInfoDesigMenuItem = new JMenuItem("request session information from designated server");
		reqSessionInfoDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(reqSessionInfoDesigMenuItem);
		JMenuItem joinSessionDesigMenuItem = new JMenuItem("join session of designated server");
		joinSessionDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(joinSessionDesigMenuItem);
		JMenuItem leaveSessionDesigMenuItem = new JMenuItem("leave session of designated server");
		leaveSessionDesigMenuItem.addActionListener(menuListener);
		sessionSubMenu.add(leaveSessionDesigMenuItem);

		cmNetworkMenu.add(sessionSubMenu);
		menuBar.add(cmNetworkMenu);
		
		JMenu cmServiceMenu = new JMenu("Services");
		
		JMenu eventSubMenu = new JMenu("Event Transmission");
		JMenuItem chatMenuItem = new JMenuItem("chat");
		chatMenuItem.addActionListener(menuListener);
		chatMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		eventSubMenu.add(chatMenuItem);
		JMenuItem multicastMenuItem = new JMenuItem("multicast chat in current group");
		multicastMenuItem.addActionListener(menuListener);
		eventSubMenu.add(multicastMenuItem);
		JMenuItem dummyEventMenuItem = new JMenuItem("test CMDummyEvent");
		dummyEventMenuItem.addActionListener(menuListener);
		eventSubMenu.add(dummyEventMenuItem);
		JMenuItem userEventMenuItem = new JMenuItem("test CMUserEvent");
		userEventMenuItem.addActionListener(menuListener);
		eventSubMenu.add(userEventMenuItem);
		JMenuItem datagramMenuItem = new JMenuItem("test datagram event");
		datagramMenuItem.addActionListener(menuListener);
		eventSubMenu.add(datagramMenuItem);
		JMenuItem posMenuItem = new JMenuItem("test user position");
		posMenuItem.addActionListener(menuListener);
		eventSubMenu.add(posMenuItem);
		JMenuItem sendrecvMenuItem = new JMenuItem("test sendrecv");
		sendrecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(sendrecvMenuItem);
		JMenuItem castrecvMenuItem = new JMenuItem("test castrecv");
		castrecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(castrecvMenuItem);
		JMenuItem asyncSendRecvMenuItem = new JMenuItem("test asynchronous sendrecv");
		asyncSendRecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(asyncSendRecvMenuItem);
		JMenuItem asyncCastRecvMenuItem = new JMenuItem("test asynchronous castrecv");
		asyncCastRecvMenuItem.addActionListener(menuListener);
		eventSubMenu.add(asyncCastRecvMenuItem);
		
		cmServiceMenu.add(eventSubMenu);
		
		JMenu infoSubMenu = new JMenu("Information");
		JMenuItem groupInfoMenuItem = new JMenuItem("show group information of default server");
		groupInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(groupInfoMenuItem);
		JMenuItem userStatMenuItem = new JMenuItem("show current user status");
		userStatMenuItem.addActionListener(menuListener);
		infoSubMenu.add(userStatMenuItem);
		JMenuItem channelInfoMenuItem = new JMenuItem("show current channels");
		channelInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(channelInfoMenuItem);
		JMenuItem serverInfoMenuItem = new JMenuItem("show current server information");
		serverInfoMenuItem.addActionListener(menuListener);
		infoSubMenu.add(serverInfoMenuItem);
		JMenuItem groupInfoDesigMenuItem = new JMenuItem("show group information of designated server");
		groupInfoDesigMenuItem.addActionListener(menuListener);
		infoSubMenu.add(groupInfoDesigMenuItem);
		JMenuItem inThroughputMenuItem = new JMenuItem("measure input network throughput");
		inThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(inThroughputMenuItem);
		JMenuItem outThroughputMenuItem = new JMenuItem("measure output network throughput");
		outThroughputMenuItem.addActionListener(menuListener);
		infoSubMenu.add(outThroughputMenuItem);
		JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
		showAllConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showAllConfMenuItem);
		JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
		changeConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(changeConfMenuItem);
		
		cmServiceMenu.add(infoSubMenu);
		
		JMenu channelSubMenu = new JMenu("Channel");
		JMenuItem addChannelMenuItem = new JMenuItem("add channel");
		addChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(addChannelMenuItem);
		JMenuItem removeChannelMenuItem = new JMenuItem("remove channel");
		removeChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(removeChannelMenuItem);
		JMenuItem blockChannelMenuItem = new JMenuItem("test blocking channel");
		blockChannelMenuItem.addActionListener(menuListener);
		channelSubMenu.add(blockChannelMenuItem);
		
		cmServiceMenu.add(channelSubMenu);
		
		JMenu fileTransferSubMenu = new JMenu("File Transfer");
		JMenuItem setPathMenuItem = new JMenuItem("set file path");
		setPathMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(setPathMenuItem);
		JMenuItem reqFileMenuItem = new JMenuItem("request file");
		reqFileMenuItem.addActionListener(menuListener);
		reqFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
		fileTransferSubMenu.add(reqFileMenuItem);
		JMenuItem pushFileMenuItem = new JMenuItem("push file");
		pushFileMenuItem.addActionListener(menuListener);
		pushFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		fileTransferSubMenu.add(pushFileMenuItem);
		JMenuItem cancelRecvMenuItem = new JMenuItem("cancel receiving file");
		cancelRecvMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(cancelRecvMenuItem);
		JMenuItem cancelSendMenuItem = new JMenuItem("cancel sending file");
		cancelSendMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(cancelSendMenuItem);
		JMenuItem printSendRecvFileInfoMenuItem = new JMenuItem("print sending/receiving file info");
		printSendRecvFileInfoMenuItem.addActionListener(menuListener);
		fileTransferSubMenu.add(printSendRecvFileInfoMenuItem);
		
		cmServiceMenu.add(fileTransferSubMenu);
		
		JMenu snsSubMenu = new JMenu("Social Network Service");
		JMenuItem reqContentMenuItem = new JMenuItem("request content list");
		reqContentMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqContentMenuItem);
		JMenuItem reqNextMenuItem = new JMenuItem("request next content list");
		reqNextMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqNextMenuItem);
		JMenuItem reqPrevMenuItem = new JMenuItem("request previous content list");
		reqPrevMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqPrevMenuItem);
		JMenuItem reqAttachMenuItem = new JMenuItem("request attached file");
		reqAttachMenuItem.addActionListener(menuListener);
		snsSubMenu.add(reqAttachMenuItem);
		JMenuItem uploadMenuItem = new JMenuItem("upload content");
		uploadMenuItem.addActionListener(menuListener);
		snsSubMenu.add(uploadMenuItem);
		
		cmServiceMenu.add(snsSubMenu);
		
		JMenu userSubMenu = new JMenu("User");
		JMenuItem regUserMenuItem = new JMenuItem("register new user");
		regUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(regUserMenuItem);
		JMenuItem deregUserMenuItem = new JMenuItem("deregister user");
		deregUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(deregUserMenuItem);
		JMenuItem findUserMenuItem = new JMenuItem("find registered user");
		findUserMenuItem.addActionListener(menuListener);
		userSubMenu.add(findUserMenuItem);
		JMenuItem addFriendMenuItem = new JMenuItem("add new friend");
		addFriendMenuItem.addActionListener(menuListener);
		userSubMenu.add(addFriendMenuItem);
		JMenuItem removeFriendMenuItem = new JMenuItem("remove friend");
		removeFriendMenuItem.addActionListener(menuListener);
		userSubMenu.add(removeFriendMenuItem);
		JMenuItem showFriendsMenuItem = new JMenuItem("show friends");
		showFriendsMenuItem.addActionListener(menuListener);
		userSubMenu.add(showFriendsMenuItem);
		JMenuItem showRequestersMenuItem = new JMenuItem("show friend requesters");
		showRequestersMenuItem.addActionListener(menuListener);
		userSubMenu.add(showRequestersMenuItem);
		JMenuItem showBiFriendsMenuItem = new JMenuItem("show bi-directional friends");
		showBiFriendsMenuItem.addActionListener(menuListener);
		userSubMenu.add(showBiFriendsMenuItem);
		
		cmServiceMenu.add(userSubMenu);
		
		JMenu pubsubSubMenu = new JMenu("Publish/Subscribe");
		JMenuItem connectMenuItem = new JMenuItem("connect MQTT service");
		connectMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(connectMenuItem);
		JMenuItem pubMenuItem = new JMenuItem("publish");
		pubMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(pubMenuItem);
		JMenuItem subMenuItem = new JMenuItem("subscribe");
		subMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(subMenuItem);
		JMenuItem sessionInfoMenuItem = new JMenuItem("print session info");
		sessionInfoMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(sessionInfoMenuItem);
		JMenuItem unsubMenuItem = new JMenuItem("unsubscribe");
		unsubMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(unsubMenuItem);
		JMenuItem disconMenuItem = new JMenuItem("disconnect MQTT service");
		disconMenuItem.addActionListener(menuListener);
		pubsubSubMenu.add(disconMenuItem);
		
		cmServiceMenu.add(pubsubSubMenu);
		
		JMenu otherSubMenu = new JMenu("Other CM Test");
		JMenuItem forwardMenuItem = new JMenuItem("test forwarding scheme");
		forwardMenuItem.addActionListener(menuListener);
		otherSubMenu.add(forwardMenuItem);
		JMenuItem forwardDelayMenuItem = new JMenuItem("test delay of forwarding scheme");
		forwardDelayMenuItem.addActionListener(menuListener);
		otherSubMenu.add(forwardDelayMenuItem);
		JMenuItem repeatSNSMenuItem = new JMenuItem("test repeated request of SNS content list");
		repeatSNSMenuItem.addActionListener(menuListener);
		otherSubMenu.add(repeatSNSMenuItem);
		JMenuItem multiFilesMenuItem = new JMenuItem("pull/push multiple files");
		multiFilesMenuItem.addActionListener(menuListener);
		otherSubMenu.add(multiFilesMenuItem);
		JMenuItem splitFileMenuItem = new JMenuItem("split file");
		splitFileMenuItem.addActionListener(menuListener);
		otherSubMenu.add(splitFileMenuItem);
		JMenuItem mergeFilesMenuItem = new JMenuItem("merge files");
		mergeFilesMenuItem.addActionListener(menuListener);
		otherSubMenu.add(mergeFilesMenuItem);
		JMenuItem distMergeMenuItem = new JMenuItem("distribute and merge file");
		distMergeMenuItem.addActionListener(menuListener);
		otherSubMenu.add(distMergeMenuItem);
		JMenuItem cscFtpMenuItem = new JMenuItem("test csc file transfer");
		cscFtpMenuItem.addActionListener(menuListener);
		otherSubMenu.add(cscFtpMenuItem);
		JMenuItem c2cFtpMenuItem = new JMenuItem("test c2c file transfer");
		c2cFtpMenuItem.addActionListener(menuListener);
		otherSubMenu.add(c2cFtpMenuItem);
		
		cmServiceMenu.add(otherSubMenu);

		menuBar.add(cmServiceMenu);
	
		setJMenuBar(menuBar);

	}
	
	// initialize button titles
	public void initializeButtons()
	{
		m_startStopButton.setText("Start Client CM");
		m_loginLogoutButton.setText("Login");
		//m_leftButtonPanel.setVisible(false);
		//m_westScroll.setVisible(false);
		revalidate();
		repaint();
	}
	
	// set button titles
	public void setButtonsAccordingToClientState()
	{
		int nClientState;
		nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		
		// nclientState: CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN
		switch(nClientState)
		{
		case CMInfo.CM_INIT:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_CONNECT:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_LOGIN:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Logout");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		case CMInfo.CM_SESSION_JOIN:
			m_startStopButton.setText("Stop Client CM");
			m_loginLogoutButton.setText("Logout");
			//m_leftButtonPanel.setVisible(true);
			//m_westScroll.setVisible(true);
			break;
		default:
			m_startStopButton.setText("Start Client CM");
			m_loginLogoutButton.setText("Login");
			//m_leftButtonPanel.setVisible(false);
			//m_westScroll.setVisible(false);
			break;
		}
		revalidate();
		repaint();
	}
	
	public void printMessage(String strText)
	{
		/*
		m_outTextArea.append(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
		*/
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, null);
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void printStyledMessage(String strText, String strStyleName)
	{
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;
	}
	
	public void printImage(String strPath)
	{
		int nTextPaneWidth = m_outTextPane.getWidth();
		int nImageWidth;
		int nImageHeight;
		int nNewWidth;
		int nNewHeight;

		File f = new File(strPath);
		if(!f.exists())
		{
			printMessage(strPath+"\n");
			return;
		}
		
		ImageIcon icon = new ImageIcon(strPath);
		Image image = icon.getImage();
		nImageWidth = image.getWidth(m_outTextPane);
		nImageHeight = image.getHeight(m_outTextPane);
		
		if(nImageWidth > nTextPaneWidth/2)
		{
			nNewWidth = nTextPaneWidth / 2;
			float fRate = (float)nNewWidth/(float)nImageWidth;
			nNewHeight = (int)(nImageHeight * fRate);
			Image newImage = image.getScaledInstance(nNewWidth, nNewHeight, java.awt.Image.SCALE_SMOOTH);
			icon = new ImageIcon(newImage);
		}
		
		m_outTextPane.insertIcon ( icon );
		printMessage("\n");
	}
	

	public void processInput(String strInput)
	{
		int nCommand = -1;
		try {
			nCommand = Integer.parseInt(strInput);
		} catch (NumberFormatException e) {
			printMessage("Incorrect command number!\n");
			return;
		}
		
		switch(nCommand)
		{
		case 0:
			printAllMenus();
			break;
		case 100:
			testStartCM();
			break;
		case 999:
			testTerminateCM();
			break;			
		case 1: // connect to default server
			testConnectionDS();
			break;
		case 2: // disconnect from default server
			testDisconnectionDS();
			break;
		case 3: // connect to a designated server
			testConnectToServer();
			break;
		case 4: // disconnect from a designated server
			testDisconnectFromServer();
			break;
		case 10: // asynchronous login to default server
			testLoginDS();
			break;
		case 11: // synchronously login to default server
			testSyncLoginDS();
			break;
		case 12: // logout from default server
			testLogoutDS();
			break;
		case 13: // log in to a designated server
			testLoginServer();
			break;
		case 14: // log out from a designated server
			testLogoutServer();
			break;
		case 22: // join a session
			testJoinSession();
			break;
		case 23: // synchronously join a session
			testSyncJoinSession();
			break;
		case 24: // leave the current session
			testLeaveSession();
			break;
		case 28: // join a session of a designated server
			testJoinSessionOfServer();
			break;
		case 29: // leave a session of a designated server
			testLeaveSessionOfServer();
			break;
		case 42: // test CMDummyEvent
			testDummyEvent();
			break;
		case 43: // test CMUserEvent
			testUserEvent();
			break;
		case 51: // print current information about the client
			testCurrentUserStatus();
			break;
		case 53: // request additional server info
			testRequestServerInfo();
			break;
		case 57: // print all configurations
			testPrintConfigurations();
			break;
		case 58: // change configuration
			testChangeConfiguration();
			break;
		case 90: // register user
			testRegisterUser();
			break;
		case 91: // deregister user
			testDeregisterUser();
			break;
		case 92: // find user
			testFindRegisteredUser();
			break;
		default:
			System.err.println("Unknown command.");
			break;
		}
	}
	
	public void printAllMenus()
	{
		printMessage("---------------------------------- Help\n");
		printMessage("0: show all menus\n");
		printMessage("---------------------------------- Start/Stop\n");
		printMessage("100: start CM, 999: terminate CM\n");
		printMessage("---------------------------------- Connection\n");
		printMessage("1: connect to default server, 2: disconnect from default server\n");
		printMessage("3: connect to designated server, 4: disconnect from designated server\n");
		printMessage("---------------------------------- Login\n");
		printMessage("10: login to default server, 11: synchronously login to default server\n");
		printMessage("12: logout from default server\n");
		printMessage("13: login to designated server, 14: logout from designated server\n");
		printMessage("---------------------------------- Session/Group\n");
		printMessage("20: request session information from default server\n");
		printMessage("21: synchronously request session information from default server\n");
		printMessage("22: join session of default server, 23: synchronously join session of default server\n");
		printMessage("24: leave session of default server, 25: change group of default server\n");
		printMessage("26: print group members\n");
		printMessage("27: request session information from designated server\n");
		printMessage("28: join session of designated server, 29: leave session of designated server\n");
		printMessage("---------------------------------- Event Transmission\n");
		printMessage("40: chat, 41: multicast chat in current group\n");
		printMessage("42: test CMDummyEvent, 43: test CMUserEvent, 44: test datagram event, 45: test user position\n");
		printMessage("46: test sendrecv, 47: test castrecv\n");
		printMessage("48: test asynchronous sendrecv, 49: test asynchronous castrecv\n");
		printMessage("---------------------------------- Information\n");
		printMessage("50: show group information of default server, 51: show current user status\n");
		printMessage("52: show current channels, 53: show current server information\n");
		printMessage("54: show group information of designated server\n");
		printMessage("55: measure input network throughput, 56: measure output network throughput\n");
		printMessage("57: show all configurations, 58: change configuration\n");
		printMessage("---------------------------------- Channel\n");
		printMessage("60: add channel, 61: remove channel, 62: test blocking channel\n");
		printMessage("---------------------------------- File Transfer\n");
		printMessage("70: set file path, 71: request file, 72: push file\n");
		printMessage("73: cancel receiving file, 74: cancel sending file\n");
		printMessage("75: print sending/receiving file info\n");
		printMessage("---------------------------------- Social Network Service\n");
		printMessage("80: request content list, 81: request next content list, 82: request previous content list\n");
		printMessage("83: request attached file, 84: upload content\n");
		printMessage("---------------------------------- User\n");
		printMessage("90: register new user, 91: deregister user, 92: find registered user\n");
		printMessage("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters\n");
		printMessage("97: show bi-directional friends\n");
		printMessage("---------------------------------- MQTT\n");
		printMessage("200: connect, 201: publish, 202: subscribe, 203: print session info\n");
		printMessage("204: unsubscribe, 205: disconnect \n");
		printMessage("---------------------------------- Other CM Tests\n");
		printMessage("101: test forwarding scheme, 102: test delay of forwarding scheme\n");
		printMessage("103: test repeated request of SNS content list\n");
		printMessage("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file\n");
		printMessage("108: send event with wrong # bytes, 109: send event with wrong type\n");
		printMessage("110: test csc file transfer, 111: test c2c file transfer\n");
	}
	
	public void testConnectionDS()
	{
		printMessage("====== connect to default server\n");
		boolean ret = m_clientStub.connectToServer();
		if(ret)
		{
			printMessage("Successfully connected to the default server.\n");
		}
		else
		{
			printMessage("Cannot connect to the default server!\n");
		}
		printMessage("======\n");
	}
	
	public void testDisconnectionDS()
	{
		printMessage("====== disconnect from default server\n");
		boolean ret = m_clientStub.disconnectFromServer();
		if(ret)
		{
			printMessage("Successfully disconnected from the default server.\n");
		}
		else
		{
			printMessage("Error while disconnecting from the default server!");
		}
		printMessage("======\n");
		
		setButtonsAccordingToClientState();
		setTitle("CM Client");
	}
	
	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;

		printMessage("====== login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(bRequestResult)
			{
				printMessage("successfully sent the login request.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
				m_eventHandler.setStartTime(0);
			}
		}
		
		printMessage("======\n");
	}
	
	public void testSyncLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		CMSessionEvent loginAckEvent = null;

		printMessage("====== synchronous login to default server\n");
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = {
				"User Name:", userNameField,
				"Password:", passwordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
		{
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security problem?
			
			m_eventHandler.setStartTime(System.currentTimeMillis());
			loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(loginAckEvent != null)
			{
				// print login result
				if(loginAckEvent.isValidUser() == 0)
				{
					printMessage("This client fails authentication by the default server!\n");		
				}
				else if(loginAckEvent.isValidUser() == -1)
				{
					printMessage("This client is already in the login-user list!\n");
				}
				else
				{
					printMessage("return delay: "+lDelay+" ms.\n");
					printMessage("This client successfully logs in to the default server.\n");
					CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
					
					// Change the title of the client window
					setTitle("CM Client ("+interInfo.getMyself().getName()+")");

					// Set the appearance of buttons in the client frame window
					setButtonsAccordingToClientState();
				}				
			}
			else
			{
				printStyledMessage("failed the login request!\n", "bold");
			}
			
		}
		
		printMessage("======\n");		
	}

	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		printMessage("====== logout from default server\n");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			printMessage("successfully sent the logout request.\n");
		else
			printStyledMessage("failed the logout request!\n", "bold");
		printMessage("======\n");

		// Change the title of the login button
		setButtonsAccordingToClientState();
		setTitle("CM Client");
	}

	public void testStartCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strCurServerAddress = null;
		int nCurServerPort = -1;
		
		strCurServerAddress = m_clientStub.getServerAddress();
		nCurServerPort = m_clientStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nCurServerPort));
		Object msg[] = {
				"Server Address: ", serverAddressTextField,
				"Server Port: ", serverPortTextField
		};
		int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

		// update the server info if the user would like to do
		if (option == JOptionPane.OK_OPTION) 
		{
			String strNewServerAddress = serverAddressTextField.getText();
			int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
			if(!strNewServerAddress.equals(strCurServerAddress) || nNewServerPort != nCurServerPort)
				m_clientStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}
		
		bRet = m_clientStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!\n", "bold");
		}
		else
		{
			m_startStopButton.setEnabled(true);
			m_loginLogoutButton.setEnabled(true);
			printStyledMessage("Client CM starts.\n", "bold");
			printStyledMessage("Type \"0\" for menu.\n", "regular");
			// change the appearance of buttons in the client window frame
			setButtonsAccordingToClientState();
		}
	}
	
	public void testTerminateCM()
	{
		//m_clientStub.disconnectFromServer();
		m_clientStub.terminateCM();
		printMessage("Client CM terminates.\n");
		// change the appearance of buttons in the client window frame
		initializeButtons();
		setTitle("CM Client");
	}

	

	public void testJoinSession()
	{
		String strSessionName = null;
		boolean bRequestResult = false;
		printMessage("====== join a session\n");
		strSessionName = JOptionPane.showInputDialog("Session Name:");
		if(strSessionName != null)
		{
			m_eventHandler.setStartTime(System.currentTimeMillis());
			bRequestResult = m_clientStub.joinSession(strSessionName);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(bRequestResult)
			{
				printMessage("successfully sent the session-join request.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
				printStyledMessage("failed the session-join request!\n", "bold");
		}
		printMessage("======\n");
	}
	
	public void testSyncJoinSession()
	{
		CMSessionEvent se = null;
		String strSessionName = null;
		printMessage("====== join a session\n");
		strSessionName = JOptionPane.showInputDialog("Session Name:");
		if(strSessionName != null)
		{
			m_eventHandler.setStartTime(System.currentTimeMillis());
			se = m_clientStub.syncJoinSession(strSessionName);
			long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
			if(se != null)
			{
				setButtonsAccordingToClientState();
				// print result of the request
				printMessage("successfully joined a session that has ("+se.getGroupNum()+") groups.\n");
				printMessage("return delay: "+lDelay+" ms.\n");
			}
			else
			{
				printStyledMessage("failed the session-join request!\n", "bold");
			}
		}
				
		printMessage("======\n");		
	}

	public void testLeaveSession()
	{
		boolean bRequestResult = false;
		printMessage("====== leave the current session\n");
		bRequestResult = m_clientStub.leaveSession();
		if(bRequestResult)
			printMessage("successfully sent the leave-session request.\n");
		else
			printStyledMessage("failed the leave-session request!\n", "bold");
		printMessage("======\n");
		setButtonsAccordingToClientState();
	}


	public void testDummyEvent()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		String strInput = null;
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group!\n");
			return;
		}
		
		printMessage("====== test CMDummyEvent in current group\n");

		strInput = JOptionPane.showInputDialog("Input Message: ");
		if(strInput == null) return;
		
		
		CMDummyEvent due = new CMDummyEvent();
		due.setHandlerSession(myself.getCurrentSession());
		due.setHandlerGroup(myself.getCurrentGroup());
		due.setDummyInfo(strInput);
		m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
		due = null;
		
		printMessage("======\n");
	}

	
	public void testUserEvent()
	{
		String strReceiver = null;
		int nValueByteNum = -1;
		CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			printMessage("You should join a session and a group!\n");
			return;
		}

		printMessage("====== test CMUserEvent\n");
		
		String strFieldNum = null;
		int nFieldNum = -1;

		strFieldNum = JOptionPane.showInputDialog("Field Numbers:");
		if(strFieldNum == null) return;
		try{
			nFieldNum = Integer.parseInt(strFieldNum);
		}catch(NumberFormatException e){
			printMessage("Input must be an integer number greater than 0!");
			return;
		}
		
		String strID = null;
		JTextField strIDField = new JTextField();
		JTextField strReceiverField = new JTextField();
		String[] dataTypes = {"CM_INT", "CM_LONG", "CM_FLOAT", "CM_DOUBLE", "CM_CHAR", "CH_STR", "CM_BYTES"};
		JComboBox<String>[] dataTypeBoxes = new JComboBox[nFieldNum]; 
		JTextField[] eventFields = new JTextField[nFieldNum*2];
		Object[] message = new Object[4+nFieldNum*3*2];
		
		for(int i = 0; i < nFieldNum; i++)
		{
			dataTypeBoxes[i] = new JComboBox<String>(dataTypes);
		}
		
		for(int i = 0; i < nFieldNum*2; i++)
		{
			eventFields[i] = new JTextField();
		}
		
		message[0] = "event ID: ";
		message[1] = strIDField;
		message[2] = "Receiver Name: ";
		message[3] = strReceiverField;
		for(int i = 4, j = 0, k = 1; i < 4+nFieldNum*3*2; i+=6, j+=2, k++)
		{
			message[i] = "Data type "+k+":";
			message[i+1] = dataTypeBoxes[k-1];
			message[i+2] = "Field Name "+k+":";
			message[i+3] = eventFields[j];
			message[i+4] = "Field Value "+k+":";
			message[i+5] = eventFields[j+1];
		}
		int option = JOptionPane.showConfirmDialog(null, message, "User Event Input", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strID = strIDField.getText();
			strReceiver = strReceiverField.getText();
			
			CMUserEvent ue = new CMUserEvent();
			ue.setStringID(strID);
			ue.setHandlerSession(myself.getCurrentSession());
			ue.setHandlerGroup(myself.getCurrentGroup());
			
			for(int i = 0, j = 0; i < nFieldNum*2; i+=2, j++)
			{
				if(dataTypeBoxes[j].getSelectedIndex() == CMInfo.CM_BYTES)
				{
					nValueByteNum = Integer.parseInt(eventFields[i+1].getText());
					if(nValueByteNum < 0)
					{
						printMessage("CMClientApp.testUserEvent(), Invalid nValueByteNum("
								+nValueByteNum+")\n");
						ue.removeAllEventFields();
						ue = null;
						return;
					}
					byte[] valueBytes = new byte[nValueByteNum];
					for(int k = 0; k < nValueByteNum; k++)
						valueBytes[k] = 1;	// dummy data
					ue.setEventBytesField(eventFields[i].getText(), nValueByteNum, valueBytes);	
				}
				else
				{
					ue.setEventField(dataTypeBoxes[j].getSelectedIndex(),
							eventFields[i].getText(), eventFields[i+1].getText());
				}
				
			}

			m_clientStub.send(ue, strReceiver);
			ue.removeAllEventFields();
			ue = null;
		}
		
		printMessage("======\n");
		
		return;
	}
	
	
	public void testCurrentUserStatus()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();

		printMessage("------ for the default server\n");
		printMessage("name("+myself.getName()+"), session("+myself.getCurrentSession()+"), group("
				+myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"), state("
				+myself.getState()+"), attachment download scheme("+confInfo.getAttachDownloadScheme()+").\n");
		
		// for additional servers
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			if(tserver.getNonBlockSocketChannelInfo().findChannel(0) != null)
			{
				printMessage("------ for additional server["+tserver.getServerName()+"]\n");
				printMessage("current session("+tserver.getCurrentSessionName()+
						"), current group("+tserver.getCurrentGroupName()+"), state("
						+tserver.getClientState()+").");
				
			}
		}
		
		return;
	}

	

	public void testRegisterUser()
	{
		String strName = null;
		String strPasswd = null;
		String strRePasswd = null;
		
		printMessage("====== register a user\n");
		JTextField nameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		JPasswordField rePasswordField = new JPasswordField();
		Object[] message = {
				"Input User Name: ", nameField,
				"Input Password: ", passwordField,
				"Retype Password: ", rePasswordField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "User Registration", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		strName = nameField.getText();
		strPasswd = new String(passwordField.getPassword());	// security problem?
		strRePasswd = new String(rePasswordField.getPassword());// security problem?

		if(!strPasswd.equals(strRePasswd))
		{
			printMessage("Password input error!\n");
			return;
		}

		m_clientStub.registerUser(strName, strPasswd);
		printMessage("======\n");
		
		return;
	}

	public void testDeregisterUser()
	{
		String strName = null;
		String strPasswd = null;
		
		printMessage("====== Deregister a user\n");
		JTextField nameField = new JTextField();
		JPasswordField passwdField = new JPasswordField();
		Object[] message = {
				"Input User Name: ", nameField,
				"Input Password: ", passwdField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "User Deregistration", JOptionPane.OK_CANCEL_OPTION);
		if(option != JOptionPane.OK_OPTION) return;
		strName = nameField.getText();
		strPasswd = new String(passwdField.getPassword());	// security problem?
		
		m_clientStub.deregisterUser(strName, strPasswd);

		printMessage("======\n");
		
		return;
	}

	public void testFindRegisteredUser()
	{
		String strName = null;
		
		printMessage("====== search for a registered user\n");
		strName = JOptionPane.showInputDialog("Input User Name: ");
		if(strName != null)
			m_clientStub.findRegisteredUser(strName);

		printMessage("======\n");
		
		return;
	}

	
	public void testRequestServerInfo()
	{
		printMessage("====== request additional server information\n");
		m_clientStub.requestServerInfo();
	}

	public void testConnectToServer()
	{
		printMessage("====== connect to a designated server\n");
		String strServerName = null;
		
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.connectToServer(strServerName);
		
		return;
	}

	public void testDisconnectFromServer()
	{
		printMessage("===== disconnect from a designated server\n");
		
		String strServerName = null;
		
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.disconnectFromServer(strServerName);

		return;
	}

	public void testLoginServer()
	{
		String strServerName = null;
		String user = null;
		String password = null;
						
		printMessage("====== log in to a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName == null) return;

		if( strServerName.equals(m_clientStub.getDefaultServerName()) )	// login to a default server
		{
			JTextField userNameField = new JTextField();
			JPasswordField passwordField = new JPasswordField();
			Object[] message = {
					"User Name:", userNameField,
					"Password:", passwordField
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
			{
				user = userNameField.getText();
				String strPassword = new String(passwordField.getPassword()); // security problem?

				m_clientStub.loginCM(user, strPassword);
			}
		}
		else // use the login info for the default server
		{
			CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
			user = myself.getName();
			password = myself.getPasswd();
			m_clientStub.loginCM(strServerName, user, password);
		}
		
		printMessage("======\n");
		
		return;
	}

	public void testLogoutServer()
	{
		String strServerName = null;
		
		printMessage("====== log out from a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if(strServerName != null)
			m_clientStub.logoutCM(strServerName);
		
		printMessage("======\n");
	}


	public void testJoinSessionOfServer()
	{
		String strServerName = null;
		String strSessionName = null;
		
		printMessage("====== join a session of a designated server\n");
		JTextField serverField = new JTextField();
		JTextField sessionField = new JTextField();
		Object[] message = {
				"Server Name", serverField, "Session Name", sessionField
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Join Session", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strServerName = serverField.getText();
			strSessionName = sessionField.getText();
			m_clientStub.joinSession(strServerName, strSessionName);
		}
		
		printMessage("======\n");
		
		return;
	}

	public void testLeaveSessionOfServer()
	{
		String strServerName = null;
		
		printMessage("====== leave a session of a designated server\n");
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if(strServerName != null)
			m_clientStub.leaveSession(strServerName);
		
		printMessage("======\n");
		
		return;
	}

	
	
	public void testPrintConfigurations()
	{
		String[] strConfigurations;
		printMessage("========== print all current configurations\n");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		printMessage("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			printMessage(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
		
	}
	
	public void testChangeConfiguration()
	{
		boolean bRet = false;
		String strField = null;
		String strValue = null;
		printMessage("========== change configuration\n");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		
		JTextField fieldTextField = new JTextField();
		JTextField valueTextField = new JTextField();
		Object[] msg = {
			"Field Name:", fieldTextField,
			"Value:", valueTextField
		};
		int nRet = JOptionPane.showConfirmDialog(null, msg, "Change Configuration", JOptionPane.OK_CANCEL_OPTION);
		if(nRet != JOptionPane.OK_OPTION) return;
		strField = fieldTextField.getText().trim();
		strValue = valueTextField.getText().trim();
		if(strField.isEmpty() || strValue.isEmpty())
		{
			printStyledMessage("There is an empty input!\n", "bold");
			return;
		}
		
		bRet = CMConfigurator.changeConfiguration(confPath.toString(), strField, strValue);
		if(bRet)
		{
			printMessage("cm-client.conf file is successfully updated: ("+strField+"="+strValue+")\n");
		}
		else
		{
			printStyledMessage("The configuration change is failed!: ("+strField+"="+strValue+")\n", "bold");
		}
		
		return;
	}
	
	
	
	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e)
		{
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER)
			{
				JTextField input = (JTextField)e.getSource();
				String strText = input.getText();
				printMessage(strText+"\n");
				// parse and call CM API
				processInput(strText);
				input.setText("");
				input.requestFocus();
			}
			else if(key == KeyEvent.VK_ALT)
			{
				
			}
		}
		
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}
	
	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Client CM"))
			{
				testStartCM();
			}
			else if(button.getText().equals("Stop Client CM"))
			{
				testTerminateCM();
			}
			else if(button.getText().equals("Login"))
			{
				// login to the default cm server
				testLoginDS();
			}
			else if(button.getText().equals("Logout"))
			{
				// logout from the default cm server
				testLogoutDS();
			}
			

			m_inTextField.requestFocus();
		}
	}
	
	public class MyMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			String strMenu = e.getActionCommand();
			switch(strMenu)
			{
			case "show all menus":
				printAllMenus();
				break;
			case "start CM":
				testStartCM();
				break;
			case "terminate CM":
				testTerminateCM();
				break;
			case "connect to default server":
				testConnectionDS();
				break;
			case "disconnect from default server":
				testDisconnectionDS();
				break;
			case "connect to designated server":
				testConnectToServer();
				break;
			case "disconnect from designated server":
				testDisconnectFromServer();
				break;
			case "login to default server":
				testLoginDS();
				break;
			case "synchronously login to default server":
				testSyncLoginDS();
				break;
			case "logout from default server":
				testLogoutDS();
				break;
			case "login to designated server":
				testLoginServer();
				break;
			case "logout from designated server":
				testLogoutServer();
				break;
			case "join session of default server":
				testJoinSession();
				break;
			case "synchronously join session of default server":
				testSyncJoinSession();
				break;
			case "leave session of default server":
				testLeaveSession();
				break;
			case "join session of designated server":
				testJoinSessionOfServer();
				break;
			case "leave session of designated server":
				testLeaveSessionOfServer();
				break;
			case "test CMDummyEvent":
				testDummyEvent();
				break;
			case "test CMUserEvent":
				testUserEvent();
				break;
			case "show current user status":
				testCurrentUserStatus();
				break;
			case "show current server information":
				testRequestServerInfo();
				break;
			case "show all configurations":
				testPrintConfigurations();
				break;
			case "change configuration":
				testChangeConfiguration();
				break;

			case "register new user":
				testRegisterUser();
				break;
			case "deregister user":
				testDeregisterUser();
				break;
			case "find registered user":
				testFindRegisteredUser();
				break;

			}
		}
	}
	
	public class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				JLabel pathLabel = (JLabel)e.getSource();
				String strPath = pathLabel.getText();
				File fPath = new File(strPath);
				int index = strPath.lastIndexOf(File.separator);
				String strFileName = strPath.substring(index+1, strPath.length()); 
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.getSource() instanceof JLabel)
			{
				Cursor cursor = Cursor.getDefaultCursor();
				setCursor(cursor);
			}
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CMWinClient client = new CMWinClient();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
	}

}
