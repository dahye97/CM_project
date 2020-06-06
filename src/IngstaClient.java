<<<<<<< HEAD
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class IngstaClient extends JFrame {
   
   MyActionListener ActionListener = new MyActionListener();
   CMClientStub clientStub = new CMClientStub();
   IngstaClientEventHandler eventHandler = new IngstaClientEventHandler (clientStub, this);
   
   private String strUserName;
   private JFrame loginFrame, mainFrame, friendFrame, contentUploadFrame, searchFriendFrame;
   private JButton add, del, search, refresh;
   private BevelBorder border;
   private JScrollPane scrollPane;
   private JTextArea friendList;
   private JTextArea contentList;
   private ArrayList<String> filePathList;
   private JTextField contentField;
   private JComboBox<String> lodBox;
   private int nNumAttachedFiles;
   private String[] friendName = null;
   private boolean isActivated = false;
   
   
   ///////////////////////////////////////////////////////////
   
   public JFrame getLoginFrame() {
      return this.loginFrame;
   }
   
   public JFrame getFriendFrame() {
      return this.friendFrame;
   }
   
   public void setFriendList(String friend) {
      this.friendList = new JTextArea();
      this.friendList.append(friend);
      return;
   }
   
   public void setContentList(String content) {
      this.contentList = new JTextArea();
      this.contentList.append(content);
      return;
   }
   
   public void setFriendName(String[] friend) {
      this.friendName = friend;
   }
   
   public CMClientStub getClientStub() {
      return clientStub;
   }
   
   public IngstaClientEventHandler getClientEventHandler() {
      return eventHandler;
   }
   

   ///////////////////////////////////////////////////////////
   
   
   
   //로그인 화면
   public void LoginFrame() {
      
       //Frame
      Font font = new Font("맑은고딕", Font.BOLD, 23);
      loginFrame = new JFrame("LOGIN");
      loginFrame.setBounds(200,50,600,800);
      loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      loginFrame.setBackground(new Color(255, 204, 203));
      loginFrame.setLayout(null);
       
      JPanel titlepanel = new JPanel();
      JLabel title = new JLabel();
      title.setText("I N G S T A G R A M");
      title.setFont(font);
      title.setBounds(100,300,600,400);
      titlepanel.add(title);
      titlepanel.setBackground(new Color(255, 204, 203));
      titlepanel.setSize(600,300);
      titlepanel.setBounds(0,0,600,500);
       
      JPanel logpanel = new JPanel();
      logpanel.setLayout(new GridLayout(1,3));
      logpanel.setSize(580,260);
      logpanel.setBounds(0,500,580,260);
      
      JButton login = new JButton("LOGIN");
      JButton register = new JButton("REGISTER");
      JButton deregister = new JButton("DEREGISTER");
       
      login.setBackground(new Color(255,164,162));
      register.setBackground(new Color(255,164,162));
      deregister.setBackground(new Color(255,164,162));
      login.addActionListener(ActionListener);
      register.addActionListener(ActionListener);
      deregister.addActionListener(ActionListener);
       
      logpanel.add(login);
      logpanel.add(register);
      logpanel.add(deregister);
      
      loginFrame.add(titlepanel);
      loginFrame.add(logpanel);
      loginFrame.setVisible(true);
      
   }
      
    
   
   //로그인
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
         if (option == JOptionPane.OK_OPTION) {
            strUserName = userNameField.getText();
            strPassword = new String(passwordField.getPassword());
         
            eventHandler.setStartTime(System.currentTimeMillis());
            bRequestResult = clientStub.loginCM(strUserName, strPassword);
            if(bRequestResult) {
            System.out.println("successfully sent the login request.\n");
            
            }
            else {
               System.out.println("failed the login request!\n");
               eventHandler.setStartTime(0);
            }
      }
   }
   
   
   //회원가입
   public void register() {
     String strName = null;
     String strPasswd = null;
     String strRePasswd = null;
     
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
     strPasswd = new String(passwordField.getPassword());   
     strRePasswd = new String(rePasswordField.getPassword());
   
     if(!strPasswd.equals(strRePasswd)) {
        System.out.println("Password input error!");
        return;
     }
   
        clientStub.registerUser(strName, strPasswd);
        
       return;
   }
   
   
   //탈퇴
   public void deregister() {
      String strName = null;
      String strPasswd = null;
     
      JTextField nameField = new JTextField();
      JPasswordField passwdField = new JPasswordField();
      Object[] message = {
            "Input User Name: ", nameField,
            "Input Password: ", passwdField
      };
      int option = JOptionPane.showConfirmDialog(null, message, "User Deregistration", JOptionPane.OK_CANCEL_OPTION);
      if(option != JOptionPane.OK_OPTION) return;
      strName = nameField.getText();
      strPasswd = new String(passwdField.getPassword()); 
      
      clientStub.deregisterUser(strName, strPasswd);
      
      return;
   }
   
   
   
   //초기화
   public void init() {
    //친구 list 요청 (clientStub -> server -> clientEH)
      clientStub.requestFriendsList();
      DownloadNewSNSContent();
      if(friendName != null && !isActivated) {
         for(int i=0; i< friendName.length; i++) {
            mqttSubscribe(friendName[i]);
         }
         isActivated = true;
      }
   }
   
   
   
   //메인 화면
   public void CMmainFrame() {
      //MainFrame
      mainFrame = new JFrame("CM_SNS");
      mainFrame.setBounds(200,50,600,800);
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      
      //UserPanel (이름, my content, 로그아웃)
      JPanel userPanel = new JPanel();
      userPanel.setLayout(new GridLayout(1,3));
      
      JLabel userName = new JLabel(strUserName);
      userName.setFont(new Font("D2Coding", Font.BOLD, 20));
      
      JButton refresh = new JButton("UPDATE");
      refresh.addActionListener(ActionListener);
      JButton logout = new JButton("LOGOUT");
      logout.addActionListener(ActionListener);
      
      userPanel.add(userName);
      userPanel.add(refresh);
      userPanel.add(logout);
     
      
      ImagePanel panel = new ImagePanel();

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new GridLayout(1,2));
      
      JButton friends_btn = new JButton("FRIENDS");
      JButton upload_btn = new JButton("UPLOAD");
      
      friends_btn.addActionListener(ActionListener);
      upload_btn.addActionListener(ActionListener);
      
      buttonPanel.add(friends_btn);
      buttonPanel.add(upload_btn);
      
      
      mainFrame.add(userPanel, BorderLayout.NORTH);
      mainFrame.add(panel, BorderLayout.CENTER);
      mainFrame.add(buttonPanel, BorderLayout.SOUTH);
      mainFrame.setVisible(true);      
   }
   
   
   
   public void DownloadNewSNSContent()
   {
      eventHandler.setStartTime(System.currentTimeMillis());
      
      if(friendName == null) clientStub.requestSNSContent(strUserName, 0);
      else {
         if(friendName.length == 0) clientStub.requestSNSContent(strUserName, 0);
         else clientStub.requestSNSContent("CM_MY_FRIEND", 0);
      }
      
      if(CMInfo._CM_DEBUG) {
         System.out.println("["+strUserName+"] requests content of friends.\n");
      }

      return;
   }
   
   
   
   //친구 화면
   public void FriendsFrame() {
     //친구 요청
     clientStub.requestFriendsList();
      
     border = new BevelBorder(BevelBorder.RAISED);
            
     //Frame
     friendFrame = new JFrame("FRIEND");
     friendFrame.setBounds(200,50,600,800);
     friendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     friendFrame.setLayout(null);
      
      //List Label (번호, 친구 이름)
      JLabel num = new JLabel("NO.");
      num.setBounds(10, 8, 275, 50);
      num.setBorder(border);
      num.setHorizontalAlignment(JLabel.CENTER);
      
      JLabel name = new JLabel("Name");
      name.setBounds(295, 8, 275, 50);
      name.setBorder(border);
      name.setHorizontalAlignment(JLabel.CENTER);
      
      
      //친구 목록
      friendList.setFont(new Font("D2Coding", Font.BOLD, 17));
      //friendList.append(eventHandler.getFriendList());
      friendList.setEditable(false);
      scrollPane = new JScrollPane(friendList);
      scrollPane.setBounds(10, 65, 560, 420);
      
      
      //Button
      add = new JButton("ADD");
      add.addActionListener(ActionListener);
      add.setBounds(10, 500, 180, 45);

      del = new JButton("DELETE");
      del.addActionListener(ActionListener);
      del.setBounds(200, 500, 180, 45);
      
      search = new JButton("SEARCH");
      search.addActionListener(ActionListener);
      search.setBounds(390, 500, 180, 45);
      
      refresh = new JButton("REFRESH");
      refresh.setBounds(390, 570, 180, 45);
      refresh.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             System.out.println("REFRESH");
             DownloadNewSNSContent();
             friendFrame.dispose();
             FriendsFrame();
          }
      });

      JButton back = new JButton("BACK");
      back.addActionListener(ActionListener);
      back.setBounds(10, 570, 180, 45);
      back.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             System.out.println("BACK");
              friendFrame.dispose();
              DownloadNewSNSContent();
              CMmainFrame();
          }
      });
      
      friendFrame.add(num);
      friendFrame.add(name);
      friendFrame.add(scrollPane);
      friendFrame.add(add);
      friendFrame.add(del);
      friendFrame.add(search);
      friendFrame.add(refresh);
      friendFrame.add(back);
      friendFrame.setVisible(true);
   }
   
   
   
   //친구 검색 화면
   public void searchFriendFrame() {
      //Frame 
      searchFriendFrame = new JFrame("SEARCH FRIEND LIST");
      searchFriendFrame.setBounds(200,50,600,800);
      searchFriendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      searchFriendFrame.setLayout(null);
      
      //List Label (번호, 친구 이름)
      JLabel num = new JLabel("NO.");
      num.setBounds(10, 8, 275, 50);
      num.setBorder(border);
      num.setHorizontalAlignment(JLabel.CENTER);
      
      JLabel name = new JLabel("Name");
      name.setBounds(295, 8, 275, 50);
      name.setBorder(border);
      name.setHorizontalAlignment(JLabel.CENTER);
      
       //친구 목록      
      friendList.setFont(new Font("D2Coding", Font.BOLD, 17));
      //friendList.append(eventHandler.getFriendList());
      friendList.setEditable(false);
      scrollPane = new JScrollPane(friendList);
      scrollPane.setBounds(10, 65, 560, 420);
      
      //Button
      JButton b1 = new JButton("나만 등록한 친구");
      b1.addActionListener(ActionListener);
      b1.setBounds(10, 500, 180, 45);
      
      JButton b2 = new JButton("나를 등록한 친구");
      b2.addActionListener(ActionListener);
      b2.setBounds(200, 500, 180, 45);
      
      JButton b3 = new JButton("같이 등록한 친구");
      b3.addActionListener(ActionListener);
      b3.setBounds(390, 500, 180, 45);
      
      JButton back = new JButton("BACK");
      back.setBounds(10, 570, 180, 45);
      back.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             searchFriendFrame.dispose(); 
             FriendsFrame();
          }
      });
      
      searchFriendFrame.add(num);
      searchFriendFrame.add(name);
      searchFriendFrame.add(b1);
      searchFriendFrame.add(b2);
      searchFriendFrame.add(b3);
      searchFriendFrame.add(scrollPane);
      searchFriendFrame.add(back);
      searchFriendFrame.setVisible(true);
   }
   
   
   
   //업로드 화면
   public void SNSContentUploadFrame() {
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
      pic2 = new JButton("PICTURE 2");
      pic3 = new JButton("PICTURE 3");
      pic1.addActionListener(ActionListener);
      pic2.addActionListener(ActionListener);
      pic3.addActionListener(ActionListener);
      
      filePanel.add(pic1);
      filePanel.add(pic2);
      filePanel.add(pic3);
      filePanel.setLayout(new GridLayout(1,3));
      
      l_attachedFilesBox.setBounds(10, 270, 560, 30);
      filePanel.setBounds(10, 320, 560, 50);
      
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
             DownloadNewSNSContent();
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
      
      return;
   }
   
   
   
   //사진 업로드 화면
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
         nNumAttachedFiles++;
         return strPath;
      }
      return strPath;
   }

   
   
   public void SNSContentUpload() {
      String strContent = null;      
      int nReplyOf = 0;
      int nLevelOfDisclosure;
      
      strContent=contentField.getText();
      nLevelOfDisclosure=lodBox.getSelectedIndex();
      
      String strUser = clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
      nNumAttachedFiles = filePathList.size();
      clientStub.requestSNSContentUpload(strUser, strContent, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure, filePathList);
   }
   
   
   
   public void mqttConnect() {
      String strWillTopic = strUserName.trim();
      String strWillMessage = strUserName.trim();
      boolean bWillRetain = true;
      byte willQoS = (byte) 2;
      boolean bWillFlag = true;
      boolean bCleanSession = false;
      CMMqttManager mqttManager = (CMMqttManager) clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null)
      {
         System.out.println("CMMqttManager is null!\n");
         return;
      }
      mqttManager.connect(strWillTopic, strWillMessage, bWillRetain, willQoS, bWillFlag, bCleanSession);
   }
   
   
   public void mqttPublish(String str) {
      String strTopic = str;
      String strMessage = str;
      byte qos = (byte) 2;
      boolean bDupFlag = true;
      boolean bRetainFlag = true;
      
      CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null) {
         System.out.println("CMMqttManager is null!\n");
         return;
      }
      mqttManager.publish(strTopic, strMessage, qos, bDupFlag, bRetainFlag);
   }
   
   
   
   public void mqttSubscribe(String name) {
      String strTopicFilter = name;
      byte qos = (byte) 2;

      CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null) {
         System.out.println("CMMqttManager is null!\n");
         return;
      }
      mqttManager.subscribe(strTopicFilter, qos);
   }
   
   
   
   public void mqttUnsubscribe(String name) {
      String strTopic = name.trim();
      if(strTopic == null || strTopic.equals("")) 
         return;

      CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null) {
         System.out.println("CMMqttManager is null!\n");
         return;
      }
      mqttManager.unsubscribe(strTopic);
   }
   
   
   public void startCM() {
      boolean bRet = false;
      
      String strCurServerAddress = null;
      int nCurServerPort = -1;
      
      strCurServerAddress = clientStub.getServerAddress();
      nCurServerPort = clientStub.getServerPort();      
      clientStub.setServerInfo(strCurServerAddress, nCurServerPort);
      
      bRet = clientStub.startCM();
      if(!bRet) {
         JOptionPane.showMessageDialog(null, "회원가입 성공");
      }
      else {
         this.LoginFrame();
      }
   }
   
   
   
   //로그아웃
   public void logout() {
   boolean bRequestResult = false;
   bRequestResult = clientStub.logoutCM();
      
   if(bRequestResult) 
      System.out.println("successfully sent the logout request.\n");
   else 
      System.out.println("failed the logout request!\n");
   }

   
   
   //ActionListener
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
         else if(button.getText().equals("UPDATE"))
         {
            init();
            eventHandler.sleep(500);            
            mainFrame.dispose();
            CMmainFrame();
         }
         else if(button.getText().equals("LOGOUT"))
         {
            logout();
            LoginFrame();
            mainFrame.dispose();
         }

         else if(button.getText().equals("UPLOAD"))
         {
           mainFrame.dispose();
           filePathList = null;
           nNumAttachedFiles = 0;
            SNSContentUploadFrame();
         }
         else if(button.getText().equals("ADD"))
           {
              String strFriendName = null;
              strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
              if(strFriendName != null) clientStub.addNewFriend(strFriendName);
              // 등록된 친구 MQTT 구독
              mqttSubscribe(strFriendName.trim());
              clientStub.requestFriendsList();
              
              return;
           }
         else if(button.getText().equals("DELETE"))
         {
             String strFriendName = null;
             strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
             if(strFriendName != null) clientStub.removeFriend(strFriendName);
             // 친구 목록에서 삭제
             mqttUnsubscribe(strFriendName.trim());
             clientStub.requestFriendsList();
             
             return;
         }
         else if(button.getText().equals("SEARCH"))
         {
            friendFrame.dispose();
            searchFriendFrame();
         }
         else if(button.getText().equals("나만 등록한 친구"))
         {
            clientStub.requestFriendsList();
              eventHandler.sleep(500);
            searchFriendFrame.dispose();  
            searchFriendFrame();
            
         }
         else if(button.getText().equals("나를 등록한 친구"))
         {
            clientStub.requestFriendRequestersList();
            eventHandler.sleep(500);
            searchFriendFrame.dispose(); 
            searchFriendFrame();
         }
         else if(button.getText().equals("같이 등록한 친구"))
         {
            clientStub.requestBiFriendsList();
            eventHandler.sleep(500);
            searchFriendFrame.dispose();  
            searchFriendFrame();
         }
         else if(button.getText().equals("PICTURE 1"))
         {
            System.out.println("PICTURE 1");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("PICTURE 2"))
         {
            System.out.println("PICTURE 2");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("PICTURE 3"))
         {
            System.out.println("PICTURE 3");
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
            System.out.println("CANCEL");
            filePathList=null;
            //init();
            CMmainFrame();
            contentUploadFrame.dispose();
         }
         
      }
   }
   
   public void requestAttachedFile(String strFileName)
   {      
      boolean bRet = clientStub.requestAttachedFileOfSNSContent(strFileName);
      if(bRet) eventHandler.setReqAttachedFile(true);
      return;
   }
   

   public static void main(String[] args) {
      IngstaClient client = new IngstaClient();
      CMClientStub cmStub = client.getClientStub();
      cmStub.setAppEventHandler(client.getClientEventHandler());
      
      client.startCM();
      
   }
   
   
   
   public class ImagePanel extends JPanel{
       private Image img;
       private BufferedImage image;
       private contentList cl = new contentList();
       private ArrayList<contentList> cont = new ArrayList<contentList>();
       private ArrayList<File> file = new ArrayList<File>();
       
       public ImagePanel() {
          cont = eventHandler.getContentList();
       }
       
       public BufferedImage loadImage(File imagePath) {
           image = null;
           try {
               image = ImageIO.read(imagePath);
           } catch (IOException e) {
               e.printStackTrace();
           }
           return image;
       }
       
       @Override
       public void paintComponent(Graphics g) {
          super.paintComponent(g);
          
          for(int i=0; i<cont.size(); i++) {
             cl = cont.get(i);
             file = cl.file;
             
             g.setColor(new Color(255, 204, 203));
             g.fillRect(10, 10+(225*i), 560, 210);
             g.setColor(Color.black);
             g.setFont(new Font("D2Coding", Font.BOLD, 15));
             g.drawString("WRITER : "+cl.writer, 20, 30+(225*i));
             g.drawString(cl.date, 20, 50+(225*i));
             g.drawString(cl.content, 20, 80+(225*i));
             
             System.out.println(file.size());
             for(int k=0; k<file.size(); k++) {
                this.image = loadImage(file.get(k));
                g.drawImage(this.image, 20+(80*k), 130+(225*i), 70, 70, this);
             }
          
          }
       }
    }
=======
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class IngstaClient extends JFrame {
   
   MyActionListener ActionListener = new MyActionListener();
   CMClientStub clientStub = new CMClientStub();
   IngstaClientEventHandler eventHandler = new IngstaClientEventHandler (clientStub, this);
   
   private String strUserName;
   private JFrame loginFrame, mainFrame, friendFrame, contentUploadFrame, searchFriendFrame;
   private JButton add, del, search, refresh;
   private BevelBorder border;
   private JScrollPane scrollPane;
   private JTextArea friendList;
   private JTextArea contentList;
   private ArrayList<String> filePathList;
   private JTextField contentField;
   private JComboBox<String> lodBox;
   private int nNumAttachedFiles;
   private String[] friendName = null;
   private boolean isActivated = false;
   
   
   ///////////////////////////////////////////////////////////
   
   public JFrame getLoginFrame() {
	   return this.loginFrame;
   }
   
   public JFrame getFriendFrame() {
	   return this.friendFrame;
   }
   
   public void setFriendList(String friend) {
	   this.friendList = new JTextArea();
	   this.friendList.append(friend);
	   return;
   }
   
   public void setContentList(String content) {
	   this.contentList = new JTextArea();
	   this.contentList.append(content);
	   return;
   }
   
   public void setFriendName(String[] friend) {
	   this.friendName = friend;
   }
   
   public CMClientStub getClientStub() {
	   return clientStub;
   }
   
   public IngstaClientEventHandler getClientEventHandler() {
	   return eventHandler;
   }
   

   ///////////////////////////////////////////////////////////
   
   
   
   //로그인 화면
   public void LoginFrame() {
	   
	    //Frame
		Font font = new Font("맑은고딕", Font.BOLD, 23);
		loginFrame = new JFrame("LOGIN");
		loginFrame.setBounds(200,50,600,800);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.setBackground(new Color(255, 204, 203));
		loginFrame.setLayout(null);
		 
		JPanel titlepanel = new JPanel();
		JLabel title = new JLabel();
		title.setText("I N G S T A G R A M");
		title.setFont(font);
		title.setBounds(100,300,600,400);
		titlepanel.add(title);
		titlepanel.setBackground(new Color(255, 204, 203));
		titlepanel.setSize(600,300);
		titlepanel.setBounds(0,0,600,500);
		 
		JPanel logpanel = new JPanel();
		logpanel.setLayout(new GridLayout(1,3));
		logpanel.setSize(580,260);
		logpanel.setBounds(0,500,580,260);
		
		JButton login = new JButton("LOGIN");
		JButton register = new JButton("REGISTER");
		JButton deregister = new JButton("DEREGISTER");
		 
		login.setBackground(new Color(255,164,162));
		register.setBackground(new Color(255,164,162));
		deregister.setBackground(new Color(255,164,162));
		login.addActionListener(ActionListener);
		register.addActionListener(ActionListener);
		deregister.addActionListener(ActionListener);
		 
		logpanel.add(login);
		logpanel.add(register);
		logpanel.add(deregister);
		
		loginFrame.add(titlepanel);
		loginFrame.add(logpanel);
		loginFrame.setVisible(true);
		
	}
      
    
   
   //로그인
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
      	if (option == JOptionPane.OK_OPTION) {
      		strUserName = userNameField.getText();
      		strPassword = new String(passwordField.getPassword());
         
      		eventHandler.setStartTime(System.currentTimeMillis());
      		bRequestResult = clientStub.loginCM(strUserName, strPassword);
      		if(bRequestResult) {
            System.out.println("successfully sent the login request.\n");
            
            }
      		else {
      			System.out.println("failed the login request!\n");
      			eventHandler.setStartTime(0);
      		}
      }
   }
   
   
   //회원가입
   public void register() {
	  String strName = null;
	  String strPasswd = null;
	  String strRePasswd = null;
	  
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
	  strPasswd = new String(passwordField.getPassword());   
	  strRePasswd = new String(rePasswordField.getPassword());
	
	  if(!strPasswd.equals(strRePasswd)) {
	     System.out.println("Password input error!");
	     return;
	  }
	
	  	clientStub.registerUser(strName, strPasswd);
	     
	 	return;
   }
   
   
   //탈퇴
   public void deregister() {
      String strName = null;
      String strPasswd = null;
     
      JTextField nameField = new JTextField();
      JPasswordField passwdField = new JPasswordField();
      Object[] message = {
            "Input User Name: ", nameField,
            "Input Password: ", passwdField
      };
      int option = JOptionPane.showConfirmDialog(null, message, "User Deregistration", JOptionPane.OK_CANCEL_OPTION);
      if(option != JOptionPane.OK_OPTION) return;
      strName = nameField.getText();
      strPasswd = new String(passwdField.getPassword()); 
      
      clientStub.deregisterUser(strName, strPasswd);
      
      return;
   }
   
   
   
   //초기화
   public void init() {
	 //친구 list 요청 (clientStub -> server -> clientEH)
	   clientStub.requestFriendsList();
	   DownloadNewSNSContent();
	   if(friendName != null && !isActivated) {
		   for(int i=0; i< friendName.length; i++) {
			   mqttSubscribe(friendName[i]);
		   }
		   isActivated = true;
	   }
   }
   
   
   
   //메인 화면
   public void CMmainFrame() {
      //MainFrame
      mainFrame = new JFrame("CM_SNS");
      mainFrame.setBounds(200,50,600,800);
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      
      //UserPanel (이름, my content, 로그아웃)
      JPanel userPanel = new JPanel();
      userPanel.setLayout(new GridLayout(1,3));
      
      JLabel userName = new JLabel(strUserName);
      userName.setFont(new Font("D2Coding", Font.BOLD, 20));
      
      JButton refresh = new JButton("UPDATE");
      refresh.addActionListener(ActionListener);
      JButton logout = new JButton("LOGOUT");
      logout.addActionListener(ActionListener);
      
      userPanel.add(userName);
      userPanel.add(refresh);
      userPanel.add(logout);
     
      
      ImagePanel panel = new ImagePanel();

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new GridLayout(1,2));
      
      JButton friends_btn = new JButton("FRIENDS");
      JButton upload_btn = new JButton("UPLOAD");
      
      friends_btn.addActionListener(ActionListener);
      upload_btn.addActionListener(ActionListener);
      
      buttonPanel.add(friends_btn);
      buttonPanel.add(upload_btn);
      
      
      mainFrame.add(userPanel, BorderLayout.NORTH);
      mainFrame.add(panel, BorderLayout.CENTER);
      mainFrame.add(buttonPanel, BorderLayout.SOUTH);
      mainFrame.setVisible(true);      
   }
   
   
   
   public void DownloadNewSNSContent()
	{
		eventHandler.setStartTime(System.currentTimeMillis());
		
		if(friendName == null) clientStub.requestSNSContent(strUserName, 0);
		else {
			if(friendName.length == 0) clientStub.requestSNSContent(strUserName, 0);
			else clientStub.requestSNSContent("CM_MY_FRIEND", 0);
		}
		
		if(CMInfo._CM_DEBUG) {
			System.out.println("["+strUserName+"] requests content of friends.\n");
		}

		return;
	}
   
   
   
   //친구 화면
   public void FriendsFrame() {
	  //친구 요청
	  clientStub.requestFriendsList();
	   
	  border = new BevelBorder(BevelBorder.RAISED);
            
	  //Frame
	  friendFrame = new JFrame("FRIEND");
	  friendFrame.setBounds(200,50,600,800);
	  friendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  friendFrame.setLayout(null);
      
      //List Label (번호, 친구 이름)
      JLabel num = new JLabel("NO.");
      num.setBounds(10, 8, 275, 50);
      num.setBorder(border);
      num.setHorizontalAlignment(JLabel.CENTER);
      
      JLabel name = new JLabel("Name");
      name.setBounds(295, 8, 275, 50);
      name.setBorder(border);
      name.setHorizontalAlignment(JLabel.CENTER);
      
      
      //친구 목록
      friendList.setFont(new Font("D2Coding", Font.BOLD, 17));
      //friendList.append(eventHandler.getFriendList());
      friendList.setEditable(false);
      scrollPane = new JScrollPane(friendList);
      scrollPane.setBounds(10, 65, 560, 420);
      
      
      //Button
      add = new JButton("ADD");
      add.addActionListener(ActionListener);
      add.setBounds(10, 500, 180, 45);

      del = new JButton("DELETE");
      del.addActionListener(ActionListener);
      del.setBounds(200, 500, 180, 45);
      
      search = new JButton("SEARCH");
      search.addActionListener(ActionListener);
      search.setBounds(390, 500, 180, 45);
      
      refresh = new JButton("REFRESH");
      refresh.setBounds(390, 570, 180, 45);
      refresh.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             System.out.println("REFRESH");
             DownloadNewSNSContent();
             friendFrame.dispose();
             FriendsFrame();
          }
      });

      JButton back = new JButton("BACK");
      back.addActionListener(ActionListener);
      back.setBounds(10, 570, 180, 45);
      back.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
        	  System.out.println("BACK");
              friendFrame.dispose();
              DownloadNewSNSContent();
              CMmainFrame();
          }
      });
      
      friendFrame.add(num);
      friendFrame.add(name);
      friendFrame.add(scrollPane);
      friendFrame.add(add);
      friendFrame.add(del);
      friendFrame.add(search);
      friendFrame.add(refresh);
      friendFrame.add(back);
      friendFrame.setVisible(true);
   }
   
   
   
   //친구 검색 화면
   public void searchFriendFrame() {
      //Frame 
      searchFriendFrame = new JFrame("SEARCH FRIEND LIST");
      searchFriendFrame.setBounds(200,50,600,800);
      searchFriendFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      searchFriendFrame.setLayout(null);
      
      //List Label (번호, 친구 이름)
      JLabel num = new JLabel("NO.");
      num.setBounds(10, 8, 275, 50);
      num.setBorder(border);
      num.setHorizontalAlignment(JLabel.CENTER);
      
      JLabel name = new JLabel("Name");
      name.setBounds(295, 8, 275, 50);
      name.setBorder(border);
      name.setHorizontalAlignment(JLabel.CENTER);
      
       //친구 목록      
      friendList.setFont(new Font("D2Coding", Font.BOLD, 17));
      //friendList.append(eventHandler.getFriendList());
      friendList.setEditable(false);
      scrollPane = new JScrollPane(friendList);
      scrollPane.setBounds(10, 65, 560, 420);
      
      //Button
      JButton b1 = new JButton("나만 등록한 친구");
      b1.addActionListener(ActionListener);
      b1.setBounds(10, 500, 180, 45);
      
      JButton b2 = new JButton("나를 등록한 친구");
      b2.addActionListener(ActionListener);
      b2.setBounds(200, 500, 180, 45);
      
      JButton b3 = new JButton("같이 등록한 친구");
      b3.addActionListener(ActionListener);
      b3.setBounds(390, 500, 180, 45);
      
      JButton back = new JButton("BACK");
      back.setBounds(10, 570, 180, 45);
      back.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e)
          {
             searchFriendFrame.dispose(); 
             FriendsFrame();
          }
      });
      
      searchFriendFrame.add(num);
      searchFriendFrame.add(name);
      searchFriendFrame.add(b1);
      searchFriendFrame.add(b2);
      searchFriendFrame.add(b3);
      searchFriendFrame.add(scrollPane);
      searchFriendFrame.add(back);
      searchFriendFrame.setVisible(true);
   }
   
   
   
   //업로드 화면
   public void SNSContentUploadFrame() {
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
      pic2 = new JButton("PICTURE 2");
      pic3 = new JButton("PICTURE 3");
      pic1.addActionListener(ActionListener);
      pic2.addActionListener(ActionListener);
      pic3.addActionListener(ActionListener);
      
      filePanel.add(pic1);
      filePanel.add(pic2);
      filePanel.add(pic3);
      filePanel.setLayout(new GridLayout(1,3));
      
      l_attachedFilesBox.setBounds(10, 270, 560, 30);
      filePanel.setBounds(10, 320, 560, 50);
      
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
        	  DownloadNewSNSContent();
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
      
      return;
   }
   
   
   
   //사진 업로드 화면
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
         nNumAttachedFiles++;
         return strPath;
      }
      return strPath;
   }

   
   
   public void SNSContentUpload() {
      String strContent = null;      
      int nReplyOf = 0;
      int nLevelOfDisclosure;
      
      strContent=contentField.getText();
      nLevelOfDisclosure=lodBox.getSelectedIndex();
      
      String strUser = clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
      nNumAttachedFiles = filePathList.size();
      clientStub.requestSNSContentUpload(strUser, strContent, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure, filePathList);
   }
   
   
   
   public void mqttConnect() {
      String strWillTopic = strUserName.trim();
      String strWillMessage = strUserName.trim();
      boolean bWillRetain = true;
      byte willQoS = (byte) 2;
      boolean bWillFlag = true;
      boolean bCleanSession = false;
      CMMqttManager mqttManager = (CMMqttManager) clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null)
      {
         System.out.println("CMMqttManager is null!\n");
         return;
      }
      mqttManager.connect(strWillTopic, strWillMessage, bWillRetain, willQoS, bWillFlag, bCleanSession);
   }
   
   
   public void mqttPublish(String str) {
      String strTopic = str;
      String strMessage = str;
      byte qos = (byte) 2;
      boolean bDupFlag = true;
      boolean bRetainFlag = true;
      
      CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null) {
         System.out.println("CMMqttManager is null!\n");
         return;
      }
      mqttManager.publish(strTopic, strMessage, qos, bDupFlag, bRetainFlag);
   }
   
   
   
   public void mqttSubscribe(String name) {
      String strTopicFilter = name;
      byte qos = (byte) 2;

      CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null) {
    	  System.out.println("CMMqttManager is null!\n");
    	  return;
      }
      mqttManager.subscribe(strTopicFilter, qos);
   }
   
   
   
   public void mqttUnsubscribe(String name) {
      String strTopic = name.trim();
      if(strTopic == null || strTopic.equals("")) 
    	  return;

      CMMqttManager mqttManager = (CMMqttManager)clientStub.findServiceManager(CMInfo.CM_MQTT_MANAGER);
      if(mqttManager == null) {
    	  System.out.println("CMMqttManager is null!\n");
    	  return;
      }
      mqttManager.unsubscribe(strTopic);
   }
   
   
   public void startCM() {
      boolean bRet = false;
      
      String strCurServerAddress = null;
      int nCurServerPort = -1;
      
      strCurServerAddress = clientStub.getServerAddress();
      nCurServerPort = clientStub.getServerPort();      
      clientStub.setServerInfo(strCurServerAddress, nCurServerPort);
      
      bRet = clientStub.startCM();
      if(!bRet) {
         JOptionPane.showMessageDialog(null, "회원가입 성공");
      }
      else {
         this.LoginFrame();
      }
   }
   
   
   
   //로그아웃
   public void logout() {
	boolean bRequestResult = false;
	bRequestResult = clientStub.logoutCM();
	   
	if(bRequestResult) 
		System.out.println("successfully sent the logout request.\n");
	else 
		System.out.println("failed the logout request!\n");
   }

   
   
   //ActionListener
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
         else if(button.getText().equals("UPDATE"))
         {
        	 init();
        	 eventHandler.sleep(500);        	 
        	 mainFrame.dispose();
        	 CMmainFrame();
         }
         else if(button.getText().equals("LOGOUT"))
         {
            logout();
            LoginFrame();
            mainFrame.dispose();
         }

         else if(button.getText().equals("UPLOAD"))
         {
        	mainFrame.dispose();
        	filePathList = null;
        	nNumAttachedFiles = 0;
            SNSContentUploadFrame();
         }
         else if(button.getText().equals("ADD"))
           {
	           String strFriendName = null;
	           strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
	           if(strFriendName != null) clientStub.addNewFriend(strFriendName);
	           // 등록된 친구 MQTT 구독
	           mqttSubscribe(strFriendName.trim());
	           clientStub.requestFriendsList();
	           
	           return;
           }
         else if(button.getText().equals("DELETE"))
         {
             String strFriendName = null;
             strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
             if(strFriendName != null) clientStub.removeFriend(strFriendName);
             // 친구 목록에서 삭제
             mqttUnsubscribe(strFriendName.trim());
             clientStub.requestFriendsList();
             
             return;
         }
         else if(button.getText().equals("SEARCH"))
         {
        	 friendFrame.dispose();
        	 searchFriendFrame();
         }
         else if(button.getText().equals("나만 등록한 친구"))
         {
            clientStub.requestFriendsList();
       	 	eventHandler.sleep(500);
            searchFriendFrame.dispose();  
            searchFriendFrame();
            
         }
         else if(button.getText().equals("나를 등록한 친구"))
         {
        	 clientStub.requestFriendRequestersList();
        	 eventHandler.sleep(500);
        	 searchFriendFrame.dispose(); 
        	 searchFriendFrame();
         }
         else if(button.getText().equals("같이 등록한 친구"))
         {
        	 clientStub.requestBiFriendsList();
        	 eventHandler.sleep(500);
        	 searchFriendFrame.dispose();  
        	 searchFriendFrame();
         }
         else if(button.getText().equals("PICTURE 1"))
         {
            System.out.println("PICTURE 1");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("PICTURE 2"))
         {
            System.out.println("PICTURE 2");
            filePathList.add(picUploadFrame());
         }
         else if(button.getText().equals("PICTURE 3"))
         {
            System.out.println("PICTURE 3");
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
            System.out.println("CANCEL");
            filePathList=null;
            //init();
            CMmainFrame();
            contentUploadFrame.dispose();
         }
         
      }
   }
   
   public void requestAttachedFile(String strFileName)
	{		
		boolean bRet = clientStub.requestAttachedFileOfSNSContent(strFileName);
		if(bRet) eventHandler.setReqAttachedFile(true);
		return;
	}
   

   public static void main(String[] args) {
      IngstaClient client = new IngstaClient();
      CMClientStub cmStub = client.getClientStub();
      cmStub.setAppEventHandler(client.getClientEventHandler());
      
      client.startCM();
      
   }
   
   
   
   public class ImagePanel extends JPanel{
	    private Image img;
	    private BufferedImage image;
	    private contentList cl = new contentList();
	    private ArrayList<contentList> cont = new ArrayList<contentList>();
	    private ArrayList<File> file = new ArrayList<File>();
	    
	    public ImagePanel() {
	    	cont = eventHandler.getContentList();
	    }
	    
	    public BufferedImage loadImage(File imagePath) {
	        image = null;
	        try {
	            image = ImageIO.read(imagePath);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return image;
	    }
	    
	    @Override
	    public void paintComponent(Graphics g) {
	    	super.paintComponent(g);
	    	
	    	for(int i=0; i<cont.size(); i++) {
    			cl = cont.get(i);
	    		file = cl.file;
	    		
	    		g.setColor(new Color(255, 204, 203));
	    		g.fillRect(10, 10+(225*i), 560, 210);
	    		g.setColor(Color.black);
	    		g.setFont(new Font("D2Coding", Font.BOLD, 15));
	    		g.drawString("WRITER : "+cl.writer, 20, 30+(225*i));
	    		g.drawString(cl.date, 20, 50+(225*i));
	    		g.drawString(cl.content, 20, 80+(225*i));
	    		
	    		System.out.println(file.size());
	    		for(int k=0; k<file.size(); k++) {
	    			this.image = loadImage(file.get(k));
    				g.drawImage(this.image, 20+(80*k), 130+(225*i), 70, 70, this);
	    		}
	    	
	    	}
	    }
	 }
>>>>>>> 62542cb4e2f6fd963a37a7c6d73046e3a87e60ce
}