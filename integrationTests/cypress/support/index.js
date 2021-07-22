// Loads all commands from the dogu integration library into this project
const doguTestLibrary = require('@cloudogu/dogu-integration-test-library')
doguTestLibrary.registerCommands()

// local commands
import './commands/required_commands_for_dogu_lib'
import './commands/jenkins_gui'
import './commands/jenkins_api'
