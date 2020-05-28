import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class IngstaServer extends JFrame {

	private static final long serialVersionUID = 1L;
	
	//private JTextArea m_outTextArea;
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private CMServerStub m_serverStub;
	private IngstaServerEventHandler m_eventHandler;
	private CMSNSUserAccessSimulator m_uaSim;
	
	IngstaServer()
	{
		
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		setTitle("CM Server");
		setSize(500, 800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setMenus();
		setLayout(new BorderLayout());
		
		m_outTextPane = new JTextPane();
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);

		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane (m_outTextPane, 
				   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		add(scroll);
		
		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);
		
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);
		
		m_startStopButton = new JButton("Start Server CM");
		m_startStopButton.addActionListener(cmActionListener);
		m_startStopButton.setEnabled(false);
		//add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);
		
		setVisible(true);

		// create CM stub object and set the event handler
		m_serverStub = new CMServerStub();
		m_eventHandler = new IngstaServerEventHandler(m_serverStub, this);
		m_uaSim = new CMSNSUserAccessSimulator();

		// start cm
		startCM();		
	}
	
	private void addStylesToDocument(StyledDocument doc)
	{
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");
		
		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);
	}
	
	public CMServerStub getServerStub()
	{
		return m_serverStub;
	}
	
	public IngstaServerEventHandler getServerEventHandler()
	{
		return m_eventHandler;
	}
	
	public void setMenus()
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

		JMenu multiServerSubMenu = new JMenu("Multi-server");
		JMenuItem connectDefaultMenuItem = new JMenuItem("connect to default server");
		connectDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(connectDefaultMenuItem);
		JMenuItem disconnectDefaultMenuItem = new JMenuItem("disconnect from default server");
		disconnectDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(disconnectDefaultMenuItem);
		JMenuItem regDefaultMenuItem = new JMenuItem("register to default server");
		regDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(regDefaultMenuItem);
		JMenuItem deregDefaultMenuItem = new JMenuItem("deregister from default server");
		deregDefaultMenuItem.addActionListener(menuListener);
		multiServerSubMenu.add(deregDefaultMenuItem);
		
		cmNetworkMenu.add(multiServerSubMenu);
		menuBar.add(cmNetworkMenu);
		
		JMenu serviceMenu = new JMenu("Services");
		JMenu infoSubMenu = new JMenu("Information");
		
		JMenuItem showUsersMenuItem = new JMenuItem("show login users");
		showUsersMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showUsersMenuItem);
		
		JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
		showAllConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(showAllConfMenuItem);
		
		JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
		changeConfMenuItem.addActionListener(menuListener);
		infoSubMenu.add(changeConfMenuItem);
		
		serviceMenu.add(infoSubMenu);
		
		JMenu snsSubMenu = new JMenu("Social Network Service");
		JMenuItem attachSchemeMenuItem = new JMenuItem("set attachment download scheme");
		attachSchemeMenuItem.addActionListener(menuListener);
		snsSubMenu.add(attachSchemeMenuItem);		

		menuBar.add(serviceMenu);
		
		setJMenuBar(menuBar);
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
			startCM();
			break;
		case 999:
			terminateCM();
			return;
		case 6: // print current login users
			printLoginUsers();
			break;
		case 7: // print all current configurations
			printConfigurations();
			break;
		case 8: // change a field value in the configuration file
			changeConfiguration();
			break;
		case 10:	// send CMDummyEvent
			sendCMDummyEvent();
			break;
		case 30: // request registration to the default server
			requestServerReg();
			break;
		case 31: // request deregistration from the default server
			requestServerDereg();
			break;
		case 32: // connect to the default server
			connectToDefaultServer();
			break;
		case 33: // disconnect from the default server
			disconnectFromDefaultServer();
			break;
		case 40: // set a scheme for attachement download of SNS content
			setAttachDownloadScheme();
			break;
		case 60:	// find session info
			findMqttSessionInfo();
			break;
		case 61:	// print all session info
			printAllMqttSessionInfo();
			break;
		case 62:	// print all retain info
			printAllMqttRetainInfo();
			break;
		case 105:	// send event with wrong # bytes
			sendEventWithWrongByteNum();
			break;
		case 106:	// send event with wrong type
			sendEventWithWrongEventType();
			break;
		default:
			//System.out.println("Unknown command.");
			printStyledMessage("Unknown command.\n", "bold");
			break;
		}
	}
	
	public void printAllMenus()
	{
		printMessage("---------------------------------- Help ----------------------------------\n");
		printMessage("0: show all menus\n");
		printMessage("---------------------------------- Start/Stop ----------------------------\n");
		printMessage("100: start CM, 999: terminate CM\n");
		printMessage("---------------------------------- Information --------------------------\n");
		printMessage("6: show login users\n");
		printMessage("7: show all configurations\n");
		printMessage("8: change configuration\n");
		printMessage("10: send CMDummyEvent\n");
		printMessage("---------------------------------- Multi-server --------------------------\n");
		printMessage("30: register to default server\n");
		printMessage("31: deregister from default server\n");
		printMessage("32: connect to default server\n");
		printMessage("33: disconnect from default server\n");
		printMessage("40: set attachment download scheme\n");
		printMessage("---------------------------------- MQTT\n");
		printMessage("60: find session info, 61: print all session info, 62: print all retain info\n");
		printMessage("---------------------------------- Other CM Tests ----------------------\n");
		printMessage("101: configure SNS user access simulation, 102: start SNS user access simulation\n");
		printMessage("103: start SNS user access simulation and measure prefetch accuracy\n");
		printMessage("104: start and write recent SNS access history simulation to CM DB\n");
		printMessage("105: send event with wrong bytes\n");
		printMessage("106: send event with wrong type\n");
	}
	
	public void updateTitle()
	{
		CMUser myself = m_serverStub.getMyself();
		if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
		{
			setTitle("CM Default Server [\""+myself.getName()+"\"]");
		}
		else
		{
			if(myself.getState() < CMInfo.CM_LOGIN)
			{
				setTitle("CM Additional Server [\"?\"]");
			}
			else
			{
				setTitle("CM Additional Server [\""+myself.getName()+"\"]");
			}			
		}
	}
	
	public void startCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strSavedServerAddress = null;
		String strCurServerAddress = null;
		int nSavedServerPort = -1;
		
		strSavedServerAddress = m_serverStub.getServerAddress();
		strCurServerAddress = CMCommManager.getLocalIP();
		nSavedServerPort = m_serverStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		JTextField serverAddressTextField = new JTextField(strCurServerAddress);
		JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
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
			if(!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
				m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
		}
		
		// start cm
		bRet = m_serverStub.startCM();
		if(!bRet)
		{
			printStyledMessage("CM initialization error!\n", "bold");
		}
		else
		{
			printStyledMessage("Movie search system이 시작되었습니다!\n", "bold");
			printMessage("Type \"0\" for menu.\n");					
			// change button to "stop CM"
			m_startStopButton.setEnabled(true);
			m_startStopButton.setText("Stop Server CM");
			updateTitle();					
		}

		m_inTextField.requestFocus();

	}
	
	public void terminateCM()
	{
		m_serverStub.terminateCM();
		printMessage("Server CM terminates.\n");
		m_startStopButton.setText("Start Server CM");
		updateTitle();
	}

	public void sendCMDummyEvent()
	{
		String strMessage = null;
		String strTarget = null;
		String strSession = null;
		String strGroup = null;
		CMDummyEvent de = null;
		printMessage("====== test event transmission\n");

		JTextField messageField = new JTextField();
		JTextField targetField = new JTextField();
		JTextField sessionField = new JTextField();
		JTextField groupField = new JTextField();

		Object[] msg = {

			"Dummy message: ", messageField,
			"Target name (for send()): ", targetField,
			"Target session (for cast() or broadcast()): ", sessionField,
			"Target group (for cast() or broadcast()): ", groupField

		};

		int option = JOptionPane.showConfirmDialog(null, msg, "CMDummyEvent Transmission", 
				JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strMessage = messageField.getText().trim();

			strTarget = targetField.getText().trim();

			strSession = sessionField.getText().trim();

			strGroup = groupField.getText().trim();

			

			if(strMessage.isEmpty())

			{

				printStyledMessage("No input message\n", "bold");

				return;

			}

			

			de = new CMDummyEvent();

			de.setDummyInfo(strMessage);

			de.setHandlerSession(strSession);

			de.setHandlerGroup(strGroup);

			

			if(!strTarget.isEmpty())

			{

				m_serverStub.send(de, strTarget);

			}

			else

			{

				if(strSession.isEmpty()) strSession = null;

				if(strGroup.isEmpty()) strGroup = null;

				m_serverStub.cast(de, strSession, strGroup);

			}

		}

			

	}
	public void setAttachDownloadScheme()
	{
		String strUserName = null;
		int nScheme = -1;
		JTextField userField = new JTextField();
		String[] attachLod = {"Full", "Thumbnail", "Prefetching", "None"};
		JComboBox<String> lodBox = new JComboBox<String>(attachLod);
		Object[] message = {
				"Target user name (Enter for all users)", userField,
				"Image QoS: ", lodBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Attachement Download Scheme", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.OK_OPTION)
		{
			strUserName = userField.getText();
			nScheme = lodBox.getSelectedIndex();
			printMessage("The attachment download scheme of user["+strUserName
					+"] is set to ["+lodBox.getItemAt(nScheme)+"].\n");
			if(strUserName.isEmpty())
				strUserName = null;
			m_serverStub.setAttachDownloadScheme(strUserName, nScheme);
		}
		
		return;
	}
	
	public void setFilePath()
	{
		printMessage("====== set file path\n");
		String strPath = null;

		strPath = JOptionPane.showInputDialog("file path: ");
		if(strPath == null)
		{
			return;
		}
		
		m_serverStub.setTransferedFileHome(Paths.get(strPath));
		
		printMessage("======\n");
	}
	
	public void requestFile()
	{
		boolean bReturn = false;
		String strFileName = null;
		String strFileOwner = null;
		byte byteFileAppendMode = -1;

		printMessage("====== request a file\n");
		JTextField fileNameField = new JTextField();
		JTextField fileOwnerField = new JTextField();
		String[] fAppendMode = {"Default", "Overwrite", "Append"};		
		JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

		Object[] message = {
		    "File Name:", fileNameField,
		    "File Owner:", fileOwnerField,
			"File Append Mode: ", fAppendBox
		};
		int option = JOptionPane.showConfirmDialog(null, message, "File Request Input", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled!\n");
			return;
		}
		
		strFileName = fileNameField.getText().trim();
		if(strFileName.isEmpty())
		{
			printMessage("File name is empty!\n");
			return;
		}
		
		strFileOwner = fileOwnerField.getText().trim();
		if(strFileOwner.isEmpty())
		{
			printMessage("File owner is empty!\n");
			return;
		}
		
		switch(fAppendBox.getSelectedIndex())
		{
		case 0:
			byteFileAppendMode = CMInfo.FILE_DEFAULT;
			break;
		case 1:
			byteFileAppendMode = CMInfo.FILE_OVERWRITE;
			break;
		case 2:
			byteFileAppendMode = CMInfo.FILE_APPEND;
			break;
		}
		
		bReturn = m_serverStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);
				
		if(!bReturn)
			printMessage("Request file error! file("+strFileName+"), owner("+strFileOwner+").\n");
		
		printMessage("======\n");
	}

	public void pushFile()
	{
		String strFilePath = null;
		File[] files;
		String strReceiver = null;
		byte byteFileAppendMode = -1;
		boolean bReturn = false;

		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
		JTextField freceiverField = new JTextField();
		String[] fAppendMode = {"Default", "Overwrite", "Append"};		
		JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

		Object[] message = { 
				"File Receiver: ", freceiverField,
				"File Append Mode: ", fAppendBox 
				};
		int option = JOptionPane.showConfirmDialog(null, message, "File Push", JOptionPane.OK_CANCEL_OPTION);
		if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
		{
			printMessage("canceled.\n");
			return;
		}

		strReceiver = freceiverField.getText().trim();
		if(strReceiver.isEmpty())
		{
			printMessage("File receiver is empty!\n");
			return;
		}
		
		switch(fAppendBox.getSelectedIndex())
		{
		case 0:
			byteFileAppendMode = CMInfo.FILE_DEFAULT;
			break;
		case 1:
			byteFileAppendMode = CMInfo.FILE_OVERWRITE;
			break;
		case 2:
			byteFileAppendMode = CMInfo.FILE_APPEND;
			break;
		}
		
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		File curDir = new File(confInfo.getTransferedFileHome().toString());
		fc.setCurrentDirectory(curDir);
		int fcRet = fc.showOpenDialog(this);
		if(fcRet != JFileChooser.APPROVE_OPTION) return;
		files = fc.getSelectedFiles();
		if(files.length < 1) return;
		for(int i=0; i < files.length; i++)
		{
			strFilePath = files[i].getPath();
			bReturn = m_serverStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
			if(!bReturn)
			{
				printMessage("push file error! file("+strFilePath+"), receiver("
						+strReceiver+").\n");
			}
		}
	
		printMessage("======\n");
	}

	public void cancelRecvFile()
	{
		String strSender = null;
		boolean bReturn = false;
		printMessage("====== cancel receiving a file\n");
		
		strSender = JOptionPane.showInputDialog("Input sender name (enter for all senders)");
		if(strSender.isEmpty())
			strSender = null;
		
		bReturn = m_serverStub.cancelPullFile(strSender);
		
		if(bReturn)
		{
			if(strSender == null)
				strSender = "all senders";
			printMessage("Successfully requested to cancel receiving a file to ["+strSender+"].\n");
		}
		else
			printMessage("Request failed to cancel receiving a file to ["+strSender+"]!\n");
		
		return;
	}
	
	public void cancelSendFile()
	{
		String strReceiver = null;
		boolean bReturn = false;
		printMessage("====== cancel sending a file\n");

		strReceiver = JOptionPane.showInputDialog("Input receiver name (enter for all receivers)");
		if(strReceiver.isEmpty())
			strReceiver = null;
		
		bReturn = m_serverStub.cancelPushFile(strReceiver);
		
		if(bReturn)
			printMessage("Successfully requested to cancel sending a file to ["+strReceiver+"]");
		else
			printMessage("Request failed to cancel sending a file to ["+strReceiver+"]!");
		
		return;
	}

	public void printSendRecvFileInfo()
	{
		CMFileTransferInfo fInfo = m_serverStub.getCMInfo().getFileTransferInfo();
		Hashtable<String, CMList<CMSendFileInfo>> sendHashtable = fInfo.getSendFileHashtable();
		Hashtable<String, CMList<CMRecvFileInfo>> recvHashtable = fInfo.getRecvFileHashtable();
		Set<String> sendKeySet = sendHashtable.keySet();
		Set<String> recvKeySet = recvHashtable.keySet();
		
		printMessage("==== sending file info\n");
		for(String receiver : sendKeySet)
		{
			CMList<CMSendFileInfo> sendList = sendHashtable.get(receiver);
			printMessage(sendList+"\n");
		}

		printMessage("==== receiving file info\n");
		for(String sender : recvKeySet)
		{
			CMList<CMRecvFileInfo> recvList = recvHashtable.get(sender);
			printMessage(recvList+"\n");
		}
	}

	public void requestServerReg()
	{
		String strServerName = null;

		printMessage("====== request registration to the default server\n");
		strServerName = JOptionPane.showInputDialog("Enter registered server name");
		if(strServerName != null)
		{
			m_serverStub.requestServerReg(strServerName);
		}

		printMessage("======\n");
		return;
	}

	public void requestServerDereg()
	{
		printMessage("====== request deregistration from the default server\n");
		boolean bRet = m_serverStub.requestServerDereg();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		
		return;
	}

	public void connectToDefaultServer()
	{
		printMessage("====== connect to the default server\n");
		boolean bRet = m_serverStub.connectToServer();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		return;
	}

	public void disconnectFromDefaultServer()
	{
		printMessage("====== disconnect from the default server\n");
		boolean bRet = m_serverStub.disconnectFromServer();
		printMessage("======\n");
		if(bRet)
			updateTitle();
		return;
	}
	
	public void printLoginUsers()
	{
		printMessage("========== print login users\n");
		CMMember loginUsers = m_serverStub.getLoginUsers();
		if(loginUsers == null)
		{
			printStyledMessage("The login users list is null!\n", "bold");
			return;
		}
		
		printMessage("Currently ["+loginUsers.getMemberNum()+"] users are online.\n");
		Vector<CMUser> loginUserVector = loginUsers.getAllMembers();
		Iterator<CMUser> iter = loginUserVector.iterator();
		int nPrintCount = 0;
		while(iter.hasNext())
		{
			CMUser user = iter.next();
			printMessage(user.getName()+" ");
			nPrintCount++;
			if((nPrintCount % 10) == 0)
			{
				printMessage("\n");
				nPrintCount = 0;
			}
		}
	}
	
	public void printConfigurations()
	{
		String[] strConfigurations;
		printMessage("========== print all current configurations\n");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		printMessage("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			printMessage(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
	}
	
	public void changeConfiguration()
	{
		boolean bRet = false;
		String strField = null;
		String strValue = null;
		printMessage("========== change configuration\n");
		Path confPath = m_serverStub.getConfigurationHome().resolve("cm-server.conf");
		
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
			printMessage("cm-server.conf file is successfully updated: ("+strField+"="+strValue+")\n");
		}
		else
		{
			printStyledMessage("The configuration change is failed!: ("+strField+"="+strValue+")\n", "bold");
		}
		
		return;
	}
	
	
	public void sendEventWithWrongByteNum()
	{
		printMessage("========== send a CMDummyEvent with wrong # bytes to a client\n");
		
		CMCommInfo commInfo = m_serverStub.getCMInfo().getCommInfo();
		CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
		CMBlockingEventQueue sendQueue = commInfo.getSendBlockingEventQueue();
		
		String strTarget = JOptionPane.showInputDialog("target client or server name: ").trim();
		SelectableChannel ch = null;
		CMUser user = interInfo.getLoginUsers().findMember(strTarget);
		CMServer server = null;
		
		String strDefServer = interInfo.getDefaultServerInfo().getServerName();
		if(user != null)
		{
			ch = user.getNonBlockSocketChannelInfo().findChannel(0);
		}
		else if(strTarget.contentEquals(strDefServer))
		{
			ch = interInfo.getDefaultServerInfo().getNonBlockSocketChannelInfo().findChannel(0);
		}
		else
		{
			server = interInfo.findAddServer(strTarget);
			if(server != null)
			{
				ch = server.getNonBlockSocketChannelInfo().findChannel(0);
			}
			else {
				printStyledMessage("["+strTarget+"] not found!\n", "bold");
				return;
			}
		}
		
		CMDummyEvent due = new CMDummyEvent();
		ByteBuffer buf = due.marshall();
		buf.clear();
		buf.putInt(-1).clear();
		CMMessage msg = new CMMessage(buf, ch);
		sendQueue.push(msg);

	}
	
	public void sendEventWithWrongEventType()
	{
		printMessage("========== send a CMDummyEvent with wrong event type\n");
		
		String strTarget = JOptionPane.showInputDialog("target client or server name: ").trim();

		CMDummyEvent due = new CMDummyEvent();
		due.setType(-1);	// set wrong event type
		m_serverStub.send(due, strTarget);
	}

	public void printMessage(String strText)
	{
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

	public void findMqttSessionInfo()

	{
		printMessage("========== find MQTT session info\n");
		String strUser = null;
		strUser = JOptionPane.showInputDialog("User name").trim();
		
		if(strUser == null || strUser.equals("")) 
			return;
		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}

		printMessage("MQTT session of \""+strUser+"\" is \n");
		printMessage(mqttManager.getSessionInfo(strUser)+"\n");

		return;
	}

	

	public void printAllMqttSessionInfo()

	{

		printMessage("========== print all MQTT session info\n");

		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);

		if(mqttManager == null)

		{

			printStyledMessage("CMMqttManager is null!\n", "bold");

			return;

		}

		printMessage(mqttManager.getAllSessionInfo());

		

		return;

	}

	

	public void printAllMqttRetainInfo()

	{

		printMessage("=========== print all MQTT retain info\n");

		CMMqttManager mqttManager = (CMMqttManager)m_serverStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);

		if(mqttManager == null)

		{

			printStyledMessage("CMMqttManager is null!\n", "bold");

			return;

		}

		printMessage(mqttManager.getAllRetainInfo());

		

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
		}
		
		public void keyReleased(KeyEvent e){}
		public void keyTyped(KeyEvent e){}
	}
	
	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			JButton button = (JButton) e.getSource();
			if(button.getText().equals("Start Server CM"))
			{
				// start cm
				boolean bRet = m_serverStub.startCM();
				if(!bRet)
				{
					printStyledMessage("CM initialization error!\n", "bold");
				}
				else
				{
					printStyledMessage("Server CM starts.\n", "bold");
					printMessage("Type \"0\" for menu.\n");					
					// change button to "stop CM"
					button.setText("Stop Server CM");
				}
				// check if default server or not
				if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
				{
					setTitle("CM Default Server (\"SERVER\")");
				}
				else
				{
					setTitle("CM Additional Server (\"?\")");
				}					
				m_inTextField.requestFocus();
			}
			else if(button.getText().equals("Stop Server CM"))
			{
				// stop cm
				m_serverStub.terminateCM();
				printMessage("Server CM terminates.\n");
				// change button to "start CM"
				button.setText("Start Server CM");
			}
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
				startCM();
				break;
			case "terminate CM":
				terminateCM();
				break;
			case "connect to default server":
				connectToDefaultServer();
				break;
			case "disconnect from default server":
				disconnectFromDefaultServer();
				break;
			case "register to default server":
				requestServerReg();
				break;
			case "deregister from default server":
				requestServerDereg();
				break;

			case "show login users":
				printLoginUsers();
				break;
			case "show all configurations":
				printConfigurations();
				break;
			case "change configuration":
				changeConfiguration();
				break;
			}
		}
	}
	
	public static void main(String[] args)
	{
		IngstaServer server = new IngstaServer();
		CMServerStub cmStub = server.getServerStub();
		cmStub.setAppEventHandler(server.getServerEventHandler());
	}
}