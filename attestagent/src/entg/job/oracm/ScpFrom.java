package entg.job.oracm;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
import com.jcraft.jsch.*;

import entg.job.JobManager;

import java.io.*;
public class ScpFrom{
  public static void doScpCopy(String user, String host, String pass, String rfile, String lfile){
/*    if(arg.length!=2){
      System.err.println("usage: java ScpFrom user@remotehost:file1 file2");
      System.exit(-1);
    }
*/
    FileOutputStream fos=null;
    try{

      String prefix=null;
      if(new File(lfile).isDirectory()){
        prefix=lfile+File.separator;
      }
System.out.println("user "+user);
System.out.println("host "+host);
System.out.println("pass "+pass);
System.out.println("rfile "+rfile);
System.out.println("lfile "+lfile);
      JSch jsch=new JSch();
      //jsch.setKnownHosts("/home/testconsole/.ssh/known_hosts");
      jsch.setKnownHosts(JobManager.getProperty("SCP_KNOWN_HOSTS_FILE"));
      Session session=jsch.getSession(user, host, 22);
      session.setPassword (pass);
      // username and password will be given via UserInfo interface.
      UserInfo ui=new MyUserInfo(pass);
      session.setUserInfo(ui);
      session.connect();
      // exec 'scp -f rfile' remotely
      String command="scp -f "+rfile;
      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand(command);
      // get I/O streams for remote scp
      OutputStream out=channel.getOutputStream();
      InputStream in=channel.getInputStream();

      channel.connect();
      byte[] buf=new byte[1024];
      // send '\0'
      buf[0]=0; out.write(buf, 0, 1); out.flush();
      while(true){
        int c=checkAck(in);
        if(c!='C'){
          break;
        }
        // read '0644 '
        in.read(buf, 0, 5);
        long filesize=0L;
        while(true){
          if(in.read(buf, 0, 1)<0){
            // error
            break;
         }
          if(buf[0]==' ')break;
          filesize=filesize*10L+(long)(buf[0]-'0');
        }
        String file=null;
        for(int i=0;;i++){
          in.read(buf, i, 1);
          if(buf[i]==(byte)0x0a){
            file=new String(buf, 0, i);
            break;
          }
        }
        //System.out.println("filesize="+filesize+", file="+file);
        // send '\0'
        buf[0]=0; out.write(buf, 0, 1); out.flush();
        // read a content of lfile
        fos=new FileOutputStream(prefix==null ? lfile : prefix+file);
       int foo;
        while(true){
          if(buf.length<filesize) foo=buf.length;
          else foo=(int)filesize;
          foo=in.read(buf, 0, foo);
          if(foo<0){
            // error
            break;
          }
          fos.write(buf, 0, foo);
          filesize-=foo;
          if(filesize==0L) break;
        }
        fos.close();
        fos=null;
        if(checkAck(in)!=0){
        }
        // send '\0'
       buf[0]=0; out.write(buf, 0, 1); out.flush();
      }
      session.disconnect();
    }
    catch(Exception e){
      System.out.println(e);
      try{if(fos!=null)fos.close();}catch(Exception ee){}
    }
  }
  static int checkAck(InputStream in) throws IOException{
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c;
      do {
        c=in.read();
        sb.append((char)c);
      }
      while(c!='\n');
      if(b==1){ // error
        System.out.print(sb.toString());
      }
      if(b==2){ // fatal error
        System.out.print(sb.toString());
      }
    }
    return b;
  }
  public static class MyUserInfo implements UserInfo{
  String p;
  public MyUserInfo(String p) {
	  this.p=p;
  }
  public String getPassphrase() { return null; }
  public   String getPassword() {return p;}
  public boolean promptPassword(String message) { return false; }
  public boolean promptPassphrase(String message) { return false; }
  public boolean promptYesNo(String message) { return false; }
  public void showMessage(String message) { }
}
}
