import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;

public class IngstaClient extends JFrame {
   
   MyActionListener ActionListener = new MyActionListener();
   CMClientStub clientStub = new CMClientStub();
   IngstaClientEventHandler eventHandler = new IngstaClientEventHandler (clientStub, this);
   
   private String strUserName;
   
   private JFrame loginFrame, mainFrame, friendFrame, contentUploadFrame, searchFriendFrame;
   private JButton add, del, search;
   private BevelBorder border;
   private JPanel panel;
   private JScrollPane scrollPane;
   private JPanel panel1, panel2, panel3;
   private LineBorder line;
   private ArrayList<String> filePathList = null;
   private JTextField contentField;
   private JComboBox<String> lodBox;
   
   public JFrame getLoginFrame() {
	   return this.loginFrame;
   }
   public JFrame getFriendFrame() {
	   return this.friendFrame;
   }
   public CMClientStub getClientStub()
   {
      return clientStub;
   }
   
   public IngstaClientEventHandler getClientEventHandler()
   {
      return eventHandler;
   }
   
   
   
   public void LoginFrame() {
      
      //Frame
      loginFrame = new JFrame("LOGIN");
      loginFrame.setBounds(200,50,600,800);
      loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      loginFrame.setLayout(new GridLayout(1,3));
      
      //Login & Register
      JButton login = new JButton("LOGIN");
      login.addActionListener(ActionListener);
      JButton register = new JButton("REGISTER");
      register.addActionListener(ActionListener);
      JButton deregister = new JButton("DEREGISTER");
      deregister.addActionListener(ActionListener);
      
      loginFrame.add(login);
      loginFrame.add(register);
      loginFrame.add(deregister);
      
      loginFrame.setVisible(true);
   }
   
   
   public void login() {
      strUserName = null;
      String strPassword = null;
      boolean bRequestResult = false;
      
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
         
         eventHandler.setStartTime(System.currentTimeMillis());
         bRequestResult = clientStub.loginCM(strUserName, strPassword);
         if(bRequestResult)
         {
            System.out.println("successfully sent the login request.\n");
         }
         else
         {
            System.out.println("failed the login request!\n");
            eventHandler.setStartTime(0);
         }
      }
   }
   
   public void register() {
      String strName = null;
      String strPasswd = null;
      String strRePasswd = null;
      
      //printMessage("====== register a user\n");
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
      strPasswd = new String(passwordField.getPassword());   // security problem?
      strRePasswd = new String(rePasswordField.getPassword());// security problem?

      if(!strPasswd.equals(strRePasswd))
      {
         //printMessage("Password input error!\n");
         return;
      }

      clientStub.registerUser(strName, strPasswd);
      
      //printMessage("======\n");
      
      return;
   }
   
   public void deregister() {
      String strName = null;
      String strPasswd = null;
      
      //printMessage("====== Deregister a user\n");
      JTextField nameField = new JTextField();
      JPasswordField passwdField = new JPasswordField();
      Object[] message = {
            "Input User Name: ", nameField,
            "Input Password: ", passwdField
      };
      int option = JOptionPane.showConfirmDialog(null, message, "User Deregistration", JOptionPane.OK_CANCEL_OPTION);
      if(option != JOptionPane.OK_OPTION) return;
      strName = nameField.getText();
      strPasswd = new String(passwdField.getPassword());   // security problem?
      
      clientStub.deregisterUser(strName, strPasswd);

      //printMessage("======\n");
      
      return;
   }
   
   
   public void CMmainFrame() {
      
      //MainFrame
      mainFrame = new JFrame("CM_SNS");
      mainFrame.setBounds(200,50,600,800);
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      //UserPanel
      JPanel userPanel = new JPanel();
      JLabel userName = new JLabel(strUserName);
      userName.setFont(new Font("D2Coding", Font.BOLD, 20));
      userPanel.add(userName);
      
      
      JButton mycontent = new JButton("My Contents");
      userPanel.add(mycontent);
      JButton logout = new JButton("LOGOUT");
      userPanel.add(logout);
      userPanel.setLayout(new GridLayout(1,3));
      
      mainFrame.add(userPanel, BorderLayout.NORTH);
      
      
      //ButtonPanel
      JPanel buttonPanel = new JPanel();
      JButton friends_btn, upload_btn;
      mainFrame.add(buttonPanel, BorderLayout.SOUTH);
      
      friends_btn = new JButton("FRIENDS");
      friends_btn.addActionListener(ActionListener);
      upload_btn = new JButton("UPLOAD");
      upload_btn.addActionListener(ActionListener);
      
      buttonPanel.add(friends_btn);
      buttonPanel.add(upload_btn);
      buttonPanel.setLayout(new GridLayout(1,2));
      
       panel = new JPanel();
         panel.setLayout(null);
         panel.setBorder(new TitledBorder(line,"Content"));
        // panel1.setBorder(new TitledBorder(new LineBorder(Color.BLACK),"Content"));
         mainFrame.add(panel, BorderLayout.CENTER);
         
         panel1 = new JPanel();
         panel1.setBounds(10, 25, 560, 210);
         panel1.setBorder(new TitledBorder(line,"content#1"));
         panel.add(panel1);
         
         panel2 = new JPanel();
         panel2.setBounds(10, 240, 560, 210);
         panel2.setBorder(new TitledBorder(line,"content#2"));
         panel.add(panel2);
         
         panel3 = new JPanel();
         panel3.setBounds(10, 455, 560, 210);
         panel3.setBorder(new TitledBorder(line,"content#3"));
         panel.add(panel3);
      
      //Visible
      mainFrame.setVisible(true);
      
      
   }
   
   
   
   public void FriendsFrame() {
      border = new BevelBorder(BevelBorder.RAISED);
      //Frame
      friendFrame = new JFrame("FRIEND");
      friendFrame.setBounds(200,50,600,800);
      friendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      friendFrame.setLayout(null);
      
      //Label
      JLabel num = new JLabel("NO.");
      num.setBounds(10, 8, 280, 50);
      num.setBorder(border);
      num.setHorizontalAlignment(JLabel.CENTER);
      
      JLabel name = new JLabel("Name");
      name.setBounds(305, 8, 280, 50);
      name.setBorder(border);
      name.setHorizontalAlignment(JLabel.CENTER);
      
      
      //Button
      add = new JButton("ADD");
      add.addActionListener(ActionListener);
      add.setBounds(8, 500, 185, 50);
   

      del = new JButton("DELETE");
      del.addActionListener(ActionListener);
      del.setBounds(205, 500, 185, 50);

      
      search = new JButton("SEARCH");
      search.addActionListener(ActionListener);
      search .setBounds(400, 500, 185, 50);
      
      scrollPane = new JScrollPane();
      scrollPane.setBounds(10, 65, 575, 420);
      
      JButton back = new JButton("BACK");
      back.addActionListener(ActionListener);
      back.setBounds(8, 570, 185, 50);
      back.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             System.out.println("BACK");
              friendFrame.dispose();
              CMmainFrame();
          }
      });

      
   
      

      // 친구목록 가져오기
      clientStub.requestFriendsList();
      
      
      friendFrame.add(num);
      friendFrame.add(name);
      friendFrame.add(add);
      friendFrame.add(del);
      friendFrame.add(search);
      friendFrame.add(scrollPane);
      friendFrame.add(back);
      
      setResizable(false);
      friendFrame.setVisible(true);
   
   }
   
   public void searchFriendFrame() {      
      
      //Frame 
      searchFriendFrame = new JFrame("SEARCH FRIEND");
      searchFriendFrame.setBounds(200,50,600,800);
      searchFriendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      searchFriendFrame.setLayout(null);
      
      //Label
      JLabel friendSearch = new JLabel();
      friendSearch.setText("Search");
      friendSearch.setFont(new Font("D2Coding", Font.BOLD, 30));
      friendSearch.setBounds(10, 10, 560, 50);
      
      
      //Button
      JButton b1, b2, b3;
      b1 = new JButton("나를 등록한 친구");
      b1.addActionListener(ActionListener);
      b1.setBounds(15, 70, 250, 40);
      
      b2 = new JButton("나만 등록한 친구");
      b2.addActionListener(ActionListener);
      b2.setBounds(15,130, 250, 40);
      
      b3 = new JButton("같이 등록한 친구");
      b3.addActionListener(ActionListener);
      b3.setBounds(15, 190, 250, 40);
      
      JButton back = new JButton("BACK");
      back.setBounds(15, 290, 250, 40);
      back.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             searchFriendFrame.dispose();
            
          }
      });
      
      
      searchFriendFrame.add(b1);
      searchFriendFrame.add(b2);
      searchFriendFrame.add(b3);
      searchFriendFrame.add(friendSearch);
      searchFriendFrame.add(back);
      
      searchFriendFrame.setResizable(false);
      searchFriendFrame.setVisible(true);
   
   }
   
   public void requestFriendsList() {
      clientStub.requestFriendsList();
      return;
   }
   
   public void requestFriendRequestersList() {
      clientStub.requestFriendRequestersList();
      return;
   }

   public void requestBiFriendsList() {
      clientStub.requestBiFriendsList();
      return;
   }
   

   public void SNSContentUploadFrame()
   {
      
      //Frame
      contentUploadFrame = new JFrame("CONTENT UPLOAD");
      contentUploadFrame.setBounds(200,50,600,800);
      contentUploadFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      
      //Content
      contentField = new JTextField();
      JLabel l_contentField = new JLabel("Content\n");
      l_contentField.setFont(new Font("D2Coding", Font.BOLD, 30));
      l_contentField.setBounds(10,10,500,30);
      contentField.setBounds(10,50,560,200);
      
      
      //File Check   
      JLabel l_attachedFilesBox = new JLabel("Picture Attachment\n");
      l_attachedFilesBox.setFont(new Font("D2Coding", Font.BOLD, 30));
      
      JPanel filePanel = new JPanel();
      JButton pic1, pic2, pic3;
      pic1 = new JButton("PICTURE 1");
      pic1.addActionListener(ActionListener);
      pic2 = new JButton("PICTURE 2");
      pic2.addActionListener(ActionListener);
      pic3 = new JButton("PICTURE 3");
      pic3.addActionListener(ActionListener);
      
      filePanel.add(pic1);
      filePanel.add(pic2);
      filePanel.add(pic3);
      filePanel.setLayout(new GridLayout(1,3));
      
      l_attachedFilesBox.setBounds(10, 270, 560, 30);
      filePanel.setBounds(10, 320, 560, 50);
      
      
      //JCheckBox attachedFilesBox = new JCheckBox();
      //JTextField replyOfField = new JTextField();
      
      //select open Range
      String[] openRange = {"Everyone", "My Followers", "Bi-Friends", "Nobody"};
      lodBox = new JComboBox<String>(openRange);
      JLabel l_lodBox = new JLabel("Level of Disclosure\n");
      l_lodBox.setFont(new Font("D2Coding", Font.BOLD, 30));
      
      l_lodBox.setBounds(10, 400, 560, 30);
      lodBox.setBounds(10, 450, 560, 30);
      
      
      //ButtonPanel
      JPanel selectPanel = new JPanel();
      JButton complete_btn, cancel_btn;
      JButton back_btn = new JButton("BACK");
      back_btn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             contentUploadFrame.dispose();
             CMmainFrame();
          }
      });
      
      
      // Make filepathlist
      filePathList = new ArrayList<String>();
      
      
      complete_btn = new JButton("OK");
      complete_btn.addActionListener(ActionListener);
      cancel_btn = new JButton("CANCEL");
      cancel_btn.addActionListener(ActionListener);
      
      
      selectPanel.add(complete_btn);
      selectPanel.add(cancel_btn);
      selectPanel.add(back_btn);
      selectPanel.setLayout(new GridLayout(1,2));
      selectPanel.setBounds(0, 615, 580, 30);
      

 

      
      
      contentUploadFrame.setLayout(null);
      contentUploadFrame.add(l_contentField);
      contentUploadFrame.add(contentField);
      contentUploadFrame.add(l_attachedFilesBox);
      contentUploadFrame.add(filePanel);
      contentUploadFrame.add(l_lodBox);
      contentUploadFrame.add(lodBox);
      contentUploadFrame.add(selectPanel);
      
      
      contentUploadFrame.setVisible(true);
      
      /*
      Object[] message = {
            "Content\n", contentField,
            "File Attachment: ", attachedFilesBox,
            //"Content ID to which this content replies(0 for no reply): ", replyOfField,
            //"Level of Disclosure(0: to everyone, 1: to my followers, 2: to bi-friends, 3: nobody): ", lodField
            "Level of Disclosure: ", lodBox
      };
      */
      return;
   }
   
   public String picUploadFrame() {
      File file = null;
      
      String strPath =null;
      JFileChooser fc = new JFileChooser();
      CMConfigurationInfo confInfo = clientStub.getCMInfo().getConfigurationInfo();
      File curDir = new File(confInfo.getTransferedFileHome().toString());
      fc.setCurrentDirectory(curDir);
      int fcRet = fc.showOpenDialog(this);
      if(fcRet == JFileChooser.APPROVE_OPTION)
      {
         file = fc.getSelectedFile();
         strPath = file.getPath();
         return strPath;
      }
      return strPath;
   }

   

   
   public void SNSContentUpload() {
      String strContent = null;
      
      int nReplyOf = 0;
      int nLevelOfDisclosure = 0;
      int nNumAttachedFiles = 0;
      strContent=contentField.getText();
      nLevelOfDisclosure=lodBox.getSelectedIndex();
      
      String strUser = clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
      nNumAttachedFiles = filePathList.size();
      clientStub.requestSNSContentUpload(strUser, strContent, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure, 
				filePathList);
      
   }
   
	public void mqttConnect()
	{
		String strWillTopic = strUserName.trim();
		String strWillMessage = strUserName.trim();
		boolean bWillRetain = true;
		byte willQoS = (byte) 2;
		boolean bWillFlag = true;
		boolean bCleanSession = false;
		Object[] msg = {
				"will Topic", strWillTopic,
				"will message", strWillMessage,
				"will retain", bWillRetain,
				"will QoS", willQoS,
				"will flag", bWillFlag,
				"clean session", bCleanSession
		};
		
		CMMqttManager mqttManager = (CMMqttManager) clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			System.out.println("CMMqttManager is null!\n");
			return;
		}
		mqttManager.connect(strWillTopic, strWillMessage, bWillRetain, willQoS, bWillFlag, 
				bCleanSession);
		
	}
   
   public class MyActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e)
      {
         JButton button = (JButton) e.getSource();
         if(button.getText().equals("LOGIN"))
         {
            login();
         }
         else if(button.getText().equals("REGISTER"))
         {
            register();
         }
         else if(button.getText().equals("DEREGISTER"))
         {
            deregister();
         }
         else if(button.getText().equals("FRIENDS"))
         {
            FriendsFrame();
            mainFrame.dispose();
         }
         else if(button.getText().equals("LOGOUT"))
         {
        	 LogoutDS(); 
         }
         else if(button.getText().equals("UPLOAD"))
         {
            SNSContentUploadFrame();
            mainFrame.dispose();
         }
         else if(button.getText().equals("ADD"))
           {
               String strFriendName = null;
               strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
               
               if(strFriendName != null) clientStub.addNewFriend(strFriendName);
               // 등록된 친구 MQTT 구독
               mqttSubscribe(strFriendName.trim());
               // ACK에 의해 친구 화면 업데이트
               
               return;
           }
         else if(button.getText().equals("DELETE"))
         {
             String strFriendName = null;
             strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
             
             if(strFriendName != null) clientStub.removeFriend(strFriendName);
             // 친구 목록에서 삭제
             mqttUnsubscribe(strFriendName.trim());
             // ACK에 의해 친구 화면 업데이트
             
             return;
         }
         else if(button.getText().equals("SEARCH"))
         {
            searchFriendFrame();
         }
         else if(button.getText().equals("내가 등록한 친구"))
         {
            requestFriendsList();
         }
         else if(button.getText().equals("나만 등록한 친구"))
         {
            requestFriendRequestersList();
         }
         else if(button.getText().equals("서로 친구"))
         {
            requestBiFriendsList();
         }
         else if(button.getText().equals("PICTURE 1"))
         {
            System.out.println("ù��° ���� ���ε�");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("PICTURE 2"))
         {
            System.out.println("�ι�° ���� ���ε�");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("PICTURE 3"))
         {
            System.out.println("����° ���� ���ε�");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("OK"))
         {
        	// Content 업로드 ok 버튼
            SNSContentUpload();
            filePathList=null;
            contentUploadFrame.dispose();
            SNSContentUploadFrame();
         }
         else if(button.getText().equals("CANCEL"))
         {
            System.out.println("content ���ε� ���");
            filePathList=null;
            CMmainFrame();
            contentUploadFrame.dispose();
         }
         
      }
   }
   
   public void mqttPublish()
	{
		
		JTextField topicTextField = new JTextField();
		JTextField messageTextField = new JTextField();
		String[] qosArray = {"0", "1", "2"};
		JComboBox<String> qosComboBox = new JComboBox<String>(qosArray);
		JCheckBox dupFlagBox = new JCheckBox();
		JCheckBox retainFlagBox = new JCheckBox();
		Object[] msg = {
				"topic", topicTextField,
				"message", messageTextField,
				"QoS", qosComboBox,
				"dup flag", dupFlagBox,
				"retain flag", retainFlagBox
		};
		int nRet = JOptionPane.showConfirmDialog(null, msg, "MQTT publish", 
				JOptionPane.OK_CANCEL_OPTION);
		if(nRet != JOptionPane.OK_OPTION) return;

		String strTopic = topicTextField.getText().trim();
		String strMessage = messageTextField.getText().trim();
		byte qos = (byte) qosComboBox.getSelectedIndex();
		boolean bDupFlag = dupFlagBox.isSelected();
		boolean bRetainFlag = retainFlagBox.isSelected();
		
		CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			//printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		//mqttManager.publish(1, "/CM/test", "This is a test message.", (byte)1);
		mqttManager.publish(strTopic, strMessage, qos, bDupFlag, bRetainFlag);
	}
	
	public void mqttSubscribe(String name)
	{

		String strTopicFilter = name;
		byte qos = (byte) 2;

		CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			//printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		mqttManager.subscribe(strTopicFilter, qos);
	}
	
	public void mqttUnsubscribe(String name)
	{
		
		String strTopic = name.trim();
		if(strTopic == null || strTopic.equals("")) 
			return;

		CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
		if(mqttManager == null)
		{
			//printStyledMessage("CMMqttManager is null!\n", "bold");
			return;
		}
		mqttManager.unsubscribe(strTopic);
	}
   public void startCM()
	{
		boolean bRet = false;
		
		// get current server info from the server configuration file
		String strCurServerAddress = null;
		int nCurServerPort = -1;
		
		strCurServerAddress = clientStub.getServerAddress();
		nCurServerPort = clientStub.getServerPort();
		
		clientStub.setServerInfo(strCurServerAddress, nCurServerPort);

		
		bRet = clientStub.startCM();
		if(!bRet)
		{
			JOptionPane.showMessageDialog(null, "회원가입 성공");
		}
		else
		{
			this.LoginFrame();
		}
	}
   
   public void LogoutDS()
	{
		boolean bRequestResult = false;
		bRequestResult = clientStub.logoutCM();
		if(bRequestResult)
			JOptionPane.showMessageDialog(null, "로그아웃 되었습니다.");
		else {
			//printStyledMessage("failed the logout request!\n", "bold");
		}
	}
   public static void main(String[] args) {
      IngstaClient client = new IngstaClient();
      CMClientStub cmStub = client.getClientStub();
      cmStub.setAppEventHandler(client.getClientEventHandler());
      client.startCM();
      
   }
   
   

}