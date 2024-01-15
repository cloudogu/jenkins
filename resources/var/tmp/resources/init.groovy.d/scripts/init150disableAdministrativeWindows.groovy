import hudson.model.*
import jenkins.model.*;

def instance = Jenkins.getInstance()

def monitor = instance.getExtensionList('hudson.model.AdministrativeMonitor').find { it.id == 'jenkins.security.ResourceDomainRecommendation' }
if (monitor != null) {
    monitor.disable(true)
} else {
    println("No administrative monitor found with ID 'monitorId'")
}
