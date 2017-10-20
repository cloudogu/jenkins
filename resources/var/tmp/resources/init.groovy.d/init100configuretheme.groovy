import jenkins.model.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.*;

// Try block to stop Jenkins in case an exception occurs in the script
try {

// copy material theme css file to Jenkins' userContent folder
def srcTheme = new File('/var/tmp/resources/jenkins-material-theme.css')
def dstTheme = new File('/var/lib/jenkins/userContent/jenkins-material-theme.css')
dstTheme << srcTheme.text

def jenkins = Jenkins.instance;

def simpleThemeDesc = jenkins.getDescriptor("SimpleThemeDecorator");

def stapler = Stapler.getCurrentRequest();

JSONObject json = JSONObject.fromObject('{cssUrl:"/jenkins/userContent/jenkins-material-theme.css", jsUrl:""}');

simpleThemeDesc.configure(stapler, json);

simpleThemeDesc.save();
jenkins.save();

// Stop Jenkins in case an exception occurs
} catch (Exception exception){
  println("An exception occured during initialization");
  exception.printStackTrace();
  println("Init script and Jenkins will be stopped now...");
  throw new Exception("initialization exception")
}
