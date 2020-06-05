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
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
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
         printStyledMessage("Ingsta Server\n", "bold");
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


   public void sendEventWithWrongEventType()
   {
      printMessage("========== send a CMDummyEvent with wrong event type\n");
      
      String strTarget = JOptionPane.showInputDialog("target client or server name: ").trim();

      CMDummyEvent due = new CMDummyEvent();
      due.setType(-1);   // set wrong event type
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
   
   
   public static void main(String[] args)
   {
      IngstaServer server = new IngstaServer();
      CMServerStub cmStub = server.getServerStub();
      cmStub.setAppEventHandler(server.getServerEventHandler());
   }
}