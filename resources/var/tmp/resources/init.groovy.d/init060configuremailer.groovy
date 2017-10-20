import jenkins.model.*;

// based on https://github.com/r-hub/rhub-jenkins/blob/master/docker-entrypoint.sh#L87

// Try block to stop Jenkins in case an exception occurs in the script
try {

def instance = Jenkins.getInstance();

// configure mail server
def mailer = instance.getDescriptor("hudson.tasks.Mailer");
// mailer.setReplyToAddress("");
mailer.setSmtpHost("postfix");
mailer.setUseSsl(false);
mailer.setSmtpPort("25");
mailer.setCharset("UTF-8");
// mailer.setSmtpAuth("", "");
mailer.save();


instance.save();

// Stop Jenkins in case an exception occurs
} catch (Exception exception){
  println("An exception occured during initialization");
  exception.printStackTrace();
  println("Init script and Jenkins will be stopped now...");
  throw new Exception("initialization exception")
}
