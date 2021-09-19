package p2pFileSharing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



public class StartRemotePeers {

private static final String scriptPrefix = "java p2pFileSharing/peerProcess ";

public static class PeerInfo {

private String peerID;
private String hostName;

public PeerInfo(String peerID, String hostName) {
super();
this.peerID = peerID;
this.hostName = hostName;
}

public String getPeerID() {
return peerID;
}

public void setPeerID(String peerID) {
this.peerID = peerID;
}

public String getHostName() {
return hostName;
}

public void setHostName(String hostName) {
this.hostName = hostName;
}

}

public static void main(String[] args) {

ArrayList<PeerInfo> peerList = new ArrayList<>();

String ciseUser = "rvaz"; // change with your CISE username

/**
* Make sure the below peer hostnames and peerIDs match those in
* PeerInfo.cfg in the remote CISE machines. Also make sure that the
* peers which have the file initially have it under the 'peer_[peerID]'
* folder.
*/

peerList.add(new PeerInfo("1", "lin114-06.cise.ufl.edu"));
peerList.add(new PeerInfo("2", "lin114-08.cise.ufl.edu"));
peerList.add(new PeerInfo("3", "lin114-09.cise.ufl.edu"));
peerList.add(new PeerInfo("4", "lin114-04.cise.ufl.edu"));
peerList.add(new PeerInfo("5", "lin114-05.cise.ufl.edu"));

for (PeerInfo remotePeer : peerList) {
try {
JSch jsch = new JSch();
/*
* Give the path to your private key. Make sure your public key
* is already within your remote CISE machine to ssh into it
* without a password. Or you can use the corressponding method
* of JSch which accepts a password.
*/
jsch.addIdentity("C:\\Users\\vazra\\.ssh\\private", "");
Session session = jsch.getSession(ciseUser, remotePeer.getHostName(), 22);
Properties config = new Properties();
config.put("StrictHostKeyChecking", "no");
session.setConfig(config);

session.connect();

System.out.println("Session to peer# " + remotePeer.getPeerID() + " at " + remotePeer.getHostName());

Channel channel = session.openChannel("exec");
System.out.println("remotePeerID"+remotePeer.getPeerID());
((ChannelExec) channel).setCommand(scriptPrefix + remotePeer.getPeerID());

channel.setInputStream(null);
((ChannelExec) channel).setErrStream(System.err);

InputStream input = channel.getInputStream();
channel.connect();

System.out.println("Channel Connected to peer# " + remotePeer.getPeerID() + " at "
+ remotePeer.getHostName() + " server with commands");

(new Thread() {
@Override
public void run() {

InputStreamReader inputReader = new InputStreamReader(input);
BufferedReader bufferedReader = new BufferedReader(inputReader);
String line = null;

try {

while ((line = bufferedReader.readLine()) != null) {
System.out.println(remotePeer.getPeerID() + ">:" + line);
}
bufferedReader.close();
inputReader.close();
} catch (Exception ex) {
System.out.println(remotePeer.getPeerID() + " Exception >:");
ex.printStackTrace();
}

channel.disconnect();
session.disconnect();
}
}).start();

} catch (JSchException e) {
// TODO Auto-generated catch block
System.out.println(remotePeer.getPeerID() + " JSchException >:");
e.printStackTrace();
} catch (IOException ex) {
System.out.println(remotePeer.getPeerID() + " Exception >:");
ex.printStackTrace();
}

}
}

}
