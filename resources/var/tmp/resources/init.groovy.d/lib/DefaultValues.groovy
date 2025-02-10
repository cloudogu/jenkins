package lib
// The List of Plugins which should be installed
// This plugins can not be added to the blocklist - they will be automatically removed from that list
def getPlugins() {
    return  [
            'mailer-plugin',
            'cas-plugin',
            'git',
            'mercurial',
            'subversion',
            'scm-manager',
            'workflow-aggregator',
            'matrix-auth',
            'maven-plugin',
            'credentials-binding',
            'ssh-slaves',
            'pipeline-github-lib',
            'authorize-project',
            'pipeline-stage-view'
    ]
}