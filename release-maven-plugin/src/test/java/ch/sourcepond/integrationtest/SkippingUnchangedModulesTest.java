package ch.sourcepond.integrationtest;

import org.junit.Test;

import ch.sourcepond.integrationtest.utils.TestProject;

import java.util.List;

import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.noneOf;
import static ch.sourcepond.integrationtest.utils.ExactCountMatcher.oneOf;
import static ch.sourcepond.integrationtest.utils.GitMatchers.hasTag;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class SkippingUnchangedModulesTest extends E2ETest {

    final TestProject testProject = TestProject.deepDependenciesProject();

    @Test
    public void changesInTheRootAreDetected() throws Exception {
        TestProject simple = TestProject.singleModuleProject();
        simple.mvnRelease("1");
        simple.commitRandomFile(".");
        List<String> output = simple.mvnRelease("2");
        assertThat(output, noneOf(containsString("No changes have been detected in any modules")));
        assertThat(output, noneOf(containsString("Will use version 1.0.1")));
    }

    @Test
    public void doesNotReReleaseAModuleThatHasNotChanged() throws Exception {
        List<String> initialBuildOutput = testProject.mvnRelease("1");
        assertTagExists("deep-dependencies-aggregator-1.0.1");
        assertTagExists("parent-module-1.2.3.1");
        assertTagExists("core-utils-2.0.1");
        assertTagExists("console-app-3.2.1");
        assertTagExists("more-utils-10.0.1");

        assertThat(initialBuildOutput, oneOf(containsString("Releasing core-utils 2.0.1 as parent-module has changed")));
        assertThat(initialBuildOutput, oneOf(containsString("Releasing console-app 3.2.1 as parent-module has changed")));

        testProject.commitRandomFile("console-app").pushIt();
        List<String> output = testProject.mvnRelease("2");
        assertTagExists("console-app-3.2.2");
        assertTagDoesNotExist("parent-module-1.2.3.2");
        assertTagDoesNotExist("core-utils-2.0.2");
        assertTagDoesNotExist("more-utils-10.0.2");
        assertTagDoesNotExist("deep-dependencies-aggregator-1.0.2");

        assertThat(output, oneOf(containsString("Going to release console-app 3.2.2")));
        assertThat(output, noneOf(containsString("Going to release parent-module")));
        assertThat(output, noneOf(containsString("Going to release core-utils")));
        assertThat(output, noneOf(containsString("Going to release more-utils")));
        assertThat(output, noneOf(containsString("Going to release deep-dependencies-aggregator")));
    }

    @Test
    public void ifThereHaveBeenNoChangesThenReReleaseAllModules() throws Exception {
        List<String> firstBuildOutput = testProject.mvnRelease("1");
        assertThat(firstBuildOutput, noneOf(containsString("No changes have been detected in any modules so will re-release them all")));
        List<String> secondBuildOutput = testProject.mvnRelease("2");
        assertThat(secondBuildOutput, oneOf(containsString("No changes have been detected in any modules so will re-release them all")));

        assertTagExists("console-app-3.2.2");
        assertTagExists("parent-module-1.2.3.2");
        assertTagExists("core-utils-2.0.2");
        assertTagExists("more-utils-10.0.2");
        assertTagExists("deep-dependencies-aggregator-1.0.2");
    }

    @Test
    public void ifADependencyHasNotChangedButSomethingItDependsOnHasChangedThenTheDependencyIsReReleased() throws Exception {
        testProject.mvnRelease("1");
        testProject.commitRandomFile("more-utilities").pushIt();
        List<String> output = testProject.mvnRelease("2");

        assertTagExists("console-app-3.2.2");
        assertTagDoesNotExist("parent-module-1.2.3.2");
        assertTagExists("core-utils-2.0.2");
        assertTagExists("more-utils-10.0.2");
        assertTagDoesNotExist("deep-dependencies-aggregator-1.0.2");

        assertThat(output, oneOf(containsString("Going to release console-app 3.2.2")));
        assertThat(output, noneOf(containsString("Going to release parent-module")));
        assertThat(output, oneOf(containsString("Going to release core-utils")));
        assertThat(output, oneOf(containsString("Going to release more-utils")));
        assertThat(output, noneOf(containsString("Going to release deep-dependencies-aggregator")));
    }

    private void assertTagExists(String tagName) {
        assertThat(testProject.local, hasTag(tagName));
        assertThat(testProject.origin, hasTag(tagName));
    }

    private void assertTagDoesNotExist(String tagName) {
        assertThat(testProject.local, not(hasTag(tagName)));
        assertThat(testProject.origin, not(hasTag(tagName)));
    }

}
