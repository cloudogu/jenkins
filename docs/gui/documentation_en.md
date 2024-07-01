# Documentation

Jenkins is an automation server that can automate all non-human aspects of software development. Thus, it offers all the prerequisites for Continuous Integration or even Continuous Deployment. Over Plugins the application can be extended versatile.

The official documentation of this application can be found here: https://jenkins.io/doc/

## Administration notes: CAS Plugin

In the *Manage Jenkins* area, all installed Jenkins plugins can be accessed via the *Manage Plugins* subitem. These are displayed in the *Installed* tab.

Users with administration rights can uninstall plugins here according to their dependencies. Please note that the **CAS plugin is operationally necessary** and must therefore never be uninstalled. If this plugin is removed, it will no longer be possible to start the Jenkins Dogus.

![CAS Plugin in Jenkins](figures/Jenkins_CAS.png)
