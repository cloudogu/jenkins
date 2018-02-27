import jenkins.model.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.*;

// copy material theme css file to Jenkins' userContent folder
def srcTheme = new File('/var/tmp/resources/jenkins-material-theme.css')
def dstTheme = new File('/var/lib/jenkins/userContent/jenkins-material-theme.css')
dstTheme << srcTheme.text

def jenkins = Jenkins.instance;

def simpleThemeDesc = jenkins.getDescriptor("SimpleThemeDecorator");

simpleThemeDesc.setCssUrl("/jenkins/userContent/jenkins-material-theme.css");

simpleThemeDesc.save();
jenkins.save();
