// this script will disable the administration monitor that shows up the new jenkins version
// available message in the ui.

import jenkins.model.*;

// Try block to stop Jenkins in case an exception occurs in the script
try {

def coreUpdateMonitorID = 'hudson.model.UpdateCenter$CoreUpdateMonitor';

def coreUpdateMonitor = Jenkins.instance.getAdministrativeMonitor(coreUpdateMonitorID);
if ( coreUpdateMonitor.isEnabled() ) {
    coreUpdateMonitor.disable(true);
    Jenkins.instance.save();
}

// Stop Jenkins in case an exception occurs
} catch (Exception exception){
  println("An exception occured during initialization");
  exception.printStackTrace();
  println("Init script and Jenkins will be stopped now...");
  throw new Exception("initialization exception")
}
