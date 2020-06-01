package buildTools

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile

class CheckoutSpecFileCommands extends DefaultTask {
    @Input
    String spec

    @Input
    String tag

    @OutputFile
    File outputFile

    @TaskAction
    def checkoutSpecFile() {
        def specsRepository = 'git@github.com:sky-uk/csc-specs.git'
        def commitRef = "refs/tags/$tag"

        project.exec {
            executable 'git'
            args = ['fetch', specsRepository, commitRef]
        }

        project.exec {
            executable 'git'
            args = ['checkout', 'FETCH_HEAD', '--', ':/' + spec]
        }

        // un-stage the checked out file
        project.exec {
            executable 'git'
            args = ['reset', 'HEAD', ':/' + spec]
        }
    }
}