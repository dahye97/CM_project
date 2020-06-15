import java.io.*;
import java.nio.file.Paths;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class IngstaServer extends JFrame {

   private static final long serialVersionUID = 1L;
   
   //private JTextArea m_outTextArea;
   private JTextPane m_outTextPane;
   private JTextField m_inTextField;
   private JButton m_startStopButton;
   private CMServerStub m_serverStub;
   private IngstaServerEventHandler m_eventHandler;
   
   IngstaServer()
   {
      
      setTitle("CM Server");
      setSize(500, 800);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      
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
      
      add(m_inTextField, BorderLayout.SOUTH);
      
      JPanel topButtonPanel = new JPanel();
      topButtonPanel.setLayout(new FlowLayout());
      add(topButtonPanel, BorderLayout.NORTH);
      
      m_startStopButton = new JButton("Start Server CM");
      m_startStopButton.setEnabled(false);
      topButtonPanel.add(m_startStopButton);
      
      setVisible(true);

      m_serverStub = new CMServerStub();
      m_eventHandler = new IngstaServerEventHandler(m_serverStub, this);

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
      
      String strSavedServerAddress = null;
      String strCurServerAddress = null;
      int nSavedServerPort = -1;
      
      strSavedServerAddress = m_serverStub.getServerAddress();
      strCurServerAddress = CMCommManager.getLocalIP();
      nSavedServerPort = m_serverStub.getServerPort();
      
      JTextField serverAddressTextField = new JTextField(strCurServerAddress);
      JTextField serverPortTextField = new JTextField(String.valueOf(nSavedServerPort));
      Object msg[] = {
            "Server Address: ", serverAddressTextField,
            "Server Port: ", serverPortTextField
      };
      int option = JOptionPane.showConfirmDialog(null, msg, "Server Information", JOptionPane.OK_CANCEL_OPTION);

      if (option == JOptionPane.OK_OPTION) 
      {
         String strNewServerAddress = serverAddressTextField.getText();
         int nNewServerPort = Integer.parseInt(serverPortTextField.getText());
         if(!strNewServerAddress.equals(strSavedServerAddress) || nNewServerPort != nSavedServerPort)
            m_serverStub.setServerInfo(strNewServerAddress, nNewServerPort);
      }
      
      
      bRet = m_serverStub.startCM();
      if(!bRet)
      {
         printStyledMessage("CM initialization error!\n", "bold");
      }
      else
      {
         printStyledMessage("Ingsta Server\n", "bold");
         printMessage("Type \"0\" for menu.\n");      
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

   public void printMessage(String strText)
   {
      StyledDocument doc = m_outTextPane.getStyledDocument();
      try {
         doc.insertString(doc.getLength(), strText, null);
         m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

      } catch (BadLocationException e) {
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
         e.printStackTrace();
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
               button.setText("Stop Server CM");
            }
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
   
   
   public static void main(String[] args)
   {
      IngstaServer server = new IngstaServer();
      CMServerStub cmStub = server.getServerStub();
      cmStub.setAppEventHandler(server.getServerEventHandler());
   }
}